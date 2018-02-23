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
package org.mycontroller.standalone.mqttbroker;

import org.mycontroller.standalone.auth.AuthUtils;

import io.moquette.spi.impl.subscriptions.Topic;
import io.moquette.spi.security.IAuthorizator;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
@Slf4j
public class MqttAuthorizatorImpl implements IAuthorizator {

    @Override
    public boolean canRead(Topic topic, String username, String client) {
        _logger.debug("Can read check for Topic:{}, Username:{}, Client:{}", topic, username, client);
        return AuthUtils.canReadMqttPermission(username, topic.toString());
    }

    @Override
    public boolean canWrite(Topic topic, String username, String client) {
        _logger.debug("Can write check for Topic:{}, Username:{}, Client:{}", topic, username, client);
        return AuthUtils.canWriteMqttPermission(username, topic.toString());
    }

}
