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
package org.mycontroller.standalone.gateway;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.mycontroller.standalone.AppProperties.STATE;
import org.mycontroller.standalone.McObjectManager;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.ResourceOperation;
import org.mycontroller.standalone.db.tables.GatewayTable;
import org.mycontroller.standalone.gateway.config.GatewayConfig;
import org.mycontroller.standalone.gateway.config.GatewayConfigEthernet;
import org.mycontroller.standalone.gateway.config.GatewayConfigMQTT;
import org.mycontroller.standalone.gateway.config.GatewayConfigPhantIO;
import org.mycontroller.standalone.gateway.config.GatewayConfigPhilipsHue;
import org.mycontroller.standalone.gateway.config.GatewayConfigSerial;
import org.mycontroller.standalone.gateway.config.GatewayConfigWunderground;
import org.mycontroller.standalone.model.ResourceModel;
import org.mycontroller.standalone.provider.IEngine;
import org.mycontroller.standalone.provider.mycontroller.MyControllerEngine;
import org.mycontroller.standalone.provider.mysensors.MySensorsEngine;
import org.mycontroller.standalone.provider.phantio.PhantIOEngine;
import org.mycontroller.standalone.provider.philipshue.PhilipsHueEngine;
import org.mycontroller.standalone.provider.rflink.RFLinkEngine;
import org.mycontroller.standalone.provider.wunderground.WundergroundEngine;

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

    public static final String RAW_MESSAGE_LOGGER = "RAW_MESSAGE_LOGGER";
    public static final String RAW_MESSAGE_REFERENCE = "gateway_reference";
    public static final String OS_ARCH_ARM = "arm";
    public static final AtomicBoolean GATEWAYS_READY = new AtomicBoolean(false);

    public enum GATEWAY_TYPE {
        SERIAL("Serial"),
        ETHERNET("Ethernet"),
        MQTT("MQTT"),
        PHANT_IO("Sparkfun [phant.io]"),
        PHILIPS_HUE("Hue bridge"),
        WUNDERGROUND("Weather Underground");
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

    public static void loadEngineAll() {
        List<GatewayTable> gateways = DaoUtils.getGatewayDao().getAll();
        //Before load all gateways, make state to unavailable
        for (GatewayTable gatewayTable : gateways) {
            if (gatewayTable.getEnabled()) {
                gatewayTable.setState(STATE.UNAVAILABLE);
                gatewayTable.setStatusSince(System.currentTimeMillis());
                gatewayTable.setStatusMessage("Yet to start this gateway!");
                DaoUtils.getGatewayDao().update(gatewayTable);
            }
        }
        //Load all the gateways
        gateways = DaoUtils.getGatewayDao().getAll();
        for (GatewayTable gatewayTable : gateways) {
            loadEngine(gatewayTable);
        }
        GATEWAYS_READY.set(true);
    }

    public static synchronized void loadEngine(GatewayTable gatewayTable) {
        IEngine _engine = null;
        switch (gatewayTable.getNetworkType()) {
            case MY_CONTROLLER:
                _engine = new MyControllerEngine(getGateway(gatewayTable));
                break;
            case MY_SENSORS:
                _engine = new MySensorsEngine(getGateway(gatewayTable));
                break;
            case PHANT_IO:
                _engine = new PhantIOEngine(getGateway(gatewayTable));
                break;
            case PHILIPS_HUE:
                _engine = new PhilipsHueEngine(getGateway(gatewayTable));
                break;
            case RF_LINK:
                _engine = new RFLinkEngine(getGateway(gatewayTable));
                break;
            case WUNDERGROUND:
                _engine = new WundergroundEngine(getGateway(gatewayTable));
                break;
            default:
                break;
        }
        if (_engine != null) {
            McObjectManager.addEngine(_engine);
            if (_engine.config().getEnabled()) {
                _engine.config().setStatus(STATE.UNAVAILABLE, "Starting...");
                _engine.start();
            } else {
                _engine.config().setStatus(STATE.UNAVAILABLE, "Disabled");
            }
        } else {
            _logger.error("Engine not available for {}", gatewayTable.getNetworkType());
        }
    }

    public static synchronized void unloadEngine(Integer gatewayId) {
        if (McObjectManager.getEngine(gatewayId) != null) {
            McObjectManager.getEngine(gatewayId).stop();
            // wait until this engine unloads completely or until timeout
            try {
                long maxWaitTime = 1000 * 10; // 10 seconds
                while (maxWaitTime > 0) {
                    Thread.sleep(10);
                    maxWaitTime -= 10;
                    if (!McObjectManager.getEngine(gatewayId).isRunning()) {
                        break;
                    }
                }
                if (McObjectManager.getEngine(gatewayId).isRunning()) {
                    McObjectManager.getEngine(gatewayId).distory();
                }
            } catch (Exception ex) {
                _logger.error("Exception", ex);
            }
        }
    }

    public static GatewayConfig getGateway(Integer gatewayId) {
        return getGateway(DaoUtils.getGatewayDao().getById(gatewayId));
    }

    public static GatewayConfig getGateway(GatewayTable gatewayTable) {
        switch (gatewayTable.getType()) {
            case SERIAL:
                return new GatewayConfigSerial(gatewayTable);
            case ETHERNET:
                return new GatewayConfigEthernet(gatewayTable);
            case MQTT:
                return new GatewayConfigMQTT(gatewayTable);
            case PHANT_IO:
                return new GatewayConfigPhantIO(gatewayTable);
            case PHILIPS_HUE:
                return new GatewayConfigPhilipsHue(gatewayTable);
            case WUNDERGROUND:
                return new GatewayConfigWunderground(gatewayTable);
            default:
                _logger.warn("Not implemented yet! GatewayTable:[{}]", gatewayTable.getType().getText());
                return null;
        }
    }

    public static synchronized void unloadEngineAll() {
        GATEWAYS_READY.set(false);
        HashMap<Integer, IEngine> engines = McObjectManager.getEngines();
        for (Integer gatewayId : engines.keySet()) {
            unloadEngine(gatewayId);
        }
    }

    public static synchronized void reloadEngines() {
        unloadEngineAll();
        loadEngineAll();
    }

    public static void reloadEngine(Integer gatewayId) {
        unloadEngine(gatewayId);
        GatewayTable gatewayTable = DaoUtils.getGatewayDao().getById(gatewayId);
        loadEngine(gatewayTable);
    }

    public static void reloadEngines(List<Integer> ids) {
        for (Integer id : ids) {
            reloadEngine(id);
        }
    }

    public static void updateGateway(GatewayTable gatewayTable) {
        unloadEngine(gatewayTable.getId());
        DaoUtils.getGatewayDao().update(gatewayTable);
        if (gatewayTable.getEnabled()) {
            loadEngine(gatewayTable);
        }
    }

    public static void addGateway(GatewayTable gatewayTable) {
        gatewayTable.setTimestamp(System.currentTimeMillis());
        DaoUtils.getGatewayDao().create(gatewayTable);
        GatewayTable _gwt = DaoUtils.getGatewayDao().get(GatewayTable.KEY_NAME, gatewayTable.getName());
        loadEngine(_gwt);
    }

    public static void enableGateway(Integer gatewayId) {
        GatewayTable gatewayTable = DaoUtils.getGatewayDao().getById(gatewayId);
        if (gatewayTable.getEnabled() && McObjectManager.getEngine(gatewayId).isRunning()) {
            return;
        }
        unloadEngine(gatewayId);
        gatewayTable = DaoUtils.getGatewayDao().getById(gatewayId);
        gatewayTable.setEnabled(true);
        DaoUtils.getGatewayDao().update(gatewayTable);
        loadEngine(gatewayTable);
    }

    public static void disableGateway(Integer gatewayId) {
        GatewayTable gatewayTable = DaoUtils.getGatewayDao().getById(gatewayId);
        if (!gatewayTable.getEnabled() && !McObjectManager.getEngine(gatewayId).isRunning()) {
            return;
        }
        // unload, stop
        unloadEngine(gatewayId);
        // disable this gateway
        gatewayTable = DaoUtils.getGatewayDao().getById(gatewayId);
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
                reloadEngine(resourceModel.getResourceId());
                break;
            default:
                _logger.warn("GatewayTable will not support for this operation!:[{}]",
                        operation.getOperationType().getText());
                break;
        }
    }

    public static String[] getMqttTopics(String topic) {
        String[] topics = topic.split(GatewayConfigMQTT.TOPICS_SPLITER);
        for (int topicId = 0; topicId < topics.length; topicId++) {
            topics[topicId] += "/#";
        }
        return topics;
    }

    public static String gwLogReference(GatewayConfig _config) {
        return String.format("%d_%s", _config.getId(), _config.getName().replaceAll("[^A-Za-z0-9]", "_"));
    }
}
