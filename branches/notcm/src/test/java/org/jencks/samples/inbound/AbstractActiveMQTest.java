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
package org.jencks.samples.inbound;

import java.util.Date;
import java.util.List;
import javax.resource.spi.XATerminator;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import javax.jms.Connection;
import javax.jms.Session;
import javax.jms.MessageProducer;
import javax.jms.TextMessage;
import javax.jms.Destination;

import org.apache.geronimo.transaction.manager.XAWork;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jencks.samples.outbound.AbstractDependencyInjectionSpringContextTests;
import org.jencks.TestingConsumer;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.context.ConfigurableApplicationContext;

public abstract class AbstractActiveMQTest extends AbstractDependencyInjectionSpringContextTests {
    private static final Log log = LogFactory.getLog(AbstractActiveMQTest.class);
    private PlatformTransactionManager transactionManager;

    // to send some messages
    protected Connection connection;
    protected Session session;
    protected MessageProducer producer;

    private TestingConsumer consumer;

    public TestingConsumer getConsumer() {
        return consumer;
    }

    public void setConsumer(TestingConsumer consumer) {
        this.consumer = consumer;
    }

    public PlatformTransactionManager getTransactionManager() {
        return transactionManager;
    }

    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public void testMessageDeliveryUsingSharedMessageListener() throws Exception {

        TextMessage message = session.createTextMessage("Hello! " + new Date());
        Destination destination = session.createTopic("TestTopic");

        Thread.sleep(1000);
        producer.send(destination, message);
        log.debug("message sent on: " + destination + " of type: " + destination.getClass());

        consumer.waitForMessageToArrive();

        List list = consumer.flushMessages();
        assertEquals("Message count: " + list, 1, list.size());
    }

    protected String getBrokerURL() {
        return "tcp://localhost:51616";
    }

    protected ConfigurableApplicationContext loadContextLocations(String[] strings) {
        return super.loadContextLocations(strings);    //To change body of overridden methods use File | Settings | File Templates.
    }

    protected Connection createConnection() throws Exception {
        return new ActiveMQConnectionFactory(getBrokerURL()).createConnection();
    }

    public void testInterfaces() throws Exception {
        assertTrue(transactionManager instanceof TransactionManager);
        assertTrue(transactionManager instanceof UserTransaction);
        assertTrue(transactionManager instanceof XATerminator);
        assertTrue(transactionManager instanceof XAWork);
    }

    protected void onSetUp() throws Exception {
        // now lets create the JMS connection
        connection = createConnection();

        // start the connection in case we do some consumption on it
        connection.start();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        producer = session.createProducer(null);
    }


    protected void onTearDown() throws Exception {
        if (connection != null) {
            connection.close();
            connection = null;
        }
    }
}
