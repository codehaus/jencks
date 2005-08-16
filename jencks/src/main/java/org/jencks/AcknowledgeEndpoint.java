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

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.resource.ResourceException;
import javax.resource.spi.endpoint.MessageEndpoint;
import java.lang.reflect.Method;

/**
 * An endpoint which uses a JMS {@link javax.jms.Message#acknowledge()} to
 * acknowledge that a message is delivered.
 *
 * @version $Revision$
 */
public class AcknowledgeEndpoint implements MessageEndpoint, MessageListener {

    private MessageListener messageListener;
    private Message message;

    public AcknowledgeEndpoint(MessageListener messageListener) {
        this.messageListener = messageListener;
    }

    public void beforeDelivery(Method method) throws NoSuchMethodException, ResourceException {
    }

    public void afterDelivery() throws ResourceException {
        if (message == null) {
            throw new ResourceException("No message has been delivered yet");
        }
        try {
            message.acknowledge();
        }
        catch (JMSException e) {
            throw new ResourceException(e);
        }
        finally {
            message = null;
        }
    }

    public void release() {
    }

    public void onMessage(Message message) {
        messageListener.onMessage(message);
        this.message = message;
    }
}
