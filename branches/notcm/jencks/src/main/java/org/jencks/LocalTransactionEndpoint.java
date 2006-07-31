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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.resource.ResourceException;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.endpoint.MessageEndpoint;
import java.lang.reflect.Method;

/**
 * Performs a local transaction while processing the message
 *
 * @version $Revision$
 */
public class LocalTransactionEndpoint implements MessageEndpoint, MessageListener {

    private static final Log log = LogFactory.getLog(LocalTransactionEndpoint.class);

    private MessageListener messageListener;
    private LocalTransaction localTransaction;

    public LocalTransactionEndpoint(MessageListener messageListener, LocalTransaction localTransaction) {
        this.messageListener = messageListener;
        this.localTransaction = localTransaction;
    }

    public void beforeDelivery(Method method) throws NoSuchMethodException, ResourceException {
        getLocalTransaction().begin();
    }

    public void afterDelivery() throws ResourceException {
        getLocalTransaction().commit();
    }

    public void release() {
        if (localTransaction != null) {
            try {
                localTransaction.rollback();
            }
            catch (ResourceException e) {
                log.warn("Failed to rollback local transaction: " + e, e);
            }
            localTransaction = null;
        }
    }

    public void onMessage(Message message) {
        messageListener.onMessage(message);
    }


    /**
     * A getter which will return the current local transaction or throw a new exception
     * if this endpoint has already been released.
     */
    protected LocalTransaction getLocalTransaction() throws ResourceException {
        if (localTransaction == null) {
            throw new ResourceException("This endpoint has already been released via a call to release() you cannot deliver messages to me");
        }
        return localTransaction;
    }

}
