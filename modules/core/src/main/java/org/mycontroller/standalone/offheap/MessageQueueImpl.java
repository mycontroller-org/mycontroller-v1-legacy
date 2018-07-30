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
package org.mycontroller.standalone.offheap;

import java.util.concurrent.BlockingQueue;

import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.message.IMessage;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.2.0
 */
@Slf4j
public class MessageQueueImpl implements IQueue<IMessage> {

    private String nameCounter;
    private String nameQueue;
    BlockingQueue<IMessage> queue;
    private final org.mapdb.Atomic.Integer counter;

    public MessageQueueImpl(String name) {
        nameCounter = COUNTER_PREFIX + "_msg_" + name;
        nameQueue = QUEUE_PREFIX + "_msg_" + name;

        counter = OffHeapFactory.store().getAtomicInteger(nameCounter);
        queue = OffHeapFactory.store().getQueue(nameQueue);
        if (AppProperties.getInstance().getClearMessagesQueueOnStart()) {
            int offlineMessagesCount = counter.get();
            queue.clear();
            counter.set(0);
            _logger.debug("Cleared offline messages[{}] from the queue[{}, {}]", offlineMessagesCount,
                    nameQueue, nameCounter);
        } else {
            _logger.debug("Continuing with offline messages[{}] in the queue[{}, {}]", counter.get(),
                    nameQueue, nameCounter);
            //Allow some time for gateways to get ready
        }
    }

    @Override
    public synchronized void add(IMessage message) {
        if (message != null) {
            queue.add(message);
            counter.incrementAndGet();
            _logger.debug("Added[Queue:{}, size:{}, Message:{}]", nameQueue, counter.get(), message);
        } else {
            _logger.debug("Received NULL message. Queue name:{}", nameQueue);
        }
    }

    @Override
    public synchronized IMessage take() {
        if (!queue.isEmpty()) {
            IMessage message = queue.remove();
            counter.decrementAndGet();
            _logger.debug("Removed[Queue:{}, size:{}, Message:{}]", nameQueue, counter.get(), message);
            return message;
        } else {
            if (counter.get() != 0) {
                _logger.warn("There is no message in the queue, but counter value:{}, returning null", counter.get());
                counter.set(0);
            } else {
                _logger.warn("There is no message in the queue, returning null");
            }
            return null;
        }
    }

    @Override
    public synchronized int size() {
        return counter.get();
    }

    @Override
    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }

    @Override
    public synchronized void clear() {
        queue.clear();
    }

    @Override
    public synchronized void delete() {
        OffHeapFactory.store().delete(nameCounter);
        OffHeapFactory.store().delete(nameQueue);
    }
}
