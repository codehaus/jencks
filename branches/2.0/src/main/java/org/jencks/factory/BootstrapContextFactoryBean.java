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

import javax.resource.spi.XATerminator;
import javax.resource.spi.work.WorkManager;

import org.apache.geronimo.connector.GeronimoBootstrapContext;
import org.apache.geronimo.connector.work.GeronimoWorkManager;
import org.apache.geronimo.transaction.manager.GeronimoTransactionManager;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * A Spring {@link FactoryBean} for creating a {@link GeronimoBootstrapContext} for the JCA container
 * with the {@link WorkManager} and {@link XATerminator}.
 *
 * @version $Revision$
 * @org.apache.xbean.XBean element="bootstrapContext"
 */
public class BootstrapContextFactoryBean implements FactoryBean, InitializingBean, DisposableBean {
    private GeronimoBootstrapContext bootstrapContext;

    private WorkManager workManager;
    private boolean createdWorkManager;
    private GeronimoTransactionManager transactionManager;
    private int threadPoolSize;

    public Object getObject() throws Exception {
        if (bootstrapContext == null) {
            if (transactionManager == null) {
                throw new FatalBeanException("transactionManager is null");
            }
            bootstrapContext = new GeronimoBootstrapContext(workManager, transactionManager);
        }
        return bootstrapContext;
    }

    public Class getObjectType() {
        return GeronimoBootstrapContext.class;
    }

    public boolean isSingleton() {
        return true;
    }

    public void destroy() throws Exception {
        if (createdWorkManager && workManager instanceof GeronimoWorkManager) {
            GeronimoWorkManager geronimoWorkManager = (GeronimoWorkManager) workManager;
            geronimoWorkManager.doStop();
            geronimoWorkManager = null;
        }
    }

    public void afterPropertiesSet() throws Exception {
        // transaction manager is required
        if (transactionManager == null) {
            throw new FatalBeanException("Geronimo transaction manager was not set");
        }

        // create a default thread pool if one was not specified
        if (workManager == null) {
            workManager = GeronimoDefaults.createWorkManager(transactionManager, getThreadPoolSize());
            createdWorkManager = true;
        }
    }

    public WorkManager getWorkManager() {
        return workManager;
    }

    public void setWorkManager(WorkManager workManager) {
        this.workManager = workManager;
    }

    public GeronimoTransactionManager getTransactionManager() {
        return transactionManager;
    }

    public void setTransactionManager(GeronimoTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    public void setThreadPoolSize(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
    }
}