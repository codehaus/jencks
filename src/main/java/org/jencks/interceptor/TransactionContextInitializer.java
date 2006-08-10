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

package org.jencks.interceptor;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.connector.outbound.connectiontracking.ConnectorInstanceContext;
import org.apache.geronimo.connector.outbound.connectiontracking.ConnectorInstanceContextImpl;
import org.apache.geronimo.connector.outbound.connectiontracking.TrackedConnectionAssociator;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * This bean is used to configure a transactional context for
 * applications that work only within a single thread.
 * <p/>
 * At the beginning, it makes enter the application in a new
 * context and, at the end, exit of it.
 *
 * @author Thierry Templier
 * @see TrackedConnectionAssociator#enter(ConnectorInstanceContext)
 * @see TrackedConnectionAssociator#exit(ConnectorInstanceContext)
 * @see ConnectorInstanceContext
 * @see ConnectorInstanceContextImpl
 */
public class TransactionContextInitializer implements InitializingBean, DisposableBean {
    private TrackedConnectionAssociator associator;
    private ConnectorInstanceContext oldContext;

    protected Log logger = LogFactory.getLog(getClass());
    private Set unshareableResources;
    private Set applicationManagedSecurityResources;

    public void afterPropertiesSet() throws Exception {
        ConnectorInstanceContextImpl myContext = new ConnectorInstanceContextImpl(unshareableResources, applicationManagedSecurityResources);
        this.oldContext = associator.enter(myContext);
    }

    public void destroy() throws Exception {
        associator.exit(oldContext);
    }

    public TrackedConnectionAssociator getAssociator() {
        return associator;
    }

    /**
     * Set the TrackedConnectionAssociator instance to allow the bean
     * to enter and exit a transactional context.
     */
    public void setAssociator(TrackedConnectionAssociator associator) {
        this.associator = associator;
    }

    public Set getUnshareableResources() {
        return unshareableResources;
    }

    public void setUnshareableResources(Set unshareableResources) {
        this.unshareableResources = unshareableResources;
    }

    public Set getApplicationManagedSecurityResources() {
        return applicationManagedSecurityResources;
    }

    public void setApplicationManagedSecurityResources(Set applicationManagedSecurityResources) {
        this.applicationManagedSecurityResources = applicationManagedSecurityResources;
    }
}
