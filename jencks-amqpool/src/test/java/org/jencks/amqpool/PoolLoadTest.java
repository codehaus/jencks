package org.jencks.amqpool;

import java.util.concurrent.CountDownLatch;

import javax.jms.ConnectionFactory;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ResourceAdapter;

import junit.framework.TestCase;

import org.apache.activemq.ra.ActiveMQManagedConnectionFactory;
import org.apache.activemq.ra.ActiveMQResourceAdapter;
import org.apache.geronimo.connector.outbound.GeronimoConnectionEventListener;
import org.jencks.GeronimoPlatformTransactionManager;
import org.jencks.factory.ConnectionFactoryFactoryBean;
import org.jencks.factory.ConnectionManagerFactoryBean;
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
    
    public void testJcaPolling() throws Exception {
        ConnectionManagerFactoryBean cmfb = new ConnectionManagerFactoryBean();
        cmfb.setTransactionManager(txManager);
        cmfb.afterPropertiesSet();
        ActiveMQResourceAdapter ra = new ActiveMQResourceAdapter();
        ra.setServerUrl("tcp://localhost:61616");
        ConnectionFactoryFactoryBean cffb = new ConnectionFactoryFactoryBean();
        cffb.setConnectionManager((ConnectionManager) cmfb.getObject());
        ActiveMQManagedConnectionFactory mcf = new ActiveMQManagedConnectionFactory();
        mcf.setResourceAdapter(ra);
        cffb.setManagedConnectionFactory(mcf);
        ConnectionFactory jcaCf = (ConnectionFactory) cffb.getConnectionFactory();
        runThreads(1, 10, jcaCf, 0);
        JcaPooledConnectionFactory pooledCf = new JcaPooledConnectionFactory("tcp://localhost:61616");
        pooledCf.setTransactionManager(txManager);
        runThreads(1, 10, pooledCf, 0);
        
        System.err.println("==============");
        //runThreads(10, 100, pooledCf, 0);
        pooledCf.setMaxConnections(10);
        runThreads(10, 100, pooledCf, 0);
        runThreads(10, 100, jcaCf, 0);
    }
    
    protected void loadTest(String url) throws Exception {
        JcaPooledConnectionFactory factory = new JcaPooledConnectionFactory(url);
        factory.setTransactionManager(txManager);

        System.err.println("==============");
        System.err.println("Using url: " + url);
        
        for (int max = 1; max <= 16; max *= 2) {
            factory.setMaxConnections(max);
            // Warm up
            System.err.println("======== " + max + " connections ======");
            System.err.println("Warm up");
            runThreads(1, max * 10, factory, 0);
            System.err.println("================");
            
            /*
            runThreads(1, 1000, factory, 0);
            runThreads(1, 1000, factory, 1);
            runThreads(1, 1000, factory, 10);
            runThreads(1, 1000, factory, 1000);
            runThreads(10, 100, factory, 0);
            runThreads(10, 100, factory, 1);
            runThreads(10, 100, factory, 10);
            runThreads(10, 100, factory, 100);
            */
            runThreads(100, 100, factory, 0);
            runThreads(100, 100, factory, 1);
            runThreads(100, 100, factory, 10);
            /*
            runThreads(1000, 1, factory, 0);
            runThreads(1000, 1, factory, 1);
            */
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
