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

import java.util.HashSet;

import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTrackingCoordinator;
import org.apache.geronimo.transaction.DefaultInstanceContext;
import org.apache.geronimo.transaction.context.OnlineUserTransaction;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.apache.geronimo.transaction.context.UserTransactionImpl;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import javax.resource.ResourceException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

/**
 * This FactoryBean creates and configures the Geronimo implementation
 * of the UserTransaction interface.
 * <p/>
 * This factory is based on the Geronimo Transaction Context Manager
 * and Connection Tracking Coordinator.
 *
 * @deprecated Use GeronimoTransactionManagerFactoryBean instead
 * @author ttemplier
 * @see UserTransactionImpl#setUp(TransactionContextManager, org.apache.geronimo.transaction.TrackedConnectionAssociator)
 * @see UserTransactionImpl#setOnline(boolean)
 * @see GeronimoTransactionManagerFactoryBean
 */
public class UserTransactionFactoryBean implements FactoryBean, InitializingBean {

    private TransactionContextManager transactionContextManager;
    private ConnectionTrackingCoordinator connectionTrackingCoordinator;

    private UserTransaction userTransaction;

    public Object getObject() throws Exception {
        return userTransaction;
    }

    public Class getObjectType() {
        return UserTransaction.class;
    }

    public boolean isSingleton() {
        return true;
    }

    /**
     * Set the transaction context manager to configure the user transaction.
     */
    public void setTransactionContextManager(TransactionContextManager manager) {
        transactionContextManager = manager;
    }

    /**
     * Set the connection tracking coordinator to configure the user transaction.
     */
    public void setConnectionTrackingCoordinator(ConnectionTrackingCoordinator coordinator) {
        connectionTrackingCoordinator = coordinator;
    }

    /**
     * This method instanciates the Geronimo user transaction implementation
     * and sets up it with the transaction context manager used and a connection
     * tracking coordinator.
     *
     * It then sets the online property to true in order that the application
     * can used it.
     */
    public void afterPropertiesSet() throws Exception {
    	this.userTransaction = new GeronimoUserTransaction();
    }
    
    /**
     * This wrapper around the OnlineUserTransaction performs per-thread
     * initialization of the geronimo transaction layer.
     * 
     * @author gnt
     */
    public class GeronimoUserTransaction implements UserTransaction {
    	
    	private OnlineUserTransaction userTransaction;
    	
    	public GeronimoUserTransaction() {
    		this.userTransaction = new OnlineUserTransaction();
    		this.userTransaction.setUp(transactionContextManager,
                connectionTrackingCoordinator);
    	}

		public void begin() throws NotSupportedException, SystemException {
			ensureContext();
			userTransaction.begin();
		}

		public void commit() throws HeuristicMixedException, HeuristicRollbackException, IllegalStateException, RollbackException, SecurityException, SystemException {
			ensureContext();
			userTransaction.commit();
		}

		public int getStatus() throws SystemException {
			ensureContext();
			return userTransaction.getStatus();
		}

		public void rollback() throws IllegalStateException, SecurityException, SystemException {
			ensureContext();
			userTransaction.rollback();
		}

		public void setRollbackOnly() throws IllegalStateException, SystemException {
			ensureContext();
			userTransaction.setRollbackOnly();
		}

		public void setTransactionTimeout(int arg0) throws SystemException {
			ensureContext();
			userTransaction.setTransactionTimeout(arg0);
		}

		private void ensureContext() throws SystemException {
			if (transactionContextManager.getContext() == null) {
				try {
					transactionContextManager.newUnspecifiedTransactionContext();
					connectionTrackingCoordinator.enter(new DefaultInstanceContext(new HashSet(), new HashSet()));
				} catch (ResourceException e) {
					throw (SystemException) new SystemException().initCause(e);
				}
			}
		}
    }


}
