/**
 *
 * Copyright 2006 Raman Gupta
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
 *
 **/
package org.jencks;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;

import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.TransactionManager;

/**
 * A factory bean for Jencks JCAConnector classes that can also manage
 * the lifecycle (i.e. start/stop) of a JCAConnector. This is necessary
 * because the JCAConnector class does not provide any means for starting
 * and stopping message consumption.
 * <p>
 * Represents a factory class that can be configured once, and then used
 * multiple times to return lightweight JCAConnector objects that can be
 * independently stopped and started.
 * <p>
 * This can be useful when JCAConnector's need to be configured via a
 * dependency injection container. The container should be used to
 * configure a JCAConnectorLifecycleFactory instead, and classes can
 * then use this class to obtain the JCAConnector's, start them, and stop
 * them.
 *
 * @version $Revision$
 * @org.apache.xbean.XBean
 */
public class JCAConnectorLifecycleFactory implements JCAConnectorLifecycle, BeanFactoryAware, BeanNameAware {

    private ActivationSpec activationSpec;
    private BootstrapContext bootstrapContext;
    private MessageEndpointFactory endpointFactory;
    private ResourceAdapter resourceAdapter;
    private String ref;
    private TransactionManager transactionManager;
    private BeanFactory beanFactory;
    private String name;
    private JCAContainer jcaContainer;

    /**
     * Called by client to start consumption.
     * @return JCAConnector instance that represents the new consuming connection.
     * @throws Exception
     */
    public JCAConnector startConsumption() throws Exception {
        JCAConnector jcaConnector = new JCAConnector();
        jcaConnector.setActivationSpec(activationSpec);
        jcaConnector.setEndpointFactory(endpointFactory);
        jcaConnector.setResourceAdapter(resourceAdapter);
        jcaConnector.setRef(ref);
        jcaConnector.setTransactionManager(transactionManager);
        jcaConnector.setBeanFactory(beanFactory);
        jcaConnector.setBeanName(name);
        jcaConnector.setJcaContainer(jcaContainer);

        // start consumption
        jcaConnector.afterPropertiesSet();

        return jcaConnector;
    }

    /**
     * Called by client to stop consumption. Pass the JCAConnector that was originally
     * provided in the #startConsumption method.
     * @param jcaConnector
     * @throws Exception
     */
    public void stopConsumption(JCAConnector jcaConnector) throws Exception {
        // stop consumption
        jcaConnector.destroy();
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
