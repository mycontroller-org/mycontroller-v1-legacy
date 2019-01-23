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
package org.mycontroller.standalone.gateway.mqtt;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;
import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.AppProperties.STATE;
import org.mycontroller.standalone.eventbus.McEventBus;
import org.mycontroller.standalone.eventbus.MessageStatus;
import org.mycontroller.standalone.exceptions.MessageParserException;
import org.mycontroller.standalone.gateway.GatewayUtils;
import org.mycontroller.standalone.gateway.config.GatewayConfigMQTT;
import org.mycontroller.standalone.message.IMessage;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_STATUS;
import org.mycontroller.standalone.offheap.IQueue;
import org.mycontroller.standalone.provider.IMessageParser;
import org.mycontroller.standalone.provider.MessageMQTT;
import org.mycontroller.standalone.utils.McUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.2.0
 */
@Slf4j
public class MQTTDriver {

    private static final Logger _RAW_MSG_LOGGER = LoggerFactory.getLogger(GatewayUtils.RAW_MESSAGE_LOGGER);

    private static final long DISCONNECT_TIME_OUT = 1000 * 1;
    private static final int CONNECTION_TIME_OUT = 1000 * 5;
    private static final int KEEP_ALIVE = 1000 * 5;

    private GatewayConfigMQTT _config = null;
    private IMqttClient _client;
    private MqttListener _listener;

    private IMessageParser<MessageMQTT> _parser;
    private IQueue<IMessage> _queue;

    private String gatewayReference;

    public MQTTDriver(GatewayConfigMQTT _config, IMessageParser<MessageMQTT> _parser, IQueue<IMessage> _queue) {
        this._config = _config;
        this._parser = _parser;
        this._queue = _queue;
        this.gatewayReference = GatewayUtils.gwLogReference(_config);
    }

    public void connect() {
        try {
            MqttDefaultFilePersistence myPersistence = new MqttDefaultFilePersistence(AppProperties.getInstance()
                    .getMqttClientPersistentStoresLocation());
            _client = new MqttClient(_config.getBrokerHost(), _config.getClientId() + "_"
                    + RandomStringUtils.randomAlphanumeric(5), myPersistence);
            MqttConnectOptions _connectOptions = new MqttConnectOptions();
            _connectOptions.setConnectionTimeout(CONNECTION_TIME_OUT);
            _connectOptions.setKeepAliveInterval(KEEP_ALIVE);
            if (_config.getUsername() != null && _config.getUsername().length() > 0) {
                _connectOptions.setUserName(_config.getUsername());
                _connectOptions.setPassword(_config.getPassword().toCharArray());
            }
            _client.connect(_connectOptions);
            _listener = new MqttListener(_client, _config, _connectOptions, _parser, _queue);
            _client.setCallback(_listener);
            _client.subscribe(GatewayUtils.getMqttTopics(_config.getTopicsSubscribe()));
            _logger.info("MQTT Gateway[name:{}, URI:{}, NetworkType:{}] connected successfully..", _config.getName(),
                    _client.getServerURI(), _config.getNetworkType().getText());
            _config.setStatus(STATE.UP, "Connected Successfully");
        } catch (MqttException ex) {
            _config.setStatus(STATE.DOWN, "ERROR: " + ex.getMessage()
                    + ", Reload this gateway when MQTT Broker comes UP");
            _logger.error("Unable to connect with MQTT broker _config[{}], Reason Code: {}, "
                    + "Reload _config [Id:{}, Name:{}, NetworkType:{}] service when MQTT Broker comes UP!",
                    _config.getBrokerHost(), ex.getReasonCode(), _config.getName(),
                    _config.getNetworkType().getText(), ex);
        } catch (Exception ex) {
            _config.setStatus(STATE.DOWN, "ERROR: " + ex.getMessage());
            _logger.error("Exception,", ex);
        }
    }

    public void disconnect() {
        try {
            if (_listener != null) {
                _listener.stopReconnect();
            }
            if (_client != null) {
                if (_client.isConnected()) {
                    _client.disconnect(DISCONNECT_TIME_OUT);
                }
                _client.close();
            }
            _config.setStatus(STATE.DOWN, "Stopped.");
        } catch (Exception ex) {
            _logger.error("Exception,", ex);
        }
    }

