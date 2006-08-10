package org.jencks;

import java.lang.reflect.Method;

import javax.jms.Connection;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.resource.ResourceException;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;

import junit.framework.TestCase;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.ra.ActiveMQActivationSpec;
import org.apache.activemq.ra.ActiveMQResourceAdapter;
import org.apache.geronimo.connector.ActivationSpecWrapper;
import org.apache.geronimo.connector.ResourceAdapterWrapper;
import org.apache.geronimo.connector.GeronimoBootstrapContext;
import org.apache.geronimo.connector.work.GeronimoWorkManager;
import org.apache.geronimo.transaction.manager.WrapperNamedXAResource;
import org.apache.geronimo.transaction.manager.GeronimoTransactionManager;

import EDU.oswego.cs.dl.util.concurrent.Latch;
import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

public class HandWiredJencksTest extends TestCase {

    public class StubMessageEndpoint implements MessageEndpoint, MessageListener {
        public int messageCount;
        private final GeronimoTransactionManager tm;
        private final XAResource resource;

        public StubMessageEndpoint(GeronimoTransactionManager tm, XAResource resource) {
            this.tm = tm;
            this.resource = new WrapperNamedXAResource(resource, "test");
        }

        public void beforeDelivery(Method method) throws NoSuchMethodException, ResourceException {
            try {
                tm.begin();
                tm.getTransaction().enlistResource(resource);
            }
            catch (Throwable e) {
                throw new ResourceException(e);
            }
        }

        public void afterDelivery() throws ResourceException {
            try {
                tm.commit();
            }
            catch (Throwable e) {
                throw new ResourceException(e);
            }
        }

        public void release() {
        }

        public void onMessage(Message message) {
            messageCount++;
        }
    }

    public void testIt() throws Exception {

        // ConnectionFactory
        ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false");
        Connection connection = cf.createConnection();
        connection.start();

        // TransactionManager
        final GeronimoTransactionManager tm = new GeronimoTransactionManager();

        // Work Manager (transaction manager)
        PooledExecutor executor = new PooledExecutor(1000);
        GeronimoWorkManager workManager = new GeronimoWorkManager(executor, executor, executor, tm);
        workManager.doStart();

        try {
            // RA
            ActiveMQResourceAdapter ra = new ActiveMQResourceAdapter();
            ra.setServerUrl("vm://localhost");

            // ResourceAdapter (RA, work manager, transaction manager)
            GeronimoBootstrapContext bootstrapContext = new GeronimoBootstrapContext(workManager, tm);

            // activation spec
            ActiveMQActivationSpec as = new ActiveMQActivationSpec();
            as.setDestination("TEST");
            as.setDestinationType(Queue.class.getName());

            // endpoint factory
            final Latch messageDelivered = new Latch();
            MessageEndpointFactory messageEndpointFactory = new MessageEndpointFactory() {
                public MessageEndpoint createEndpoint(XAResource resource) {
                    return new StubMessageEndpoint(tm, resource) {
                        public void onMessage(Message message) {
                            super.onMessage(message);
                            messageDelivered.release();
                        }
                    };
                }

                public boolean isDeliveryTransacted(Method method) {
                    return true;
                }
            };

            // Geronimo wrapper
            ResourceAdapterWrapper raWrapper = new ResourceAdapterWrapper(ra, bootstrapContext);
            raWrapper.doStart();
            ActivationSpecWrapper asWrapper = new ActivationSpecWrapper(as, raWrapper);
            asWrapper.activate(messageEndpointFactory);

            // test it
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer producer = session.createProducer(new ActiveMQQueue("TEST"));
            producer.send(session.createTextMessage("Hello"));

            assertTrue(messageDelivered.attempt(1000 * 5));
        } finally {
            workManager.doStop();
        }
    }

}
