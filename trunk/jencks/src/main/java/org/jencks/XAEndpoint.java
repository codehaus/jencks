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
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;
import java.lang.reflect.Method;

/**
 * An XA based Endpoint which uses an XA transaction per message delivery.
 *
 * @version $Revision$
 */
public class XAEndpoint implements MessageEndpoint, MessageListener {
    private static final Log log = LogFactory.getLog(XAEndpoint.class);

    private MessageListener messageListener;
    private XAResource xaResource;
    private TransactionManager transactionManager;
    private Transaction transaction;
    private boolean beforeDeliveryCompleted;
    private boolean messageDelivered;

    public XAEndpoint(MessageListener messageListener, XAResource xaResource, TransactionManager transactionManager) {
        this.messageListener = messageListener;
        this.xaResource = xaResource;
        this.transactionManager = transactionManager;
    }

    public void beforeDelivery(Method method) throws NoSuchMethodException, ResourceException {
        if (transaction != null) {
            throw new IllegalStateException("Transaction still in progress");
        }
        beforeDeliveryCompleted = false;
        try {
            transactionManager.begin();
            transaction = transactionManager.getTransaction();

            transaction.enlistResource(xaResource);
            beforeDeliveryCompleted = true;

            log.trace("Transaction started and resource enlisted");
        }
        catch (NotSupportedException e) {
            System.out.println("Caught: " + e);
            throw new ResourceException(e);
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
                if (transactionManager.getTransaction() != transaction) {
                    throw new IllegalStateException("Transaction is not bound to the current thread");
                }
                if (transaction.getStatus() == Status.STATUS_MARKED_ROLLBACK) {
                    transactionManager.rollback();
                    log.trace("Transaction rolled back");
                } else {
                    transactionManager.commit();
                    log.trace("Transaction committed");
                }
            }
            catch (RollbackException e) {
                // The transaction has been 
            }
            catch (HeuristicMixedException e) {
                doRollback(e);
            }
            catch (HeuristicRollbackException e) {
                doRollback(e);
            }
            catch (SystemException e) {
                doRollback(e);
            }
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
                if (transactionManager.getTransaction() != transaction) {
                    throw new IllegalStateException("Transaction is not bound to the current thread");
                }
                transactionManager.rollback();
                // transaction.rollback();
            }
            catch (SystemException e) {
                log.warn("Failed to rollback transaction: " + e, e);
            }
        }
    }

    protected void doRollback(Exception e) throws ResourceException {
        try {
            if (transactionManager.getTransaction() != transaction) {
                throw new IllegalStateException("Transaction is not bound to the current thread");
            }
            transactionManager.rollback();
            //transaction.rollback();
            log.trace("Transaction rolled back");
        }
        catch (SystemException e1) {
            log.warn("Caught exception while rolling back: " + e1, e1);
        }
        transaction = null;
        throw new ResourceException(e);
    }
}
