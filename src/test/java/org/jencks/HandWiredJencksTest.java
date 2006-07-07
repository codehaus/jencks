package org.jencks;

import java.lang.reflect.Method;

import javax.jms.Connection;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.resource.ResourceException;
import javax.resource.spi.UnavailableException;
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
import org.apache.geronimo.connector.work.GeronimoWorkManager;
import org.apache.geronimo.transaction.ExtendedTransactionManager;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.apache.geronimo.transaction.log.UnrecoverableLog;
import org.apache.geronimo.transaction.manager.TransactionManagerImpl;
import org.apache.geronimo.transaction.manager.WrapperNamedXAResource;
import org.apache.geronimo.transaction.manager.XidImporter;

import EDU.oswego.cs.dl.util.concurrent.CopyOnWriteArrayList;
import EDU.oswego.cs.dl.util.concurrent.Latch;

public class HandWiredJencksTest extends TestCase {

    public class StubMessageEndpoint implements MessageEndpoint, MessageListener {
        public int messageCount;
        private final ExtendedTransactionManager etm;
        private final XAResource resource;

        public StubMessageEndpoint(ExtendedTransactionManager etm, XAResource resource) {
            this.etm = etm;
            this.resource = new WrapperNamedXAResource(resource, "test");
        }

        public void beforeDelivery(Method method) throws NoSuchMethodException, ResourceException {
            try {
                etm.begin();
                etm.getTransaction().enlistResource(resource);
            }
            catch (Throwable e) {
                throw new ResourceException(e);
            }
        }

        public void afterDelivery() throws ResourceException {
            try {
                etm.commit();
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


        ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false");
        Connection connection = cf.createConnection();
        connection.start();

        CopyOnWriteArrayList resourceAdapters = null; //new CopyOnWriteArrayList();
        TransactionManagerImpl tm = new TransactionManagerImpl(600, new UnrecoverableLog(), resourceAdapters);

        final ExtendedTransactionManager etm = tm;
        XidImporter xidImporter = tm;
        TransactionContextManager manager = new TransactionContextManager(etm, xidImporter);

        GeronimoWorkManager workManager = new GeronimoWorkManager(1000, manager);
        workManager.doStart();

        ActiveMQResourceAdapter ra = new ActiveMQResourceAdapter();
        ra.setServerUrl("vm://localhost");

        ResourceAdapterWrapper raWrapper = new ResourceAdapterWrapper(ra, workManager);
        raWrapper.doStart();

        ActiveMQActivationSpec as = new ActiveMQActivationSpec();
        as.setDestination("TEST");
        as.setDestinationType(Queue.class.getName());
            ActivationSpecWrapper asWrapper = new ActivationSpecWrapper(as, raWrapper);

        final Latch messageDelivered = new Latch();
        MessageEndpointFactory messageEndpointFactory = new MessageEndpointFactory() {
            public MessageEndpoint createEndpoint(XAResource resource) throws UnavailableException {
                return new StubMessageEndpoint(etm, resource) {
                    public void onMessage(Message message) {
                        super.onMessage(message);
                        messageDelivered.release();
                    }

                    ;
                };
            }

            public boolean isDeliveryTransacted(Method method) throws NoSuchMethodException {
                return true;
            }
        };

        asWrapper.activate(messageEndpointFactory);

        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageProducer producer = session.createProducer(new ActiveMQQueue("TEST"));
        producer.send(session.createTextMessage("Hello"));

        assertTrue(messageDelivered.attempt(1000 * 5));
    }

}
