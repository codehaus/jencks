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
import java.util.Map;

import javax.transaction.xa.XAException;

import org.apache.geronimo.transaction.ExtendedTransactionManager;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.apache.geronimo.transaction.log.UnrecoverableLog;
import org.apache.geronimo.transaction.manager.TransactionLog;
import org.apache.geronimo.transaction.manager.TransactionManagerImpl;
import org.apache.geronimo.transaction.manager.XidImporter;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * This FactoryBean creates and configures the TransactionManagerContext
 * of Geronimo.
 *
 * @author Thierry Templier
 * @see org.apache.geronimo.transaction.log.UnrecoverableLog
 * @see org.apache.geronimo.transaction.log.HOWLLog
 */
public class TransactionContextManagerFactoryBean implements FactoryBean, InitializingBean, ApplicationContextAware {

	private XidImporter xidImporter;
    private ExtendedTransactionManager transactionManager;
	private ApplicationContext applicationContext;
    private TransactionContextManager transactionContextManager;
    private int defaultTransactionTimeoutSeconds = 600;
    private TransactionLog transactionLog;
    private Collection resourceManagers;

    public Object getObject() throws Exception {
        return transactionContextManager;
    }

    public Class getObjectType() {
        return TransactionContextManager.class;
    }

    public boolean isSingleton() {
        return true;
    }

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

    public ExtendedTransactionManager getTransactionManager() throws XAException {
		if (transactionManager == null) {
			Map map = applicationContext.getBeansOfType(ExtendedTransactionManager.class);
			if (map.size() > 1) {
				throw new IllegalStateException("only one ExtendedTransactionManager can be registered");
			} else if (map.size() == 1) {
				transactionManager = (ExtendedTransactionManager) map.values().iterator().next();
			} else {
	            transactionManager = new TransactionManagerImpl(getDefaultTransactionTimeoutSeconds(), getTransactionLog(), getResourceManagers());
			}
		}
        return transactionManager;
    }

    public void setTransactionManager(ExtendedTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public XidImporter getXidImporter() throws XAException {
		if (xidImporter == null) {
			if (getTransactionManager() instanceof XidImporter) {
				xidImporter = (XidImporter) getTransactionManager(); 
			} else {
				Map map = applicationContext.getBeansOfType(XidImporter.class);
				if (map.size() > 1) {
					throw new IllegalStateException("only one XidImporter can be registered");
				} else if (map.size() == 1) {
					transactionManager = (ExtendedTransactionManager) map.values().iterator().next();
				} else {
					throw new IllegalStateException("no XidImporter is registered");
				}
			}
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

    /**
     * This method initializes the transaction context manager basing on
     * the Geronimo implementation of the transaction manager and a dedicated
     * transaction log.
     */
    public void afterPropertiesSet() throws Exception {
        // Instanciate the transaction context manager
        this.transactionContextManager = new TransactionContextManager(getTransactionManager(), getXidImporter());
    }
    

}
