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

import java.lang.reflect.Method;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.resource.ResourceException;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.xa.XAResource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * An XA based Endpoint which uses an XA transaction per message delivery.
 *
 * @version $Revision$
 */
public class SpringEndpoint implements MessageEndpoint, MessageListener {
    private static final Log log = LogFactory.getLog(SpringEndpoint.class);

    private MessageListener messageListener;
    private XAResource xaResource;
    private JtaTransactionManager jtaTransactionManager;
    private TransactionStatus transaction;
    private boolean beforeDeliveryCompleted;
    private boolean messageDelivered;

    public SpringEndpoint(MessageListener messageListener, XAResource xaResource, JtaTransactionManager jtaTransactionManager) {
        this.messageListener = messageListener;
        this.xaResource = xaResource;
        this.jtaTransactionManager = jtaTransactionManager;
    }

    public void beforeDelivery(Method method) throws NoSuchMethodException, ResourceException {
        if (transaction != null) {
            throw new IllegalStateException("Transaction still in progress");
        }
        beforeDeliveryCompleted = false;
        try {
        	TransactionDefinition txDef = new DefaultTransactionDefinition();
            transaction = jtaTransactionManager.getTransaction(txDef);

            jtaTransactionManager.getTransactionManager().getTransaction().enlistResource(xaResource);
            beforeDeliveryCompleted = true;

            log.trace("Transaction started and resource enlisted");
        }
        catch (SystemException e) {
            System.out.println("Caught: " + e);
            throw new ResourceException(e);
        }
        catch (RollbackException e) {
            System.out.println("Caught: " + e);
            throw new ResourceException(e);
        }
        catch (Throwable e) {
            System.out.println("Caught: " + e);
            e.printStackTrace();
            throw new ResourceException(e);
        }
    }

    public void afterDelivery() throws ResourceException {
        if (transaction == null) {
            throw new IllegalStateException("Transaction not in progress");
        }
        if (beforeDeliveryCompleted && messageDelivered) {
            try {
            	jtaTransactionManager.getTransactionManager().getTransaction().delistResource(xaResource, XAResource.TMSUSPEND);
            }
            catch (SystemException e) {
                throw new ResourceException(e);
            }
            try {
            	jtaTransactionManager.commit(transaction);
                log.trace("Transaction committed");
            }
            // TODO: rollback if an exception is thrown
            //       may check jtaTransactionManager.rollbackOnCommitFailure
            finally {
                transaction = null;
            }
        }
    }

    public void onMessage(Message message) {
        messageDelivered = false;
        messageListener.onMessage(message);
        messageDelivered = true;
    }

    public void release() {
        if (transaction != null) {
            try {
            	jtaTransactionManager.rollback(transaction);
            } catch (TransactionException e) {
                log.warn("Failed to rollback transaction: " + e, e);
            }
        }
    }

    protected void doRollback(Exception e) throws ResourceException {
        try {
        	jtaTransactionManager.rollback(transaction);
            log.trace("Transaction rolled back");
        }
        catch (TransactionException e1) {
            log.warn("Caught exception while rolling back: " + e1, e1);
        }
        transaction = null;
        throw new ResourceException(e);
    }
}
