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

import org.apache.geronimo.connector.outbound.connectionmanagerconfig.PartitionedPool;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.PoolingSupport;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.SinglePool;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * This FactoryBean creates the partitionned pool strategy for
 * the Geronimo connection manager.
 * <p/>
 * This class is based on the common pool properties defined in
 * the AbstractGeronimoPool class and two other properties:
 * partitionByConnectionRequestInfo to manage pooling with the
 * request informations and partitionBySubject to manage pooling
 * with the subjects.
 *
 * @author Thierry Templier
 * @see PartitionedPool
 * @see AbstractGeronimoPool
 * @see ConnectionManagerFactoryBean#setPoolingSupport(PoolingSupport)
 * @org.apache.xbean.XBean
 */
public class PartitionedPoolFactoryBean extends AbstractGeronimoPool implements FactoryBean, InitializingBean {

    protected boolean partitionByConnectionRequestInfo;
    protected boolean partitionBySubject;

    private PartitionedPool pool;

    public Object getObject() throws Exception {
        return pool;
    }

    public Class getObjectType() {
        return SinglePool.class;
    }

    public boolean isSingleton() {
        return true;
    }

    /**
     * Set the partitionByConnectionRequestInfo for the pool.
     */
    public void setPartitionByConnectionRequestInfo(boolean partitionByConnectionRequestInfo) {
        this.partitionByConnectionRequestInfo = partitionByConnectionRequestInfo;
    }

    /**
     * Set the partitionBySubject for the pool.
     */
    public void setPartitionBySubject(boolean partitionBySubject) {
        this.partitionBySubject = partitionBySubject;
    }

    public void afterPropertiesSet() throws Exception {
        this.pool = new PartitionedPool(maxSize, minSize, blockingTimeoutMilliseconds,
                idleTimeoutMinutes, matchOne, matchAll, selectOneAssumeMatch,
                partitionByConnectionRequestInfo, partitionBySubject);
    }

}
