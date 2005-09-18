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
import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;

/**
 * The GeronimoTransactionDelegate wraps a GeronimoTransactionContext
 * so that it can be used as a Transaction. 
 *  
 * @version $Revision$
 * @author <a href="mailto:gnt@codehaus.org">Guillaume Nodet</a>
 */
class GeronimoTransactionDelegate implements Transaction {

	private GeronimoTransactionContext context;
	
	GeronimoTransactionDelegate(GeronimoTransactionContext context) {
		this.context = context;
	}
	
	public GeronimoTransactionContext getContext() {
		return context;
	}

	public void commit() throws HeuristicMixedException, HeuristicRollbackException, RollbackException, SecurityException, SystemException {
		if (context.txnManager.transactionContextManager.getContext() != context) {
			throw new IllegalStateException("Cannot commit if the transaction is not bound to the current thread");
		}
		context.txnManager.commit();
	}

	public boolean delistResource(XAResource xaResource, int flag) throws IllegalStateException, SystemException {
		return context.delistResource(xaResource, flag);
	}

	public boolean enlistResource(XAResource xaResource) throws IllegalStateException, RollbackException, SystemException {
		return context.enlistResource(xaResource);
	}

	public int getStatus() throws SystemException {
		return context.getTransaction().getStatus();
	}

	public void registerSynchronization(Synchronization synchronization) throws IllegalStateException, RollbackException, SystemException {
		context.registerSynchronization(synchronization);
	}

	public void rollback() throws IllegalStateException, SystemException {
		if (context.txnManager.transactionContextManager.getContext() != context) {
			throw new IllegalStateException("Cannot commit if the transaction is not bound to the current thread");
		}
		context.txnManager.rollback();
	}

	public void setRollbackOnly() throws IllegalStateException, SystemException {
		context.setRollbackOnly();
	}
}
