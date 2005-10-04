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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.transaction.DefaultInstanceContext;
import org.apache.geronimo.transaction.InstanceContext;
import org.apache.geronimo.transaction.TrackedConnectionAssociator;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.HashSet;
import java.util.Set;

/**
 * This bean is used to configure a transactional context for
 * applications that work only within a single thread.
 * <p/>
 * At the beginning, it makes enter the application in a new
 * context and, at the end, exit of it.
 *
 * @author Thierry Templier
 * @see TrackedConnectionAssociator#enter(InstanceContext)
 * @see TrackedConnectionAssociator#exit(InstanceContext)
 * @see InstanceContext
 * @see DefaultInstanceContext
 */
public class TransactionContextInitializer implements InitializingBean, DisposableBean {

    private TrackedConnectionAssociator associator;
    private InstanceContext oldContext;

    protected transient Log logger = LogFactory.getLog(getClass());

    public void afterPropertiesSet() throws Exception {
        Set unshareableResources = new HashSet();
        Set applicationManagedSecurityResources = new HashSet();
        this.oldContext = associator.enter(
                new DefaultInstanceContext(unshareableResources, applicationManagedSecurityResources));
        logger.info("Geronimo transaction context set.");
    }

    public void destroy() throws Exception {
        associator.exit(oldContext);
        logger.info("Geronimo transaction context unset.");
    }

    /**
     * Set the TrackedConnectionAssociator instance to allow the bean
     * to enter and exit a transactional context.
     */
    public void setAssociator(TrackedConnectionAssociator associator) {
        this.associator = associator;
    }

}
