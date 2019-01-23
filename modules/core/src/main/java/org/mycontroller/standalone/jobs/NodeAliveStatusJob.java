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
package org.mycontroller.standalone.jobs;

import java.util.List;

import org.knowm.sundial.Job;
import org.knowm.sundial.exceptions.JobInterruptException;
import org.mycontroller.standalone.AppProperties.NETWORK_TYPE;
import org.mycontroller.standalone.AppProperties.STATE;
import org.mycontroller.standalone.McObjectManager;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.gateway.GatewayUtils;
import org.mycontroller.standalone.utils.McUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
public class NodeAliveStatusJob extends Job {
    public static final long MIN_ALIVE_CHECK_DURATION = McUtils.MINUTE * 5; //Run this job every five minutes once
    public static final String NAME = "node_alive_status_job";
    public static final String TRIGGER_NAME = "node_alive_status_trigger";
    private static final Logger _logger = LoggerFactory.getLogger(NodeAliveStatusJob.class);
    private static final long WAIT_TIME_TO_CHECK_ALIVE_STATUS = McUtils.SECOND * 15;
    public static final long DEFAULT_ALIVE_CHECK_INTERVAL = 30 * McUtils.MINUTE;
    private static boolean terminateAliveCheck = false;

    private long currentTime = System.currentTimeMillis();

    @Override
    public void doRun() throws JobInterruptException {
        _logger.debug("Job triggered: Node alive status");
        currentTime = System.currentTimeMillis();
        long start = System.currentTimeMillis();
        try {
            this.sendHeartbeat();
            long referenceTimestamp = 0;
            while (referenceTimestamp <= WAIT_TIME_TO_CHECK_ALIVE_STATUS) {
                if (terminateAliveCheck) {
                    _logger.debug("Termination issued for job Node alive status.");
                    return;
                }
                Thread.sleep(100);
                referenceTimestamp += 100;
            }
            this.checkHeartbeat();
        } catch (Exception ex) {
            _logger.error("Exception, ", ex);
        }
        _logger.debug("Job completed: Node alive status, time taken:{} ms", System.currentTimeMillis() - start);
    }

    private void sendHeartbeat() {
        List<Node> nodes = DaoUtils.getNodeDao().getAll();
        for (Node node : nodes) {
            _logger.debug("Checking for the node[eui:{}, name:{}, gateway:{}], Last check happened {} ms ago",
                    node.getEui(), node.getName(), node.getGatewayTable().getName(),
                    currentTime - node.getLastHeartbeatTxTime());
            //If gateway not available, do not send
            if (McObjectManager.getEngine(node.getGatewayTable().getId()) == null
                    || McObjectManager.getEngine(node.getGatewayTable().getId()).config().getState() != STATE.UP) {
                continue;
            }
            // for smart sleep node do not send heart beat message
            if (node.getSmartSleepEnabled()) {
                continue;
            }
            //for now supports only for MySensors and MyController
            if (node.getGatewayTable().getEnabled()
                    && (node.getGatewayTable().getNetworkType() == NETWORK_TYPE.MY_SENSORS
                    || node.getGatewayTable().getNetworkType() == NETWORK_TYPE.MY_CONTROLLER)
                    && currentTime >= (node.getLastHeartbeatTxTime() + node.getHeartbeatInterval())) {
                McObjectManager.getMcActionEngine().sendAliveStatusRequest(node);
                _logger.debug("Heartbeat request sent for the node[eui:{}, name:{}, gateway:{}]",
                        node.getEui(), node.getName(), node.getGatewayTable().getName());
                // added 3 seconds offset
                DaoUtils.getNodeDao().update(
                        Node.KEY_PROPERTIES,
                        node.setProperty(Node.KEY_HEARTBEAT_LAST_TX_TIME, currentTime - (3 * 1000L)),
                        node.getId());
            } else {
                _logger.debug("Heartbeat request not sent for the node[eui:{}, name:{}, gateway:{}]",
                        node.getEui(), node.getName(), node.getGatewayTable().getName());
            }
        }
    }

    private void checkHeartbeat() {
        List<Node> nodes = DaoUtils.getNodeDao().getAll();
        for (Node node : nodes) {
            STATE newState = null;
            if (node.getLastSeen() == null
                    || node.getLastSeen() <= (currentTime - node.getAliveCheckInterval())) {
                if (_logger.isDebugEnabled()) {
                    _logger.debug(
                            "Status not updated for the node[eui:{}, name:{}, gateway:{}], Overdue:{} ms",
                            node.getEui(),
                            node.getName(),
                            node.getGatewayTable().getName(),
                            node.getLastSeen() == null ? "-" :
                                    (currentTime - node.getAliveCheckInterval()) - node.getLastSeen());
                }
                if (node.getGatewayTable().getEnabled()) {
                    if (node.getState() != STATE.DOWN) {
                        newState = STATE.DOWN;
                    }
                } else {
                    newState = STATE.UNAVAILABLE;
                }
                if (newState != null) {
                    DaoUtils.getNodeDao().update(Node.KEY_STATE, newState, node.getId());
                    _logger.debug("Node new state[eui:{}, name:{}, gateway:{}, state:{}]",
                            node.getEui(), node.getName(), node.getGatewayTable().getName(), newState);
                    // for mysensors network, node 0 is gateway. if the node 0 is down. reload the gateway.
                    // In MySensors node 0 is a gateway
                    if (node.getEui().equals("0")
                            && node.getGatewayTable().getNetworkType() == NETWORK_TYPE.MY_SENSORS
                            && newState == STATE.DOWN) {
                        _logger.info("Seems this gateway[{}] is down for a while, realoading this gateway.",
                                node.getGatewayTable().getName());
                        GatewayUtils.reloadEngine(node.getGatewayTable().getId());
                    }
                }
            }
        }
    }

    public static synchronized void setTerminateAliveCheck(boolean terminateAliveCheck) {
        NodeAliveStatusJob.terminateAliveCheck = terminateAliveCheck;
    }
}
