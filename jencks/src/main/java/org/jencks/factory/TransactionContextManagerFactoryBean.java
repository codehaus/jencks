/*
 * Copyright 2002-2005 the original author or authors.
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

import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.apache.geronimo.transaction.manager.TransactionManagerImpl;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * This FactoryBean creates and configures the TransactionManagerContext
 * of Geronimo.
 *
 * @author Thierry Templier
 * @see org.apache.geronimo.transaction.log.UnrecoverableLog
 * @see org.apache.geronimo.transaction.log.HOWLLog
 */
public class TransactionContextManagerFactoryBean implements FactoryBean, InitializingBean {

    private TransactionManagerImpl transactionManagerImpl;
    private TransactionContextManager transactionContextManager;

    public Object getObject() throws Exception {
        return transactionContextManager;
    }

    public Class getObjectType() {
        return TransactionContextManager.class;
    }

    public boolean isSingleton() {
        return true;
    }

    public TransactionManagerImpl getTransactionManagerImpl() {
        return transactionManagerImpl;
    }

    public void setTransactionManagerImpl(TransactionManagerImpl transactionManagerImpl) {
        this.transactionManagerImpl = transactionManagerImpl;
    }

    /**
     * This method initializes the transaction context manager basing on
     * the Geronimo implementation of the transaction manager and a dedicated
     * transaction log.
     * <p/>
     * It specifies too an unspecified transaction context for the transaction
     * context manager instanciated.
     */
    public void afterPropertiesSet() throws Exception {
        //Instanciate the transaction context manager
        this.transactionContextManager = new TransactionContextManager(this.transactionManagerImpl,
                this.transactionManagerImpl);

        //TODO to uncomment?!
        this.transactionContextManager.newUnspecifiedTransactionContext();
    }

}
