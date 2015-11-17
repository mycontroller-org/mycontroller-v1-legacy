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
package org.mycontroller.standalone;

import java.util.Properties;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class AppProperties {
    private static final Logger _logger = LoggerFactory.getLogger(AppProperties.class.getName());

    public static final String APPLICATION_NAME = "MyController.org";
    public static final String EMAIL_TEMPLATE_ALARM = "../conf/templates/emailTemplateAlarm.html";

    private String gatewayType;

    private String gatewaySerialPortName;
    private String gatewaySerialPortDriver;
    private Integer gatewaySerialPortBaudRate;
    private Integer gatewaySerialPortRetryFrequency;

    private String gatewayEthernetHost;
    private Integer gatewayEthernetPort;
    private Integer gatewayEthernetKeepAliveFrequency;

    private String gatewayMqttBrokerHost;
    private String gatewayMqttClientId;
    private String gatewayMqttUser;
    private String gatewayMqttPassword;
    private String gatewayMqttTopicSubscribe;
    private String gatewayMqttTopicPublish;

    private String dbH2DbLocation;
    private String webFileLocation;
    private boolean isWebHttpsEnabled = false;
    private int webHttpPort;
    private String webSslKeystoreFile;
    private String webSslKeystorePassword;
    private String webSslKeystoreType;
    private String webBindAddress;

    private boolean mqttBrokerEnable;
    private String mqttBrokerBindAddress;
    private Integer mqttBrokerPort;

    private MC_LANGUAGE mcLanguage;

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public enum SERIAL_PORT_DRIVER {
        AUTO, PI4J, JSSC, JSERIALCOMM;
    }

    public enum GATEWAY_TYPES {
        SERIAL, ETHERNET, MQTT;
    }

    public enum MYSENSORS_CONFIG {
        METRIC, IMPERIAL
    }

    public enum MC_LANGUAGE {
        EN_US("English (US)"), TA_IN("தமிழ் (IN)"), DE_DE("Deutsch (DE)"), RU_RU("Russian (RU)");

        private final String name;

        private MC_LANGUAGE(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        public static MC_LANGUAGE get(int id) {
            for (MC_LANGUAGE type : values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

    }

    public AppProperties() {
    }

    public AppProperties(Properties properties) {
        this.loadProperties(properties);
    }

    public void loadProperties(Properties properties) {
        this.gatewayType = getValue(properties, "mcc.gateway.type");

        if (this.gatewayType.equalsIgnoreCase(GATEWAY_TYPES.SERIAL.toString())) {
            this.gatewaySerialPortName = getValue(properties, "mcc.gateway.serialport.name");
            this.gatewaySerialPortDriver = getValue(properties, "mcc.gateway.serialport.driver.type");
            this.gatewaySerialPortBaudRate = Integer.valueOf(getValue(properties, "mcc.gateway.serialport.baud.rate"));
            this.gatewaySerialPortRetryFrequency = Integer
                    .valueOf(getValue(properties, "mcc.gateway.serialport.retry.frequency"));

        } else if (this.gatewayType.equalsIgnoreCase(GATEWAY_TYPES.ETHERNET.toString())) {
            this.gatewayEthernetHost = getValue(properties, "mcc.gateway.ethernet.host");
            this.gatewayEthernetPort = Integer.valueOf(getValue(properties, "mcc.gateway.ethernet.port"));
            this.gatewayEthernetKeepAliveFrequency = Integer
                    .valueOf(getValue(properties, "mcc.gateway.ethernet.keep.alive.frequency"));

        } else if (this.gatewayType.equalsIgnoreCase(GATEWAY_TYPES.MQTT.toString())) {
            this.gatewayMqttBrokerHost = getValue(properties, "mcc.gateway.mqtt.broker.host");
            this.gatewayMqttClientId = getValue(properties, "mcc.gateway.mqtt.client.id");
            this.gatewayMqttUser = getValue(properties, "mcc.gateway.mqtt.user");
            this.gatewayMqttPassword = getValue(properties, "mcc.gateway.mqtt.password");
            this.gatewayMqttTopicSubscribe = getValue(properties, "mcc.gateway.mqtt.topic.subscribe");
            this.gatewayMqttTopicPublish = getValue(properties, "mcc.gateway.mqtt.topic.publish");

        } else {
            throw new RuntimeException("Unknown gateway type defined! Type:" + this.gatewayType);
        }

        this.dbH2DbLocation = getValue(properties, "mcc.db.h2db.location");
        this.webFileLocation = getValue(properties, "mcc.web.file.location");
        this.webHttpPort = Integer.valueOf(getValue(properties, "mcc.web.http.port"));
        if (getValue(properties, "mcc.web.enable.https") != null) {
            if (Boolean.valueOf(getValue(properties, "mcc.web.enable.https"))) {
                this.isWebHttpsEnabled = true;
                this.webSslKeystoreFile = getValue(properties, "mcc.web.ssl.keystore.file");
                this.webSslKeystorePassword = getValue(properties, "mcc.web.ssl.keystore.password");
                this.webSslKeystoreType = getValue(properties, "mcc.web.ssl.keystore.type");
            }
        }
        this.webBindAddress = getValue(properties, "mcc.web.bind.address");

        //MQTT Broker
        this.mqttBrokerEnable = Boolean.valueOf(getValue(properties, "mcc.mqtt.broker.enable"));
        this.mqttBrokerBindAddress = getValue(properties, "mcc.mqtt.broker.bind.address");
        this.mqttBrokerPort = Integer.valueOf(getValue(properties, "mcc.mqtt.broker.port"));
    }

    private String getValue(Properties properties, String key) {
        String value = properties.getProperty(key);
        _logger.debug("Key:{}-->{}", key, value);
        if (value != null) {
            return value.trim();
        } else {
            return null;
        }
    }

    public int getNodeId() {
        Settings settings = DaoUtils.getSettingsDao().get(Settings.AUTO_NODE_ID);
        return Integer.valueOf(settings.getValue());
    }

    public String getMetricType() {
        Settings settings = DaoUtils.getSettingsDao().get(Settings.MY_SENSORS_CONFIG);
        if (settings != null && settings.getValue() != null) {
            if (settings.getValue().equalsIgnoreCase(MYSENSORS_CONFIG.METRIC.toString())) {
                return "M";
            } else if (settings.getValue().equalsIgnoreCase(MYSENSORS_CONFIG.IMPERIAL.toString())) {
                return "I";
            }
        }
        return "M";
    }

    public MC_LANGUAGE getLanguage() {
        if (mcLanguage == null) {
            mcLanguage = MC_LANGUAGE
                    .get(Integer.valueOf(DaoUtils.getSettingsDao().get(Settings.MC_LANGUAGE).getValue()));
        }
        return mcLanguage;
    }

    public void updatePropertiesFromDb() {
        Settings settings = DaoUtils.getSettingsDao().get(Settings.MC_LANGUAGE);
        if (settings != null && settings.getValue() != null) {
            mcLanguage = MC_LANGUAGE.get(Integer.valueOf(settings.getValue()));
        }
    }

    public int getNextNodeId() throws NodeIdException {
        int nodeId = this.getNodeId();
        nodeId++;
        boolean isIdAvailable = false;
        int nodeIdRef = nodeId;
        for (; nodeId < 255; nodeId++) {
            if (DaoUtils.getNodeDao().get(nodeId) == null) {
                isIdAvailable = true;
                break;
            }
        }
        if (!isIdAvailable) {
            for (nodeId = 1; nodeId <= nodeIdRef; nodeIdRef++) {
                if (DaoUtils.getNodeDao().get(nodeId) == null) {
                    isIdAvailable = true;
                    break;
                }
            }
        }

        if (isIdAvailable) {
            Settings settings = DaoUtils.getSettingsDao().get(Settings.AUTO_NODE_ID);
            settings.setValue(String.valueOf(nodeId));
            DaoUtils.getSettingsDao().update(settings);
            return nodeId;
        } else {
            throw new NodeIdException("Reached Node Id 254, that is the maximum limit.");
        }
    }

    public static String getOsName() {
        return System.getProperties().getProperty("os.name");
    }

    public static String getOsArch() {
        return System.getProperties().getProperty("os.arch");
    }

    public static String getOsVersion() {
        return System.getProperties().getProperty("os.version");
    }

    public String getGatewayType() {
        return gatewayType;
    }

    public String getGatewaySerialPortName() {
        return gatewaySerialPortName;
    }

    public String getGatewaySerialPortDriver() {
        return gatewaySerialPortDriver;
    }

    public Integer getGatewaySerialPortBaudRate() {
        return gatewaySerialPortBaudRate;
    }

    public Integer getGatewaySerialPortRetryFrequency() {
        return gatewaySerialPortRetryFrequency;
    }

    public String getGatewayEthernetHost() {
        return gatewayEthernetHost;
    }

    public Integer getGatewayEthernetPort() {
        return gatewayEthernetPort;
    }

    public Integer getGatewayEthernetKeepAliveFrequency() {
        return gatewayEthernetKeepAliveFrequency;
    }

    public String getgatewayMqttBrokerHost() {
        return gatewayMqttBrokerHost;
    }

    public String getGatewayMqttClientId() {
        return gatewayMqttClientId;
    }

    public String getGatewayMqttUser() {
        return gatewayMqttUser;
    }

    public String getGatewayMqttPassword() {
        return gatewayMqttPassword;
    }

    public String getGatewayMqttTopicSubscribe() {
        return gatewayMqttTopicSubscribe;
    }

    public String getGatewayMqttTopicPublish() {
        return gatewayMqttTopicPublish;
    }

    public String getDbH2DbLocation() {
        return dbH2DbLocation;
    }

    public String getWebFileLocation() {
        return webFileLocation;
    }

    public boolean isWebHttpsEnabled() {
        return isWebHttpsEnabled;
    }

    public int getWebHttpPort() {
        return webHttpPort;
    }

    public String getWebSslKeystoreFile() {
        return webSslKeystoreFile;
    }

    public String getWebSslKeystorePassword() {
        return webSslKeystorePassword;
    }

    public String getWebSslKeystoreType() {
        return webSslKeystoreType;
    }

    public String getWebBindAddress() {
        return webBindAddress;
    }

    public boolean isMqttBrokerEnabled() {
        return mqttBrokerEnable;
    }

    public String getMqttBrokerBindAddress() {
        return mqttBrokerBindAddress;
    }

    public Integer getMqttBrokerPort() {
        return mqttBrokerPort;
    }
}
