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
package org.mycontroller.standalone.mysensors;

import org.mycontroller.standalone.ObjectFactory;
import org.mycontroller.standalone.mysensors.MyMessages.MESSAGE_TYPE;
import org.mycontroller.standalone.mysensors.MyMessages.MESSAGE_TYPE_INTERNAL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */

public class NodeDiscover implements Runnable {
    private static final Logger _logger = LoggerFactory.getLogger(NodeDiscover.class.getName());
    public static final int NODE_ID_MIN = 1;
    public static final int NODE_ID_MAX = 254;

    private static boolean discoverNodesRunning = false;

    public NodeDiscover() {

    }

    private void discoverNodes() {
        if (NodeDiscover.discoverNodesRunning) {
            _logger.warn("Node discover already running! nothing to do..");
            return;
        }
        NodeDiscover.discoverNodesRunning = true;
        RawMessage rawMessage = new RawMessage();
        rawMessage.setAck(0);
        rawMessage.setChildSensorId(255);
        rawMessage.setMessageType(MESSAGE_TYPE.C_INTERNAL.ordinal());
        rawMessage.setSubType(MESSAGE_TYPE_INTERNAL.I_REBOOT.ordinal());
        rawMessage.setTxMessage(true);
        rawMessage.setPayLoad("");

        try {
            int nodeId = NODE_ID_MIN;
            _logger.debug("Starting Node discover util");
            while (nodeId <= NODE_ID_MAX) {
                rawMessage.setNodeId(nodeId);
                ObjectFactory.getRawMessageQueue().putMessage(rawMessage);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    _logger.error("Exception, ", ex);
                }
                nodeId++;
            }
            _logger.debug("Node discover util completed");
        } finally {
            NodeDiscover.discoverNodesRunning = false;
        }
    }

    public static synchronized boolean isDiscoverNodesRunning() {
        return discoverNodesRunning;
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