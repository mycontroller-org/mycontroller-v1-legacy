/**
 * Copyright (C) 2015-2016 Jeeva Kandasamy (jkandasa@gmail.com)
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
package org.mycontroller.standalone.gateway.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.mycontroller.standalone.AppProperties.STATE;
import org.mycontroller.standalone.db.tables.Gateway;
import org.mycontroller.standalone.gateway.GatewayMQTT;
import org.mycontroller.standalone.gateway.IGateway;
import org.mycontroller.standalone.gateway.GatewayException;
import org.mycontroller.standalone.message.RawMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
public class MqttGatewayImpl implements IGateway {
    private static final Logger _logger = LoggerFactory.getLogger(MqttGatewayImpl.class.getName());

    public static final long TIME_TO_WAIT = 100;
    public static final long DISCONNECT_TIME_OUT = 1000 * 1;
    public static final int CONNECTION_TIME_OUT = 1000 * 5;
    public static final int KEEP_ALIVE = 1000 * 5;
    public static final int MY_SENSORS_QOS = 0;
    private GatewayMQTT gateway = null;

    private IMqttClient mqttClient;
    private MqttCallbackListener mqttCallbackListener;

    public MqttGatewayImpl(Gateway gateway) {
        try {
            this.gateway = new GatewayMQTT(gateway);

            mqttClient = new MqttClient(this.gateway.getBrokerHost(), this.gateway.getClientId());
            MqttConnectOptions connectOptions = new MqttConnectOptions();
            connectOptions.setConnectionTimeout(CONNECTION_TIME_OUT);
            connectOptions.setKeepAliveInterval(KEEP_ALIVE);
            if (this.gateway.getUserName() != null && this.gateway.getUserName().length() > 0) {
                connectOptions.setUserName(this.gateway.getUserName());
                connectOptions.setPassword(this.gateway.getPassword().toCharArray());
            }
            mqttClient.connect(connectOptions);
            mqttCallbackListener = new MqttCallbackListener(mqttClient, this.gateway);
            mqttClient.setCallback(mqttCallbackListener);
            mqttClient.subscribe(this.gateway.getTopicSubscribe() + "/#");
            _logger.info("MQTT Gateway[{}] connected successfully..", mqttClient.getServerURI());
            this.gateway.setStatus(STATE.UP, "Connected Successfully");
            this.gateway.updateGateway();
        } catch (MqttException ex) {
            this.gateway.setStatus(STATE.DOWN, "ERROR: " + ex.getMessage());
            this.gateway.updateGateway();
            _logger.error("Unable to connect with MQTT broker gateway[{}], Reason Code: {}, "
                    + "Reload gateway [Id:{}, Name:{}] service once MQTT Broker gateway comes UP!",
                    mqttClient.getServerURI(), ex.getReasonCode(), gateway.getName(), ex);
        }
    }

    @Override
    public synchronized void write(RawMessage rawMessage) throws GatewayException {
        _logger.debug("Message to send, Topic:[{}], PayLoad:[{}]", rawMessage.getSubData(),
                rawMessage.getData());
        try {
            MqttMessage message = new MqttMessage(rawMessage.getData().getBytes());
            message.setQos(MY_SENSORS_QOS);
            mqttClient.publish(rawMessage.getSubData(), message);
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
            mqttCallbackListener.setReconnect(false);
            mqttClient.disconnect(DISCONNECT_TIME_OUT);
            mqttClient.close();
        } catch (Exception ex) {
            _logger.error("Exception,", ex);
        }
    }

    @Override
    public GatewayMQTT getGateway() {
        return gateway;
    }

}
