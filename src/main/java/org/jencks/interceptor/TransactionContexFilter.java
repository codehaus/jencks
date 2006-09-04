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

import javax.resource.ResourceException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * This servlet filter is used to enter in a transactional context
 * automtically at every servlet call and exit of it when the response
 * is sent back to the client.
 * <p/>
 * This class must be used with a mechanism (for example, Acegi) to
 * use injection on filters.
 * <p/>
 * The following is an example of use:
 * <p/>
 * <web-app id="WebApp">
 * ...
 * <filter>
 * <filter-name>Geronimo Transaction Context Filter</filter-name>
 * <filter-class>
 * org.springframework.web.filter.DelegatingFilterProxy</filter-class>
 * <init-param>
 * <param-name>targetBeanName</param-name>
 * <param-value>
 * org.springframework.jca.interceptor.TransactionContexFilter
 * </param-value>
 * </init-param>
 * </filter>
 * <p/>
 * <filter-mapping>
 * <filter-name>Geronimo Transaction Context Filter</filter-name>
 * <url-pattern>/*</url-pattern>
 * </filter-mapping>
 * ...
 * </web-app>
 *
 * @author Thierry Templier
 * @see TrackedConnectionAssociator#enter(InstanceContext)
 * @see TrackedConnectionAssociator#exit(InstanceContext)
 * @see InstanceContext
 * @see DefaultInstanceContext
 */
public class TransactionContexFilter implements Filter {

    private TrackedConnectionAssociator associator;

    protected transient Log logger = LogFactory.getLog(getClass());

    public void init(FilterConfig config) throws ServletException {
    }

    /**
     * This is the central method of the filter which allows the
     * request to enter in a transactionnal context and exit when
     * the request is sent back to the client.
     *
     * @see #enterContext(Set, Set)
     * @see #exitContext(InstanceContext)
     */
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        // Enter in the transactionnal context
        Set unshareableResources = new HashSet();
        Set applicationManagedSecurityResources = new HashSet();
        InstanceContext oldContext =
                enterContext(unshareableResources, applicationManagedSecurityResources);

        // Proceed with chain
        chain.doFilter(request, response);

        // Exit the transactionnal context
        exitContext(oldContext);
    }

    /**
     * This method enters in a new context and returns it
     * in order to exit of it when the request is sent back to
     * the client.
     */
    private InstanceContext enterContext(Set unshareableResources,
                                         Set applicationManagedSecurityResources) {
        try {
            InstanceContext oldContext =
                    associator.enter(new DefaultInstanceContext(
                            unshareableResources, applicationManagedSecurityResources));
            if (logger.isDebugEnabled()) {
                logger.info("Geronimo transaction context set.");
            }
            return oldContext;
        }
        catch (ResourceException ex) {
        }
        return null;
    }

    /**
     * This method exits of the specified context. This context is
     * created when entering a new one.
     *
     * @see #enterContext(Set, Set)
     */
    private void exitContext(InstanceContext oldContext) {
        try {
            associator.exit(oldContext);
            if (logger.isDebugEnabled()) {
                logger.info("Geronimo transaction context unset.");
            }
        }
        catch (ResourceException ex) {
        }
    }

    public void destroy() {
    }

    /**
     * Set the TrackedConnectionAssociator instance to allow the bean
     * to enter and exit a transactional context.
     */
    public void setAssociator(TrackedConnectionAssociator associator) {
        this.associator = associator;
	}

}
