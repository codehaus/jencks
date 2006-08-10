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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @version $Revision$
 */
public class JCAContainerTest extends JCAContainerTestSupport {
    private static final Log log = LogFactory.getLog(JCAContainerTest.class);

    public void testMessageDeliveryUsingSharedMessageListener() throws Exception {

        TextMessage message = session.createTextMessage("Hello! " + new Date());
        Destination destination = session.createTopic("test.spring.inboundConnectorA");

        Thread.sleep(1000);
        producer.send(destination, message);
        log.debug("message sent on: " + destination + " of type: " + destination.getClass());


        TestingConsumer consumer = (TestingConsumer) applicationContext.getBean("echoBean");
        consumer.waitForMessageToArrive();

        List list = consumer.flushMessages();
        assertEquals("Message count: " + list, 1, list.size());
    }

    /*
        TODO we need a better testing consumer which uses static counters to handle testing of pooled beans

    public void testMessageDeliveryUsingPooledMesssageListener() throws Exception {
        TextMessage message = session.createTextMessage("Hello! " + new Date());
        Destination destination = session.createQueue("test.spring.inboundConnectorB");
        producer.send(destination, message);

        log.debug("message sent on: " + destination);

        Thread.sleep(2000);
    }
    */

}
