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

import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTrackingCoordinator;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.apache.geronimo.transaction.context.UserTransactionImpl;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import javax.transaction.UserTransaction;

/**
 * This FactoryBean creates and configures the Geronimo implementation
 * of the UserTransaction interface.
 * <p/>
 * This factory is based on the Geronimo Transaction Context Manager
 * and Connection Tracking Coordinator.
 *
 * @author ttemplier
 * @see UserTransactionImpl#setUp(TransactionContextManager, org.apache.geronimo.transaction.TrackedConnectionAssociator)
 * @see UserTransactionImpl#setOnline(boolean)
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
        this.userTransaction = new UserTransactionImpl();
        ((UserTransactionImpl) this.userTransaction).setUp(transactionContextManager,
                connectionTrackingCoordinator);
        ((UserTransactionImpl) this.userTransaction).setOnline(true);
    }


}
