package org.jencks.samples.outbound;


public class TranqlOutboundMessagingTest extends AbstractJdbcOutboundMessagingTest {

	protected String[] getConfigLocations() {
		return new String[] { "org/jencks/samples/outbound/jencks-tranql.xml" };
	}

}
