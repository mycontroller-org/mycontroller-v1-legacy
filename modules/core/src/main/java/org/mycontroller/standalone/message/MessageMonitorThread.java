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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.mycontroller.standalone.McObjectManager;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.GatewayTable;
import org.mycontroller.standalone.gateway.GatewayUtils;
import org.mycontroller.standalone.utils.McUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Slf4j
public class MessageMonitorThread implements Runnable {
    private static final AtomicBoolean TERMINATE = new AtomicBoolean(false);
    public static final AtomicBoolean TERMINATED = new AtomicBoolean(false);
    // delay time to avoid collisions on network,
    // in milliseconds, Like my sensors network
    public static final long MC_MSG_DELAY = 20;
    private static long CURRENT_PROCESSING_RATE = -1;
    private static long AVERAGE_PROCESSING_RATE = -1;
    private static long RATE_SAMPLES = 0;
    private static long referanceTime = System.currentTimeMillis();
    private static long messageDoneCount = 0;
    private static long LAST_MESSAGE_PROCESSING_TIME = -1;
    private static long AVG_MESSAGE_PROCESSING_TIME = 0;
    private static long TIME_SAMPLES = 0;

    public static long getCurrentProcessingRate() {
        return CURRENT_PROCESSING_RATE;
    }

    public static long getAvgProcessingRate() {
        return AVERAGE_PROCESSING_RATE;
    }

    public static long getLastMessageProcessingTime() {
        return LAST_MESSAGE_PROCESSING_TIME;
    }

    public static long getAvgtMessageProcessingTime() {
        return AVG_MESSAGE_PROCESSING_TIME;
    }

    public static int getMessagesInQueue() {
        return RawMessageQueue.getInstance().getQueueSize();
    }

    public static void shutdown() {
        if (TERMINATED.get()) {
            return;
        }
        TERMINATE.set(true);
        long start = System.currentTimeMillis();
        long waitTime = McUtils.ONE_MINUTE;
        while (!TERMINATED.get()) {
            try {
                Thread.sleep(10);
                if ((System.currentTimeMillis() - start) >= waitTime) {
                    _logger.warn("Unable to stop MessageMonitorThread on specied wait time[{}ms]", waitTime);
                    break;
                }
            } catch (InterruptedException ex) {
                _logger.debug("Exception in sleep thread,", ex);
            }
        }
        _logger.debug("MessageMonitorThread terminated");
    }

    private void processRawMessage() {
        while (!RawMessageQueue.getInstance().isEmpty() && !TERMINATE.get()) {
            if (!GatewayUtils.GATEWAYS_READY.get()) {
                //Gateways not ready
                return;
            }
            RawMessage rawMessage = RawMessageQueue.getInstance().getMessage();
            _logger.debug("Processing:[{}]", rawMessage);
            if (McObjectManager.getGateway(rawMessage.getGatewayId()) == null && rawMessage.isTxMessage()) {
                GatewayTable gatewayTable = DaoUtils.getGatewayDao().getById(rawMessage.getGatewayId());
                _logger.error("Gateway not available! dropping message... {}, {}", gatewayTable, rawMessage);
                return;
            }
            long startTime = System.currentTimeMillis();
            try {
                McMessageUtils.sendToProviderBridge(rawMessage);
                messageDoneCount++;
                calculateProcessingRate();
                //A delay to avoid collisions on any networks with continues messages. Only for Tx message
                if (!RawMessageQueue.getInstance().isEmpty() && rawMessage.isTxMessage()) {
                    Thread.sleep(McObjectManager.getGateway(rawMessage.getGatewayId()).getGateway().getTxDelay(),
                            333333);
                } else {
                    //This sleep to reduce CPU load, in nanoseconds
                    Thread.sleep(0, 333333);
                }
            } catch (Exception ex) {
                _logger.error("Throws exception while processing!, [{}]", rawMessage, ex);
            }
            updateProcessingTime(System.currentTimeMillis() - startTime);
            _logger.debug("Process done in {} ms for:[{}]", getLastMessageProcessingTime(), rawMessage);
        }
    }

