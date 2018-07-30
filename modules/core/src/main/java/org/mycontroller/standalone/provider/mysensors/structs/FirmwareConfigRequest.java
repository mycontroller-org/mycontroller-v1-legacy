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

import java.nio.ByteOrder;

import javolution.io.Struct;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class FirmwareConfigRequest extends Struct {
    private final Unsigned16 type = new Unsigned16();
    private final Unsigned16 version = new Unsigned16();
    private final Unsigned16 blocks = new Unsigned16();
    private final Unsigned16 crc = new Unsigned16();
    private final Unsigned16 BLVersion = new Unsigned16();

    @Override
    public ByteOrder byteOrder() {
        return ByteOrder.LITTLE_ENDIAN;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Type:").append(getType());
        builder.append(", Version:").append(getVersion());
        builder.append(", Blocks:").append(getBlocks());
        builder.append(", CRC:").append(getCrc());
        builder.append(", BLVersion:").append(getBLVersion());
        return builder.toString();
    }

    public Integer getVersion() {
        return version.get();
    }

    public Integer getType() {
        return type.get();
    }

    public Integer getBlocks() {
        return blocks.get();
    }

    public Integer getCrc() {
        return crc.get();
    }

    public Integer getBLVersion() {
        return BLVersion.get();
    }

}
