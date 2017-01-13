/*
 * Copyright 2015-2017 Jeeva Kandasamy (jkandasa@gmail.com)
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
import org.mycontroller.standalone.gateway.GatewayException;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_INTERNAL;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
@Slf4j
@AllArgsConstructor
public class McNodeInfoUpdate implements Runnable {

    private Integer gatewayId;
    private List<Integer> nodeIds;

    private void updateNodesInfo() throws GatewayException {
        List<Node> nodes = null;
        if (gatewayId != null) {
            if (McMessageUtils.isNodeInfoUpdateRunning(gatewayId)) {
                _logger.warn("Node info update already running! nothing to do..");
                return;
            }
            McMessageUtils.updateNodeInfoRunningState(gatewayId, true);
            nodes = DaoUtils.getNodeDao().getAllByGatewayId(gatewayId);
        } else if (nodeIds != null) {
            nodes = DaoUtils.getNodeDao().getAll(nodeIds);
        } else {
            _logger.warn("either 'gatewayId' or 'nodeIds' must specified!");
            return;
        }
        try {
            _logger.debug("Starting Node info update util");
            for (Node node : nodes) {
                if (node.getGatewayTable().getEnabled()) {
                    McMessage mcMessage = McMessage.builder()
                            .gatewayId(node.getGatewayTable().getId())
                            .nodeEui(node.getEui())
                            .sensorId(McMessage.SENSOR_BROADCAST_ID)
                            .type(MESSAGE_TYPE.C_INTERNAL)
                            .subType(MESSAGE_TYPE_INTERNAL.I_PRESENTATION.getText())
                            .ack(McMessage.NO_ACK)
                            .payload(McMessage.PAYLOAD_EMPTY)
                            .isTxMessage(true)
                            .build();
                    McMessageUtils.sendToMessageQueue(mcMessage);
                }
            }
            _logger.debug("Node info update util completed");
        } finally {
            if (gatewayId != null) {
                McMessageUtils.updateNodeInfoRunningState(gatewayId, false);
            }
        }
    }

    @Override
    public void run() {
        try {
            this.updateNodesInfo();
        } catch (Exception ex) {
            _logger.error("Error on node info update util, ", ex);
        }
    }
}