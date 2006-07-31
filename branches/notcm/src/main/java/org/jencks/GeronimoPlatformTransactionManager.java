/* =====================================================================
 *
 * Copyright (c) 2006 Dain Sundstrom.  All rights reserved.
 *
 * =====================================================================
 */
package org.jencks;

import java.util.Collection;
import javax.transaction.xa.XAException;

import org.apache.geronimo.transaction.manager.GeronimoTransactionManager;
import org.apache.geronimo.transaction.manager.TransactionLog;
import org.apache.geronimo.transaction.manager.XidFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.jta.JtaTransactionManager;

/**
 * @version $Revision$ $Date$
 */
public class GeronimoPlatformTransactionManager extends GeronimoTransactionManager implements PlatformTransactionManager {
    private final PlatformTransactionManager platformTransactionManager;

    public GeronimoPlatformTransactionManager() throws XAException {
        platformTransactionManager = new JtaTransactionManager(this, this);
    }

    public GeronimoPlatformTransactionManager(int defaultTransactionTimeoutSeconds) throws XAException {
        super(defaultTransactionTimeoutSeconds);
        platformTransactionManager = new JtaTransactionManager(this, this);
    }

    public GeronimoPlatformTransactionManager(int defaultTransactionTimeoutSeconds, TransactionLog transactionLog) throws XAException {
        super(defaultTransactionTimeoutSeconds, transactionLog);
        platformTransactionManager = new JtaTransactionManager(this, this);
    }

    public GeronimoPlatformTransactionManager(int defaultTransactionTimeoutSeconds,
            XidFactory xidFactory,
            TransactionLog transactionLog,
            Collection resourceManagers) throws XAException {

        super(defaultTransactionTimeoutSeconds, xidFactory, transactionLog, resourceManagers);
        platformTransactionManager = new JtaTransactionManager(this, this);
    }

    public TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException {
        return platformTransactionManager.getTransaction(definition);
    }

    public void commit(TransactionStatus status) throws TransactionException {
        platformTransactionManager.commit(status);
    }

    public void rollback(TransactionStatus status) throws TransactionException {
        platformTransactionManager.rollback(status);
    }
}
