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

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;

/**
 * An example POJO based bean which uses JMS via Dependency Injection
 *
 * @version $Revision$
 */
public class ServiceBean {
    private Session session;
    private MessageProducer producer;
    private Destination destination;

    public ServiceBean(Session session, MessageProducer producer, Destination destination) {
        this.session = session;
        this.producer = producer;
        this.destination = destination;
    }

    public void sayHello(String name) throws JMSException {
        Message message = session.createTextMessage("Hello " + name);
        producer.send(destination, message);
    }
}
