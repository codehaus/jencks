/*
 * Copyright 2006 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jencks.samples.inbound;

import org.springframework.context.ConfigurableApplicationContext;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;

/**
 * @version $Revision: 86 $
 */
public class ActiveMQInboundXBeanTest extends AbstractActiveMQTest {
    protected ConfigurableApplicationContext loadContextLocations(String[] strings) {
        return new ClassPathXmlApplicationContext(strings);
    }

    protected String[] getConfigLocations() {
        return new String[]{"org/jencks/samples/inbound/activemq-xbean.xml"};
    }
}
