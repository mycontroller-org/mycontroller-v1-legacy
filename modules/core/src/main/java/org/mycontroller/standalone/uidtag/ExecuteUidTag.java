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

import org.apache.commons.codec.DecoderException;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.db.tables.UidTag;
import org.mycontroller.standalone.message.McMessage;
import org.mycontroller.standalone.message.McMessageUtils;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE;
import org.mycontroller.standalone.uidtag.UidTagUtils.UIDQ_TYPE;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Slf4j
@AllArgsConstructor
public class ExecuteUidTag implements Runnable {
    private SensorVariable sensorVariable;

    //Request -
    //Response -
    private void executeUidTag() throws DecoderException {
        UidTagMapper uidTagMapper = UidTagMapper.get(sensorVariable.getValue());
        if (_logger.isDebugEnabled()) {
            _logger.debug("UID TAG RX String:[{}]", sensorVariable.getValue());
            _logger.debug("{}", uidTagMapper);
        }
        UidTag uidTag = DaoUtils.getUidTagDao().getById(uidTagMapper.getUid());
        if (uidTag == null) {
            uidTagMapper.setType(UIDQ_TYPE.UNAVAILABLE);
        }

        switch (uidTagMapper.getType()) {
            case REQUEST:
                uidTagMapper.setType(UIDQ_TYPE.RESPONSE);
                uidTagMapper.setPayload(uidTag.getSensorVariable().getValue());
                break;
            case SET:
                McMessage mcMessage = McMessage
                        .builder()
                        .gatewayId(uidTag.getSensorVariable().getSensor().getNode().getGatewayTable().getId())
                        .nodeEui(uidTag.getSensorVariable().getSensor().getNode().getEui())
                        .SensorId(uidTag.getSensorVariable().getSensor().getSensorId())
                        .acknowledge(false)
                        .networkType(
                                uidTag.getSensorVariable().getSensor().getNode().getGatewayTable().getNetworkType())
                        .type(MESSAGE_TYPE.C_SET)
                        .subType(uidTag.getSensorVariable().getVariableType().getText())
                        .isTxMessage(true)
                        .payload(uidTag.getSensorVariable().getValue())
                        .build();
                McMessageUtils.sendToProviderBridge(mcMessage);
                break;
            default:
                break;

        }
        if (uidTagMapper.getType() == UIDQ_TYPE.RESPONSE || uidTagMapper.getType() == UIDQ_TYPE.UNAVAILABLE) {
            McMessage mcMessage = McMessage.builder()
                    .gatewayId(sensorVariable.getSensor().getNode().getGatewayTable().getId())
                    .nodeEui(sensorVariable.getSensor().getNode().getEui())
                    .SensorId(sensorVariable.getSensor().getSensorId())
                    .acknowledge(false)
                    .networkType(uidTag.getSensorVariable().getSensor().getNode().getGatewayTable().getNetworkType())
                    .type(MESSAGE_TYPE.C_SET)
                    .subType(sensorVariable.getVariableType().getText())
                    .isTxMessage(true)
                    .payload(uidTagMapper.getStructString())
                    .build();
            McMessageUtils.sendToProviderBridge(mcMessage);
        }
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
