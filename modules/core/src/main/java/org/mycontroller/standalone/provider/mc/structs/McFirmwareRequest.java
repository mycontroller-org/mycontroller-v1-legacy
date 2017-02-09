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
package org.mycontroller.standalone.provider.mc.structs;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Slf4j
public class McFirmwareRequest extends McCommon {
    public McFirmwareRequest() {
        try {
            this.setByteBuffer(
                    ByteBuffer.wrap(Hex.decodeHex("FFFFFFFFFFFF"
                            .toCharArray())).order(ByteOrder.LITTLE_ENDIAN), 0);
        } catch (DecoderException ex) {
            _logger.error("Unable to create 'FirmwareConfigResponse' struct", ex);
        }
    }
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Type:").append(getType());
        builder.append(", Version:").append(getVersion());
        builder.append(", Block:").append(getBlock());
        return builder.toString();
    }

}
