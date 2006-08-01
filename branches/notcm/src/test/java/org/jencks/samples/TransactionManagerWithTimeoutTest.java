/* =====================================================================
 *
 * Copyright (c) 2006 Dain Sundstrom.  All rights reserved.
 *
 * =====================================================================
 */
package org.jencks.samples;

public class TransactionManagerWithTimeoutTest extends AbstractTransactionManagerTest {
    protected String[] getConfigLocations() {
        return new String[]{"org/jencks/samples/transaction-manager-with-timeout.xml"};
    }
}
