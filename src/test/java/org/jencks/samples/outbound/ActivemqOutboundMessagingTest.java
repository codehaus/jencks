package org.jencks.samples.outbound;


public class ActivemqOutboundMessagingTest extends AbstractJmsOutboundMessagingTest {

	protected String[] getConfigLocations() {
		return new String[] { "org/jencks/samples/outbound/jencks-activemq.xml" };
	}

}
