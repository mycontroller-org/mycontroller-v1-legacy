/**
 * Copyright (C) 2015 Jeeva Kandasamy (jkandasa@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mycontroller.standalone.db.uidtag;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.mycontroller.standalone.ObjectFactory;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.db.tables.UidTag;
import org.mycontroller.standalone.mysensors.RawMessage;
import org.mycontroller.standalone.mysensors.MyMessages.MESSAGE_TYPE;
import org.mycontroller.standalone.mysensors.structs.UidTagStruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class ExecuteUidTag implements Runnable {
    private static final Logger _logger = LoggerFactory.getLogger(ExecuteUidTag.class);

    private Sensor sensor;

    public ExecuteUidTag(Sensor sensor) {
        this.sensor = sensor;
    }

    //Request - 
    //Response -
    private void executeUidTag() throws DecoderException {
        _logger.debug("UID TAG RX String:[{}]", sensor.getLastValue());

        if (sensor.getLastValue().equalsIgnoreCase("NA")) {
            //Nothing to do, just return from here
            return;
        }
        UidTagStruct uidTagStruct = new UidTagStruct();

        uidTagStruct.setByteBuffer(
                ByteBuffer.wrap(Hex.decodeHex(sensor.getLastValue().toCharArray())).order(
                        ByteOrder.LITTLE_ENDIAN), 0);

        _logger.debug("ByteBuffer:[{}]", Hex.encodeHexString(uidTagStruct.getByteBuffer().array()));

        _logger.debug("UID TAG:[{}]", uidTagStruct.toString());

        RawMessage rawMessage = new RawMessage(
                sensor.getNode().getId(),
                sensor.getSensorId(),
                MESSAGE_TYPE.C_SET.ordinal(), //messageType
                0, //ack
                sensor.getMessageType(),//subType
                Hex.encodeHexString(uidTagStruct.getByteBuffer().array()),
                true);// isTxMessage

        UidTag uidTag = DaoUtils.getUidTagDao().get(uidTagStruct.getUid());
        if (uidTagStruct.getStatus() == 0) {
            if (uidTag != null) {
                uidTagStruct.setStatus(1); //Set success
                uidTagStruct.setPayload(Integer.valueOf(uidTag.getSensor().getLastValue()));
                rawMessage.setPayLoad(Hex.encodeHexString(uidTagStruct.getByteBuffer().array()));
            }
        } else if (uidTag != null) {
            RawMessage rawMessageToDevice = new RawMessage(
                    uidTag.getSensor().getNode().getId(),
                    uidTag.getSensor().getSensorId(),
                    MESSAGE_TYPE.C_SET.ordinal(), //messageType
                    0, //ack
                    uidTag.getSensor().getMessageType(),//subType
                    String.valueOf(uidTagStruct.getPayload()),
                    true);// isTxMessage
            ObjectFactory.getRawMessageQueue().putMessage(rawMessageToDevice);
        } else {
            uidTagStruct.setStatus(0);
            rawMessage.setPayLoad(Hex.encodeHexString(uidTagStruct.getByteBuffer().array()));
        }
        ObjectFactory.getRawMessageQueue().putMessage(rawMessage);
        _logger.debug("Message Sent:[{}]", rawMessage.toString());
    }

    @Override
    public void run() {
        try {
            this.executeUidTag();
        } catch (Exception ex) {
            _logger.error("Exception,", ex);
        }
    }

}
