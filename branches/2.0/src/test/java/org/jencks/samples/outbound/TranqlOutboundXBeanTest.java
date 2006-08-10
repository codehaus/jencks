package org.jencks.samples.outbound;

import org.springframework.context.ConfigurableApplicationContext;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;


public class TranqlOutboundXBeanTest extends AbstractJdbcOutboundMessagingTest {
    protected ConfigurableApplicationContext loadContextLocations(String[] strings) {
        return new ClassPathXmlApplicationContext(strings);
    }

    protected String[] getConfigLocations() {
        return new String[] { "org/jencks/samples/outbound/jencks-tranql-xbean.xml" };
    }

}
