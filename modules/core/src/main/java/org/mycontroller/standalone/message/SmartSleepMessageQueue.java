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

import org.mapdb.HTreeMap;
import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.MapDbFactory;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Slf4j
public class SmartSleepMessageQueue {
    public static final String AVAILABLE_QUEUE_LIST = "smart_sleep_msg_queue";
    HTreeMap<String, ArrayList<McMessage>> messagesQueue;

    //Do not load until some calls getInstance
    private static class SmartSleepMessageQueueHelper {
        private static final SmartSleepMessageQueue INSTANCE = new SmartSleepMessageQueue();
    }

    public static SmartSleepMessageQueue getInstance() {
        return SmartSleepMessageQueueHelper.INSTANCE;
    }

    private SmartSleepMessageQueue() {
        messagesQueue = MapDbFactory.getDbStore().getHashMap(AVAILABLE_QUEUE_LIST);
        if (AppProperties.getInstance().getClearMessagesQueueOnStart()) {
            messagesQueue.clear();
            _logger.debug("Cleared all smart sleep messages...");
        }
    }

    private String getQueueName(Integer gatewayId, String nodeEui) {
        return gatewayId + "_" + nodeEui;
    }

    private ArrayList<McMessage> getQueue(String queueName) {
        if (!messagesQueue.containsKey(queueName)) {
            messagesQueue.put(queueName, new ArrayList<McMessage>());
        }
        return messagesQueue.get(queueName);
    }

    public synchronized void removeQueue(Integer gatewayId, String nodeEui) {
        String queueName = getQueueName(gatewayId, nodeEui);
        if (messagesQueue.containsKey(queueName)) {
            messagesQueue.remove(queueName);
            _logger.debug("Queue removed:[{}]", queueName);
        }
    }

    public synchronized void removeMessages(Integer gatewayId, String nodeEui, String sensorId) {
        String queueName = getQueueName(gatewayId, nodeEui);
        ArrayList<McMessage> queue = getQueue(queueName);
        for (int index = 0; index < queue.size(); index++) {
            if (queue.get(index).getSensorId().equals(sensorId)) {
                queue.remove(index);
            }
        }
    }

    public void clearAll() {
        messagesQueue.clear();
        _logger.debug("Cleared all queues..");
    }

    public synchronized void putMessage(McMessage mcMessage) {
        String queueName = getQueueName(mcMessage.getGatewayId(), mcMessage.getNodeEui());
        ArrayList<McMessage> queue = getQueue(queueName);
        queue.add(mcMessage);
        _logger.debug("Added new {}, on queue [{}], size:{}", mcMessage, queueName, queue.size());
    }

    public synchronized McMessage getMessage(Integer gatewayId, String nodeEui) {
        String queueName = getQueueName(gatewayId, nodeEui);
        McMessage mcMessage = null;
        ArrayList<McMessage> queue = getQueue(queueName);
        if (!queue.isEmpty()) {
            mcMessage = queue.remove(0);
        }
        _logger.debug("Retriving {}, on queue [{}], size:{}", mcMessage, queueName, queue.size());
        return mcMessage;
    }
}
