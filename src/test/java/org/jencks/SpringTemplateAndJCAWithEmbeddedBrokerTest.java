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

import java.util.List;

import javax.jms.ConnectionFactory;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;

/**
 * @author srt
 * @version $Id$
 */
public class SpringTemplateAndJCAWithEmbeddedBrokerTest extends TestCase {
    private static final Log log = LogFactory.getLog(SpringTemplateAndJCAWithEmbeddedBrokerTest.class);
    private static Log logger = LogFactory.getLog(SpringTemplateAndJCAWithEmbeddedBrokerTest.class);

    private ConfigurableApplicationContext applicationContext;
    private ConnectionFactory connectionFactory;
    protected int messageCount = 10;

    public void testRun() {
        for (int i = 0; i < messageCount; i++) {
            String text = "Message " + i;
            logger.info("Sending " + text);
            sendMessage(text);
        }

        TestingConsumer consumer = (TestingConsumer) applicationContext.getBean("consumerBean");
        consumer.waitForMessagesToArrive(messageCount);

        List list = consumer.flushMessages();
        assertEquals("Message count: " + list, messageCount, list.size());

        log.debug("Received all: " + list.size() + " messages");
    }


    public void sendMessage(String text) throws JmsException {
        JmsTemplate template = new JmsTemplate(connectionFactory);
        template.setPubSubDomain(true);
        template.convertAndSend("myTopic", text);
    }

    protected void setUp() throws Exception {
        super.setUp();
        applicationContext = new ClassPathXmlApplicationContext(getSpringConfig());
        connectionFactory = (ConnectionFactory) applicationContext.getBean("jmsFactory");

        assertTrue("Should have found a non-null connection factory", connectionFactory != null);
    }

    protected String getSpringConfig() {
        return "org/jencks/spring-topic-embedded-broker.xml";
    }

    protected void tearDown() throws Exception {
        if (applicationContext != null) {
            log.debug("Closing the application context");
            applicationContext.close();
        }
        super.tearDown();
    }
}
