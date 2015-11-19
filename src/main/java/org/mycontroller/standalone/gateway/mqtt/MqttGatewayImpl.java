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
package org.mycontroller.standalone.gateway.mqtt;

import java.util.HashMap;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.ObjectFactory;
import org.mycontroller.standalone.api.jaxrs.mapper.GatewayInfo;
import org.mycontroller.standalone.gateway.IMySensorsGateway;
import org.mycontroller.standalone.gateway.MySensorsGatewayException;
import org.mycontroller.standalone.mysensors.RawMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
public class MqttGatewayImpl implements IMySensorsGateway {
    private static final Logger _logger = LoggerFactory.getLogger(MqttGatewayImpl.class.getName());

    public static final long TIME_TO_WAIT = 100;
    public static final long DISCONNECT_TIME_OUT = 1000 * 1;
    public static final int CONNECTION_TIME_OUT = 1000 * 5;
    public static final int KEEP_ALIVE = 1000 * 5;
    public static final int MY_SENSORS_QOS = 0;
    private GatewayInfo gatewayInfo = new GatewayInfo();

    private IMqttClient mqttClient;
    private MqttCallbackListener mqttCallbackListener;

    public MqttGatewayImpl() {
        try {
            //Update Gateway Info
            gatewayInfo.setType(ObjectFactory.getAppProperties().getGatewayType());
            gatewayInfo.setData(new HashMap<String, Object>());

            gatewayInfo.getData().put(MqttGatewayCommon.BROKER_HOST, ObjectFactory.getAppProperties().getgatewayMqttBrokerHost());
            gatewayInfo.getData().put(MqttGatewayCommon.CLIENT_ID,
                    ObjectFactory.getAppProperties().getGatewayMqttClientId());
            gatewayInfo.getData().put(MqttGatewayCommon.USER, ObjectFactory.getAppProperties().getGatewayMqttUser());
            gatewayInfo.getData().put(MqttGatewayCommon.TOPIC_SUBSCRIBE,
                    ObjectFactory.getAppProperties().getGatewayMqttTopicSubscribe());
            gatewayInfo.getData().put(MqttGatewayCommon.TOPIC_PUBLISH,
                    ObjectFactory.getAppProperties().getGatewayMqttTopicPublish());

            mqttClient = new MqttClient(ObjectFactory.getAppProperties().getgatewayMqttBrokerHost(), ObjectFactory
                    .getAppProperties().getGatewayMqttClientId());
            MqttConnectOptions connectOptions = new MqttConnectOptions();
            connectOptions.setConnectionTimeout(CONNECTION_TIME_OUT);
            connectOptions.setKeepAliveInterval(KEEP_ALIVE);
            if (ObjectFactory.getAppProperties().getGatewayMqttUser() != null
                    && ObjectFactory.getAppProperties().getGatewayMqttUser().length() > 0) {
                connectOptions.setUserName(ObjectFactory.getAppProperties().getGatewayMqttUser());
                connectOptions.setPassword(ObjectFactory.getAppProperties().getGatewayMqttPassword().toCharArray());
            }
            mqttClient.connect(connectOptions);
            mqttCallbackListener = new MqttCallbackListener(mqttClient);
            mqttClient.setCallback(mqttCallbackListener);
            mqttClient.subscribe(ObjectFactory.getAppProperties().getGatewayMqttTopicSubscribe() + "/#");
            _logger.info("MQTT Gateway[{}] connected successfully..", mqttClient.getServerURI());
            gatewayInfo.getData().put(MqttGatewayCommon.CONNECTION_STATUS, "Connected Successfully");
        } catch (MqttException ex) {
            gatewayInfo.getData().put(MqttGatewayCommon.CONNECTION_STATUS, "ERROR: " + ex.getMessage());
            _logger.error(
                    "Unable to connect with MQTT broker gateway[{}], Reason Code: {}, Reboot '{}' service once MQTT Broker gateway comes UP!",
                    mqttClient.getServerURI(), ex.getReasonCode(), AppProperties.APPLICATION_NAME, ex);
        }
    }

    @Override
    public synchronized void write(RawMessage rawMessage) throws MySensorsGatewayException {
        _logger.debug("Message to send, Topic:[{}], PayLoad:[{}]", rawMessage.getMqttTopic(), rawMessage.getPayload());
        try {
            MqttMessage message = new MqttMessage(rawMessage.getPayloadBytes());
            message.setQos(MY_SENSORS_QOS);
            mqttClient.publish(rawMessage.getMqttTopic(), message);
        } catch (MqttException ex) {
            if (ex.getMessage().contains("Timed out waiting for a response from the server")) {
                _logger.debug(ex.getMessage());
            } else {
                _logger.error("Exception, Reason Code:{}", ex.getReasonCode(), ex);
                throw new MySensorsGatewayException(IMySensorsGateway.GATEWAY_STATUS.GATEWAY_ERROR + ": Reason Code: "
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
    public GatewayInfo getGatewayInfo() {
        return gatewayInfo;
    }

}
