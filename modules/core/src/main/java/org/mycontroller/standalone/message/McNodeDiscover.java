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
package org.mycontroller.standalone.message;

import java.util.List;

import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_INTERNAL;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
@Slf4j
public class McNodeDiscover implements Runnable {

    private int gatewayId;

    public McNodeDiscover(int gatewayId) {
        this.gatewayId = gatewayId;
    }

    private void discoverNodes() {
        if (McMessageUtils.isDiscoverRunning(gatewayId)) {
            _logger.warn("Node discover already running! nothing to do..");
            return;
        }
        McMessageUtils.updateDiscoverRunning(gatewayId, true);
        try {
            _logger.debug("Starting Node discover util");
            /*
            int nodeId = MySensorsUtils.NODE_ID_MIN;
            while (nodeId <= MySensorsUtils.NODE_ID_MAX) {
                MySensorsRawMessage mySensorsRawMessage = new MySensorsRawMessage(
                        this.gatewayId,
                        nodeId,   //Node Id
                        MySensorsUtils.SENSOR_ID_BROADCAST,    //Sensor Id
                        MYS_MESSAGE_TYPE.C_INTERNAL.ordinal(), //Message Type
                        MySensorsUtils.NO_ACK,  //Ack
                        MYS_MESSAGE_TYPE_INTERNAL.I_PRESENTATION.ordinal(), //Message Sub Type
                        MySensorsUtils.EMPTY_DATA, //Payload
                        true    //Is TX Message?
                );
                McObjectManager.getRawMessageQueue().putMessage(mySensorsRawMessage.getRawMessage());
                nodeId++;
            }
             */

            //TODO: For now discovery disabled, doing only for known nodes with I_PRESENTATION,
            //We should update this with I_DISCOVERY and I_PRESENTATION combination
            List<Node> nodes = DaoUtils.getNodeDao().getAllByGatewayId(gatewayId);
            for (Node node : nodes) {
                McMessage mcMessage = McMessage.builder()
                        .gatewayId(gatewayId)
                        .nodeEui(node.getEui())
                        .SensorId(McMessage.SENSOR_BROADCAST_ID)
                        .type(MESSAGE_TYPE.C_INTERNAL)
                        .subType(MESSAGE_TYPE_INTERNAL.I_PRESENTATION.getText())
                        .acknowledge(false)
                        .payload(McMessage.PAYLOAD_EMPTY)
                        .isTxMessage(true)
                        .build();
                McMessageUtils.sendToProviderBridge(mcMessage);
            }

            _logger.debug("Node discover util completed");
        } finally {
            McMessageUtils.updateDiscoverRunning(gatewayId, false);
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