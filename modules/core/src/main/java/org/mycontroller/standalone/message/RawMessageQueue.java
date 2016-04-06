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

import java.util.ArrayList;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Slf4j
public class RawMessageQueue {
    private static RawMessageQueue _instance = new RawMessageQueue();
    private ArrayList<RawMessage> rawMessages;
    private int queueSize;

    public static RawMessageQueue getInstance() {
        return _instance;
    }

    private RawMessageQueue() {
        this(1000);
    }

    private RawMessageQueue(int queueSize) {
        rawMessages = new ArrayList<RawMessage>();
        this.queueSize = queueSize;
        _logger.debug("Defined Queue Size:{}", queueSize);
    }

    public synchronized void putMessage(RawMessage rawMessage) {
        if (rawMessages.size() < queueSize) {
            rawMessages.add(rawMessage);
            _logger.debug("Added new message, Queue size:{}, Message:[{}]", rawMessages.size(), rawMessage);
        } else {
            _logger.warn("Reached Maximun limit: {}, Unable to add new message. Dropped", rawMessages.size());
        }
    }

    public synchronized RawMessage getMessage() {
        if (rawMessages.size() > 0) {
            RawMessage rawMessage = this.rawMessages.get(0);
            rawMessages.remove(0);
            _logger.debug("Removed a message, Queue size:{}, Message:[{}]", rawMessages.size(), rawMessage);
            return rawMessage;
        } else {
            _logger.warn("There are no messages in the queue, returning null");
            return null;
        }
    }

    public synchronized int getQueueSize() {
        return this.rawMessages.size();
    }

    public synchronized boolean isEmpty() {
        return this.rawMessages.isEmpty();
    }
}
