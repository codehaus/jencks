package org.jencks.samples.outbound;

public abstract class AbstractDependencyInjectionSpringContextTests extends org.springframework.test.AbstractDependencyInjectionSpringContextTests {

	protected void onTearDown() throws Exception {
		applicationContext.close();
	}
}
