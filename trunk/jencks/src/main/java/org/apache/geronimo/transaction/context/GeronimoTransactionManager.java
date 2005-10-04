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
package org.apache.geronimo.transaction.context;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.apache.geronimo.transaction.ExtendedTransactionManager;

/**
 * The GeronimoTransactionManager is a wrapper around the TransactionContextManager
 * to offer both UserTransaction and TransactionManager interfaces.
 * 
 * @version $Revision$
 * @author <a href="mailto:gnt@codehaus.org">Guillaume Nodet</a>
 */
public class GeronimoTransactionManager implements UserTransaction, TransactionManager {
	
    TransactionContextManager transactionContextManager;
    
    public GeronimoTransactionManager(TransactionContextManager transactionContextManager) {
    	this.transactionContextManager = transactionContextManager;
	}
	
	public void begin() throws NotSupportedException, SystemException {
		// Create a context if none has been set yet
		if (transactionContextManager.getContext() == null) {
			transactionContextManager.newUnspecifiedTransactionContext();
		}
        TransactionContext ctx = transactionContextManager.getContext();
        if (ctx instanceof UnspecifiedTransactionContext == false) {
            throw new NotSupportedException("Previous Transaction has not been committed");
        }
        UnspecifiedTransactionContext oldContext = (UnspecifiedTransactionContext) ctx;
        GeronimoTransactionContext transactionContext = new GeronimoTransactionContext(
        		this,
        		(ExtendedTransactionManager) transactionContextManager.getTransactionManager(), 
        		oldContext);
       	oldContext.suspend();
        try {
            transactionContext.begin(0);
        } catch (SystemException e) {
            oldContext.resume();
            throw e;
        } catch (NotSupportedException e) {
            oldContext.resume();
            throw e;
        }
        transactionContextManager.setContext(transactionContext);
	}
	
	public void commit() throws HeuristicMixedException, HeuristicRollbackException, IllegalStateException, RollbackException, SecurityException, SystemException {
        TransactionContext ctx = transactionContextManager.getContext();
        if (ctx instanceof GeronimoTransactionContext == false) {
            throw new IllegalStateException("Transaction has not been started");
        }
        GeronimoTransactionContext beanContext = (GeronimoTransactionContext) ctx;
        try {
            if (!beanContext.commit()) {
                throw new RollbackException();
            }
        } finally {
            transactionContextManager.setContext(null);
        }
	}
	
	public int getStatus() throws SystemException {
		return transactionContextManager.getStatus();
	}
	
	public void rollback() throws IllegalStateException, SecurityException, SystemException {
        TransactionContext ctx = transactionContextManager.getContext();
        if (ctx instanceof GeronimoTransactionContext == false) {
            throw new IllegalStateException("Transaction has not been started");
        }
        GeronimoTransactionContext beanContext = (GeronimoTransactionContext) ctx;
        try {
            beanContext.rollback();
        } finally {
            transactionContextManager.setContext(null);
        }
	}
	
	public void setRollbackOnly() throws IllegalStateException, SystemException {
        transactionContextManager.setRollbackOnly();
	}
	
	public void setTransactionTimeout(int seconds) throws SystemException {
        if (seconds < 0) {
            throw new SystemException("transaction timeout must be positive or 0, not " + seconds);
        }
        transactionContextManager.setTransactionTimeout(seconds);
	}
	
	public Transaction getTransaction() throws SystemException {
		Object context = transactionContextManager.getContext();
		if (context == null || context instanceof UnspecifiedTransactionContext) {
			return null;
		} else if (context instanceof GeronimoTransactionContext){
			return ((GeronimoTransactionContext) context).getTransactionDelegate();
		} else {
			throw new IllegalStateException("unrecognized transaction context");
		}
	}

	public void resume(Transaction tx) throws IllegalStateException, InvalidTransactionException, SystemException {
		// Create a context if none has been set yet
		if (transactionContextManager.getContext() == null) {
			transactionContextManager.newUnspecifiedTransactionContext();
		}
		if (tx instanceof GeronimoTransactionDelegate == false) {
			throw new InvalidTransactionException("invalid transaction specified");
		}
		TransactionContext ctx = ((GeronimoTransactionDelegate) tx).getContext(); 
		transactionContextManager.resumeBeanTransactionContext(ctx);
	}

	public Transaction suspend() throws SystemException {
		return ((GeronimoTransactionContext) transactionContextManager.suspendBeanTransactionContext()).getTransactionDelegate();
	}
	
}
