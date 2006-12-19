package org.jencks.amqpool;

import java.util.concurrent.CountDownLatch;

import javax.jms.ConnectionFactory;

import junit.framework.TestCase;

import org.jencks.GeronimoPlatformTransactionManager;
import org.springframework.jms.core.JmsTemplate;

public class PoolLoadTest extends TestCase {

    public final static String TEST_MESSAGE = "test message";
    public final static String INPUT_QUEUE = "test.in";
    
    private GeronimoPlatformTransactionManager txManager;
    
    public PoolLoadTest() throws Exception {
        txManager = new GeronimoPlatformTransactionManager();
    }
    
    public void testSyncSend() throws Exception {
        loadTest("tcp://localhost:61616?jms.useAsyncSend=false");
    }
    
    public void testAsyncSend() throws Exception {
        loadTest("tcp://localhost:61616?jms.useAsyncSend=true");
    }
    
    protected void loadTest(String url) throws Exception {
        JcaPooledConnectionFactory factory = new JcaPooledConnectionFactory(url);
        factory.setTransactionManager(txManager);

        System.err.println("==============");
        System.err.println("Using url: " + url);
        
        for (int max = 1; max <= 100; max *= 10) {
            factory.setMaxConnections(max);
            // Warm up
            System.err.println("======== " + max + " connections ======");
            System.err.println("Warm up");
            runThreads(1, max, factory, 0);
            System.err.println("================");
            
            //runThreads(1, 1000, factory, 0);
            //runThreads(1, 1000, factory, 1);
            //runThreads(1, 1000, factory, 10);
            //runThreads(1, 1000, factory, 1000);
            runThreads(10, 100, factory, 0);
            runThreads(10, 100, factory, 1);
            runThreads(10, 100, factory, 10);
            runThreads(10, 100, factory, 100);
            runThreads(100, 10, factory, 0);
            runThreads(100, 10, factory, 1);
            runThreads(100, 10, factory, 10);
            runThreads(1000, 1, factory, 0);
            runThreads(1000, 1, factory, 1);
        }
    }
    
    protected long runThreads(final int nbThreads, final int msgsPerThread, final ConnectionFactory factory, final int txBatch) throws InterruptedException {
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
                    try {
                        int i = 0;
                        for (int msg = 0; msg < msgsPerThread; msg++) {
                            //System.out.println("Sending message " + msg + " from thread " + Thread.currentThread().getName());
                            if (txBatch > 0) {
                                if (i++ == 0) {
                                    txManager.begin();
                                }
                            }
                            template.convertAndSend(INPUT_QUEUE, TEST_MESSAGE);
                            if (txBatch > 0) {
                                if (i == txBatch || msg == msgsPerThread - 1) {
                                    txManager.commit();
                                    i = 0;
                                }
                            }
                            l.countDown();
                        }
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
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
        System.err.println("TxBatch: " + txBatch);
        System.err.println("Threads: " + nbThreads + ", Msg/Thread: " + msgsPerThread);
        System.err.println("Sent " + (nbThreads * msgsPerThread) + " messages in " + ((t1 - t0) / 1000.0) + " secs");
        System.err.println("Throughput: " + ((nbThreads * msgsPerThread * 1000.0) / (t1 - t0)) + " msgs/sec");
        return t1 - t0;
    }
    
}
