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
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @version $Revision$
 */
public class JCAContainerRequestReplyTest extends JCAContainerTestSupport implements MessageListener {
    private static final Log log = LogFactory.getLog(JCAContainerRequestReplyTest.class);
    protected Session serverSession;
    protected MessageProducer serverProducer;
    protected MessageConsumer serverConsumer;
    protected Queue queue;

    protected void setUp() throws Exception {
        super.setUp();
        queue = session.createQueue(getClass().getName());

        serverSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        serverProducer = serverSession.createProducer(null);
        serverConsumer = serverSession.createConsumer(queue);
        serverConsumer.setMessageListener(this);

    }

    public void onMessage(Message msg) {
        try {
            Destination dest = msg.getJMSReplyTo();
            serverProducer.send(dest, msg);
        }
        catch (JMSException jmsEx) {
            jmsEx.printStackTrace();
        }
    }

    public void testRequestReply() throws Exception {


        Requestor requestor = new Requestor(session, queue);

        TextMessage message = session.createTextMessage("Hello! " + new Date());

        log.debug("About to send: " + message);


        Message answer = requestor.request(message);

        assertTrue("Should have received an answer", answer != null);

        log.debug("Received: " + answer);
    }


    protected ConfigurableApplicationContext createApplicationContext() {
        return new ClassPathXmlApplicationContext("org/jencks/spring-request-reply.xml");
    }
}
