/* =====================================================================
 *
 * Copyright (c) 2006 Dain Sundstrom.  All rights reserved.
 *
 * =====================================================================
 */
package org.jencks.factory;

import org.apache.geronimo.connector.outbound.connectionmanagerconfig.PartitionedPool;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.PoolingSupport;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.SinglePool;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.FactoryBean;

/**
 * @org.apache.xbean.XBean element="poolingSupport"
 */
public class PoolingSupportFactoryBean implements FactoryBean {
    private PoolingSupport poolingSupport;

    // none, by-subject, by-connector-properties
    private String partitionStrategy;
    private int poolMaxSize = 10;
    private int poolMinSize = 0;
    private boolean allConnectionsEqual = true;
    private int connectionMaxWaitMilliseconds = 5000;
    private int connectionMaxIdleMinutes = 15;

    public Object getObject() throws Exception {
        if (poolingSupport == null) {
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
        return poolingSupport;

    }

    public Class getObjectType() {
        return PoolingSupport.class;
    }

    public boolean isSingleton() {
        return true;
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
}
