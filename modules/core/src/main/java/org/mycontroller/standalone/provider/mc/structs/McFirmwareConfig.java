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

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Slf4j
public class McFirmwareConfig extends McCommon {
    private final Unsigned8[] md5sum = array(new Unsigned8[32]);

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Type:").append(getType());
        builder.append(", Version:").append(getVersion());
        builder.append(", Blocks:").append(getBlocks());
        builder.append(", MD5SUM:").append(getMd5Sum());
        return builder.toString();
    }

    public McFirmwareConfig() {
        try {
            this.setByteBuffer(ByteBuffer.wrap(Hex.decodeHex(
                    ("FFFFFFFFFFFF"
                            + "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"
                            + "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF").toCharArray()))
                    .order(ByteOrder.LITTLE_ENDIAN), 0);
        } catch (DecoderException ex) {
            _logger.error("Unable to create 'FirmwareResponse' struct", ex);
        }
    }

    public String getMd5Sum() {
        return getHexString(md5sum);
    }

    public void setMd5Sum(String md5sumString) {
        //try {
        //byte[] md5sumBytes = Hex.decodeHex(md5sumString.toCharArray());
        byte[] md5sumBytes = md5sumString.getBytes();
        for (int index = 0; index < md5sum.length; index++) {
            md5sum[index].set(md5sumBytes[index]);
        }
        //} catch (DecoderException ex) {
        //    _logger.error("Exception, md5sum:{}", md5sumString, ex);
        //}
    }
}
