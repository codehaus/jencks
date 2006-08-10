/**
 *
 * Copyright 2004 Protique Ltd
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

import java.util.ArrayList;
import java.util.List;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A simple consumer which is useful for testing which can be used to wait until the consumer has received
 * a specific number of messages.  Swiped from activemq 3.x
 *
 * @author Mike Perham
 * @version $Revision$
 */
public class TestingConsumer implements MessageListener {
    private static final Log log = LogFactory.getLog(TestingConsumer.class);
    private final List messages = new ArrayList();
    private final Object semaphore;

    public TestingConsumer() {
        this(new Object());
    }

    public TestingConsumer(Object semaphore) {
        this.semaphore = semaphore;
    }

    /**
     * @return all the messages on the list so far, clearing the buffer
     */
    public List flushMessages() {
        synchronized (semaphore) {
            List answer = new ArrayList(messages);
            messages.clear();
            return answer;
        }
    }

    public void onMessage(Message message) {
        synchronized (semaphore) {
            messages.add(message);
            semaphore.notifyAll();
        }
    }

    public void waitForMessageToArrive() {
        waitForMessagesToArrive(1);
    }

    public void waitForMessagesToArrive(int messageCount) {
        log.debug("Waiting for message to arrive");

        long start = System.currentTimeMillis();

        while (System.currentTimeMillis() - start < 10000) {
            try {
                if (hasReceivedMessages(messageCount)) {
                    break;
                }
                synchronized (semaphore) {
                    semaphore.wait(1000);
                }
            }
            catch (InterruptedException e) {
                log.debug("Caught: " + e);
            }
        }
        long end = System.currentTimeMillis() - start;

        log.debug("End of wait for " + end + " millis");
    }

    protected boolean hasReceivedMessage() {
        synchronized (semaphore) {
            return messages.isEmpty();
        }
    }

    protected boolean hasReceivedMessages(int messageCount) {
        synchronized (semaphore) {
            return messages.size() >= messageCount;
        }
    }
}
