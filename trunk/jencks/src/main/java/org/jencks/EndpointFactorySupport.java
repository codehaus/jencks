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

import org.apache.geronimo.transaction.manager.NamedXAResource;
import org.apache.geronimo.transaction.manager.WrapperNamedXAResource;
import org.springframework.transaction.jta.JtaTransactionManager;

import javax.jms.MessageListener;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.UnavailableException;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import javax.transaction.xa.XAException;
import java.lang.reflect.Method;

/**
 * @version $Revision$
 */
public abstract class EndpointFactorySupport implements MessageEndpointFactory {
    protected TransactionManager transactionManager;
    protected JtaTransactionManager jtaTransactionManager;
    private String name;

    public MessageEndpoint createEndpoint(XAResource xaResource) throws UnavailableException {
        MessageListener messageListener = createMessageListener();
        xaResource = wrapXAResource(xaResource);
        if (jtaTransactionManager != null) {
            return new SpringEndpoint(messageListener, xaResource, jtaTransactionManager);
        } else if (transactionManager != null) {
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
        return transactionManager != null || jtaTransactionManager != null;
    }

    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    public void setTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Implementation methods
    //-------------------------------------------------------------------------
    protected abstract MessageListener createMessageListener() throws UnavailableException;

    /**
     * {@link XAResource} instances must be named to support recovery, so either pass
     * {@link NamedXAResource} instances through or wrap with the Spring name.
     *
     * @param xaResource
     * @return the wrapped XAResource instance
     */
    protected XAResource wrapXAResource(XAResource xaResource) {
        String name = getName();
        if (xaResource instanceof NamedXAResource || name == null) {
            return xaResource;
        }
        return new WrapperNamedXAResource(xaResource, name);
    }

	public JtaTransactionManager getJtaTransactionManager() {
		return jtaTransactionManager;
	}

	public void setJtaTransactionManager(JtaTransactionManager jtaTransactionManager) {
		this.jtaTransactionManager = jtaTransactionManager;
	}

}
