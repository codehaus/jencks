/**
 * 
 * Copyright 2005 LogicBlaze, Inc. http://www.logicblaze.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 **/
package org.jencks;

import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Represents a connector in the JCA container - which represents a single
 * activation specification on a resource adapter
 * 
 * @version $Revision$
 */
public class JCAConnector implements InitializingBean, DisposableBean, BeanFactoryAware, BeanNameAware {
    private static final transient Log log = LogFactory.getLog(JCAConnector.class);

    private ActivationSpec activationSpec;
    private BootstrapContext bootstrapContext;
    private MessageEndpointFactory endpointFactory;
    private ResourceAdapter resourceAdapter;
    private String ref;
    private TransactionManager transactionManager;
    private BeanFactory beanFactory;
    private String name;
    private JCAContainer jcaContainer;

    public JCAConnector() {
    }

    public JCAConnector(BootstrapContext bootstrapContext, ResourceAdapter resourceAdapter) {
        this.bootstrapContext = bootstrapContext;
        this.resourceAdapter = resourceAdapter;
    }

    public void afterPropertiesSet() throws Exception {
        if (activationSpec == null) {
            throw new IllegalArgumentException("activationSpec must be set");
        }

        if (resourceAdapter == null) {
            resourceAdapter = activationSpec.getResourceAdapter();
        }
        if (resourceAdapter == null && jcaContainer != null) {
            resourceAdapter = jcaContainer.getResourceAdapter();
        }
        if (resourceAdapter == null) {
            throw new IllegalArgumentException("resourceAdapter property must be set on the activationSpec object");
        }
        if (activationSpec.getResourceAdapter() == null) {
            activationSpec.setResourceAdapter(resourceAdapter);
        }

        if (bootstrapContext == null && jcaContainer != null) {
            bootstrapContext = jcaContainer.getBootstrapContext();
        }
        if (bootstrapContext == null) {
            throw new IllegalArgumentException("bootstrapContext must be set");
        }
        if (endpointFactory == null) {
            if (ref == null) {
                throw new IllegalArgumentException("either the endpointFactory or ref properties must be set");
            }
            if (transactionManager != null) {
                endpointFactory = new DefaultEndpointFactory(beanFactory, ref, transactionManager, getName());
            }
            else {
                // TODO should we have some way of finding a ManagedConnection
                // or other local transaction hook?
                endpointFactory = new DefaultEndpointFactory(beanFactory, ref);
            }
        }
        log.info("Activating endpoint for activationSpec: " + activationSpec + " using endpointFactory: " + endpointFactory);
        resourceAdapter.endpointActivation(endpointFactory, activationSpec);
    }

    public void destroy() throws Exception {
        if (resourceAdapter != null && activationSpec != null) {
            resourceAdapter.endpointDeactivation(endpointFactory, activationSpec);
        }
    }

    // Properties
    // -------------------------------------------------------------------------

    public String getName() {
        return name;
    }

    public void setBeanName(String name) {
        this.name = name;
    }

    public ActivationSpec getActivationSpec() {
        return activationSpec;
    }

    public void setActivationSpec(ActivationSpec activationSpec) {
        this.activationSpec = activationSpec;
    }

    /**
     * Returns the name of the MessageListener POJO in Spring
     */
    public String getRef() {
        return ref;
    }

    /**
     * Sets the name of the MessageListener POJO in Spring
     */
    public void setRef(String ref) {
        this.ref = ref;
    }

    public MessageEndpointFactory getEndpointFactory() {
        return endpointFactory;
    }

    public void setEndpointFactory(MessageEndpointFactory endpointFactory) {
        this.endpointFactory = endpointFactory;
    }

    public BootstrapContext getBootstrapContext() {
        return bootstrapContext;
    }

    public void setBootstrapContext(BootstrapContext bootstrapContext) {
        this.bootstrapContext = bootstrapContext;
    }

    public ResourceAdapter getResourceAdapter() {
        return resourceAdapter;
    }

    public void setResourceAdapter(ResourceAdapter resourceAdapter) {
        this.resourceAdapter = resourceAdapter;
    }

    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    public void setTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public BeanFactory getBeanFactory() {
        return beanFactory;
    }

    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    public JCAContainer getJcaContainer() {
        return jcaContainer;
    }

    public void setJcaContainer(JCAContainer jcaConnector) {
        this.jcaContainer = jcaConnector;
    }
}
