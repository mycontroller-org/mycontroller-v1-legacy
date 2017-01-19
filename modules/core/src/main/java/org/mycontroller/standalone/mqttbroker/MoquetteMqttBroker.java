/*
 * Copyright 2015-2017 Jeeva Kandasamy (jkandasa@gmail.com)
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mycontroller.standalone.mqttbroker;

import java.io.IOException;

import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.mdns.McmDNSFactory;

import io.moquette.server.Server;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MoquetteMqttBroker {
    private static boolean isRunning = false;
    private static Server mqttServer = null;

    public static synchronized void start() {
        if (!AppProperties.getInstance().getMqttBrokerSettings().getEnabled()) {
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
            _logger.info("MQTT Broker started successfully. {}", AppProperties.getInstance().getMqttBrokerSettings());
            if (AppProperties.getInstance().isMDNSserviceEnabled()) {
                McmDNSFactory.updateMqttService();
            }
        } catch (IOException ex) {
            _logger.error("Unable to start MQTT Broker, Exception, ", ex);
        }
    }

    public static synchronized void stop() {
        if (!isRunning) {
            _logger.debug("MQTT Broker is not running, nothing to do...");
            return;
        }
        if (mqttServer != null) {
            mqttServer.stopServer();
            mqttServer = null;
            isRunning = false;
            _logger.info("MQTT Broker has been stopped successfully");
            if (AppProperties.getInstance().isMDNSserviceEnabled()) {
                McmDNSFactory.updateMqttService();
            }
        }
    }

    public static synchronized void restart() {
        _logger.info("MQTT broker restart triggered...");
        stop();
        start();
    }

}
