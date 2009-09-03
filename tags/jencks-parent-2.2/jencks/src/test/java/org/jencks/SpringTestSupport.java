package org.jencks;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @version $Revision$
 */
public abstract class SpringTestSupport extends TestCase {

    protected Log log = LogFactory.getLog(getClass());
    protected ConfigurableApplicationContext applicationContext;

    static {
        org.apache.log4j.PropertyConfigurator.configure(SpringTestSupport.class.getClassLoader().getResource("log4j.properties"));
    }

    protected void setUp() throws Exception {
        applicationContext = createApplicationContext();
        assertNotNull("Should have an ApplicationContext", applicationContext);
    }

    protected void tearDown() throws Exception {
        if (applicationContext != null) {
            applicationContext.close();
        }
    }

    protected ConfigurableApplicationContext createApplicationContext() {
        return new ClassPathXmlApplicationContext(getApplicationContextXml());
    }

    protected abstract String getApplicationContextXml();

    /**
     * Finds the mandatory bean in the application context failing if its not
     * there
     */
    protected Object getBean(String name) {
        Object answer = applicationContext.getBean(name);
        assertNotNull("Could not find bean in ApplicationContext called: " + name, answer);
        return answer;
    }
}
