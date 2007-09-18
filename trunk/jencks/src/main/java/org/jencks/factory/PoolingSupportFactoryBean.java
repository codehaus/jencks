/*
 * Copyright 2006 the original author or authors.
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

    public Class<?> getObjectType() {
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
