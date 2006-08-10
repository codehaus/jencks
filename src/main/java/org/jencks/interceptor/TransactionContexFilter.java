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

import java.io.IOException;
import java.util.Set;
import javax.resource.ResourceException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.geronimo.connector.outbound.connectiontracking.ConnectorInstanceContext;
import org.apache.geronimo.connector.outbound.connectiontracking.ConnectorInstanceContextImpl;
import org.apache.geronimo.connector.outbound.connectiontracking.TrackedConnectionAssociator;

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
 * @see TrackedConnectionAssociator#enter(ConnectorInstanceContext)
 * @see TrackedConnectionAssociator#exit(ConnectorInstanceContext)
 * @see ConnectorInstanceContext
 * @see ConnectorInstanceContextImpl
 */
public class TransactionContexFilter implements Filter {
    private TrackedConnectionAssociator associator;
    private Set unshareableResources;
    private Set applicationManagedSecurityResources;
    private ConnectorInstanceContext myContext;

    public void init(FilterConfig config) throws ServletException {
        myContext = new ConnectorInstanceContextImpl(unshareableResources, applicationManagedSecurityResources);
    }

    /**
     * This is the central method of the filter which allows the
     * request to enter in a transactionnal context and exit when
     * the request is sent back to the client.
     */
    public void doFilter(ServletRequest request,
            ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        try {
            // Enter in the transactionnal context
            ConnectorInstanceContext oldContext = associator.enter(myContext);
            try {
                // Proceed with chain
                chain.doFilter(request, response);
            } finally {
                // Exit the transactionnal context
                associator.exit(oldContext);
            }
        } catch (ResourceException e) {
            throw new ServletException("Error while notifying connection tracker", e);
        }
    }

    public void destroy() {
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
