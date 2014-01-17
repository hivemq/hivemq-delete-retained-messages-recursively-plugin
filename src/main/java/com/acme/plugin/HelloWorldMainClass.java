/*
 * Copyright 2013 dc-square GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.acme.plugin;

import com.acme.callbacks.ClientConnect;
import com.acme.callbacks.ClientDisconnect;
import com.acme.callbacks.HiveMQStart;
import com.acme.callbacks.PublishReceived;
import com.dcsquare.hivemq.spi.PluginEntryPoint;
import com.dcsquare.hivemq.spi.callback.registry.CallbackRegistry;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * This is the main class of the plugin, which is instanciated during the HiveMQ start up process.
 */
public class HelloWorldMainClass extends PluginEntryPoint {

    Logger log = LoggerFactory.getLogger(HelloWorldMainClass.class);

    private final Configuration configuration;

    private final PublishReceived publishReceived;



    /**
     * @param configuration Injected configuration, which is declared in the {@link HelloWorldPluginModule}.
     */
    @Inject
    public HelloWorldMainClass(Configuration configuration, PublishReceived publishReceived) {
        this.configuration = configuration;
        this.publishReceived = publishReceived;
    }

    /**
     * This method is executed after the instanciation of the whole class. It is used to initialize
     * the implemented callbacks and make them known to the HiveMQ core.
     */
    @PostConstruct
    public void postConstruct() {

        CallbackRegistry callbackRegistry = getCallbackRegistry();

        callbackRegistry.addCallback(new HiveMQStart());
        callbackRegistry.addCallback(new ClientConnect());
        callbackRegistry.addCallback(new ClientDisconnect());
        callbackRegistry.addCallback(publishReceived);

        log.info("Plugin configuration property: {}", configuration.getString("myProperty"));
    }
}
