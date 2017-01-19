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

import java.util.ArrayList;

import org.mycontroller.standalone.McObjectManager;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Slf4j
public class SmartSleepMessageTxThread implements Runnable {
    private Integer gatewayId;
    private String nodeEui;
    private static final ArrayList<String> RUNNING_THREADS = new ArrayList<String>();

    private static synchronized boolean isRunning(String thName) {
        return RUNNING_THREADS.contains(thName);
    }

    private static synchronized void completed(String thName) {
        RUNNING_THREADS.remove(thName);
    }

    private static synchronized void started(String thName) {
        RUNNING_THREADS.add(thName);
    }

    public SmartSleepMessageTxThread(Integer gatewayId, String nodeEui) {
        this.gatewayId = gatewayId;
        this.nodeEui = nodeEui;
    }

    @Override
    public void run() {
        if (isRunning(gatewayId + "_" + nodeEui)) {
            _logger.info("A thread is running to send offline messages for '{}_{}'", gatewayId, nodeEui);
            return;
        }
        started(gatewayId + "_" + nodeEui);
        try {
            while (true) {
                McMessage mcMessage = SmartSleepMessageQueue.getInstance().getMessage(gatewayId, nodeEui);
                if (mcMessage != null) {
                    McMessageUtils.sendToProviderBridgeFinal(mcMessage);
                    _logger.debug("Smart sleep message sent {}", mcMessage);
                    Thread.sleep(McObjectManager.getGateway(mcMessage.getGatewayId()).getGateway().getTxDelay(),
                            333333);
                } else {
                    break;
                }
            }
        } catch (Exception ex) {
            _logger.error("Exception, ", ex);
        } finally {
            completed(gatewayId + "_" + nodeEui);
        }
    }
}
