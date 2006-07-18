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

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.geronimo.transaction.DefaultInstanceContext;
import org.apache.geronimo.transaction.InstanceContext;
import org.apache.geronimo.transaction.TrackedConnectionAssociator;

import java.util.HashSet;
import java.util.Set;

/**
 * This servlet filter is used to enter in a transactional context
 * automtically at every servlet call and exit of it when the response
 * is sent back to the client.
 *
 * @author Thierry Templier
 * @see TrackedConnectionAssociator#enter(InstanceContext)
 * @see TrackedConnectionAssociator#exit(InstanceContext)
 * @see InstanceContext
 * @see DefaultInstanceContext
 */
public class TransactionContextInterceptor implements MethodInterceptor {

    private TrackedConnectionAssociator associator;

    /**
     * This is the central method of the filter which allows the
     * request to enter in a transactionnal context and exit when
     * the request is sent back to the client.
     */
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Set unshareableResources = new HashSet();
        Set applicationManagedSecurityResources = new HashSet();
        InstanceContext context = associator.enter(
                new DefaultInstanceContext(unshareableResources, applicationManagedSecurityResources));
        Object returnValue = invocation.proceed();
        associator.exit(context);
        return returnValue;
    }

    /**
     * Set the TrackedConnectionAssociator instance to allow the bean
     * to enter and exit a transactional context.
     */
    public void setAssociator(TrackedConnectionAssociator associator) {
        this.associator = associator;
    }

}
