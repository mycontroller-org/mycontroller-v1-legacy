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
public class UidTagStruct extends Struct {
    private Unsigned16 uid = new Unsigned16();
    private Unsigned16 type = new Unsigned16();
    private Unsigned16 payload = new Unsigned16();

    /*
     * typedef struct {
        uint16_t uid;
        uint16_t status;
        uint16_t payload;
    } UID_STRUCT;
     */
    public int getUid() {
        return uid.get();
    }

    public int getPayload() {
        return payload.get();
    }

    @Override
    public ByteOrder byteOrder() {
        return ByteOrder.LITTLE_ENDIAN;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("UID: ").append(uid);
        builder.append(", Type: ").append(type);
        builder.append(", Payload: ").append(payload);
        return builder.toString();
    }

    public void setUid(int uid) {
        this.uid.set(uid);
    }

    public void setPayload(int payload) {
        this.payload.set(payload);
    }

    public int getType() {
        return type.get();
    }

    public void setType(int type) {
        this.type.set(type);
    }
}