    public void write(IMessage message) throws MessageParserException {
        try {
            MessageMQTT mqttMessage = _parser.getGatewayData(message);
            MqttMessage rawMessage = new MqttMessage(mqttMessage.getPayload().getBytes());
            rawMessage.setQos(_config.getQos());
            String topicRoot = _config.getTopicsPublish().endsWith("/") ? _config.getTopicsPublish() : _config
                    .getTopicsPublish() + "/";

            // add raw message to a debug file
            if (_RAW_MSG_LOGGER.isDebugEnabled()) {
                MDC.put(GatewayUtils.RAW_MESSAGE_REFERENCE, gatewayReference);
                _RAW_MSG_LOGGER.debug("Tx: {}{} [{}]", topicRoot, mqttMessage.getTopic(), mqttMessage.getPayload());
            }

            _client.publish(topicRoot + mqttMessage.getTopic(), rawMessage);
            _logger.debug("published on:{}{}, {}", topicRoot, mqttMessage.getTopic(), message);
        } catch (MqttException ex) {
            if (ex.getMessage().contains("Timed out waiting for a response from the server")) {
                _logger.debug(ex.getMessage());
            } else {
                _logger.error("Exception, Reason Code:{}", ex.getReasonCode(), ex);
            }
        }
    }

}

@Slf4j
class MqttListener implements MqttCallback {

    private static final Logger _RAW_MSG_LOGGER = LoggerFactory.getLogger(GatewayUtils.RAW_MESSAGE_LOGGER);
    public static final long RECONNECT_WAIT_TIME = McUtils.SECOND * 5;

    private IMqttClient _client;
    private GatewayConfigMQTT _config;
    private IMessageParser<MessageMQTT> _parser;
    private IQueue<IMessage> _queue;
    private MqttConnectOptions _connectOptions;
    private boolean reconnect = true;
    private boolean reconnectRunning = false;
    private String gatewayReference;

    public MqttListener(IMqttClient _client, GatewayConfigMQTT _config, MqttConnectOptions _connectOptions,
            IMessageParser<MessageMQTT> _parser, IQueue<IMessage> _queue) {
        this._client = _client;
        this._config = _config;
        this._connectOptions = _connectOptions;
        this._parser = _parser;
        this._queue = _queue;
        this.gatewayReference = GatewayUtils.gwLogReference(_config);
    }

    @Override
    public void connectionLost(Throwable throwable) {
        _logger.error("MQTT Gateway[id:{}, Name:{}, serverURI:{}] connection lost! Error:{}",
                _config.getId(), _config.getName(), _client.getServerURI(), throwable.getMessage());
        _config.setStatus(STATE.DOWN, "ERROR: Connection lost! [" + throwable.getMessage() + "]");
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
    public void messageArrived(String topic, MqttMessage rawMessage) {
        try {
            if (rawMessage.isDuplicate()) {
                _logger.warn("Duplicate message received!! {}", rawMessage);
            }

            // add raw message to a debug file
            if (_RAW_MSG_LOGGER.isDebugEnabled()) {
                MDC.put(GatewayUtils.RAW_MESSAGE_REFERENCE, gatewayReference);
                _RAW_MSG_LOGGER.debug("Rx: {} [{}]", topic, new String(rawMessage.getPayload()));
            }

            _logger.debug("Message Received, Topic:[{}], Payload:[{}]", topic, new String(rawMessage.getPayload()));
            IMessage message = _parser.getMessage(_config, MessageMQTT.builder()
                    .gatewayId(_config.getId())
                    .topic(topic)
                    .payload(new String(rawMessage.getPayload()))
                    .build());
            if (message != null) {
                if (message.getAck() == IMessage.ACK_RESPONSE) {
                    McEventBus.getInstance().publish(
                            message.getEventTopic(),
                            MessageStatus.builder()
                                    .status(MESSAGE_STATUS.ACK_RECEIVED).message("Ack received")
                                    .build());
                    _logger.debug("Ack received: {}", message);
                } else {
                    _queue.add(message);
                }
            }
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
            if (_client.isConnected()) {
                break;
            } else {
                try {
                    _client.connect(_connectOptions);
                    _client.subscribe(GatewayUtils.getMqttTopics(_config.getTopicsSubscribe()));
                    _logger.info("MQTT Gateway[{}] Reconnected successfully...", _client.getServerURI());
                    _config.setStatus(STATE.UP, "Reconnected successfully...");
                    if (_client.isConnected()) {
                        break;
                    }
                } catch (MqttException ex) {
                    _logger.debug("Exception, Reason Code:{}", ex.getReasonCode(), ex);
                    _config.setStatus(STATE.DOWN, ex.getMessage());
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
