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

import java.util.concurrent.BlockingQueue;

import org.mapdb.Atomic.Integer;
import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.MapDbFactory;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Slf4j
public class RawMessageQueue {
    public static final String RAW_MESSAGES_QUEUE_NAME = "mc_raw_messages_queue";
    public static final String RAW_MESSAGES_QUEUE_COUNTER_NAME = "mc_raw_messages_queue_counter";
    BlockingQueue<RawMessage> rawMessagesQueue;
    private final Integer counter;

    //Do not load until some calls getInstance
    private static class RawMessageQueueHelper {
        private static final RawMessageQueue INSTANCE = new RawMessageQueue();
    }

    public static RawMessageQueue getInstance() {
        return RawMessageQueueHelper.INSTANCE;
    }

    private RawMessageQueue() {
        counter = MapDbFactory.getDbStore().getAtomicInteger(RAW_MESSAGES_QUEUE_COUNTER_NAME);
        rawMessagesQueue = MapDbFactory.getDbStore().getQueue(RAW_MESSAGES_QUEUE_NAME);
        if (AppProperties.getInstance().getClearMessagesQueueOnStart()) {
            int offlineMessagesCount = counter.get();
            rawMessagesQueue.clear();
            counter.set(0);
            _logger.info("Cleared offline messages[{}] from the queue.", offlineMessagesCount);
        } else {
            _logger.info("Continuing with offline messages[{}] in queue", counter.get());
            //Allow some time for gateways to get ready
        }

    }

    public synchronized void putMessage(RawMessage rawMessage) {
        rawMessagesQueue.add(rawMessage);
        counter.incrementAndGet();
        _logger.debug("Added new {}, queue size:{}", rawMessage, counter.get());
    }

    public synchronized RawMessage getMessage() {
        if (!rawMessagesQueue.isEmpty()) {
            RawMessage rawMessage = this.rawMessagesQueue.remove();
            counter.decrementAndGet();
            _logger.debug("Removed a {}, queue size:{}", rawMessage, counter.get());
            return rawMessage;
        } else {
            _logger.warn("There is no message in the queue, returning null");
            return null;
        }
    }

    public int getQueueSize() {
        return counter.get();
    }

    public synchronized boolean isEmpty() {
        return rawMessagesQueue.isEmpty();
    }
}
