/**
 * 
 * Copyright 2005 LogicBlaze, Inc.
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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;

import javax.jms.MessageListener;
import javax.resource.spi.UnavailableException;
import javax.transaction.TransactionManager;

/**
 * A factory of {@link javax.resource.spi.endpoint.MessageEndpoint} instances, either using XA transactions,
 * Local (JMS) transactions or regular JMS message acknowledgements.
 * <p/>
 * To use XA you must set the transactionManager property via the
 * {@link #setTransactionManager(javax.transaction.TransactionManager)}
 * method.
 * <p/>
 * To use a local JMS transaction, then the XAResouce object passed in the {@see #createEndpoint(XAResource)}
 * call must implement {@see javax.resource.spi.LocalTransaction}.
 *
 * @version $Revision$
 */
public class DefaultEndpointFactory extends EndpointFactorySupport implements InitializingBean, BeanFactoryAware {
    private BeanFactory beanFactory;
    private String ref;

    public DefaultEndpointFactory() {
    }

    public DefaultEndpointFactory(BeanFactory beanFactory, String ref) {
        this.beanFactory = beanFactory;
        this.ref = ref;
    }

    public DefaultEndpointFactory(BeanFactory beanFactory, String ref, TransactionManager transactionManager) {
        this.beanFactory = beanFactory;
        this.ref = ref;
        this.transactionManager = transactionManager;
    }

    public void afterPropertiesSet() throws Exception {
        if (ref == null) {
            throw new IllegalArgumentException("ref property must be set");
        }
    }

    // Properties
    //-------------------------------------------------------------------------
    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public BeanFactory getBeanFactory() {
        return beanFactory;
    }

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    // Implementation methods
    //-------------------------------------------------------------------------
    protected MessageListener createMessageListener() throws UnavailableException {
        MessageListener messageListener = (MessageListener) beanFactory.getBean(ref, MessageListener.class);
        if (messageListener == null) {
            throw new UnavailableException("No MessageListener bean available for reference name: " + ref);
        }
        return messageListener;
    }
}