    private void updateProcessingTime(long lastMessageTime) {
        LAST_MESSAGE_PROCESSING_TIME = lastMessageTime;
        AVG_MESSAGE_PROCESSING_TIME = ((AVG_MESSAGE_PROCESSING_TIME * TIME_SAMPLES) + LAST_MESSAGE_PROCESSING_TIME)
                / (TIME_SAMPLES + 1);
        TIME_SAMPLES++;
        //if sample goes beyond 10000, reset it to avoid big calculations.
        if (TIME_SAMPLES > 10000 || AVG_MESSAGE_PROCESSING_TIME == 0) {
            TIME_SAMPLES = 1;
            AVG_MESSAGE_PROCESSING_TIME = LAST_MESSAGE_PROCESSING_TIME;
        }
    }

    private void calculateProcessingRate() {
        if ((System.currentTimeMillis() - referanceTime) >= McUtils.MINUTE) {
            referanceTime = System.currentTimeMillis();
            CURRENT_PROCESSING_RATE = messageDoneCount;
            AVERAGE_PROCESSING_RATE = (long) (((AVERAGE_PROCESSING_RATE * RATE_SAMPLES)
                    + CURRENT_PROCESSING_RATE) / (RATE_SAMPLES + 1));
            RATE_SAMPLES++;
            //if sample goes beyond 10000, reset it to avoid big calculations.
            if (RATE_SAMPLES > 10000 || AVERAGE_PROCESSING_RATE == 0) {
                RATE_SAMPLES = 1;
                AVERAGE_PROCESSING_RATE = CURRENT_PROCESSING_RATE;
            }
            messageDoneCount = 0;
        }
    }

    public static void printStatistics() {
        _logger.info(
                "Message engine statistics, Rate[Last minute:{}, {}/s, Avg:{}, Samples:{}], "
                        + "Time:[Last:{} ms, Avg:{} ms, Samples:{}], In queue:{}",
                getCurrentProcessingRate(), getCurrentProcessingRate() / 60, getAvgProcessingRate(), RATE_SAMPLES,
                getLastMessageProcessingTime(), getAvgtMessageProcessingTime(), TIME_SAMPLES, getMessagesInQueue());
    }

    public static Map<String, Object> getStatistics() {
        HashMap<String, Object> statistics = new HashMap<String, Object>();
        statistics.put("processingRateLastMinute", getCurrentProcessingRate());
        statistics.put("processingRateAvgPerMinute", getAvgProcessingRate());
        statistics.put("processingRateSamples", RATE_SAMPLES);
        statistics.put("processingTimeLastMessage", getLastMessageProcessingTime());
        statistics.put("processingTimeAverage", getAvgtMessageProcessingTime());
        statistics.put("processingTimeSamples", TIME_SAMPLES);
        statistics.put("messagesInQueue", getMessagesInQueue());
        statistics.put("timestamp", System.currentTimeMillis());
        return statistics;
    }

    @Override
    public void run() {
        try {
            _logger.debug("MessageMonitorThread new thread started.");
            referanceTime = System.currentTimeMillis();
            while (!TERMINATE.get()) {
                try {
                    this.processRawMessage();
                    Thread.sleep(10);
                    calculateProcessingRate();
                } catch (InterruptedException ex) {
                    _logger.debug("Exception in sleep thread,", ex);
                }
            }
            if (!RawMessageQueue.getInstance().isEmpty()) {
                _logger.warn("MessageMonitorThread terminating with {} message(s) in queue!",
                        RawMessageQueue.getInstance().getQueueSize());
            }
            if (TERMINATE.get()) {
                _logger.debug("MessageMonitorThread termination issues. Terminating.");
                TERMINATED.set(true);
            }
        } catch (Exception ex) {
            TERMINATED.set(true);
            _logger.error("MessageMonitorThread terminated!, ", ex);
        }
    }

}
