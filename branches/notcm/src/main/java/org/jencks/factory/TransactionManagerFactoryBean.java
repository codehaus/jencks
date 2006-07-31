/*
 * Copyright 2002-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jencks.factory;

import java.util.Collection;

import org.apache.geronimo.transaction.log.UnrecoverableLog;
import org.apache.geronimo.transaction.manager.TransactionLog;
import org.apache.geronimo.transaction.manager.XidFactory;
import org.apache.geronimo.transaction.manager.XidFactoryImpl;
import org.jencks.GeronimoPlatformTransactionManager;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * This FactoryBean creates and configures the Geronimo implementation
 * of the TransactionManager interface.
 *
 * @author Thierry Templier
 * @see UnrecoverableLog
 * @see org.apache.geronimo.transaction.log.HOWLLog
 * @org.apache.xbean.XBean element="transactionManager"
 */
public class TransactionManagerFactoryBean implements FactoryBean, InitializingBean {
    private GeronimoPlatformTransactionManager transactionManager;

    private int defaultTransactionTimeoutSeconds = 600;
    private XidFactory xidFactory;
    private TransactionLog transactionLog;
    private Collection resourceManagers;


    public Object getObject() throws Exception {
        if (transactionManager == null) {
            this.transactionManager = new GeronimoPlatformTransactionManager(defaultTransactionTimeoutSeconds,
                    xidFactory,
                    transactionLog,
                    resourceManagers);
        }
        return transactionManager;
    }

    public Class getObjectType() {
        return GeronimoPlatformTransactionManager.class;
    }

    public boolean isSingleton() {
        return true;
    }

    /**
     * Set the default transaction timeout in second.
     */
    public void setDefaultTransactionTimeoutSeconds(int timeout) {
        defaultTransactionTimeoutSeconds = timeout;
    }

    /**
     * Set the transaction log for the transaction context manager.
     */
    public void setTransactionLog(TransactionLog log) {
        transactionLog = log;
    }

    public Collection getResourceManagers() {
        return resourceManagers;
    }

    public XidFactory getXidFactory() {
        return xidFactory;
    }

    public void setXidFactory(XidFactory xidFactory) {
        this.xidFactory = xidFactory;
    }

    /**
     * Set the resource managers
     */
    public void setResourceManagers(Collection resourceManagers) {
        this.resourceManagers = resourceManagers;
    }

    public void afterPropertiesSet() throws Exception {
        if (transactionLog == null) {
            transactionLog = new UnrecoverableLog();
        }
        if (xidFactory == null) {
            xidFactory = new XidFactoryImpl();
        }
    }

}
