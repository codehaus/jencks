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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;

/**
 * @version $Revision$
 */
public class TestReplyBean implements MessageListener, InitializingBean {
    private static final Log log = LogFactory.getLog(TestReplyBean.class);
    private static int globalCounter = 0;

    private JmsTemplate jmsTemplate;
    private int counter = getNextValue();

    public void onMessage(final Message message) {
        log.info("TestReplyBean: " + counter + " received message: " + message);

        // now lets send a reply message
        try {
            Destination replyTo = message.getJMSReplyTo();
            if (replyTo != null) {
                log.info("About to send reply to: " + replyTo);
                //jmsTemplate.convertAndSend(replyTo, "This is the reply from message: " + message);
                jmsTemplate.send(replyTo, new MessageCreator() {
                    public Message createMessage(Session session) throws JMSException {
                        return session.createTextMessage("This is the reply from message: " + message);
                    }
                });
            }
            else {
                log.warn("No JMSReployTo destination so cannot send reply for: " + message);
            }
        }
        catch (JMSException e) {
            log.error("Failed to send message: " + e, e);
        }
    }

    /**
     * Provide container construction validation
     */
    public void afterPropertiesSet() throws Exception {
        if (jmsTemplate == null) {
            throw new IllegalArgumentException("jmsTemplate property must be set");
        }
    }

    // Properties
    //-------------------------------------------------------------------------
    public JmsTemplate getJmsTemplate() {
        return jmsTemplate;
    }

    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    protected static synchronized int getNextValue() {
        return ++globalCounter;
    }

}
