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
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.db.tables.UidTag;
import org.mycontroller.standalone.message.McMessage;
import org.mycontroller.standalone.message.McMessageUtils;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_SET_REQ;
import org.mycontroller.standalone.provider.mysensors.structs.UidTagStruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class ExecuteUidTag implements Runnable {
    private static final Logger _logger = LoggerFactory.getLogger(ExecuteUidTag.class);

    private Sensor sensor;
    private SensorVariable sensorVariable;

    public ExecuteUidTag(Sensor sensor, SensorVariable sensorVariable) {
        this.sensor = sensor;
    }

    //Request -
    //Response -
    private void executeUidTag() throws DecoderException {
        _logger.debug("UID TAG RX String:[{}]", sensorVariable.getValue());

        if (sensorVariable.getValue().equalsIgnoreCase("NA")) {
            //Nothing to do, just return from here
            return;
        }
        UidTagStruct uidTagStruct = new UidTagStruct();

        uidTagStruct.setByteBuffer(
                ByteBuffer.wrap(Hex.decodeHex(sensorVariable.getValue().toCharArray())).order(
                        ByteOrder.LITTLE_ENDIAN), 0);

        _logger.debug("ByteBuffer:[{}]", Hex.encodeHexString(uidTagStruct.getByteBuffer().array()));

        _logger.debug("UID TAG:[{}]", uidTagStruct.toString());
        McMessage mcMessage = McMessage.builder()
                .gatewayId(sensor.getNode().getGatewayTable().getId())
                .nodeEui(sensor.getNode().getEui())
                .SensorId(sensor.getSensorId())
                .type(MESSAGE_TYPE.C_SET)
                .subType(MESSAGE_TYPE_SET_REQ.V_VAR5.getText())
                .acknowledge(false)
                .payload(Hex.encodeHexString(uidTagStruct.getByteBuffer().array()))
                .isTxMessage(true)
                .build();

        UidTag uidTag = DaoUtils.getUidTagDao().get(uidTagStruct.getUid());
        if (uidTagStruct.getStatus() == 0) {
            if (uidTag != null) {
                SensorVariable sensorValueDes =
                        DaoUtils.getSensorVariableDao().get(uidTag.getSensor().getId(),
                                MESSAGE_TYPE_SET_REQ.get(uidTagStruct.getType()));
                if (sensorValueDes != null) {
                    uidTagStruct.setStatus(1); //Set success
                    //TODO: add support for string, payload might be anything, should not restrect with Integer
                    uidTagStruct.setPayload(Integer.valueOf(sensorValueDes.getValue()));
                    mcMessage.setPayload(Hex.encodeHexString(uidTagStruct.getByteBuffer().array()));
                }
            }
        } else if (uidTag != null) {
            mcMessage = McMessage.builder()
                    .gatewayId(uidTag.getSensor().getNode().getGatewayTable().getId())
                    .nodeEui(uidTag.getSensor().getNode().getEui())
                    .SensorId(uidTag.getSensor().getSensorId())
                    .type(MESSAGE_TYPE.C_SET)
                    .subType(MESSAGE_TYPE_SET_REQ.V_VAR5.getText())
                    .acknowledge(false)
                    .payload(String.valueOf(uidTagStruct.getPayload()))
                    .isTxMessage(true)
                    .build();
            McMessageUtils.sendToProviderBridge(mcMessage);
        } else {
            uidTagStruct.setStatus(0);
            mcMessage.setPayload(Hex.encodeHexString(uidTagStruct.getByteBuffer().array()));
        }
        McMessageUtils.sendToProviderBridge(mcMessage);
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
