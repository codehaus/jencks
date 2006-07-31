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

import EDU.oswego.cs.dl.util.concurrent.Executor;
import org.apache.geronimo.transaction.manager.GeronimoTransactionManager;
import org.apache.geronimo.connector.work.GeronimoWorkManager;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.DisposableBean;

/**
 * A Spring {@link FactoryBean} for creating a {@link GeronimoWorkManager} using a {@link GeronimoTransactionManager}.
 *
 * @version $Revision: 88 $
 * @org.apache.xbean.XBean element="workManager"
 */
public class WorkManagerFactoryBean implements FactoryBean, InitializingBean, DisposableBean {
    private GeronimoWorkManager workManager;

    private GeronimoTransactionManager transactionManager;

    private Executor threadPool;
    private int threadPoolSize;

    public Object getObject() throws Exception {
        if (workManager == null) {
            workManager = new GeronimoWorkManager(threadPool, threadPool, threadPool, transactionManager);
            workManager.doStart();
        }
        return workManager;
    }

    public Class getObjectType() {
        return GeronimoWorkManager.class;
    }

    public boolean isSingleton() {
        return true;
    }

    public void destroy() throws Exception {
        if (workManager != null) {
            workManager.doStop();
            workManager = null;
        }
    }

    public void afterPropertiesSet() throws Exception {
        // transaction manager is required
        if (transactionManager == null) {
            throw new FatalBeanException("Geronimo transaction manager was not set");
        }

        // create a default thread pool if one was not specified
        if (threadPool == null) {
            threadPool = GeronimoDefaults.createThreadPool(getThreadPoolSize());
        }
    }

    public GeronimoTransactionManager getTransactionManager() {
        return transactionManager;
    }

    public void setTransactionManager(GeronimoTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public Executor getThreadPool() {
        return threadPool;
    }

    public void setThreadPool(Executor threadPool) {
        this.threadPool = threadPool;
    }

    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    public void setThreadPoolSize(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
    }
}