package org.jencks.samples.outbound;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class AbstractDependencyInjectionSpringContextTests extends org.springframework.test.AbstractDependencyInjectionSpringContextTests {

    protected Log log = LogFactory.getLog(getClass());
    
	protected void onTearDown() throws Exception {
		applicationContext.close();
	}
}
