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

import java.util.Map;

import org.apache.geronimo.transaction.context.GeronimoTransactionManager;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * This FactoryBean creates and configures the Geronimo implementation
 * of the UserTransaction and TransactionManager interfaces.
 * <p/>
 * This factory is based on the Geronimo Transaction Context Manager.
 *
 * @version $Revision$
 * @author <a href="mailto:gnt@codehaus.org">Guillaume Nodet</a>
 * @see org.apache.geronimo.transaction.context.GeronimoTransactionManager
 * @org.apache.xbean.XBean
 */
public class GeronimoTransactionManagerFactoryBean implements FactoryBean, InitializingBean, ApplicationContextAware {

	private GeronimoTransactionManager transactionManager;
	private ApplicationContext applicationContext;
    private TransactionContextManager transactionContextManager;
	
	public Object getObject() throws Exception {
		if (transactionManager == null) {
			transactionManager = new GeronimoTransactionManager(getTransactionContextManager());
		}
		return transactionManager;
	}

	public Class getObjectType() {
		return GeronimoTransactionManager.class;
	}

	public boolean isSingleton() {
		return true;
	}

	public void afterPropertiesSet() throws Exception {
	}

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public TransactionContextManager getTransactionContextManager() {
		if (transactionContextManager == null) {
			Map map = applicationContext.getBeansOfType(TransactionContextManager.class);
			if (map.size() == 1) {
				transactionContextManager = (TransactionContextManager) map.values().iterator().next();
			} else {
				throw new IllegalStateException("no TransactionContextManager is registered");
			}
		}
		return transactionContextManager;
	}

	public void setTransactionContextManager(TransactionContextManager transactionContextManager) {
		this.transactionContextManager = transactionContextManager;
	}

}
