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

import java.util.Date;
import java.util.List;

import javax.jms.Destination;
import javax.jms.TextMessage;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @version $Revision$
 */
public class JCAContainerUsingSpringJtaBatchTest extends JCAContainerTest {
    protected ConfigurableApplicationContext createApplicationContext() {
        return new ClassPathXmlApplicationContext("org/jencks/spring-with-jta-batch.xml");
    }

    public void testMessageDeliveryUsingSharedMessageListener() throws Exception {
    	int number = 100;
    	long start = System.currentTimeMillis();
        Destination destination = session.createTopic("test.spring.inboundConnectorA");
        
        // Wait to make sure the consumers have been established.
        Thread.sleep(1000);
        
        for (int i = 0; i < number; i++) {
            TextMessage message = session.createTextMessage("Hello! " + new Date());
        	producer.send(destination, message);
        }

        log.info("message sent on: " + destination + " of type: " + destination.getClass());


        TestingConsumer consumer = (TestingConsumer) applicationContext.getBean("echoBean");
        consumer.waitForMessagesToArrive(number);
        long stop = System.currentTimeMillis();
        System.err.println("Time to send/receive " + number + " messages: " + (stop - start) + " ms");
        List list = consumer.flushMessages();
        assertEquals("Message count: " + list, number, list.size());
    }
}
