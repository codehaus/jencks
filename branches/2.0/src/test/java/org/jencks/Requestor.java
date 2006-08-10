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
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;

/**
 * A helper class for performing remote method invocations over JMS.
 * This class is a polymorphic version of the {@link javax.jms.QueueRequestor} and {@link javax.jms.TopicRequestor}
 * that ship with the JMS API which take advantage of the polymorphism of JMS 1.1 and which also supports timeouts.
 *
 * @version $Revision$
 */
public class Requestor {
    private Session session;
    private Destination temporaryDestination;
    private MessageProducer sender;
    private MessageConsumer receiver;
    private long maximumTimeout = 20000L;


    /**
     * Constructor for the <CODE>Requestor</CODE> class.
     * <p/>
     * <P>This implementation assumes the session parameter to be non-transacted,
     * with a delivery mode of either <CODE>AUTO_ACKNOWLEDGE</CODE> or
     * <CODE>DUPS_OK_ACKNOWLEDGE</CODE>.
     *
     * @param session     the <CODE>Session</CODE> the queue belongs to
     * @param destination the destination to perform the request/reply call on
     * @throws javax.jms.JMSException if the JMS provider fails to create the
     *                                <CODE>Requestor</CODE> due to some internal
     *                                error.
     * @throws javax.jms.InvalidDestinationException
     *                                if an invalid queue is specified.
     */
    public Requestor(Session session, Destination destination) throws JMSException {
        this.session = session;
        temporaryDestination = createTemporaryDestination(session);
        sender = session.createProducer(destination);
        receiver = session.createConsumer(temporaryDestination);
    }


    /**
     * Sends a request and waits for a reply. The temporary queue is used for
     * the <CODE>JMSReplyTo</CODE> destination, and only one reply per request
     * is expected.
     *
     * @param message the message to send
     * @return the reply message
     * @throws javax.jms.JMSException if the JMS provider fails to complete the
     *                                request due to some internal error.
     */
    public Message request(Message message) throws JMSException {
        message.setJMSReplyTo(temporaryDestination);
        sender.send(message);
        long timeout = getMaximumTimeout();
        if (timeout > 0) {
            return receiver.receive(timeout);
        }
        else {
            return receiver.receive();
        }
    }


    /**
     * Sends a request and waits for a reply up to a maximum timeout. The temporary queue is used for
     * the <CODE>JMSReplyTo</CODE> destination, and only one reply per request
     * is expected.
     *
     * @param message the message to send
     * @return the reply message
     * @throws javax.jms.JMSException if the JMS provider fails to complete the
     *                                request due to some internal error.
     */
    public Message request(Message message, long timeout) throws JMSException {
        message.setJMSReplyTo(temporaryDestination);
        sender.send(message);
        return receiver.receive(timeout);
    }


    /**
     * Closes the <CODE>Requestor</CODE> and its session.
     * <p/>
     * <P>Since a provider may allocate some resources on behalf of a
     * <CODE>Requestor</CODE> outside the Java virtual machine, clients
     * should close them when they
     * are not needed. Relying on garbage collection to eventually reclaim
     * these resources may not be timely enough.
     * <p/>
     * <P>Note that this method closes the <CODE>Session</CODE> object
     * passed to the <CODE>Requestor</CODE> constructor.
     *
     * @throws javax.jms.JMSException if the JMS provider fails to close the
     *                                <CODE>Requestor</CODE> due to some internal
     *                                error.
     */
    public void close() throws JMSException {
        // producer and consumer created by constructor are implicitly closed.
        session.close();
        if (temporaryDestination instanceof TemporaryQueue) {
            ((TemporaryQueue) temporaryDestination).delete();
        }
        else if (temporaryDestination instanceof TemporaryTopic) {
            ((TemporaryTopic) temporaryDestination).delete();
        }
    }

    public long getMaximumTimeout() {
        return maximumTimeout;
    }

    /**
     * Sets the maximum default timeout used for remote requests. If set to <= 0 then
     * the timeout is ignored.
     *
     * @param maximumTimeout
     */
    public void setMaximumTimeout(long maximumTimeout) {
        this.maximumTimeout = maximumTimeout;
    }

    protected TemporaryQueue createTemporaryDestination(Session session) throws JMSException {
        return session.createTemporaryQueue();
    }
}
