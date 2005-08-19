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

import org.apache.geronimo.connector.outbound.GenericConnectionManager;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.NoPool;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.NoTransactions;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.PoolingSupport;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.TransactionSupport;
import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTracker;
import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTrackingCoordinator;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import javax.resource.spi.ConnectionManager;

/**
 * This FactoryBean creates a local JCA connection factory outside
 * a J2EE application server.
 * <p/>
 * The connection manager will be then injected in the
 * LocalConnectionFactoryBean, class of the JCA support of Spring.
 *
 * @author Thierry Templier
 * @see org.springframework.jca.support.LocalConnectionFactoryBean#setConnectionManager(ConnectionManager)
 * @see NoTransactionFactoryBean
 * @see LocalTransactionFactoryBean
 * @see XATransactionFactoryBean
 * @see PartitionedPoolFactoryBean
 * @see SinglePoolFactoryBean
 */
public class ConnectionManagerFactoryBean implements FactoryBean, InitializingBean {

    private TransactionSupport transactionSupport;
    private PoolingSupport poolingSupport;
    private TransactionContextManager transactionContextManager;
    private ConnectionTracker connectionTracker;

    private ConnectionManager connectionManager;
    private boolean containerManagedSecurity;

    public Object getObject() throws Exception {
        return connectionManager;
    }

    public Class getObjectType() {
        return ConnectionManager.class;
    }

    public boolean isSingleton() {
        return true;
    }

    /**
     * Set the pooling support for the Geronimo Connection Manager.
     * Geronimo provides two kinds of pool: single and partitioned.
     *
     * @see org.apache.geronimo.connector.outbound.connectionmanagerconfig.SinglePool
     * @see org.apache.geronimo.connector.outbound.connectionmanagerconfig.PartitionedPool
     */
    public void setPoolingSupport(PoolingSupport support) {
        poolingSupport = support;
    }

    /**
     * Set the transaction context manager for the Geronimo Connection Manager.
     */
    public void setTransactionContextManager(TransactionContextManager manager) {
        transactionContextManager = manager;
    }

    /**
     * Set the transaction support for the Geronimo Connection Manager.
     * Geronimo provides in this case three kinds of support like the
     * JCA specification: no transaction, local transactions, XA transactions.
     *
     * @see NoTransactions
     * @see org.apache.geronimo.connector.outbound.connectionmanagerconfig.LocalTransactions
     * @see org.apache.geronimo.connector.outbound.connectionmanagerconfig.XATransactions
     */
    public void setTransactionSupport(TransactionSupport support) {
        transactionSupport = support;
    }

    /**
     * Set the connection tracker for the Geronimo Connection Manager.
     */
    public void setConnectionTracker(ConnectionTracker tracker) {
        connectionTracker = tracker;
    }

    /**
     * Enables/disables container managed security
     */
    public void setContainerManagedSecurity(boolean containerManagedSecurity) {
        this.containerManagedSecurity = containerManagedSecurity;
    }

    /**
     * This method checks all the needed parameters to construct
     * the Geronimo connection manager which is implemented by the
     * GenericConnectionManager class.
     * If the transaction support property is not set, the method
     * configures the connection manager with the no transaction value.
     * If the pooling support property is not set, the method
     * configures the connection manager with the no pool value.
     * If the realm bridge is not set, the method configure
     * the connection manager with an identity realm bridge.
     *
     * @see GenericConnectionManager
     */
    public void afterPropertiesSet() throws Exception {
        //Apply the default value for property if necessary
        if (this.transactionSupport == null) {
            //No transaction
            this.transactionSupport = NoTransactions.INSTANCE;
        }
        if (this.poolingSupport == null) {
            //No pool
            this.poolingSupport = new NoPool();
        }
        if (this.connectionTracker == null) {
            //Instanciate the Geronimo Connection Track implementation
            this.connectionTracker = new ConnectionTrackingCoordinator();
        }

        //Instanciate the Geronimo Connection Manager
        this.connectionManager = new GenericConnectionManager(this.transactionSupport, this.poolingSupport,
                containerManagedSecurity, this.connectionTracker, this.transactionContextManager,
                getClass().getName(), getClass().getClassLoader());
    }

}
