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
package org.mycontroller.standalone.api;

import java.util.ArrayList;
import java.util.List;

import org.mycontroller.standalone.message.McMessage;
import org.mycontroller.standalone.message.SmartSleepMessageQueue;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */

public class SmartSleepMessageQueueApi {
    public List<String> getQueueNames() {
        return SmartSleepMessageQueue.getInstance().getQueueNames();
    }

    public ArrayList<McMessage> getQueue(String queueName) {
        return SmartSleepMessageQueue.getInstance().getQueue(queueName);
    }

    public ArrayList<McMessage> getQueue(Integer gatewayId, String nodeEui) {
        return SmartSleepMessageQueue.getInstance().getQueue(gatewayId, nodeEui);
    }

    public void clearAll() {
        SmartSleepMessageQueue.getInstance().clearAll();
    }

    public void removeQueue(String queueName) {
        SmartSleepMessageQueue.getInstance().removeQueue(queueName);
    }

    public void removeQueue(Integer gatewayId, String nodeEui) {
        SmartSleepMessageQueue.getInstance().removeQueue(gatewayId, nodeEui);
    }

    public void removeMessages(String queueName, String sensorId) {
        SmartSleepMessageQueue.getInstance().removeMessages(queueName, sensorId);
    }

    public void removeMessages(Integer gatewayId, String nodeEui, String sensorId) {
        SmartSleepMessageQueue.getInstance().removeMessages(gatewayId, nodeEui, sensorId);
    }

    public void removeMessages(String queueName, int index) {
        SmartSleepMessageQueue.getInstance().removeMessages(queueName, index);
    }

    public void removeMessages(Integer gatewayId, String nodeEui, int index) {
        SmartSleepMessageQueue.getInstance().removeMessages(gatewayId, nodeEui, index);
    }
}
