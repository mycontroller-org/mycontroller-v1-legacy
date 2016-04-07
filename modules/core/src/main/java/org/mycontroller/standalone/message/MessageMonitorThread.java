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

import org.mycontroller.standalone.McObjectManager;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.GatewayTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class MessageMonitorThread implements Runnable {
    private static Logger _logger = LoggerFactory.getLogger(MessageMonitorThread.class.getName());
    private static boolean terminationIssued = false;
    private static boolean terminated = false;
    public static final long MYS_MSG_DELAY = 20; // delay time to avoid collisions in network, in milliseconds

    public static boolean isTerminationIssued() {
        return terminationIssued;
    }

    public static synchronized void setTerminationIssued(boolean terminationIssued) {
        MessageMonitorThread.terminationIssued = terminationIssued;
        long start = System.currentTimeMillis();
        long waitTime = 1000 * 60 * 5;
        while (!terminated) {
            try {
                Thread.sleep(10);
                if ((System.currentTimeMillis() - start) >= waitTime) {
                    _logger.warn("Unable to stop MessageMonitorThread on specied wait time[{}ms]", waitTime);
                    break;
                }
            } catch (InterruptedException ex) {
                _logger.debug("Exception in xsleep thread,", ex);
            }
        }
        _logger.debug("MessageMonitorThread terminated");
    }

    private void processRawMessage() {
        while (!RawMessageQueue.getInstance().isEmpty() && !isTerminationIssued()) {
            RawMessage rawMessage = RawMessageQueue.getInstance().getMessage();
            _logger.debug("Processing message:[{}]", rawMessage);
            if (McObjectManager.getGateway(rawMessage.getGatewayId()) != null) {
                try {
                    McMessageUtils.sendToProviderBridge(rawMessage);
                    //A delay to avoid collisions on MySensor networks on continues messages
                    if (!RawMessageQueue.getInstance().isEmpty()) {
                        Thread.sleep(MYS_MSG_DELAY);
                    }
                } catch (Exception ex) {
                    _logger.error("RawMessage[{}] throws exception while processing!, ", rawMessage, ex);
                }
            } else {
                GatewayTable gatewayTable = DaoUtils.getGatewayDao().getById(rawMessage.getGatewayId());
                _logger.error("GatewayTable not available! dropping message... GatewayTable[{}], RawMessage[{}]",
                        gatewayTable, rawMessage);
            }

        }
        if (!RawMessageQueue.getInstance().isEmpty()) {
            _logger.warn("MessageMonitorThread terminating with {} message(s) in queue!",
                    RawMessageQueue.getInstance().getQueueSize());
        }
    }

    @Override
    public void run() {
        try {
            _logger.debug("MessageMonitorThread new thread started.");
            while (!isTerminationIssued()) {
                try {
                    this.processRawMessage();
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                    _logger.debug("Exception in sleep thread,", ex);
                }
            }
            if (isTerminationIssued()) {
                _logger.debug("MessageMonitorThread termination issues. Terminating.");
                terminated = true;
            }
        } catch (Exception ex) {
            terminated = true;
            _logger.error("MessageMonitorThread terminated!, ", ex);
        }
    }

    public static boolean isTerminated() {
        return terminated;
    }

}
