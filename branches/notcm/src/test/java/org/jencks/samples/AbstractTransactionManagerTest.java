/* =====================================================================
 *
 * Copyright (c) 2006 Dain Sundstrom.  All rights reserved.
 *
 * =====================================================================
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
