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

import org.springframework.beans.factory.InitializingBean;

import javax.jms.MessageListener;
import javax.resource.spi.UnavailableException;
import javax.transaction.TransactionManager;

/**
 * An implementation of {@link javax.resource.spi.endpoint.MessageEndpointFactory} which always
 * uses a single shared instance of a {@link javax.jms.MessageListener} for every endpoint, irrespective
 * of how many physical endpoints are used.
 *
 * @version $Revision$
 */
public class SingletonEndpointFactory extends EndpointFactorySupport implements InitializingBean {
    private MessageListener messageListener;

    public SingletonEndpointFactory() {
    }

    public SingletonEndpointFactory(MessageListener messageListener) {
        this.messageListener = messageListener;
    }

    public SingletonEndpointFactory(MessageListener messageListener, TransactionManager tm) {
        this.messageListener = messageListener;
        this.transactionManager = tm;
    }

    public void afterPropertiesSet() throws Exception {
        if (messageListener == null) {
            throw new IllegalArgumentException("messageListener property must be set");
        }
    }

    public MessageListener getMessageListener() {
        return messageListener;
    }

    public void setMessageListener(MessageListener messageListener) {
        this.messageListener = messageListener;
    }

    // Implementation methods
    //-------------------------------------------------------------------------
    protected MessageListener createMessageListener() throws UnavailableException {
        return messageListener;
    }
}
