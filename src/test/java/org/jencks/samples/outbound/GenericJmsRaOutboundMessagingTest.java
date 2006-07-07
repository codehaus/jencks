package org.jencks.samples.outbound;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;


public class GenericJmsRaOutboundMessagingTest extends AbstractJmsOutboundMessagingTest {

	protected String[] getConfigLocations() {
		return new String[] { "org/jencks/samples/outbound/jencks-genericjmsra.xml" };
	}

	protected void checkIfMessageExist(final String sentMessage) {
		TransactionTemplate tt = new TransactionTemplate(getTransactionManager());
		tt.execute(new TransactionCallback() {
			public Object doInTransaction(TransactionStatus status) {
				JmsTemplate template=new JmsTemplate(getConnectionFactory());
				template.setReceiveTimeout(10);
				String receivedMessage=(String)template.receiveAndConvert(getQueue());
				assertEquals(sentMessage,receivedMessage);
				return null;
			}
		});
	}

	protected void checkIfMessageNotExist() {
		TransactionTemplate tt = new TransactionTemplate(getTransactionManager());
		tt.execute(new TransactionCallback() {
			public Object doInTransaction(TransactionStatus status) {
				JmsTemplate template=new JmsTemplate(getConnectionFactory());
				template.setReceiveTimeout(10);
				String receivedMessage=null;
				receivedMessage=(String)template.receiveAndConvert(getQueue());
				assertNull(receivedMessage);
				return null;
			}
		});
	}

}
