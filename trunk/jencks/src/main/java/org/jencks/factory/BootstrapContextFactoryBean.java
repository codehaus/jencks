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

import javax.resource.spi.BootstrapContext;
import javax.resource.spi.work.WorkManager;
import javax.transaction.xa.XAException;

import org.apache.geronimo.connector.BootstrapContextImpl;
import org.apache.geronimo.connector.work.GeronimoWorkManager;
import org.apache.geronimo.transaction.ExtendedTransactionManager;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.apache.geronimo.transaction.manager.TransactionLog;
import org.apache.geronimo.transaction.manager.XidImporter;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * A Spring {@link FactoryBean} for creating a {@link BootstrapContext} for the JCA container
 * with the {@link WorkManager} and {@link ExtendedTransactionManager}.
 *
 * @version $Revision$
 */
public class BootstrapContextFactoryBean implements FactoryBean, InitializingBean, ApplicationContextAware {

	private ApplicationContext applicationContext;
    private BootstrapContext bootstrapContext;
    private GeronimoWorkManager workManager;
    private WorkManagerFactoryBean workManagerFactory = new WorkManagerFactoryBean();

    public Object getObject() throws Exception {
        return bootstrapContext;
    }

    public Class getObjectType() {
        return BootstrapContext.class;
    }

    public boolean isSingleton() {
        return true;
    }

    public void afterPropertiesSet() throws Exception {
        bootstrapContext = new BootstrapContextImpl(getWorkManager());
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
    	this.applicationContext = applicationContext;
    }


    // Properties
    //-------------------------------------------------------------------------
    public GeronimoWorkManager getWorkManager() throws Exception {
        if (workManager == null) {
        	workManagerFactory.setApplicationContext(applicationContext);
            workManager = workManagerFactory.getWorkManager();
        }
        return workManager;
    }

    public void setWorkManager(GeronimoWorkManager workManager) {
        this.workManager = workManager;
    }

    public TransactionContextManager getTransactionContextManager() throws XAException {
        return workManagerFactory.getTransactionContextManager();
    }

    public void setTransactionContextManager(TransactionContextManager transactionContextManager) {
        workManagerFactory.setTransactionContextManager(transactionContextManager);
    }

    public int getThreadPoolSize() {
        return workManagerFactory.getThreadPoolSize();
    }

    public void setThreadPoolSize(int threadPoolSize) {
        workManagerFactory.setThreadPoolSize(threadPoolSize);
    }

    public ExtendedTransactionManager getTransactionManager() throws XAException {
        return workManagerFactory.getTransactionManager();
    }

    public void setTransactionManager(ExtendedTransactionManager transactionManager) {
        workManagerFactory.setTransactionManager(transactionManager);
    }

    public XidImporter getXidImporter() {
        return workManagerFactory.getXidImporter();
    }

    public void setXidImporter(XidImporter xidImporter) {
        workManagerFactory.setXidImporter(xidImporter);
    }

    public int getDefaultTransactionTimeoutSeconds() {
        return workManagerFactory.getDefaultTransactionTimeoutSeconds();
    }

    public void setDefaultTransactionTimeoutSeconds(int defaultTransactionTimeoutSeconds) {
        workManagerFactory.setDefaultTransactionTimeoutSeconds(defaultTransactionTimeoutSeconds);
    }

    public TransactionLog getTransactionLog() {
        return workManagerFactory.getTransactionLog();
    }

    public void setTransactionLog(TransactionLog transactionLog) {
        workManagerFactory.setTransactionLog(transactionLog);
    }

    public Collection getResourceManagers() {
        return workManagerFactory.getResourceManagers();
    }

    public void setResourceManagers(Collection resourceManagers) {
        workManagerFactory.setResourceManagers(resourceManagers);
    }
}