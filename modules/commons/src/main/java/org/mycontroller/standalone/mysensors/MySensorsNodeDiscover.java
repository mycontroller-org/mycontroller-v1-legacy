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
package org.mycontroller.standalone.mysensors;

import java.util.HashMap;
import java.util.List;

import org.mycontroller.standalone.ObjectFactory;
import org.mycontroller.standalone.MYCMessages.MESSAGE_TYPE;
import org.mycontroller.standalone.MYCMessages.MESSAGE_TYPE_INTERNAL;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */

public class MySensorsNodeDiscover implements Runnable {
    private static final Logger _logger = LoggerFactory.getLogger(MySensorsNodeDiscover.class.getName());

    private int gatewayId;

    public MySensorsNodeDiscover(int gatewayId) {
        this.gatewayId = gatewayId;
    }

    private void discoverNodes() {
        if (MySensorsUtils.isDiscoverRunning(gatewayId)) {
            _logger.warn("Node discover already running! nothing to do..");
            return;
        }
        MySensorsUtils.updateDiscoverRunning(gatewayId, true);
        try {
            _logger.debug("Starting Node discover util");
            /*
            int nodeId = MySensorsUtils.NODE_ID_MIN;
            while (nodeId <= MySensorsUtils.NODE_ID_MAX) {
                MySensorsRawMessage mySensorsRawMessage = new MySensorsRawMessage(
                        this.gatewayId,
                        nodeId,   //Node Id
                        MySensorsUtils.SENSOR_ID_BROADCAST,    //Sensor Id
                        MESSAGE_TYPE.C_INTERNAL.ordinal(), //Message Type
                        MySensorsUtils.NO_ACK,  //Ack
                        MESSAGE_TYPE_INTERNAL.I_PRESENTATION.ordinal(), //Message Sub Type
                        MySensorsUtils.EMPTY_DATA, //Payload
                        true    //Is TX Message?
                );
                ObjectFactory.getRawMessageQueue().putMessage(mySensorsRawMessage.getRawMessage());
                nodeId++;
            }
            */

            //TODO: For now discovery disabled, doing only for known nodes with I_PRESENTATION,
            //We should update this with I_DISCOVERY and I_PRESENTATION combination
            List<Node> nodes = DaoUtils.getNodeDao().getAllByGatewayId(this.gatewayId);
            for (Node node : nodes) {
                MySensorsRawMessage mySensorsRawMessage = new MySensorsRawMessage(
                        this.gatewayId,
                        node.getEuiInt(),   //Node Id
                        MySensorsUtils.SENSOR_ID_BROADCAST,    //Sensor Id
                        MESSAGE_TYPE.C_INTERNAL.ordinal(), //Message Type
                        MySensorsUtils.NO_ACK,  //Ack
                        MESSAGE_TYPE_INTERNAL.I_PRESENTATION.ordinal(), //Message Sub Type
                        MySensorsUtils.EMPTY_DATA, //Payload
                        true    //Is TX Message?
                );
                ObjectFactory.getRawMessageQueue().putMessage(mySensorsRawMessage.getRawMessage());
            }

            _logger.debug("Node discover util completed");
        } finally {
            MySensorsUtils.updateDiscoverRunning(gatewayId, false);
        }
    }

    @Override
    public void run() {
        try {
            this.discoverNodes();
        } catch (Exception ex) {
            _logger.error("Error on node discover, ", ex);
        }
    }
}