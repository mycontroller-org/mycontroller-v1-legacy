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
package org.mycontroller.standalone.provider.mysensors.structs;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import javolution.io.Struct;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Slf4j
public class FirmwareConfigResponse extends Struct {

    private final Unsigned16 type = new Unsigned16();
    private final Unsigned16 version = new Unsigned16();
    private final Unsigned16 blocks = new Unsigned16();
    private final Unsigned16 crc = new Unsigned16();

    public FirmwareConfigResponse() {
        try {
            this.setByteBuffer(
                    ByteBuffer.wrap(Hex.decodeHex("FFFFFFFFFFFFFFFF".toCharArray())).order(
                            ByteOrder.LITTLE_ENDIAN), 0);
        } catch (DecoderException ex) {
            _logger.error("Unable to create 'FirmwareConfigResponse' struct", ex);
        }

    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Type/BlCommand:").append(getType());
        builder.append(", Version/BlData:").append(getVersion());
        builder.append(", Blocks:").append(getBlocks());
        builder.append(", CRC:").append(getCrc());
        return builder.toString();
    }

    //Refer https://github.com/mysensors/MySensorsBootloaderRF24/blob/master/MYSBootloader.c#L261
    //https://github.com/mysensors/MySensorsBootloaderRF24/blob/master/MYSBootloader.h#L19
    private void loadBootloaderCommand(Integer command) {
        setBlocks(0);
        setCrc(0xDA7A);
        setType(command);
    }

    //Erase complete EEPROM of the node
    public void loadEraseEepromCommand() {
        loadBootloaderCommand(0x01);
    }

    //Change Node Id of the node
    public void loadNewNodeId(Integer nodeId) {
        loadBootloaderCommand(0x02);
        setVersion(nodeId);
    }

    @Override
    public ByteOrder byteOrder() {
        return ByteOrder.LITTLE_ENDIAN;
    }

    public Integer getType() {
        return type.get();
    }

    public Integer getVersion() {
        return version.get();
    }

    public Integer getBlocks() {
        return blocks.get();
    }

    public Integer getCrc() {
        return crc.get();
    }

    public void setType(Integer type) {
        this.type.set(type);
    }

    public void setVersion(Integer version) {
        this.version.set(version);
    }

    public void setBlocks(Integer blocks) {
        this.blocks.set(blocks);
    }

    public void setCrc(Integer crc) {
        this.crc.set(crc);
    }
}
