/* =====================================================================
 *
 * Copyright (c) 2006 Dain Sundstrom.  All rights reserved.
 *
 * =====================================================================
 */
package org.jencks.samples.outbound;

public class TranqlOutboundTest extends AbstractJdbcOutboundMessagingTest {

    protected String[] getConfigLocations() {
        return new String[] { "org/jencks/samples/outbound/jencks-tranql.xml" };
    }

}
