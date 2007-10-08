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

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.geronimo.connector.outbound.connectiontracking.ConnectorInstanceContext;
import org.apache.geronimo.connector.outbound.connectiontracking.ConnectorInstanceContextImpl;
import org.apache.geronimo.connector.outbound.connectiontracking.TrackedConnectionAssociator;

/**
 * This servlet filter is used to enter in a transactional context
 * automtically at every servlet call and exit of it when the response
 * is sent back to the client.
 *
 * @author Thierry Templier
 * @see TrackedConnectionAssociator#enter(ConnectorInstanceContext)
 * @see TrackedConnectionAssociator#exit(ConnectorInstanceContext)
 * @see ConnectorInstanceContext
 * @see ConnectorInstanceContextImpl
 */
public class TransactionContextInterceptor implements MethodInterceptor {
    private TrackedConnectionAssociator associator;
    private Set unshareableResources;
    private Set applicationManagedSecurityResources;
    private ConnectorInstanceContextImpl connectorInstanceContext;

    /**
     * This is the central method of the filter which allows the
     * request to enter in a transactionnal context and exit when
     * the request is sent back to the client.
     */
    public Object invoke(MethodInvocation invocation) throws Throwable {
        if (connectorInstanceContext == null) {
            connectorInstanceContext = new ConnectorInstanceContextImpl(unshareableResources, applicationManagedSecurityResources);
        }
        ConnectorInstanceContext context = associator.enter(
                connectorInstanceContext);
        Object returnValue = invocation.proceed();
        associator.exit(context);
        return returnValue;
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
