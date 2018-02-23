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
package org.mycontroller.standalone.provider.mysensors;

import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.gateway.config.GatewayConfig;
import org.mycontroller.standalone.gateway.config.GatewayConfigEthernet;
import org.mycontroller.standalone.gateway.ethernet.GatewayEthernet;
import org.mycontroller.standalone.gateway.mqtt.GatewayMQTT;
import org.mycontroller.standalone.gateway.serial.GatewaySerial;
import org.mycontroller.standalone.message.IMessage;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_INTERNAL;
import org.mycontroller.standalone.provider.EngineAbstract;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.2.0
 */
@Slf4j
public class MySensorsEngine extends EngineAbstract {
    private GatewayConfig _config;
    private boolean isEthernetDriver = false;
    private long lastAliveCheck = 0;
    private long aliveCheckInterval;

    public MySensorsEngine(GatewayConfig _config) {
        super(_config);
        this._config = _config;
        switch (_config.getType()) {
            case ETHERNET:
                _gateway = new GatewayEthernet(_config.getGatewayTable(), new MessageParserEthernet(), _queue);
                isEthernetDriver = true;
                // interval in seconds, multiple with 1000L to make it in milliseconds
                aliveCheckInterval = ((GatewayConfigEthernet) _config).getAliveFrequency() * 1000L;
                break;
            case MQTT:
                _gateway = new GatewayMQTT(_config.getGatewayTable(), new MessageParserMQTT(), _queue);
                break;
            case SERIAL:
                _gateway = new GatewaySerial(_config.getGatewayTable(), new MessageParserSerial(), _queue);
                break;
            default:
                _logger.warn("not implemented! {}", _config);
                return;
        }
        _executor = new MySensorsExecutor(_queue, _queueSleep, _gateway);
    }

    @Override
    public boolean validate(Sensor sensor) {
        if (Integer.valueOf(sensor.getSensorId()) <= MySensors.SENSOR_ID_MAX
                && Integer.valueOf(sensor.getSensorId()) >= MySensors.SENSOR_ID_MIN) {
            return true;
        } else {
            throw new RuntimeException("Sensor Id should be in the range of 0~254");
        }
    }

    @Override
    public boolean validate(Node node) {
        if (Integer.valueOf(node.getEui()) <= MySensors.NODE_ID_MAX && Integer.valueOf(node.getEui()) >= 0) {
            return true;
        } else {
            throw new RuntimeException("Node Id should be in the range of 0~254");
        }
    }

    @Override
    public void routineTasks() {
        if (isEthernetDriver) {
            if (aliveCheckInterval <= (System.currentTimeMillis() - lastAliveCheck)) {
                lastAliveCheck = System.currentTimeMillis();
                doAliveTest();
            }
        }
    }

    private void doAliveTest() {
        IMessage _message = IMessage.builder()
                .gatewayId(_config.getId())
                .nodeEui(String.valueOf(MySensors.GATEWAY_ID))
                .sensorId(IMessage.SENSOR_BROADCAST_ID)
                .type(MESSAGE_TYPE.C_INTERNAL.getText())
                .ack(IMessage.NO_ACK)
                .subType(MESSAGE_TYPE_INTERNAL.I_VERSION.getText())
                .payload(IMessage.PAYLOAD_EMPTY)
                .isTxMessage(true)
                .build();
        _queue.add(_message);
    }
}
