/**
 * Copyright (C) 2015 Jeeva Kandasamy (jkandasa@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mycontroller.standalone.mqttbroker;

import java.io.IOException;

import org.eclipse.moquette.server.Server;
import org.mycontroller.standalone.ObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
public class MoquetteMqttBroker {
    private static final Logger _logger = LoggerFactory.getLogger(MoquetteMqttBroker.class.getName());
    private static boolean isRunning = false;
    private static Server mqttServer = null;

    private MoquetteMqttBroker() {

    }

    public static synchronized void start() {
        if (!ObjectFactory.getAppProperties().isMqttBrokerEnabled()) {
            _logger.debug("InBuilt MQTT broker is not enabled... Skipping to start...");
            return;
        }
        if (isRunning) {
            _logger.info("MQTT Broker already running, nothing to do...");
            return;
        }
        try {
            mqttServer = new Server();
            mqttServer.startServer(new BrokerConfiguration());
            isRunning = true;
            _logger.debug("MQTT Broker started successfully");
        } catch (IOException ex) {
            _logger.error("Unable to start MQTT Broker, Exception, ", ex);
        }
    }

    public static synchronized void stop() {
        if (!ObjectFactory.getAppProperties().isMqttBrokerEnabled()) {
            _logger.debug("InBuilt MQTT broker is not enabled... Skipping to stop...");
            return;
        }
        if (!isRunning) {
            _logger.info("MQTT Broker is not running, nothing to do...");
            return;
        }
        mqttServer.stopServer();
        mqttServer = null;
        isRunning = false;
        _logger.debug("MQTT Broker has been stopped successfully");
    }

}
