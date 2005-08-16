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

import org.springframework.aop.TargetSource;
import org.springframework.beans.factory.InitializingBean;

import javax.jms.Message;
import javax.jms.MessageListener;

/**
 * Represents a pool of MessageListener instances using Spring's
 * TargetSource for the underlying pool.
 *
 * @version $Revision$
 */
public class TargetSourceMessageListener implements MessageListener, InitializingBean {
    private TargetSource targetSource;

    public void onMessage(Message message) {
        MessageListener delegate = null;
        try {
            delegate = (MessageListener) targetSource.getTarget();
        }
        catch (Exception e) {
            handleException(e);
        }
        try {
            delegate.onMessage(message);
        }
        finally {
            try {
                targetSource.releaseTarget(delegate);
            }
            catch (Exception e) {
                handleException(e);
            }
        }
    }

    public void afterPropertiesSet() throws Exception {
        if (targetSource == null) {
            throw new IllegalArgumentException("targetSource must be set");
        }
    }

    public TargetSource getTargetSource() {
        return targetSource;
    }

    public void setTargetSource(TargetSource targetSource) {
        this.targetSource = targetSource;
    }

    protected void handleException(Exception e) {
        throw new RuntimeException(e);
    }

}
