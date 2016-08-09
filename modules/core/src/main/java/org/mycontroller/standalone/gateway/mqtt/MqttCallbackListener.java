/*
 * Copyright 2015-2016 Jeeva Kandasamy (jkandasa@gmail.com)
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
package org.mycontroller.standalone.gateway.mqtt;

import org.apache.commons.lang.StringUtils;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.mycontroller.standalone.AppProperties.STATE;
import org.mycontroller.standalone.gateway.model.GatewayMQTT;
import org.mycontroller.standalone.message.RawMessage;
import org.mycontroller.standalone.message.RawMessageQueue;
import org.mycontroller.standalone.utils.McUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
@Slf4j
public class MqttCallbackListener implements MqttCallback {
    private IMqttClient mqttClient;
    private GatewayMQTT gateway;
    private MqttConnectOptions connectOptions;
    private boolean reconnect = true;
    private boolean reconnectRunning = false;
    public static final long RECONNECT_WAIT_TIME = McUtils.SECOND * 5;

    public MqttCallbackListener(IMqttClient mqttClient, GatewayMQTT gateway, MqttConnectOptions connectOptions) {
        this.mqttClient = mqttClient;
        this.gateway = gateway;
        this.connectOptions = connectOptions;
    }

    @Override
    public void connectionLost(Throwable throwable) {
        _logger.error("MQTT Gateway[id:{}, Name:{}, serverURI:{}] connection lost! Error:{}",
                gateway.getId(), gateway.getName(), mqttClient.getServerURI(), throwable.getMessage());
        gateway.setStatus(STATE.DOWN, "ERROR: Connection lost! [" + throwable.getMessage() + "]");
        tryReconnect();
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken deliveryToken) {
        try {
            _logger.debug("Message Delivery Complete, [Message Id:{}, Topic:{}, Payload:{}]",
                    deliveryToken.getMessageId(),
                    StringUtils.join(deliveryToken.getTopics(), ","),
                    deliveryToken.getMessage());
        } catch (MqttException ex) {
            _logger.error("Exception, ", ex);
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        try {
            _logger.debug("Message Received, Topic:[{}], Payload:[{}]", topic, message);
            RawMessageQueue.getInstance().putMessage(RawMessage.builder()
                    .gatewayId(gateway.getId())
                    .data(message.toString())
                    .subData(topic)
                    .networkType(gateway.getNetworkType())
                    .build());
        } catch (Exception ex) {
            _logger.error("Exception, ", ex);
        }
    }

    public synchronized void stopReconnect() {
        this.reconnect = false;
        //Wait till reconnect loop completes
        long waitTime = McUtils.SECOND * 20;
        while (reconnectRunning && waitTime > 0) {
            try {
                Thread.sleep(50);
                //Do decrement wait time
                waitTime -= 50;
            } catch (InterruptedException ex) {
                _logger.error("Exception, ", ex);
            }
        }
    }

    private void tryReconnect() {
        if (reconnectRunning) {
            return;
        }
        reconnectRunning = true;
        while (reconnect) {
            _logger.debug("Trying to reconnect...");
            if (mqttClient.isConnected()) {
                break;
            } else {
                try {
                    mqttClient.connect(connectOptions);
                    _logger.info("MQTT Gateway[{}] Reconnected successfully...", mqttClient.getServerURI());
                    gateway.setStatus(STATE.UP, "Reconnected successfully...");
                    if (mqttClient.isConnected()) {
                        break;
                    }
                } catch (MqttException ex) {
                    _logger.debug("Exception, Reason Code:{}", ex.getReasonCode(), ex);
                    gateway.setStatus(STATE.DOWN, ex.getMessage());
                }
                long waitTime = RECONNECT_WAIT_TIME;
                while (waitTime > 0 && reconnect) {
                    try {
                        Thread.sleep(100);
                        //Do decrement wait time
                        waitTime -= 100;
                    } catch (InterruptedException ex) {
                        _logger.error("Exception, ", ex);
                    }
                }
            }
        }
        reconnectRunning = false;
    }
}
