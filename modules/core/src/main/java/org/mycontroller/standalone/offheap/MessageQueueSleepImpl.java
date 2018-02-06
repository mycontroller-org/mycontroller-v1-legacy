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

import java.util.ArrayList;

import org.mapdb.HTreeMap;
import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.message.IMessage;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.2.0
 */
@Slf4j
public class MessageQueueSleepImpl {

    private String _nameMap;
    HTreeMap<String, ArrayList<IMessage>> _map;

    public MessageQueueSleepImpl(String name) {
        _nameMap = IMap.MAP_PREFIX + "_msg_sleep_" + name;

        _map = OffHeapFactory.store().getHashMap(_nameMap);
        if (AppProperties.getInstance().getClearMessagesQueueOnStart()) {
            _map.clear();
        } else {
            _logger.debug("Continuing with offline messages in the map:[{}]", _nameMap);
        }
    }

    public synchronized void clear() {
        _map.clear();
    }

    public synchronized void delete() {
        OffHeapFactory.store().delete(_nameMap);
    }

    public synchronized void put(IMessage message) {
        // no need to handle broadcast messages
        if (message.getNodeEui().equalsIgnoreCase(IMessage.NODE_BROADCAST_ID)) {
            return;
        }
        updateEmpty(message.getNodeEui());
        ArrayList<IMessage> _queue = _map.get(message.getNodeEui());
        _queue.add(message);
        _logger.debug("Adding [key:{}, size:{}, {}] in to the map", message.getNodeEui(), _queue.size(), message);
    }

    public synchronized ArrayList<IMessage> get(String key) {
        updateEmpty(key);
        return _map.get(key);
    }

    public synchronized ArrayList<IMessage> remove(String key) {
        updateEmpty(key);
        ArrayList<IMessage> _queue = _map.remove(key);
        _logger.debug("Removing[key:{}, size:{}] in to the map", key, _queue.size());
        return _queue;
    }

    public synchronized boolean isEmpty(String key) {
        updateEmpty(key);
        return _map.get(key).isEmpty();
    }

    private synchronized void updateEmpty(String key) {
        if (_map.get(key) == null) {
            _map.put(key, new ArrayList<IMessage>());
        }
    }
}
