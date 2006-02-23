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

import java.lang.reflect.Method;

import javax.jms.MessageListener;
import javax.jms.Session;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.UnavailableException;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

import org.apache.geronimo.transaction.manager.NamedXAResource;
import org.apache.geronimo.transaction.manager.WrapperNamedXAResource;

/**
 * @version $Revision$
 */
public abstract class EndpointFactorySupport implements MessageEndpointFactory {
	
    protected TransactionManager transactionManager;
    private String name;
    
    int acknowledgeMode = Session.SESSION_TRANSACTED;
    
	public int getAcknowledgeMode() {
		return acknowledgeMode;
	}
	public void setAcknowledgeMode(int acknowledgeMode) {
		this.acknowledgeMode = acknowledgeMode;
	}

	public String getAcknowledgeTpe() {
		switch ( acknowledgeMode ) { 
		case Session.SESSION_TRANSACTED:  return "SESSION_TRANSACTED";
		case Session.AUTO_ACKNOWLEDGE:    return "AUTO_ACKNOWLEDGE"; 
		case Session.DUPS_OK_ACKNOWLEDGE: return "DUPS_OK_ACKNOWLEDGE";
		case Session.CLIENT_ACKNOWLEDGE:  return "CLIENT_ACKNOWLEDGE";
		}
		return "UNKNOWN";
	}
	
	public void setAcknowledgeType(String acknowledgeMode) {
		if( "SESSION_TRANSACTED".equals(acknowledgeMode) ) {
			setAcknowledgeMode(Session.SESSION_TRANSACTED);
		} else if( "AUTO_ACKNOWLEDGE".equals(acknowledgeMode) ) {
			setAcknowledgeMode(Session.AUTO_ACKNOWLEDGE);
		} else if( "DUPS_OK_ACKNOWLEDGE".equals(acknowledgeMode) ) {
			setAcknowledgeMode(Session.DUPS_OK_ACKNOWLEDGE);
		} else if( "CLIENT_ACKNOWLEDGE".equals(acknowledgeMode) ) {
			setAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE);
		} else {
			throw new IllegalArgumentException("Value of AcknowledgeType must be set to SESSION_TRANSACTED, AUTO_ACKNOWLEDGE, DUPS_OK_ACKNOWLEDGE, or CLIENT_ACKNOWLEDGE");
		}
	}

    public MessageEndpoint createEndpoint(XAResource xaResource) throws UnavailableException {
        MessageListener messageListener = createMessageListener();
        if( acknowledgeMode == Session.SESSION_TRANSACTED ) {
	        if (transactionManager != null) {
	            xaResource = wrapXAResource(xaResource);
	            return new XAEndpoint(messageListener, xaResource, transactionManager);
	        } else if (xaResource instanceof LocalTransaction) {
	            return new LocalTransactionEndpoint(messageListener, (LocalTransaction) xaResource);
	        } else {
	        	throw new UnavailableException("Endpoint configured to use transactions but resource could not support this.");
	        }
        } else if (acknowledgeMode==Session.DUPS_OK_ACKNOWLEDGE) {
            return new AcknowledgeEndpoint(messageListener);
        } else {
            return new SimpleEndpoint(messageListener);
        }
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


}
