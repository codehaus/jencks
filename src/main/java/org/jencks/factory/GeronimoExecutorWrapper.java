/* =====================================================================
 *
 * Copyright (c) 2006 Dain Sundstrom.  All rights reserved.
 *
 * =====================================================================
 */
package org.jencks.factory;

import org.apache.geronimo.pool.GeronimoExecutor;
import EDU.oswego.cs.dl.util.concurrent.Executor;

/**
 * @version $Revision$ $Date$
 */
public class GeronimoExecutorWrapper implements GeronimoExecutor {
    private final Executor executor;

    public GeronimoExecutorWrapper(Executor executor) {
        this.executor = executor;
    }

    public String getName() {
        return executor.toString();
    }

    public String getObjectName() {
        return null;
    }

    public void execute(String name, Runnable runnable) throws InterruptedException {
        executor.execute(runnable);
    }

    public int getPoolSize() {
        return 0;
    }

    public void execute(Runnable runnable) throws InterruptedException {
        executor.execute(runnable);
    }
}
