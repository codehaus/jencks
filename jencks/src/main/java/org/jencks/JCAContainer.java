/**
 * 
 * Copyright 2005 LogicBlaze, Inc. http://www.logicblaze.com
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
import org.jencks.factory.BootstrapContextFactoryBean;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ResourceAdapter;

/**
 * Represents a base JCA container which has no dependency on Geronimo
 * and requires a mandatory {@link BootstrapContext} and {@link ResourceAdapter}
 * properties to be configured.
 * <p/>
 * Typically Spring users will use the {@link BootstrapContextFactoryBean} to create
 * the {@link BootstrapContext} instance, with the work manager and transaction manager.
 *
 * @version $Revision$
 */
public class JCAContainer implements InitializingBean, DisposableBean, BeanFactoryAware {
    private static final transient Log log = LogFactory.getLog(JCAContainer.class);
    private BootstrapContext bootstrapContext;
    private ResourceAdapter resourceAdapter;
    private BeanFactory beanFactory;
    private boolean lazyLoad = false;

    public JCAConnector addConnector() {
        return new JCAConnector(getBootstrapContext(), getResourceAdapter());
    }

    public void afterPropertiesSet() throws Exception {
        if (resourceAdapter == null) {
            throw new IllegalArgumentException("resourceAdapter must be set");
        }
        if (bootstrapContext == null) {
            if (bootstrapContext == null) {
                throw new IllegalArgumentException("bootstrapContext must be set");
            }
        }
        resourceAdapter.start(bootstrapContext);

        // now lets start all of the JCAConnector instances
        if (beanFactory == null) {
            throw new IllegalArgumentException("beanFactory should have been set by Spring");
        }
        else if (!lazyLoad && beanFactory instanceof BeanDefinitionRegistry) {
            BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
            String[] names = registry.getBeanDefinitionNames();
            for (int i = 0; i < names.length; i++) {
                String name = names[i];
                BeanDefinition definition = registry.getBeanDefinition(name);
                if (!definition.isAbstract()) {
                    beanFactory.getBean(name);
                }
            }
        }

        String version = null;
        Package aPackage = Package.getPackage("org.jencks");
        if (aPackage != null) {
            version = aPackage.getImplementationVersion();
        }

        log.info("Jencks JCA Container (http://jencks.org/) has started running version: " + version);
    }

    public void destroy() throws Exception {
        if (resourceAdapter != null) {
            resourceAdapter.stop();
        }
    }

    // Properties
    //-------------------------------------------------------------------------
    public BeanFactory getBeanFactory() {
        return beanFactory;
    }

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    public ResourceAdapter getResourceAdapter() {
        return resourceAdapter;
    }

    public void setResourceAdapter(ResourceAdapter resourceAdapter) {
        this.resourceAdapter = resourceAdapter;
    }

    public BootstrapContext getBootstrapContext() {
        return bootstrapContext;
    }

    public void setBootstrapContext(BootstrapContext bootstrapContext) {
        this.bootstrapContext = bootstrapContext;
    }

    public boolean isLazyLoad() {
        return lazyLoad;
    }

    public void setLazyLoad(boolean lazyLoad) {
        this.lazyLoad = lazyLoad;
    }

}
