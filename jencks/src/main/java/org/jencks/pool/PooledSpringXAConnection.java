/** 
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
package org.jencks.pool;

import javax.jms.Session;
import javax.jms.JMSException;
import javax.jms.ConnectionConsumer;
import javax.jms.Destination;
import javax.jms.ServerSessionPool;
import javax.jms.Topic;
import javax.jms.ExceptionListener;
import javax.jms.ConnectionMetaData;
import javax.jms.Queue;
import javax.jms.QueueSession;
import javax.jms.TopicSession;
import javax.jms.XAConnection;
import javax.jms.TopicConnection;
import javax.jms.QueueConnection;
import javax.jms.XASession;
import javax.transaction.TransactionManager;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.RollbackException;
import javax.transaction.xa.XAResource;

import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionSynchronization;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PooledSpringXAConnection implements TopicConnection,
		QueueConnection, XAConnection {
	private static final Log log = LogFactory
			.getLog(PooledSpringXAConnection.class);

	private final ConnectionInfo connectionInfo;

	private XASessionPool sessionPool;

	private TransactionManager transactionManager;

	private boolean stopped;

	private boolean closed;

	private boolean clientIdSetSinceReopen = false;

	private PooledSpringXAConnectionFactory pooledConnectionFactory;

	public PooledSpringXAConnection(
			final PooledSpringXAConnectionFactory pooledConnectionFactory,
			final TransactionManager transactionManager,
			final XAConnection connection) {
		this(pooledConnectionFactory, transactionManager, new ConnectionInfo(
				connection), new XASessionPool(connection));
	}

	public PooledSpringXAConnection(
			final PooledSpringXAConnectionFactory pooledConnectionFactory,
			final TransactionManager transactionManager,
			final ConnectionInfo connectionInfo, final XASessionPool sessionPool) {
		this.pooledConnectionFactory = pooledConnectionFactory;
		this.transactionManager = transactionManager;
		this.connectionInfo = connectionInfo;
		this.sessionPool = sessionPool;
		this.closed = false;
	}

	/**
	 * Factory method to create a new instance.
	 */
	public PooledSpringXAConnection newInstance() {
		return new PooledSpringXAConnection(this.pooledConnectionFactory,
				this.transactionManager, this.connectionInfo, this.sessionPool);
	}

	public void close() throws JMSException {
		this.closed = true;
	}

	public void start() throws JMSException {
		// TODO should we start connections first before pooling them?
		getConnection().start();
	}

	public void stop() throws JMSException {
		this.stopped = true;
	}

	public ConnectionConsumer createConnectionConsumer(
			final Destination destination, final String selector,
			final ServerSessionPool serverSessionPool, final int maxMessages)
			throws JMSException {
		return getConnection().createConnectionConsumer(destination, selector,
				serverSessionPool, maxMessages);
	}

	public ConnectionConsumer createConnectionConsumer(Topic topic, String s,
			ServerSessionPool serverSessionPool, int maxMessages)
			throws JMSException {
		return getConnection().createConnectionConsumer(topic, s,
				serverSessionPool, maxMessages);
	}

	public ConnectionConsumer createDurableConnectionConsumer(
			final Topic topic, final String selector, final String s1,
			final ServerSessionPool serverSessionPool, final int i)
			throws JMSException {
		return getConnection().createDurableConnectionConsumer(topic, selector,
				s1, serverSessionPool, i);
	}

	public String getClientID() throws JMSException {
		return getConnection().getClientID();
	}

	public ExceptionListener getExceptionListener() throws JMSException {
		return getConnection().getExceptionListener();
	}

	public ConnectionMetaData getMetaData() throws JMSException {
		return getConnection().getMetaData();
	}

	public void setExceptionListener(ExceptionListener exceptionListener)
			throws JMSException {
		getConnection().setExceptionListener(exceptionListener);
	}

	public void setClientID(String clientID) throws JMSException {
		if (this.clientIdSetSinceReopen) {
			throw new JMSException(
					"ClientID is already set on this connection.");
		}

		synchronized (this.connectionInfo) {
			if (this.connectionInfo.isActualClientIdSet()) {
				if (this.connectionInfo.getActualClientIdBase() == null ? clientID != null
						: !this.connectionInfo.getActualClientIdBase().equals(
								clientID)) {
					throw new JMSException(
							"A pooled Connection must only ever have its client ID set to the same value for the duration of the pooled ConnectionFactory.  It looks like code has set a client ID, returned the connection to the pool, and then later obtained the connection from the pool and set a different client ID.");
				}
			} else {
				final String generatedId = getPooledConnectionFactory()
						.generateClientID(clientID);
				getConnection().setClientID(generatedId);
				this.connectionInfo.setActualClientIdBase(clientID);
				this.connectionInfo.setActualClientIdSet(true);
			}
		}

		this.clientIdSetSinceReopen = true;
	}

	public ConnectionConsumer createConnectionConsumer(final Queue queue,
			final String selector, final ServerSessionPool serverSessionPool,
			final int maxMessages) throws JMSException {
		return getConnection().createConnectionConsumer(queue, selector,
				serverSessionPool, maxMessages);
	}

	public XASession createXASession() throws JMSException {
		try {
			if (log.isDebugEnabled()) {
				log.debug("-->> ENTERING PooledSpringXAConnection.createXASession()");
			}
			if (this.transactionManager.getStatus() != Status.STATUS_NO_TRANSACTION) {
				if (log.isDebugEnabled()) {
					log.debug("-->> ACTUAL TRANSACTION IS ACTIVE!");
				}
				final XASession session = (XASession) TransactionSynchronizationManager.getResource(this.connectionInfo.getConnection());
				if (session != null) {
					if (log.isDebugEnabled()) {
						log.debug("-->> RETURNING ALREADY ACTIVE SESSION ASSOCIATED WITH CURRENT THREAD...");
					}
					return session;
				} else {
					if (log.isDebugEnabled()) {
						log.debug("-->> NO ACTIVE SESSION ASSOCIATED WITH CURRENT THREAD, BORROWING...");
					}
					final PooledSpringXASession newSession = this.sessionPool.borrowSession();
					newSession.setIgnoreClose(true);
					if (log.isDebugEnabled()) {
						log.debug("-->> ENLISTING NEW SESSION'S XAResource WITH TRANSACTION...");
					}
					this.transactionManager.getTransaction().enlistResource(newSession.getXAResource());
					try {
						if (log.isDebugEnabled()) {
							log.debug("-->> BINDING NEW SESSION WITH TRANSACTION...");
						}
						TransactionSynchronizationManager.bindResource(this.connectionInfo.getConnection(), newSession);
						try {
							if (log.isDebugEnabled()) {
								log.debug("-->> REGISTERING SYNCHRONIZATION WITH TRANSACTION...");
							}
							TransactionSynchronizationManager.registerSynchronization(new Synchronization(newSession));
							return newSession;
						} catch (Throwable t) {
							if (log.isDebugEnabled()) {
								log.debug("-->> CAUGHT EXCEPTION WHILE ASSOCIATING SESSION WITH TRANSACTION, UNBINDING RESOURCE.", t);
							}
							TransactionSynchronizationManager.unbindResource(connectionInfo.getConnection());
							newSession.setIgnoreClose(false);
							throw t;
						}
					} catch (Throwable t) {
						if (log.isDebugEnabled()) {
							log.debug("-->> CAUGHT EXCEPTION WHILE ASSOCIATING SESSION WITH TRANSACTION (2), DELISTING RESOURCE...", t);
						}
						this.transactionManager.getTransaction().delistResource(newSession.getXAResource(), XAResource.TMSUCCESS);
						if (log.isDebugEnabled()) {
							log.debug("-->> DESTROYING SESSION AND REMOVING FROM POOL...");
						}
						newSession.destroyAndRemoveFromPool();
						if (log.isDebugEnabled()) {
							log.debug("-->> RETHROWING EXCEPTION...");
						}
						if (t instanceof JMSException) {
							throw (JMSException) t;
						} else if (t instanceof RuntimeException) {
							throw (RuntimeException) t;
						} else if (t instanceof Error) {
							throw (Error) t;
						} else {
							final JMSException jmsException = new JMSException("Unable to enlist session with transaction.");
							jmsException.initCause(t);
							throw jmsException;
						}
					}
				}
			} else {
				if (log.isDebugEnabled())
					log
							.debug("-->> THERE IS NO ACTIVE TRANSACTION, SO JUST RETURNING BORROWED SESSION...");
				return this.sessionPool.borrowSession();
			}
		} catch (SystemException e) {
			final JMSException jmsException = new JMSException(
					"System Exception");
			jmsException.initCause(e);
			throw jmsException;
		} catch (RollbackException re) {
			final JMSException jmsException = new JMSException(
					"Rollback exception");
			jmsException.initCause(re);
			throw jmsException;
		}
	}

	// Session factory methods
	//-------------------------------------------------------------------------
	public QueueSession createQueueSession(boolean transacted, int ackMode)
			throws JMSException {
		return (QueueSession) createSession(transacted, ackMode);
	}

	public TopicSession createTopicSession(boolean transacted, int ackMode)
			throws JMSException {
		return (TopicSession) createSession(transacted, ackMode);
	}

	public Session createSession(boolean transacted, int ackMode)
			throws JMSException {
		return createXASession();
	}

	// Implementation methods
	//-------------------------------------------------------------------------
	protected XAConnection getConnection() throws JMSException {
		if (this.stopped || this.closed) {
			throw new JMSException("Already closed");
		}
		return this.connectionInfo.getConnection();
	}

	public PooledSpringXAConnectionFactory getPooledConnectionFactory() {
		return this.pooledConnectionFactory;
	}

	private class Synchronization implements TransactionSynchronization {
		private final PooledSpringXASession session;

		private Synchronization(PooledSpringXASession session) {
			this.session = session;
		}

		public void suspend() {
			if (log.isDebugEnabled()) {
				log.debug("-->> PooledSpringXAConnection.[synchronization].suspend() CALLED...");
			}
			TransactionSynchronizationManager.unbindResource(connectionInfo.getConnection());
		}

		public void resume() {
			if (log.isDebugEnabled()) {
				log.debug("-->> PooledSpringXAConnection.[synchronization].resume() CALLED...");
			}
			TransactionSynchronizationManager.bindResource(connectionInfo.getConnection(), session);
		}

		public void beforeCommit(boolean readOnly) {
		}

		public void beforeCompletion() {
		}

		public void afterCompletion(int status) {
			if (log.isDebugEnabled()) {
				log.debug("-->> PooledSpringXAConnection.[synchronization].afterCompletion() CALLED...");
			}
			TransactionSynchronizationManager.unbindResource(connectionInfo.getConnection());
			try {
				// This will return session to the pool.
				if (log.isDebugEnabled()) {
					log.debug("-->> RETURNING JMS SESSION TO POOL...");
				}
				session.setIgnoreClose(false);
				session.close();
			} catch (JMSException e) {
				throw new RuntimeException(e);
			}
		}
        
        public void afterCommit() {
        }
	}

	private static class ConnectionInfo {
		private XAConnection connection;

		private boolean actualClientIdSet;

		private String actualClientIdBase;

		public ConnectionInfo(final XAConnection connection) {
			this.connection = connection;
			this.actualClientIdSet = false;
			this.actualClientIdBase = null;
		}

		public XAConnection getConnection() {
			return connection;
		}

		public void setConnection(final XAConnection connection) {
			this.connection = connection;
		}

		public synchronized boolean isActualClientIdSet() {
			return actualClientIdSet;
		}

		public synchronized void setActualClientIdSet(
				final boolean actualClientIdSet) {
			this.actualClientIdSet = actualClientIdSet;
		}

		public synchronized String getActualClientIdBase() {
			return actualClientIdBase;
		}

		public synchronized void setActualClientIdBase(
				final String actualClientIdBase) {
			this.actualClientIdBase = actualClientIdBase;
		}
	}
}
