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
    private String serialPortName;
    private String serialPortDriver;
    private int serialPortBaudRate;
    private String h2DbLocation;
    private String wwwFileLocation;
    private boolean isHttpsEnabled = false;
    private int httpPort;
    private String sslKeystoreFile;
    private String sslKeystorePassword;
    private String sslKeystoreType;
    private String bindAddress;
    private String gatewayType;
    private String ethernetGatewayHost;
    private Integer ethernetGatewayPort;
    private Integer ethernetGatewayKeepAliveFrequency;
    private String mqttGatewayBrokerHost;
    private Integer mqttGatewayBrokerPort;
    private String mqttGatewayBrokerRootTopic;

    public enum SERIAL_PORT_DRIVER {
        AUTO,
        PI4J,
        JSSC,
        JSERIALCOMM;
    }

    public enum GATEWAY_TYPES {
        SERIAL,
        ETHERNET,
        MQTT;
    }

    public enum MYSENSORS_CONFIG {
        METRIC,
        IMPERIAL
    }

    public AppProperties() {
    }

    public AppProperties(Properties properties) {
        this.loadProperties(properties);
    }

    public void loadProperties(Properties properties) {
        this.gatewayType = getValue(properties, "mcc.ethernet.gateway.type");
        this.serialPortName = getValue(properties, "mcc.serialport.name");
        this.serialPortDriver = getValue(properties, "mcc.serialport.driver.type");
        this.serialPortBaudRate = Integer.valueOf(getValue(properties, "mcc.serialport.baud.rate"));
        this.ethernetGatewayHost = getValue(properties, "mcc.ethernet.gateway.host");
        this.ethernetGatewayPort = Integer.valueOf(getValue(properties, "mcc.ethernet.gateway.port"));
        this.ethernetGatewayKeepAliveFrequency = Integer.valueOf(getValue(properties,
                "mcc.ethernet.gateway.keep.alive.frequency"));
        this.mqttGatewayBrokerHost = getValue(properties, "mcc.mqtt.gateway.broker.host");
        this.mqttGatewayBrokerPort = Integer.valueOf(getValue(properties, "mcc.mqtt.gateway.broker.port"));
        this.mqttGatewayBrokerRootTopic = getValue(properties, "mcc.mqtt.gateway.broker.root.topic");
        this.h2DbLocation = getValue(properties, "mcc.h2db.location");
        this.wwwFileLocation = getValue(properties, "www.file.location");
        this.httpPort = Integer.valueOf(getValue(properties, "http.port"));
        if (getValue(properties, "enable.https") != null) {
            if (Boolean.valueOf(getValue(properties, "enable.https"))) {
                this.isHttpsEnabled = true;
                this.sslKeystoreFile = getValue(properties, "ssl.keystore.file");
                this.sslKeystorePassword = getValue(properties, "ssl.keystore.password");
                this.sslKeystoreType = getValue(properties, "ssl.keystore.type");
            }
        }
        this.bindAddress = getValue(properties, "bind.address");

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

    public String getSerialPortName() {
        return serialPortName;
    }

    public void setSerialPortName(String serialPortName) {
        this.serialPortName = serialPortName;
    }

    public String getSerialPortDriver() {
        return serialPortDriver;
    }

    public void setSerialPortDriver(String serialPortDriver) {
        this.serialPortDriver = serialPortDriver;
    }

    public int getSerialPortBaudRate() {
        return serialPortBaudRate;
    }

    public void setSerialPortBaudRate(int serialPortBaudRate) {
        this.serialPortBaudRate = serialPortBaudRate;
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

    public String getH2DbLocation() {
        return h2DbLocation;
    }

    public String getWwwFileLocation() {
        return wwwFileLocation;
    }

    public boolean isHttpsEnabled() {
        return isHttpsEnabled;
    }

    public int getHttpPort() {
        return httpPort;
    }

    public String getSslKeystoreFile() {
        return sslKeystoreFile;
    }

    public String getSslKeystorePassword() {
        return sslKeystorePassword;
    }

    public String getSslKeystoreType() {
        return sslKeystoreType;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public String getBindAddress() {
        return bindAddress;
    }

    public String getEthernetGatewayHost() {
        return ethernetGatewayHost;
    }

    public String getGatewayType() {
        return gatewayType;
    }

    public Integer getEthernetGatewayPort() {
        return ethernetGatewayPort;
    }

    public Integer getEthernetGatewayKeepAliveFrequency() {
        return ethernetGatewayKeepAliveFrequency;
    }

    public String getMqttGatewayBrokerHost() {
        return mqttGatewayBrokerHost;
    }

    public Integer getMqttGatewayBrokerPort() {
        return mqttGatewayBrokerPort;
    }

    public String getMqttGatewayBrokerRootTopic() {
        return mqttGatewayBrokerRootTopic;
    }
}
