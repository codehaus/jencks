package org.jencks;

import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageListener;

import junit.framework.TestCase;

import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.jms.core.JmsTemplate;

public class LoadTest extends TestCase {

    public final static String TEST_MESSAGE = "test message";
    public final static String INPUT_QUEUE = "test.in";
    public final static String OUTPUT_QUEUE = "test.out";
    
    public static final int SENDER_THREADS = 100;
    public static final int SENDER_MSGS_PER_THREAD = 100;
    
    protected AbstractXmlApplicationContext context;
    
    protected void setUp() throws Exception {
        context = new ClassPathXmlApplicationContext("org/jencks/load-test.xml");
    }
    
    protected void tearDown() throws Exception {
        if (context != null) {
            context.destroy();
        }
    }
    public void test() throws Exception {
        ConnectionFactory factory = (ConnectionFactory) context.getBean("pooledConnectionFactory");
        final JmsTemplate template = new JmsTemplate(factory);
        Thread[] threads = new Thread[SENDER_THREADS];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread() {
                public void run() {
                    for (int msg = 0; msg < SENDER_MSGS_PER_THREAD; msg++) {
                        //System.out.println("Sending message " + msg + " from thread " + Thread.currentThread().getName());
                        template.convertAndSend(INPUT_QUEUE, TEST_MESSAGE);
                    }
                }
            };
        }
        for (int i = 0; i < threads.length; i++) {
            threads[i].start();
        }
        for (int i = 0; i < threads.length; i++) {
            threads[i].join();
        }
        System.err.println("Sent");
    }
    
    public static class Sender implements MessageListener {
        private JmsTemplate template;
        public void setConnectionFactory(ConnectionFactory factory) {
            this.template = new JmsTemplate(factory);
        }
        public void onMessage(Message msg) {
            template.convertAndSend(OUTPUT_QUEUE, TEST_MESSAGE);
        }
    }

}
