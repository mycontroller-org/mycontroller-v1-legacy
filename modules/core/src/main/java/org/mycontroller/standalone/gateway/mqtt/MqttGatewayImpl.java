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

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.mycontroller.standalone.AppProperties.STATE;
import org.mycontroller.standalone.db.tables.GatewayTable;
import org.mycontroller.standalone.gateway.GatewayException;
import org.mycontroller.standalone.gateway.IGateway;
import org.mycontroller.standalone.gateway.model.GatewayMQTT;
import org.mycontroller.standalone.message.RawMessage;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
@Slf4j
public class MqttGatewayImpl implements IGateway {

    public static final long TIME_TO_WAIT = 100;
    public static final long DISCONNECT_TIME_OUT = 1000 * 1;
    public static final int CONNECTION_TIME_OUT = 1000 * 5;
    public static final int KEEP_ALIVE = 1000 * 5;
    public static final int MQTT_QOS = 0;
    private GatewayMQTT gateway = null;

    private IMqttClient mqttClient;
    private MqttCallbackListener mqttCallbackListener;

    public MqttGatewayImpl(GatewayTable gatewayTable) {
        try {
            this.gateway = new GatewayMQTT(gatewayTable);

            mqttClient = new MqttClient(this.gateway.getBrokerHost(), this.gateway.getClientId());
            MqttConnectOptions connectOptions = new MqttConnectOptions();
            connectOptions.setConnectionTimeout(CONNECTION_TIME_OUT);
            connectOptions.setKeepAliveInterval(KEEP_ALIVE);
            if (this.gateway.getUsername() != null && this.gateway.getUsername().length() > 0) {
                connectOptions.setUserName(this.gateway.getUsername());
                connectOptions.setPassword(this.gateway.getPassword().toCharArray());
            }
            mqttClient.connect(connectOptions);
            mqttCallbackListener = new MqttCallbackListener(mqttClient, this.gateway, connectOptions);
            mqttClient.setCallback(mqttCallbackListener);
            String[] topicsSubscribe = gateway.getTopicsSubscribe().split(GatewayMQTT.TOPICS_SPLITER);
            for (int topicId = 0; topicId < topicsSubscribe.length; topicId++) {
                topicsSubscribe[topicId] += "/#";
            }
            mqttClient.subscribe(topicsSubscribe);
            _logger.info("MQTT Gateway[{}] connected successfully..", mqttClient.getServerURI());
            this.gateway.setStatus(STATE.UP, "Connected Successfully");
        } catch (MqttException ex) {
            this.gateway.setStatus(STATE.DOWN, "ERROR: " + ex.getMessage()
                    + ", Reload this gateway when MQTT Broker comes UP");
            _logger.error("Unable to connect with MQTT broker gateway[{}], Reason Code: {}, "
                    + "Reload gateway [Id:{}, Name:{}] service when MQTT Broker comes UP!",
                    mqttClient.getServerURI(), ex.getReasonCode(), gateway.getName(), ex);
        }
    }

    @Override
    public synchronized void write(RawMessage rawMessage) throws GatewayException {
        _logger.debug("Message about to send, Topic:[{}], PayLoad:[{}]", rawMessage.getSubData(),
                rawMessage.getData());
        try {
            MqttMessage message = new MqttMessage(((String) rawMessage.getData()).getBytes());
            message.setQos(MQTT_QOS);
            String[] topicsPublish = rawMessage.getSubData().split(GatewayMQTT.TOPICS_SPLITER);
            for (String topic : topicsPublish) {
                mqttClient.publish(topic, message);
            }
        } catch (MqttException ex) {
            if (ex.getMessage().contains("Timed out waiting for a response from the server")) {
                _logger.debug(ex.getMessage());
            } else {
                _logger.error("Exception, Reason Code:{}", ex.getReasonCode(), ex);
                throw new GatewayException(IGateway.GATEWAY_STATUS.GATEWAY_ERROR + ": Reason Code: "
                        + ex.getReasonCode() + ", Error: "
                        + ex.getMessage());
            }
        }
    }

    @Override
    public void close() {
        try {
            if (mqttCallbackListener != null) {
                mqttCallbackListener.stopReconnect();
            }
            if (mqttClient != null) {
                if (mqttClient.isConnected()) {
                    mqttClient.disconnect(DISCONNECT_TIME_OUT);
                }
                mqttClient.close();
            }
        } catch (Exception ex) {
            _logger.error("Exception,", ex);
        }
    }

    @Override
    public GatewayMQTT getGateway() {
        return gateway;
    }

}
