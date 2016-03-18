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
package org.mycontroller.standalone.gateway;

import java.util.List;

import org.mycontroller.standalone.AppProperties.STATE;
import org.mycontroller.standalone.ObjectManager;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.PayloadOperation;
import org.mycontroller.standalone.db.tables.Gateway;
import org.mycontroller.standalone.gateway.ethernet.EthernetGatewayImpl;
import org.mycontroller.standalone.gateway.mqtt.MqttGatewayImpl;
import org.mycontroller.standalone.gateway.serialport.MYCSerialPort;
import org.mycontroller.standalone.model.ResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
public class GatewayUtils {
    private static final Logger _logger = LoggerFactory.getLogger(GatewayUtils.class.getName());

    public static final String OS_ARCH_ARM = "arm";

    private GatewayUtils() {

    }

    public enum TYPE {
        SERIAL("Serial"),
        ETHERNET("Ethernet"),
        MQTT("MQTT");
        public static TYPE get(int id) {
            for (TYPE type : values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

        private String value;

        private TYPE(String value) {
            this.value = value;
        }

        public String getText() {
            return this.value;
        }

        public static TYPE fromString(String text) {
            if (text != null) {
                for (TYPE type : TYPE.values()) {
                    if (text.equalsIgnoreCase(type.getText())) {
                        return type;
                    }
                }
            }
            return null;
        }
    }

    public enum SERIAL_PORT_DRIVER {
        AUTO("Auto"),
        PI4J("pi4j"),
        JSSC("jssc"),
        JSERIALCOMM("jSerialComm");
        private final String name;

        private SERIAL_PORT_DRIVER(String name) {
            this.name = name;
        }

        public String getText() {
            return this.name;
        }

        public static SERIAL_PORT_DRIVER get(int id) {
            for (SERIAL_PORT_DRIVER type : values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

        public static SERIAL_PORT_DRIVER fromString(String text) {
            if (text != null) {
                for (SERIAL_PORT_DRIVER type : SERIAL_PORT_DRIVER.values()) {
                    if (text.equalsIgnoreCase(type.getText())) {
                        return type;
                    }
                }
            }
            return null;
        }
    }

    public synchronized static void loadGateway(Gateway gateway) {
        if (!gateway.getEnabled()) {
            return;
        }
        IGateway iGateway = null;
        switch (gateway.getType()) {
            case SERIAL:
                iGateway = new MYCSerialPort(gateway);
                break;
            case ETHERNET:
                iGateway = new EthernetGatewayImpl(gateway);
                break;
            case MQTT:
                iGateway = new MqttGatewayImpl(gateway);
                break;
            default:
                _logger.warn("Not implemented yet! Gateway:[{}]", gateway.getType().getText());
        }
        if (iGateway == null) {
            throw new RuntimeException("Unable to create gateway[" + gateway + "]...Check your input");
        }
        ObjectManager.addGateway(iGateway);
    }

    public synchronized static void unloadGateway(Integer gatewayId) {
        if (ObjectManager.getGateway(gatewayId) != null) {
            ObjectManager.getGateway(gatewayId).close();
            ObjectManager.removeGateway(gatewayId);
        }
    }

    public synchronized static void loadAllGateways() {
        List<Gateway> gateways = DaoUtils.getGatewayDao().getAllEnabled();
        for (Gateway gateway : gateways) {
            loadGateway(gateway);
        }
    }

    public synchronized static void unloadAllGateways() {
        for (Integer gatewayId : ObjectManager.getGatewayIds()) {
            unloadGateway(gatewayId);
        }
    }

    public synchronized static void reloadGateways() {
        unloadAllGateways();
        loadAllGateways();
    }

    public static void reloadGateway(Integer gatewayId) {
        unloadGateway(gatewayId);
        Gateway gateway = DaoUtils.getGatewayDao().getById(gatewayId);
        loadGateway(gateway);
    }

    public static void reloadGateways(List<Integer> ids) {
        for (Integer id : ids) {
            reloadGateway(id);
        }
    }

    public static void updateGateway(Gateway gateway) {
        unloadGateway(gateway.getId());
        DaoUtils.getGatewayDao().update(gateway);
        if (gateway.getEnabled()) {
            loadGateway(gateway);
        }
    }

    public static void addGateway(Gateway gateway) {
        DaoUtils.getGatewayDao().create(gateway);
        gateway.setTimestamp(System.currentTimeMillis());
        if (gateway.getEnabled()) {
            gateway.setEnabled(true);
            loadGateway(gateway);
        }
    }

    public static void enableGateway(Integer gatewayId) {
        unloadGateway(gatewayId);
        Gateway gateway = DaoUtils.getGatewayDao().getById(gatewayId);
        gateway.setEnabled(true);
        loadGateway(gateway);
        DaoUtils.getGatewayDao().update(gateway);
    }

    public static void disableGateway(Integer gatewayId) {
        unloadGateway(gatewayId);
        Gateway gateway = DaoUtils.getGatewayDao().getById(gatewayId);
        gateway.setEnabled(false);
        gateway.setState(STATE.UNAVAILABLE);
        gateway.setStatusMessage("Disabled by user");
        DaoUtils.getGatewayDao().update(gateway);
    }

    public static void enableGateways(List<Integer> ids) {
        for (Integer id : ids) {
            enableGateway(id);
        }
    }

    public static void disableGateways(List<Integer> ids) {
        for (Integer id : ids) {
            disableGateway(id);
        }
    }

    public static String getConnectionDetails(Gateway gateway) {
        if (gateway.getType() == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        switch (gateway.getType()) {
            case SERIAL:
                GatewaySerial gatewaySerial = new GatewaySerial(gateway);
                builder.append("Port:").append(gatewaySerial.getPortName());
                builder.append(", BaudRate:").append(gatewaySerial.getBaudRate());
                builder.append(", Driver:").append(gatewaySerial.getDriver().getText());
                if (gatewaySerial.getDriver() == SERIAL_PORT_DRIVER.AUTO && gatewaySerial.getRunningDriver() != null) {
                    builder.append("[").append(gatewaySerial.getRunningDriver().getText()).append("]");
                }
                builder.append(", RetryFrequency:").append(gatewaySerial.getRetryFrequency()).append(" Second(s)");
                break;
            case ETHERNET:
                GatewayEthernet gatewayEthernet = new GatewayEthernet(gateway);
                builder.append("Host:").append(gatewayEthernet.getHost());
                builder.append(", Port:").append(gatewayEthernet.getPort());
                break;
            case MQTT:
                GatewayMQTT gatewayMqtt = new GatewayMQTT(gateway);
                builder.append("BrokerHost:").append(gatewayMqtt.getBrokerHost());
                builder.append(", ClientId:").append(gatewayMqtt.getClientId());
                break;

            default:
                break;
        }
        return builder.toString();
    }

    public static void executeGatewayOperation(ResourceModel resourceModel, PayloadOperation operation) {
        switch (operation.getOperationType()) {
            case ENABLE:
                enableGateway(resourceModel.getResourceId());
                break;
            case DISABLE:
                disableGateway(resourceModel.getResourceId());
                break;
            case RELOAD:
                reloadGateway(resourceModel.getResourceId());
                break;
            default:
                _logger.warn("Gateway will not support for this operation!:[{}]",
                        operation.getOperationType().getText());
                break;
        }
    }
}
