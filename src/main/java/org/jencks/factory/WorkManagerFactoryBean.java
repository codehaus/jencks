/**
 * 
 * Copyright 2005 LogicBlaze, Inc. http://www.logicblaze.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 **/
package org.jencks.factory;

import java.util.Collection;
import java.util.Map;

import javax.resource.spi.BootstrapContext;
import javax.resource.spi.work.WorkManager;
import javax.transaction.xa.XAException;

import org.apache.geronimo.connector.work.GeronimoWorkManager;
import org.apache.geronimo.transaction.ExtendedTransactionManager;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.apache.geronimo.transaction.log.UnrecoverableLog;
import org.apache.geronimo.transaction.manager.TransactionLog;
import org.apache.geronimo.transaction.manager.TransactionManagerImpl;
import org.apache.geronimo.transaction.manager.XidImporter;
import org.apache.geronimo.transaction.manager.XidFactoryImpl;
import org.apache.geronimo.pool.GeronimoExecutor;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import EDU.oswego.cs.dl.util.concurrent.Executor;
import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

/**
 * A Spring {@link FactoryBean} for creating a {@link BootstrapContext} for the JCA container
 * with the {@link WorkManager} and {@link ExtendedTransactionManager}.
 *
 * @version $Revision$
 * @org.apache.xbean.XBean
 */
public class WorkManagerFactoryBean implements FactoryBean, InitializingBean, ApplicationContextAware {

    private ApplicationContext applicationContext;
    private GeronimoWorkManager workManager;
    private TransactionContextManager transactionContextManager;
    private int threadPoolSize = 30;
    private ExtendedTransactionManager transactionManager;
    private XidImporter xidImporter;
    private int defaultTransactionTimeoutSeconds = 600;
    private TransactionLog transactionLog;
    private Collection resourceManagers;
    private Executor threadPool;

    public Object getObject() throws Exception {
        return getWorkManager();
    }

    public Class getObjectType() {
        return WorkManager.class;
    }

    public boolean isSingleton() {
        return true;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void afterPropertiesSet() throws Exception {
    }

    public GeronimoWorkManager getWorkManager() throws Exception {
        if (workManager == null) {
            workManager = createWorkManager();
            workManager.doStart();
        }
        return workManager;
    }

    public TransactionContextManager getTransactionContextManager() throws XAException {
        if (transactionContextManager == null && applicationContext != null) {
            Map map = applicationContext.getBeansOfType(TransactionContextManager.class);
            if (map.size() > 1) {
                throw new IllegalStateException("only one TransactionContextManager can be registered");
            } else if (map.size() == 1) {
                transactionContextManager = (TransactionContextManager) map.values().iterator().next();
            }
        }
        if (transactionContextManager == null) {
            transactionContextManager = createTransactionContextManager();
        }
        return transactionContextManager;
    }

    public void setTransactionContextManager(TransactionContextManager transactionContextManager) {
        this.transactionContextManager = transactionContextManager;
    }

    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    public void setThreadPoolSize(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
    }

    public Executor getThreadPool() {
        if (threadPool == null) {
            threadPool = new PooledExecutor(getThreadPoolSize());
        }
        return threadPool;
    }

    public void setThreadPool(Executor threadPool) {
        this.threadPool = threadPool;
    }

    public ExtendedTransactionManager getTransactionManager() throws XAException {
        if (transactionManager == null && applicationContext != null) {
            Map map = applicationContext.getBeansOfType(ExtendedTransactionManager.class);
            if (map.size() > 1) {
                throw new IllegalStateException("only one ExtendedTransactionManager can be registered");
            } else if (map.size() == 1) {
                transactionManager = (ExtendedTransactionManager) map.values().iterator().next();
            }
        }
        if (transactionManager == null) {
            transactionManager = new TransactionManagerImpl(getDefaultTransactionTimeoutSeconds(),
                    new XidFactoryImpl(),
                    getTransactionLog(),
                    getResourceManagers());
        }
        return transactionManager;
    }

    public void setTransactionManager(ExtendedTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public XidImporter getXidImporter() {
        if (xidImporter == null && transactionManager instanceof XidImporter) {
            xidImporter = (XidImporter) transactionManager;
        }
        return xidImporter;
    }

    public void setXidImporter(XidImporter xidImporter) {
        this.xidImporter = xidImporter;
    }

    public int getDefaultTransactionTimeoutSeconds() {
        return defaultTransactionTimeoutSeconds;
    }

    public void setDefaultTransactionTimeoutSeconds(int defaultTransactionTimeoutSeconds) {
        this.defaultTransactionTimeoutSeconds = defaultTransactionTimeoutSeconds;
    }

    public TransactionLog getTransactionLog() {
        if (transactionLog == null) {
            transactionLog = new UnrecoverableLog();
        }
        return transactionLog;
    }

    public void setTransactionLog(TransactionLog transactionLog) {
        this.transactionLog = transactionLog;
    }

    public Collection getResourceManagers() {
        return resourceManagers;
    }

    public void setResourceManagers(Collection resourceManagers) {
        this.resourceManagers = resourceManagers;
    }

    // Implementation methods
    //-------------------------------------------------------------------------
    protected TransactionContextManager createTransactionContextManager() throws XAException {
        return new TransactionContextManager(getTransactionManager(), getXidImporter());
    }

    protected GeronimoWorkManager createWorkManager() throws XAException {
        GeronimoExecutor geronimoExecutor = getGeronimoExecutor();
        return new GeronimoWorkManager(geronimoExecutor, geronimoExecutor, geronimoExecutor, getTransactionContextManager());
    }

    protected GeronimoExecutor getGeronimoExecutor() {
        return new GeronimoExecutorWrapper(getThreadPool());
    }
}