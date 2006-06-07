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

import org.apache.geronimo.connector.outbound.connectionmanagerconfig.LocalTransactions;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.TransactionSupport;
import org.springframework.beans.factory.FactoryBean;

/**
 * This FactoryBean creates the local transaction strategy for the
 * JCA connection manager used.
 * <p/>
 * This class can be injected in the ConnectionManagerFactoryBean to
 * configure the ConnectionManager instance returned.
 *
 * @author Thierry Templier
 * @see ConnectionManagerFactoryBean#setTransactionSupport(TransactionSupport)
 * @org.apache.xbean.XBean
 */
public class LocalTransactionFactoryBean implements FactoryBean {

    public Object getObject() throws Exception {
        return LocalTransactions.INSTANCE;
    }

    public Class getObjectType() {
        return TransactionSupport.class;
    }

    public boolean isSingleton() {
        return true;
    }

}
