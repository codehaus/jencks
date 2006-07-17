package org.jencks.samples.outbound;

import java.sql.Types;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class MultipleOutboundMessagingTest extends AbstractDependencyInjectionSpringContextTests {

	public final static String TEST_FIELD_VALUE="test value";
	public final static String INITIAL_FIELD_VALUE="initial value";
	public final static int FIELD_ID=1;
	
	public final static String CREATE_SCHEMA="create table TEST (" +
			"TEST_ID bigint generated by default as identity(start with 1)," +
			"TEST_VALUE varchar(255)," +
			"primary key (TEST_ID))";
	public final static String DROP_SCHEMA="drop table TEST";
	public final static String POPULATE_SCHEMA="insert into TEST" +
			" (TEST_ID,TEST_VALUE) values("+FIELD_ID+",'"+INITIAL_FIELD_VALUE+"')";
	
	public final static String UPDATE_FIELD_REQUEST="update TEST" +
			" set TEST_VALUE=? where TEST_ID=?";
	public final static String SELECT_FIELD_REQUEST="select TEST_VALUE" +
			" from TEST where TEST_ID=?";
	
	public final static String TEST_MESSAGE="test message";
	
	private DataSource dataSource;
	private ConnectionFactory connectionFactory;
	private Queue queue;
	private PlatformTransactionManager transactionManager;
	
	public PlatformTransactionManager getTransactionManager() {
		return transactionManager;
	}

	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
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

	private void updateDatabase(String ddlRequest) {
		JdbcTemplate template=new JdbcTemplate(getDataSource());
		template.update(ddlRequest);
	}
	
	protected String[] getConfigLocations() {
		return new String[] { "org/jencks/samples/outbound/jencks-multiple.xml" };
	}

	protected void onSetUp() throws Exception {
		super.onSetUp();
		updateDatabase(CREATE_SCHEMA);
		updateDatabase(POPULATE_SCHEMA);
	}

	protected void onTearDown() throws Exception {
		super.onTearDown();
		updateDatabase(DROP_SCHEMA);
	}

	private void checkStoredMessage(String message) {
		JdbcTemplate template=new JdbcTemplate(getDataSource());
		String storedMessage=(String)template.queryForObject(SELECT_FIELD_REQUEST,
				new Object[] {new Integer(FIELD_ID)},
				new int[] {Types.INTEGER},String.class);
		assertEquals(message,storedMessage);
	}

	private void checkIfMessageExist(String sentMessage) {
		JmsTemplate template=new JmsTemplate(getConnectionFactory());
		template.setReceiveTimeout(1000);
		String receivedMessage=(String)template.receiveAndConvert(queue);
		assertEquals(sentMessage,receivedMessage);
	}

	private void checkIfMessageNotExist() {
		JmsTemplate template=new JmsTemplate(getConnectionFactory());
		template.setReceiveTimeout(10);
		String receivedMessage=null;
		receivedMessage=(String)template.receiveAndConvert(queue);
		assertNull(receivedMessage);
	}

	public void testOutboundWithCommit() throws Exception {
		//Update the field and send a JMS message in a JTA transaction
		DefaultTransactionDefinition definition=new DefaultTransactionDefinition();
		definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		TransactionStatus status=null;
		try {
			status=transactionManager.getTransaction(definition);

			//JDBC
			JdbcTemplate jdbcTemplate=new JdbcTemplate(getDataSource());
			jdbcTemplate.update(UPDATE_FIELD_REQUEST,
					new Object[] {TEST_FIELD_VALUE,new Integer(FIELD_ID)},
					new int[] {Types.VARCHAR,Types.INTEGER});
			//JMS
			JmsTemplate jmsTemplate=new JmsTemplate(getConnectionFactory());
			jmsTemplate.convertAndSend(queue,TEST_MESSAGE);

			transactionManager.commit(status);
		} catch(Exception ex) {
			ex.printStackTrace();
			transactionManager.rollback(status);
			fail("Undesired exception.");
		}

		//Check if the message has been stored in the database
		checkStoredMessage(TEST_FIELD_VALUE);
		//Check if the message has been sent
		checkIfMessageExist(TEST_MESSAGE);
	}

	public void testOutboundWithRollback() throws Exception {
		//Update the field and send a JMS message in a JTA transaction
		DefaultTransactionDefinition definition=new DefaultTransactionDefinition();
		definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		TransactionStatus status=null;
		try {
			status=transactionManager.getTransaction(definition);

			//JDBC
			JdbcTemplate jdbcTemplate=new JdbcTemplate(getDataSource());
			jdbcTemplate.update(UPDATE_FIELD_REQUEST,
					new Object[] {TEST_FIELD_VALUE,new Integer(FIELD_ID)},
					new int[] {Types.VARCHAR,Types.INTEGER});
			//JMS
			JmsTemplate jmsTemplate=new JmsTemplate(getConnectionFactory());
			jmsTemplate.convertAndSend(queue,TEST_MESSAGE);

			transactionManager.rollback(status);
		} catch(Exception ex) {
			ex.printStackTrace();
			transactionManager.rollback(status);
			fail("Undesired exception.");
		}

		//Check if the message has not been stored in the database
		checkStoredMessage(INITIAL_FIELD_VALUE);
		//Check if the message has not been sent
		checkIfMessageNotExist();
	}

}
