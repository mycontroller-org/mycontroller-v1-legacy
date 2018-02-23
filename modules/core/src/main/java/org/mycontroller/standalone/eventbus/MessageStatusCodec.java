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

import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_STATUS;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.JsonObject;

// source : https://github.com/vert-x3/vertx-examples/blob/master/core-examples/
//          src/main/java/io/vertx/example/core/eventbus/messagecodec/util/CustomMessageCodec.java
/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.2.0
 */
public class MessageStatusCodec implements MessageCodec<MessageStatus, MessageStatus> {

    @Override
    public MessageStatus decodeFromWire(int position, Buffer buffer) {
        // My custom message starting from this *position* of buffer
        int _pos = position;

        // Length of JSON
        int length = buffer.getInt(_pos);

        // Get JSON string by it`s length
        // Jump 4 because getInt() == 4 bytes
        String jsonStr = buffer.getString(_pos += 4, _pos += length);
        JsonObject contentJson = new JsonObject(jsonStr);

        // Get fields
        MESSAGE_STATUS status = MESSAGE_STATUS.valueOf(contentJson.getString("status"));
        String message = contentJson.getString("message");

        // We can finally create custom message object
        return MessageStatus.builder().status(status).message(message).build();
    }

    @Override
    public void encodeToWire(Buffer buffer, MessageStatus messageStatus) {
        // Easiest ways is using JSON object
        JsonObject jsonToEncode = new JsonObject();
        jsonToEncode.put("status", messageStatus.getStatus().name());
        jsonToEncode.put("message", messageStatus.getMessage());

        // Encode object to string
        String jsonToStr = jsonToEncode.encode();

        // Length of JSON: is NOT characters count
        int length = jsonToStr.getBytes().length;

        // Write data into given buffer
        buffer.appendInt(length);
        buffer.appendString(jsonToStr);
    }

    @Override
    public String name() {
        // Each codec must have a unique name.
        // This is used to identify a codec when sending a message and for unregistering codecs.
        return this.getClass().getSimpleName();
    }

    @Override
    public byte systemCodecID() {
        // Always -1
        return -1;
    }

    @Override
    public MessageStatus transform(MessageStatus messageStatus) {
        // If a message is sent *locally* across the event bus.
        // This example sends message just as is
        return messageStatus;
    }

}
