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
package org.mycontroller.standalone.provider.mc.structs;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.mycontroller.standalone.firmware.FirmwareUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Slf4j
public class McFirmwareResponse extends McCommon {
    private final Unsigned16 size = new Unsigned16();
    private final Unsigned8[] data = array(new Unsigned8[FirmwareUtils.FIRMWARE_BLOCK_SIZE_BIN]);

    public McFirmwareResponse() {
        try {
            StringBuilder builder = new StringBuilder("FFFFFFFFFFFFFFFF");
            for (int i = 0; i < FirmwareUtils.FIRMWARE_BLOCK_SIZE_BIN; i++) {
                builder.append("FF");
            }
            this.setByteBuffer(ByteBuffer.wrap(Hex.decodeHex(
                    builder.toString().toCharArray())).order(ByteOrder.LITTLE_ENDIAN), 0);
        } catch (DecoderException ex) {
            _logger.error("Unable to create 'FirmwareResponse' struct", ex);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Type:").append(getType());
        builder.append(", Version:").append(getVersion());
        builder.append(", Block:").append(getBlock());
        builder.append(", Size:").append(getSize());
        builder.append(", Data:").append(getData());
        return builder.toString();
    }

    public Integer getSize() {
        return size.get();
    }

    public void setSize(Integer size) {
        this.size.set(size);
    }

    public String getData() {
        return getHexString(data);
    }

    public void setData(List<Byte> dataByte) {
        for (int index = 0; index < dataByte.size(); index++) {
            data[index].set(dataByte.get(index));
        }
    }
}
