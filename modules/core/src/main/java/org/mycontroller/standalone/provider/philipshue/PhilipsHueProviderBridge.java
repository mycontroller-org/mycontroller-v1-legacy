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
package org.mycontroller.standalone.provider.philipshue;

import org.mycontroller.standalone.AppProperties.NETWORK_TYPE;
import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.message.IProviderBridge;
import org.mycontroller.standalone.message.McMessage;
import org.mycontroller.standalone.message.McMessageUtils;
import org.mycontroller.standalone.message.RawMessage;
import org.mycontroller.standalone.message.RawMessageException;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Fraid(https://github.com/Fraid)
 */
@Slf4j
public class PhilipsHueProviderBridge implements IProviderBridge {

    public PhilipsHueProviderBridge() {
    }

    @Override
    public void executeMcMessage(McMessage mcMessage) {
        if (mcMessage.getNetworkType() != NETWORK_TYPE.PHILIPS_HUE) {
            _logger.error("This is not '{}' message! McMessage:{}", NETWORK_TYPE.PHILIPS_HUE.getText(), mcMessage);
        }
        try {
            _logger.debug("McMessage about to send to gateway: [{}]", mcMessage);
            McMessageUtils.sendToGateway(new PhilipsHueRawMessage(mcMessage).getRawMessage());
        } catch (RawMessageException ex) {
            _logger.error("Unable to process this McMessage:{}", mcMessage, ex);
        }
    }

    @Override
    public void executeRawMessage(RawMessage rawMessage) {
        if (rawMessage.getNetworkType() != NETWORK_TYPE.PHILIPS_HUE) {
            _logger.error("This is not '{}' message! RawMessage:{}", NETWORK_TYPE.PHILIPS_HUE.getText(), rawMessage);
        }
        try {
            _logger.debug("Received raw message: [{}]", rawMessage);
            McMessageUtils.sendToMcMessageEngine(new PhilipsHueRawMessage(rawMessage).getMcMessage());
        } catch (RawMessageException ex) {
            _logger.error("Unable to process this rawMessage:{}", rawMessage, ex);
        }
    }

    @Override
    public boolean validateSensorId(Sensor sensor) {
        if (sensor.getSensorId().contains(" ")) {
            throw new RuntimeException("Sensor Id should not contain any space");
        }
        return true;
    }

    @Override
    public boolean validateNodeId(Node node) {
        if (node.getEui().contains(" ")) {
            throw new RuntimeException("Node EUI should not contain any space");
        }
        return true;
    }

}
