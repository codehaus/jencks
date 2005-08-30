package org.jencks.samples.outbound;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public abstract class AbstractJmsOutboundMessagingTest extends AbstractDependencyInjectionSpringContextTests {

	public final static String TEST_MESSAGE="test message";
	
	private ConnectionFactory connectionFactory;
	private Queue queue;
	private PlatformTransactionManager transactionManager;
	
	public PlatformTransactionManager getTransactionManager() {
		return transactionManager;
	}

	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	public Queue getQueue() {
		return queue;
	}

	public void setQueue(Queue queue) {
		this.queue = queue;
	}

	public ConnectionFactory getConnectionFactory() {
		return connectionFactory;
	}

	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	public void testOutboundWithCommit() throws Exception {
		//Send the message in a JTA transaction
		DefaultTransactionDefinition definition=new DefaultTransactionDefinition();
		definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		TransactionStatus status=null;
		try {
			status=transactionManager.getTransaction(definition);
			JmsTemplate template=new JmsTemplate(getConnectionFactory());
			template.convertAndSend(queue,TEST_MESSAGE);
			transactionManager.commit(status);
		} catch(Exception ex) {
			ex.printStackTrace();
			transactionManager.rollback(status);
			fail("Undesired exception.");
		}

		//Check if the message has been sent
		checkIfMessageExist(TEST_MESSAGE);
	}

	private void checkIfMessageExist(String sentMessage) {
		JmsTemplate template=new JmsTemplate(getConnectionFactory());
		template.setReceiveTimeout(10);
		String receivedMessage=(String)template.receiveAndConvert(queue);
		assertEquals(sentMessage,receivedMessage);
	}

	public void testOutboundWithRollback() throws Exception {
		//Send the message in a JTA transaction
		DefaultTransactionDefinition definition=new DefaultTransactionDefinition();
		definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		TransactionStatus status=null;
		try {
			status=transactionManager.getTransaction(definition);
			JmsTemplate template=new JmsTemplate(getConnectionFactory());
			template.convertAndSend(queue,TEST_MESSAGE);
			transactionManager.rollback(status);
		} catch(Exception ex) {
			ex.printStackTrace();
			transactionManager.rollback(status);
			fail("Undesired exception.");
		}

		//Check if the message has not been sent
		checkIfMessageNotExist();
	}

	private void checkIfMessageNotExist() {
		JmsTemplate template=new JmsTemplate(getConnectionFactory());
		template.setReceiveTimeout(10);
		String receivedMessage=null;
		receivedMessage=(String)template.receiveAndConvert(queue);
		assertNull(receivedMessage);
	}

}
