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

import javax.jms.MessageListener;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.UnavailableException;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;
import java.lang.reflect.Method;

/**
 * @version $Revision$
 */
public abstract class EndpointFactorySupport implements MessageEndpointFactory {
    protected TransactionManager transactionManager;

    public MessageEndpoint createEndpoint(XAResource xaResource) throws UnavailableException {
        MessageListener messageListener = createMessageListener();
        if (transactionManager != null) {
            return new XAEndpoint(messageListener, xaResource, transactionManager);
        }
        else if (xaResource instanceof LocalTransaction) {
            return new LocalTransactionEndpoint(messageListener, (LocalTransaction) xaResource);
        }
        return new AcknowledgeEndpoint(messageListener);
    }

    public String toString() {
        return super.toString() + "[transactionManager=" + transactionManager + "]";
    }

    // Properties
    //-------------------------------------------------------------------------
    public boolean isDeliveryTransacted(Method method) throws NoSuchMethodException {
        return transactionManager != null;
    }

    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    public void setTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    // Implementation methods
    //-------------------------------------------------------------------------
    protected abstract MessageListener createMessageListener() throws UnavailableException;
}
