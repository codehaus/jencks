package org.jencks.factory;

import org.jencks.SpringTestSupport;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkManager;

/**
 * @version $Revision$
 */
public class WorkManagerFactoryBeanTest extends SpringTestSupport {

    AtomicBoolean flag = new AtomicBoolean(false);
    Object lock = new Object();

    public void testWorkManager() throws Exception {
        WorkManager workManager = (WorkManager) getBean("workManager");
        workManager.scheduleWork(new Work() {
            public void release() {
            }

            public void run() {
                flag.set(true);
                synchronized (lock) {
                    lock.notifyAll();
                }
            }
        });


        if (!flag.get()) {
            synchronized (lock) {
                lock.wait(2000);
            }
        }

        assertTrue("Should have set the flag by now", flag.get());
    }

    protected String getApplicationContextXml() {
        return "org/jencks/factory/workManager.xml";
    }
}
