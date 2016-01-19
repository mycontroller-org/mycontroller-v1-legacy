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
package org.mycontroller.standalone.mysensors;

import java.util.HashMap;

import org.mycontroller.standalone.AppProperties.UNIT_CONFIG;
import org.mycontroller.standalone.NodeIdException;
import org.mycontroller.standalone.ObjectFactory;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.db.tables.Sensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
public class MySensorsUtils {
    private static final Logger _logger = LoggerFactory.getLogger(MySensorsUtils.class);

    public static final int NODE_ID_BROADCAST = 255;
    public static final int SENSOR_ID_BROADCAST = 255;
    public static final int GATEWAY_ID = 0;
    public static final int ACK = 1;
    public static final int NO_ACK = 0;
    public static final String EMPTY_DATA = "";
    public static final int NODE_ID_MIN = 1;
    public static final int NODE_ID_MAX = 254;

    private static HashMap<Integer, Boolean> discoverRunning = new HashMap<Integer, Boolean>();

    private MySensorsUtils() {

    }

    public static String getMetricType() {
        if (ObjectFactory.getAppProperties().getControllerSettings().getUnitConfig() != null) {
            if (ObjectFactory.getAppProperties().getControllerSettings().getUnitConfig()
                    .equalsIgnoreCase(UNIT_CONFIG.METRIC.getText())) {
                return "M";
            } else if (ObjectFactory.getAppProperties().getControllerSettings().getUnitConfig()
                    .equalsIgnoreCase(UNIT_CONFIG.IMPERIAL.getText())) {
                return "I";
            }
        }
        return "M";
    }

    public static int getNextNodeId(Integer gatewayId) throws NodeIdException {
        int nodeId = 1; //Always starts from number 1
        boolean isIdAvailable = false;
        for (; nodeId < 255; nodeId++) {
            if (DaoUtils.getNodeDao().get(gatewayId, String.valueOf(nodeId)) == null) {
                isIdAvailable = true;
                break;
            }
        }
        if (isIdAvailable) {
            return nodeId;
        } else {
            throw new NodeIdException("There is no free node id! All 254 id's are already reserved!");
        }
    }

    public static void addUpdateNode(Node node, boolean isAdd) {
        if (node.getEuiInt() < 255 && node.getEuiInt() >= 0) {
            if (isAdd) {
                DaoUtils.getNodeDao().create(node);
            } else {
                DaoUtils.getNodeDao().update(node);
            }
        } else {
            _logger.warn("Node:[{}], Node Id should be in the range of 0~254", node);
            throw new RuntimeException("Node Id should be in the range of 0~254");
        }
    }

    public static void addUpdateSensor(Sensor sensor, boolean isAdd) {
        if (sensor.getSensorId() < 255 && sensor.getSensorId() >= 0) {
            if (isAdd) {
                DaoUtils.getSensorDao().create(sensor);
            } else {
                DaoUtils.getSensorDao().update(sensor);
            }
        } else {
            _logger.warn("Sensor:[{}], Sensor Id should be in the range of 0~254", sensor);
            throw new RuntimeException("Sensor Id should be in the range of 0~254");
        }
    }

    public static synchronized boolean isDiscoverRunning(int gatewayId) {
        if (discoverRunning.get(gatewayId) == null) {
            discoverRunning.put(gatewayId, false);
        }
        return discoverRunning.get(gatewayId);
    }

    public static synchronized void updateDiscoverRunning(int gatewayId, boolean status) {
        discoverRunning.put(gatewayId, status);
    }

}
