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
package org.mycontroller.standalone.externalserver.driver;

import org.apache.commons.lang.RandomStringUtils;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;
import org.mycontroller.restclient.core.TRUST_HOST_TYPE;
import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.externalserver.config.ExternalServerConfigMqtt;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.2.0
 */
@Slf4j
public class DriverMQTT extends DriverAbstract {
    private ExternalServerConfigMqtt _config = null;
    private ExternalMqttClient _client = null;

    public DriverMQTT(ExternalServerConfigMqtt _config) {
        super(_config);
        this._config = _config;
    }

    @Override
    public synchronized void write(SensorVariable sensorVariable) {
        if (_client != null) {
            if (_client.isConnected() == null) {
                return;
            } else if (!_client.isConnected()) {
                _logger.warn("MQTT client not connected! reconnecting...");
                _client.reConnect();
            }
            _client.publish(getVariableKey(sensorVariable, _config.getKeyFormat()), sensorVariable.getValue());
        }
    }

    @Override
    public void connect() {
        _client = new ExternalMqttClient(
                _config.getUrl(),
                _config.getName(),
                _config.getUsername(),
                _config.getPassword(),
                _config.getTrustHostType());
    }

    @Override
    public void disconnect() {
        if (_client != null && _client.isConnected()) {
            _client.disconnect();
        }

    }

}

@Slf4j
class ExternalMqttClient {
    public static final long TIME_TO_WAIT = 100;
    public static final long DISCONNECT_TIME_OUT = 1000 * 1;
    public static final int CONNECTION_TIME_OUT = 1000 * 5;
    public static final int KEEP_ALIVE = 1000 * 5;
    public static final int QOS = 0;

    private IMqttClient mqttClient = null;
    private MqttConnectOptions connectOptions = new MqttConnectOptions();

    public ExternalMqttClient(String url, String clientId, String username, String password,
            TRUST_HOST_TYPE trustHostType) {
        try {
            MqttDefaultFilePersistence myPersistence = new MqttDefaultFilePersistence(AppProperties.getInstance()
                    .getMqttClientPersistentStoresLocation());
            mqttClient = new MqttClient(url, clientId + "_" + RandomStringUtils.randomAlphanumeric(5), myPersistence);
            connectOptions.setConnectionTimeout(CONNECTION_TIME_OUT);
            connectOptions.setKeepAliveInterval(KEEP_ALIVE);
            if (username != null && password.length() > 0) {
                connectOptions.setUserName(username);
                connectOptions.setPassword(password.toCharArray());
            }
            mqttClient.connect(connectOptions);
        } catch (Exception ex) {
            _logger.error("Exception, ", ex);
        }
    }

    public Boolean isConnected() {
        if (mqttClient == null) {
            _logger.error("This client was not initialized correctly!");
            return null;
        } else {
            return mqttClient.isConnected();
        }
    }

    public void reConnect() {
        if (isConnected() != null && !isConnected()) {
            try {
                mqttClient.connect(connectOptions);
            } catch (MqttException ex) {
                _logger.error("Exception,", ex);
            }
        }
    }

    public void publish(String topic, String value) {
        if (isConnected() == null) {
            return;
        }
        if (isConnected()) {
            try {
                MqttMessage message = new MqttMessage(value.getBytes());
                message.setQos(QOS);
                mqttClient.publish(topic, message);
            } catch (MqttException ex) {
                _logger.error("Unable to send MQTT message, ", ex);
            }
        } else {
            _logger.warn("This client is not connected with broker!");
        }
    }

    public void disconnect() {
        if (isConnected() != null && isConnected()) {
            try {
                mqttClient.disconnect();
            } catch (MqttException ex) {
                _logger.error("exception, ", ex);
            }
        }
    }
}
