/**
 * 
 * Copyright 2005 LogicBlaze, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 **/
package org.jencks;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.Message;

/**
 * @version $Revision$
 */
public class TestBean extends TestingConsumer {
    private static final Log log = LogFactory.getLog(TestBean.class);
    
    private static int globalCounter = 0;

    public TestBean() {
        log.info("Created instance of consumer bean: " + this);
    }

    public void onMessage(Message message) {
        log.info("TestBean: " + getNextValue() + " received message: " + message);
        super.onMessage(message);
    }

    protected static synchronized int getNextValue() {
        return ++globalCounter;
    }

}
