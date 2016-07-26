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
package org.mycontroller.standalone.uidtag;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.mycontroller.standalone.provider.mysensors.structs.UidTagStruct;
import org.mycontroller.standalone.uidtag.UidTagUtils.UIDQ_TYPE;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@ToString
@Data
@Builder
public class UidTagMapper {
    private Integer uid;
    private UIDQ_TYPE type;
    private String payload;

    public static UidTagMapper get(String payload) throws DecoderException {
        UidTagStruct uidTagStruct = new UidTagStruct();
        uidTagStruct.setByteBuffer(
                ByteBuffer.wrap(Hex.decodeHex(payload.toCharArray()))
                        .order(ByteOrder.LITTLE_ENDIAN), 0);
        return UidTagMapper.builder()
                .uid(uidTagStruct.getPayload())
                .type(UIDQ_TYPE.get(uidTagStruct.getType()))
                .payload(String.valueOf(uidTagStruct.getPayload()))
                .build();
    }

    public String getStructString(){
        UidTagStruct uidTagStruct = new UidTagStruct();
        uidTagStruct.setUid(uid);
        uidTagStruct.setType(type.ordinal());
        uidTagStruct.setPayload(Integer.valueOf(payload));
        return Hex.encodeHexString(uidTagStruct.getByteBuffer().array());
    }
}
