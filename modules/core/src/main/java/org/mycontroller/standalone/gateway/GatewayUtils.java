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
package org.mycontroller.standalone.gateway;

import java.util.HashMap;
import java.util.List;

import org.mycontroller.standalone.AppProperties.NETWORK_TYPE;
import org.mycontroller.standalone.AppProperties.STATE;
import org.mycontroller.standalone.McObjectManager;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.ResourceOperation;
import org.mycontroller.standalone.db.tables.GatewayTable;
import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.gateway.ethernet.EthernetGatewayImpl;
import org.mycontroller.standalone.gateway.model.Gateway;
import org.mycontroller.standalone.gateway.model.GatewayEthernet;
import org.mycontroller.standalone.gateway.model.GatewayMQTT;
import org.mycontroller.standalone.gateway.model.GatewayPhantIO;
import org.mycontroller.standalone.gateway.model.GatewaySerial;
import org.mycontroller.standalone.gateway.mqtt.MqttGatewayImpl;
import org.mycontroller.standalone.gateway.phantio.PhantIOGatewayImpl;
import org.mycontroller.standalone.gateway.serialport.MYCSerialPort;
import org.mycontroller.standalone.model.ResourceModel;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GatewayUtils {

    public static final String OS_ARCH_ARM = "arm";

    public enum GATEWAY_TYPE {
        SERIAL("Serial"),
        ETHERNET("Ethernet"),
        MQTT("MQTT"),
        PHANT_IO("Sparkfun [phant.io]");
        public static GATEWAY_TYPE get(int id) {
            for (GATEWAY_TYPE type : values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

        private String value;

        private GATEWAY_TYPE(String value) {
            this.value = value;
        }

        public String getText() {
            return this.value;
        }

        public static GATEWAY_TYPE fromString(String text) {
            if (text != null) {
                for (GATEWAY_TYPE type : GATEWAY_TYPE.values()) {
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

    public static Gateway getGateway(Integer gatewayId) {
        return getGateway(DaoUtils.getGatewayDao().getById(gatewayId));
    }

    public static Gateway getGateway(GatewayTable gatewayTable) {
        switch (gatewayTable.getType()) {
            case SERIAL:
                return new GatewaySerial(gatewayTable);
            case ETHERNET:
                return new GatewayEthernet(gatewayTable);
            case MQTT:
                return new GatewayMQTT(gatewayTable);
            case PHANT_IO:
                return new GatewayPhantIO(gatewayTable);
            default:
                _logger.warn("Not implemented yet! GatewayTable:[{}]", gatewayTable.getType().getText());
                return null;
        }
    }

    public static NETWORK_TYPE getNetworkType(Integer gatewayId) {
        if (McObjectManager.getGateway(gatewayId) != null) {
            return McObjectManager.getGateway(gatewayId).getGateway().getNetworkType();
        } else {
            GatewayTable gatewayTable = DaoUtils.getGatewayDao().getById(gatewayId);
            return gatewayTable.getNetworkType();
        }
    }

    public static NETWORK_TYPE getNetworkType(Sensor sensor) {
        Integer gatewayId = null;
        if (sensor.getNode().getGatewayTable() != null && sensor.getNode().getGatewayTable().getId() != null) {
            gatewayId = sensor.getNode().getGatewayTable().getId();
        } else if (sensor.getNode() != null) {
            Node node = DaoUtils.getNodeDao().getById(sensor.getNode().getId());
            if (node != null) {
                return node.getGatewayTable().getNetworkType();
            }
        }
        return getNetworkType(gatewayId);
    }

    /* review required*/

    public static synchronized void loadGateway(GatewayTable gatewayTable) {
        if (!gatewayTable.getEnabled()) {
            return;
        }
        IGateway iGateway = null;
        switch (gatewayTable.getType()) {
            case SERIAL:
                iGateway = new MYCSerialPort(gatewayTable);
                break;
            case ETHERNET:
                iGateway = new EthernetGatewayImpl(gatewayTable);
                break;
            case MQTT:
                iGateway = new MqttGatewayImpl(gatewayTable);
                break;
            case PHANT_IO:
                iGateway = new PhantIOGatewayImpl(gatewayTable);
                break;
            default:
                _logger.warn("Not implemented yet! GatewayTable:[{}]", gatewayTable.getType().getText());
        }
        if (iGateway == null) {
            throw new RuntimeException("Unable to create gateway[" + gatewayTable + "]...Check your input");
        }
        McObjectManager.addGateway(iGateway);
    }

    public static synchronized void unloadGateway(Integer gatewayId) {
        if (McObjectManager.getGateway(gatewayId) != null) {
            McObjectManager.getGateway(gatewayId).close();
            McObjectManager.removeGateway(gatewayId);
        }
    }

    public static synchronized void loadAllGateways() {
        List<GatewayTable> gateways = DaoUtils.getGatewayDao().getAllEnabled();
        //Before load all gateways, make state to unavailable
        for (GatewayTable gatewayTable : gateways) {
            gatewayTable.setState(STATE.UNAVAILABLE);
            gatewayTable.setStatusSince(System.currentTimeMillis());
            gatewayTable.setStatusMessage("Yet to start this gateway!");
            DaoUtils.getGatewayDao().update(gatewayTable);
        }
        //Load all the enabled gateways
        gateways = DaoUtils.getGatewayDao().getAllEnabled();
        for (GatewayTable gatewayTable : gateways) {
            loadGateway(gatewayTable);
        }

    }

    public static synchronized void unloadAllGateways() {
        HashMap<Integer, IGateway> gateways = McObjectManager.getGateways();
        for (Integer gatewayId : gateways.keySet()) {
            unloadGateway(gatewayId);
        }
    }

    public static synchronized void reloadGateways() {
        unloadAllGateways();
        loadAllGateways();
    }

    public static void reloadGateway(Integer gatewayId) {
        unloadGateway(gatewayId);
        GatewayTable gatewayTable = DaoUtils.getGatewayDao().getById(gatewayId);
        loadGateway(gatewayTable);
    }

    public static void reloadGateways(List<Integer> ids) {
        for (Integer id : ids) {
            reloadGateway(id);
        }
    }

    public static void updateGateway(GatewayTable gatewayTable) {
        unloadGateway(gatewayTable.getId());
        DaoUtils.getGatewayDao().update(gatewayTable);
        if (gatewayTable.getEnabled()) {
            loadGateway(gatewayTable);
        }
    }

    public static void addGateway(GatewayTable gatewayTable) {
        DaoUtils.getGatewayDao().create(gatewayTable);
        gatewayTable.setTimestamp(System.currentTimeMillis());
        if (gatewayTable.getEnabled()) {
            gatewayTable.setEnabled(true);
            loadGateway(gatewayTable);
        }
    }

    public static void enableGateway(Integer gatewayId) {
        unloadGateway(gatewayId);
        GatewayTable gatewayTable = DaoUtils.getGatewayDao().getById(gatewayId);
        gatewayTable.setEnabled(true);
        DaoUtils.getGatewayDao().update(gatewayTable);
        loadGateway(gatewayTable);
    }

    public static void disableGateway(Integer gatewayId) {
        unloadGateway(gatewayId);
        GatewayTable gatewayTable = DaoUtils.getGatewayDao().getById(gatewayId);
        gatewayTable.setEnabled(false);
        gatewayTable.setStatusSince(System.currentTimeMillis());
        gatewayTable.setState(STATE.UNAVAILABLE);
        gatewayTable.setStatusMessage("Disabled by user");
        DaoUtils.getGatewayDao().update(gatewayTable);
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

    public static void executeGatewayOperation(ResourceModel resourceModel, ResourceOperation operation) {
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
                _logger.warn("GatewayTable will not support for this operation!:[{}]",
                        operation.getOperationType().getText());
                break;
        }
    }
}
