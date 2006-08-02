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
package org.jencks.factory;

import javax.resource.spi.BootstrapContext;
import javax.resource.spi.work.WorkManager;

import EDU.oswego.cs.dl.util.concurrent.Executor;
import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;
import org.apache.geronimo.connector.GeronimoBootstrapContext;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.LocalTransactions;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.NoPool;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.NoTransactions;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.PoolingSupport;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.TransactionSupport;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.XATransactions;
import org.apache.geronimo.connector.work.GeronimoWorkManager;
import org.apache.geronimo.transaction.log.UnrecoverableLog;
import org.apache.geronimo.transaction.manager.GeronimoTransactionManager;
import org.apache.geronimo.transaction.manager.TransactionLog;
import org.apache.geronimo.transaction.manager.XidFactory;
import org.apache.geronimo.transaction.manager.XidFactoryImpl;
import org.springframework.beans.FatalBeanException;

/**
 * @version $Revision$ $Date$
 */
public final class GeronimoDefaults {
    public static final int DEFAULT_THREAD_POOL_SIZE = 30;

    private GeronimoDefaults() {
    }

    public static XidFactory createXidFactory() {
        XidFactory xidFactory;
        xidFactory = new XidFactoryImpl();
        return xidFactory;
    }

    public static PoolingSupport createPoolingSupport() {
        return new NoPool();
    }

    public static TransactionSupport createTransactionSupport(String transaction) {
        if (transaction == null || "local".equalsIgnoreCase(transaction)) {
            return LocalTransactions.INSTANCE;
        } else if ("none".equalsIgnoreCase(transaction)) {
            return NoTransactions.INSTANCE;
        } else if ("xa".equalsIgnoreCase(transaction)) {
            return new XATransactions(true, false);
        } else {
            throw new FatalBeanException("Unknown transaction type " + transaction);
        }
    }

    public static Executor createThreadPool() {
        return createThreadPool(DEFAULT_THREAD_POOL_SIZE);
    }

    public static Executor createThreadPool(int threadPoolSize) {
        if (threadPoolSize <= 0) threadPoolSize = DEFAULT_THREAD_POOL_SIZE;
        Executor pooledExecutor = new PooledExecutor(threadPoolSize);
        return pooledExecutor;
    }

    public static WorkManager createWorkManager(GeronimoTransactionManager transactionManager, int threadPoolSize) {
        Executor threadPool = createThreadPool(threadPoolSize);
        WorkManager geronimoWorkManager = new GeronimoWorkManager(threadPool, threadPool, threadPool, transactionManager);
        return geronimoWorkManager;
    }

    public static BootstrapContext createBootstrapContext(GeronimoTransactionManager transactionManager, int threadPoolSize) {
        WorkManager workManager = createWorkManager(transactionManager, threadPoolSize);
        return createBootstrapContext(transactionManager, workManager);
    }

    public static BootstrapContext createBootstrapContext(GeronimoTransactionManager transactionManager, WorkManager workManager) {
        BootstrapContext bootstrapContext = new GeronimoBootstrapContext(workManager, transactionManager);
        return bootstrapContext;
    }

    public static TransactionLog createTransactionLog(XidFactory xidFactory, String logDir) throws Exception {
        if (logDir == null) {
            return new UnrecoverableLog();
        } else {
            HowlLogFactoryBean howlLogFactoryBean = new HowlLogFactoryBean();
            howlLogFactoryBean.setLogFileDir(logDir);
            howlLogFactoryBean.setXidFactory(xidFactory);
            return (TransactionLog) howlLogFactoryBean.getObject();
        }
    }
}
