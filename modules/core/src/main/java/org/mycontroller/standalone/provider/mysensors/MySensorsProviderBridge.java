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
package org.mycontroller.standalone.provider.mysensors;

import org.mycontroller.standalone.AppProperties.NETWORK_TYPE;
import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.message.IProviderBridge;
import org.mycontroller.standalone.message.McMessage;
import org.mycontroller.standalone.message.McMessageUtils;
import org.mycontroller.standalone.message.RawMessage;
import org.mycontroller.standalone.message.RawMessageException;
import org.mycontroller.standalone.utils.McUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Slf4j
public class MySensorsProviderBridge implements IProviderBridge {

    @Override
    public void executeMcMessage(McMessage mcMessage) {
        if (mcMessage.getNetworkType() != NETWORK_TYPE.MY_SENSORS) {
            _logger.error("This is not MySensors message! McMessage:{}", mcMessage);
        }
        try {
            _logger.debug("McMessage about to send to gateway: [{}]", mcMessage);
            McMessageUtils.sendToGateway(new MySensorsRawMessage(mcMessage).getRawMessage());
        } catch (RawMessageException ex) {
            _logger.error("Unable to process this McMessage:{}", mcMessage, ex);
        }
    }

    @Override
    public void executeRawMessage(RawMessage rawMessage) {
        if (rawMessage.getNetworkType() != NETWORK_TYPE.MY_SENSORS) {
            _logger.error("This is not MySensors message! RawMessage:{}", rawMessage);
        }
        try {
            _logger.debug("Received raw message: [{}]", rawMessage);
            McMessageUtils.sendToMcMessageEngine(new MySensorsRawMessage(rawMessage).getMcMessage());
        } catch (RawMessageException ex) {
            _logger.error("Unable to process this rawMessage:{}", rawMessage, ex);
        }
    }

    @Override
    public boolean validateSensorId(Sensor sensor) {
        if (McUtils.getInteger(sensor.getSensorId()) < 255 && McUtils.getInteger(sensor.getSensorId()) >= 0) {
            return true;
        } else {
            _logger.warn("Sensor:[{}], Sensor Id should be in the range of 0~254", sensor);
            throw new RuntimeException("Sensor Id should be in the range of 0~254");
        }
    }

    @Override
    public boolean validateNodeId(Node node) {
        if (McUtils.getInteger(node.getEui()) < 255 && McUtils.getInteger(node.getEui()) >= 0) {
            return true;
        } else {
            _logger.warn("Node:[{}], Node Id should be in the range of 0~254", node);
            throw new RuntimeException("Node Id should be in the range of 0~254");
        }
    }

    @Override
    public RawMessage getRawMessage(McMessage mcMessage) throws RawMessageException {
        return new MySensorsRawMessage(mcMessage).getRawMessage();
    }
}
