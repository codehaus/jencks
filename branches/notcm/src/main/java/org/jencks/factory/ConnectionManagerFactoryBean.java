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

import javax.resource.spi.ConnectionManager;
import javax.transaction.TransactionManager;

import org.apache.geronimo.connector.outbound.GenericConnectionManager;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.NoTransactions;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.PoolingSupport;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.TransactionSupport;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.NoPool;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.SinglePool;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.PartitionedPool;
import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTracker;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.FatalBeanException;

/**
 * This FactoryBean creates a local JCA connection factory outside
 * a J2EE application server.
 * <p/>
 * The connection manager will be then injected in the
 * LocalConnectionFactoryBean, class of the JCA support of Spring.
 *
 * @author Thierry Templier
 * @see org.springframework.jca.support.LocalConnectionFactoryBean#setConnectionManager(ConnectionManager)
 * @org.apache.xbean.XBean element="connectionManager"
 */
public class ConnectionManagerFactoryBean implements FactoryBean, InitializingBean, DisposableBean {
    private GenericConnectionManager connectionManager;

    private TransactionManager transactionManager;

    private TransactionSupport transactionSupport;
    private String transaction;

    private boolean containerManagedSecurity;
    private ConnectionTracker connectionTracker;

    private PoolingSupport poolingSupport;
    private boolean pooling = true;
    private String partitionStrategy; //: none, by-subject, by-connector-properties
    private int poolMaxSize = 10;
    private int poolMinSize = 0;
    private boolean allConnectionsEqual = true;
    private int connectionMaxWaitMilliseconds = 5000;
    private int connectionMaxIdleMinutes = 15;

    public Object getObject() throws Exception {
        if (connectionManager == null) {
            if (transactionManager == null) {
                throw new NullPointerException("transactionManager is null");
            }

            // Instanciate the Geronimo Connection Manager
            this.connectionManager = new GenericConnectionManager(
                    transactionSupport,
                    poolingSupport,
                    containerManagedSecurity,
                    connectionTracker,
                    transactionManager,
                    getClass().getName(),
                    getClass().getClassLoader());

            connectionManager.doStart();
        }
        return connectionManager;
    }

    public void destroy() throws Exception {
        if (connectionManager != null) {
            connectionManager.doStop();
            connectionManager = null;
        }
    }

    public Class getObjectType() {
        return ConnectionManager.class;
    }

    public boolean isSingleton() {
        return true;
    }

    public PoolingSupport getPoolingSupport() {
        return poolingSupport;
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

    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    /**
     * Set the transaction manager for the Geronimo Connection Manager.
     */
    public void setTransactionManager(TransactionManager manager) {
        transactionManager = manager;
    }

    public String getTransaction() {
        return transaction;
    }

    public void setTransaction(String transaction) {
        this.transaction = transaction;
    }

    public TransactionSupport getTransactionSupport() {
        return transactionSupport;
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

    public ConnectionTracker getConnectionTracker() {
        return connectionTracker;
    }

    /**
     * Set the connection tracker for the Geronimo Connection Manager.
     */
    public void setConnectionTracker(ConnectionTracker tracker) {
        connectionTracker = tracker;
    }

    public boolean isContainerManagedSecurity() {
        return containerManagedSecurity;
    }

    /**
     * Enables/disables container managed security
     */
    public void setContainerManagedSecurity(boolean containerManagedSecurity) {
        this.containerManagedSecurity = containerManagedSecurity;
    }

    public boolean isPooling() {
        return pooling;
    }

    public void setPooling(boolean pooling) {
        this.pooling = pooling;
    }

    public String getPartitionStrategy() {
        return partitionStrategy;
    }

    public void setPartitionStrategy(String partitionStrategy) {
        this.partitionStrategy = partitionStrategy;
    }

    public int getPoolMaxSize() {
        return poolMaxSize;
    }

    public void setPoolMaxSize(int poolMaxSize) {
        this.poolMaxSize = poolMaxSize;
    }

    public int getPoolMinSize() {
        return poolMinSize;
    }

    public void setPoolMinSize(int poolMinSize) {
        this.poolMinSize = poolMinSize;
    }

    public boolean isAllConnectionsEqual() {
        return allConnectionsEqual;
    }

    public void setAllConnectionsEqual(boolean allConnectionsEqual) {
        this.allConnectionsEqual = allConnectionsEqual;
    }

    public int getConnectionMaxWaitMilliseconds() {
        return connectionMaxWaitMilliseconds;
    }

    public void setConnectionMaxWaitMilliseconds(int connectionMaxWaitMilliseconds) {
        this.connectionMaxWaitMilliseconds = connectionMaxWaitMilliseconds;
    }

    public int getConnectionMaxIdleMinutes() {
        return connectionMaxIdleMinutes;
    }

    public void setConnectionMaxIdleMinutes(int connectionMaxIdleMinutes) {
        this.connectionMaxIdleMinutes = connectionMaxIdleMinutes;
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
        // Apply the default value for property if necessary
        if (this.transactionSupport == null) {
            // No transaction
            this.transactionSupport = GeronimoDefaults.createTransactionSupport(transaction);
        }
        if (this.poolingSupport == null) {
            // No pool
            if (!pooling) {
                poolingSupport = new NoPool();
            } else {
                if (partitionStrategy == null || "none".equalsIgnoreCase(partitionStrategy)) {

                    // unpartitioned pool
                    poolingSupport = new SinglePool(poolMaxSize,
                            poolMinSize,
                            connectionMaxWaitMilliseconds,
                            connectionMaxIdleMinutes,
                            allConnectionsEqual,
                            !allConnectionsEqual,
                            false);

                } else if ("by-connector-properties".equalsIgnoreCase(partitionStrategy)) {

                    // partition by contector properties such as username and password on a jdbc connection
                    poolingSupport = new PartitionedPool(poolMaxSize,
                            poolMinSize,
                            connectionMaxWaitMilliseconds,
                            connectionMaxIdleMinutes,
                            allConnectionsEqual,
                            !allConnectionsEqual,
                            false,
                            true,
                            false);
                } else if ("by-subject".equalsIgnoreCase(partitionStrategy)) {

                    // partition by caller subject
                    poolingSupport = new PartitionedPool(poolMaxSize,
                            poolMinSize,
                            connectionMaxWaitMilliseconds,
                            connectionMaxIdleMinutes,
                            allConnectionsEqual,
                            !allConnectionsEqual,
                            false,
                            false,
                            true);
                } else {
                    throw new FatalBeanException("Unknown partition strategy " + partitionStrategy);
                }
            }
        }
    }
}
