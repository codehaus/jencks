/*
 * Copyright 2006 the original author or authors.
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
 */
package org.jencks.samples;

import javax.resource.spi.XATerminator;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import javax.transaction.Status;
import javax.transaction.Transaction;

import org.apache.geronimo.transaction.manager.XAWork;
import org.jencks.samples.outbound.AbstractDependencyInjectionSpringContextTests;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public abstract class AbstractTransactionManagerTest extends AbstractDependencyInjectionSpringContextTests {
    private PlatformTransactionManager transactionManager;

    public PlatformTransactionManager getTransactionManager() {
        return transactionManager;
    }

    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public void testInterfaces() throws Exception {
        assertTrue(transactionManager instanceof TransactionManager);
        assertTrue(transactionManager instanceof UserTransaction);
        assertTrue(transactionManager instanceof XATerminator);
        assertTrue(transactionManager instanceof XAWork);
    }

    public void testSpringCommit() throws Exception {
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = null;
        try {
            status = transactionManager.getTransaction(definition);

            transactionManager.commit(status);
        } catch (Exception ex) {
            transactionManager.rollback(status);
            throw ex;
        }
    }

    public void testSpringRollback() throws Exception {
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = null;
        try {
            status = transactionManager.getTransaction(definition);

            transactionManager.rollback(status);
        } catch (Exception ex) {
            transactionManager.rollback(status);
            throw ex;
        }
    }

    public void testTransactionManagerCommit() throws Exception {
        TransactionManager tm = (TransactionManager) this.transactionManager;
        tm.begin();
        try {
            assertEquals(Status.STATUS_ACTIVE, tm.getStatus());

            Transaction transaction = tm.getTransaction();
            assertEquals(Status.STATUS_ACTIVE, transaction.getStatus());

            tm.commit();
            assertEquals(Status.STATUS_NO_TRANSACTION, transaction.getStatus());
            assertEquals(Status.STATUS_NO_TRANSACTION, transaction.getStatus());
        } finally {
            if (tm.getStatus() != Status.STATUS_NO_TRANSACTION) {
                tm.rollback();
            }
        }
    }

    public void testTransactionManagerRollback() throws Exception {
        TransactionManager tm = (TransactionManager) this.transactionManager;
        tm.begin();
        try {
            assertEquals(Status.STATUS_ACTIVE, tm.getStatus());

            Transaction transaction = tm.getTransaction();
            assertEquals(Status.STATUS_ACTIVE, transaction.getStatus());

            tm.rollback();

            assertEquals(Status.STATUS_NO_TRANSACTION, transaction.getStatus());
            assertEquals(Status.STATUS_NO_TRANSACTION, transaction.getStatus());
        } finally {
            if (tm.getStatus() != Status.STATUS_NO_TRANSACTION) {
                tm.rollback();
            }
        }
    }
}
