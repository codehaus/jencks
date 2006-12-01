package org.jencks;

import java.util.concurrent.CountDownLatch;

import javax.jms.ConnectionFactory;

import junit.framework.TestCase;

import org.apache.activemq.pool.PooledConnectionFactory;
import org.springframework.jms.core.JmsTemplate;

public class PoolLoadTest extends TestCase {

    public final static String TEST_MESSAGE = "test message";
    public final static String INPUT_QUEUE = "test.in";
    
    public void testSyncSend() throws Exception {
        loadTest("tcp://localhost:61616?jms.useAsyncSend=false");
    }
    
    public void testAsyncSend() throws Exception {
        loadTest("tcp://localhost:61616?jms.useAsyncSend=true");
    }
    
    protected void loadTest(String url) throws Exception {
        PooledConnectionFactory factory = new PooledConnectionFactory(url);

        System.err.println("==============");
        System.err.println("Using url: " + url);
        
        for (int max = 1; max <= 100; max *= 10) {
            factory.setMaxConnections(max);
            // Warm up
            System.err.println("======== " + max + " connections ======");
            System.err.println("Warm up");
            runThreads(1, max, factory);
            System.err.println("================");
            
            runThreads(1, 1000, factory);
            runThreads(10, 100, factory);
            runThreads(100, 10, factory);
            runThreads(1000, 1, factory);
        }
    }
    
    protected long runThreads(final int nbThreads, final int msgsPerThread, final ConnectionFactory factory) throws InterruptedException {
        final JmsTemplate template = new JmsTemplate(factory);
        final CountDownLatch sem = new CountDownLatch(1);
        final CountDownLatch l = new CountDownLatch(nbThreads * msgsPerThread);
        Thread[] threads = new Thread[nbThreads];
        for (int i = 0; i < nbThreads; i++) {
            threads[i] = new Thread() {
                public void run() {
                    try {
                        sem.await();
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
    
}
