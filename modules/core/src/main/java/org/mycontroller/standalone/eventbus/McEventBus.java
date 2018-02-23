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
package org.mycontroller.standalone.eventbus;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.2.0
 */
@Slf4j
public class McEventBus extends AbstractVerticle {
    private static McEventBus _instance = new McEventBus();

    private McEventBus() {
        vertx = Vertx.vertx();
        vertx.deployVerticle(this);
        getVertx().eventBus().registerDefaultCodec(MessageStatus.class, new MessageStatusCodec());
    }

    public static McEventBus getInstance() {
        return _instance;
    }

    public void start() {
    }

    public void publish(String topic, Object data) {
        getVertx().eventBus().publish(topic, data);
        _logger.debug("Message published. [topic:{}, data:{}]", topic, data);
    }

    public MessageConsumer<MessageStatus> registerConsumer(String topic, Handler<Message<MessageStatus>> handler) {
        return getVertx().eventBus().consumer(topic, handler);
    }

}