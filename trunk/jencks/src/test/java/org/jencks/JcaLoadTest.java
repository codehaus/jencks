package org.jencks;

import java.util.concurrent.CountDownLatch;

import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageListener;

import junit.framework.TestCase;

import org.apache.activemq.pool.PooledConnectionFactory;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.jms.core.JmsTemplate;

public class JcaLoadTest extends TestCase {

    public final static String TEST_MESSAGE = "test message";
    public final static String INPUT_QUEUE = "test.in";
    public final static String OUTPUT_QUEUE = "test.out";
    
    public static final int SENDER_THREADS = 1;
    public static final int SENDER_MSGS_PER_THREAD = 10;
    
    protected AbstractXmlApplicationContext context;
    
    protected void setUp() throws Exception {
        context = new ClassPathXmlApplicationContext("org/jencks/load-test.xml");
    }
    
    protected void tearDown() throws Exception {
        if (context != null) {
            context.destroy();
        }
    }
    
    public void testLoad() throws Exception {
        ConnectionFactory factory = (ConnectionFactory) context.getBean("managedConnectionFactory");
        runThreads(1, 1000, factory);
    }
    
    protected long runThreads(final int nbThreads, final int msgsPerThread, final ConnectionFactory factory) throws InterruptedException {
        final JmsTemplate template = new JmsTemplate(factory);
        final CountDownLatch sem = new CountDownLatch(1);
        final CountDownLatch l = new CountDownLatch(nbThreads * msgsPerThread);
        Thread[] threads = new Thread[nbThreads];
        for (int i = 0; i < nbThreads; i++) {
            final long waitTime = i * 1000L; 
            threads[i] = new Thread() {
                public void run() {
                    try {
                        sem.await();
                        Thread.sleep(waitTime);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    for (int msg = 0; msg < msgsPerThread; msg++) {
                        //System.out.println("Sending message " + msg + " from thread " + Thread.currentThread().getName());
                        template.convertAndSend(INPUT_QUEUE, TEST_MESSAGE);
                        l.countDown();
                    }
                }
            };
        }
        for (int i = 0; i < nbThreads; i++) {
            threads[i].start();
        }
        long t0 = System.currentTimeMillis();
        sem.countDown();
        l.await();
        long t1 = System.currentTimeMillis();
        for (int i = 0; i < nbThreads; i++) {
            threads[i].join();
        }
        System.err.println("Threads: " + nbThreads + ", Msg/Thread: " + msgsPerThread);
        System.err.println("Sent " + (nbThreads * msgsPerThread) + " messages in " + ((t1 - t0) / 1000.0) + " secs");
        System.err.println("Throughput: " + ((nbThreads * msgsPerThread * 1000.0) / (t1 - t0)) + " msgs/sec");
        return t1 - t0;
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
