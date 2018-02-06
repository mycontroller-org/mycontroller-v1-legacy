/*
 * Copyright 2015-2018 Jeeva Kandasamy (jkandasa@gmail.com)
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
package org.mycontroller.standalone.provider.mycontroller;

import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.gateway.config.GatewayConfig;
import org.mycontroller.standalone.gateway.mqtt.GatewayMQTT;
import org.mycontroller.standalone.provider.EngineAbstract;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.2.0
 */
@Slf4j
public class MyControllerEngine extends EngineAbstract {

    public MyControllerEngine(GatewayConfig _config) {
        super(_config);
        switch (_config.getType()) {
            case MQTT:
                _gateway = new GatewayMQTT(_config.getGatewayTable(), new MessageParserMyController(), _queue);
                break;
            default:
                _logger.warn("not implemented! {}", _config);
                return;
        }
        _executor = new MyControllerExecutor(_queue, _queueSleep);
    }

    @Override
    public boolean validate(Sensor sensor) {
        if (sensor.getSensorId().contains(" ")) {
            throw new RuntimeException("Sensor Id should not contain any space");
        }
        return true;
    }

    @Override
    public boolean validate(Node node) {
        if (node.getEui().contains(" ")) {
            throw new RuntimeException("Node EUI should not contain any space");
        } else if (node.getEui().length() > MyController.MAX_NODE_EUI_LENGTH) {
            throw new RuntimeException("Node EUI string length should not go more than 12 char");
        }
        return true;
    }

}
