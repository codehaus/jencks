/**
 * 
 * Copyright 2005 LogicBlaze, Inc.
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
package org.jencks;

import junit.framework.TestCase;
import org.activemq.ActiveMQConnectionFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.jms.Connection;
import javax.jms.MessageProducer;
import javax.jms.Session;

/**
 * @version $Revision$
 */
public class JCAContainerTestSupport extends TestCase {
    protected ConfigurableApplicationContext applicationContext;

    // to send some messages
    protected Connection connection;
    protected Session session;
    protected MessageProducer producer;

    protected void setUp() throws Exception {
        applicationContext = createApplicationContext();

        // now lets create the JMS connection
        connection = createConnection();

        // start the connection in case we do some consumption on it
        connection.start();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        producer = session.createProducer(null);

    }


    protected void tearDown() throws Exception {
        if (connection != null) {
            connection.close();
        }
        if (applicationContext != null) {
            applicationContext.close();
        }
    }

    protected String getBrokerURL() {
        return "tcp://localhost:61616";
    }

    protected ConfigurableApplicationContext createApplicationContext() {
        return new ClassPathXmlApplicationContext("org/jencks/spring.xml");
    }

    protected Connection createConnection() throws Exception {
        return new ActiveMQConnectionFactory(getBrokerURL()).createConnection();
    }
}
