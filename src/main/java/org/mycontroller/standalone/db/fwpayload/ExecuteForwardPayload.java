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
package org.mycontroller.standalone.db.fwpayload;

import java.util.List;

import org.mycontroller.standalone.ObjectFactory;
import org.mycontroller.standalone.db.tables.ForwardPayload;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.mysensors.RawMessage;
import org.mycontroller.standalone.mysensors.MyMessages.MESSAGE_TYPE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class ExecuteForwardPayload implements Runnable {
    private static final Logger _logger = LoggerFactory.getLogger(ExecuteForwardPayload.class);
    private List<ForwardPayload> forwardPayloads;
    private Sensor sensor;

    public ExecuteForwardPayload(List<ForwardPayload> forwardPayloads, Sensor sensor) {
        this.forwardPayloads = forwardPayloads;
        this.sensor = sensor;
    }

    private void execute(ForwardPayload forwardPayload) {
        _logger.debug("Sensor:[{}], Details of ForwardPayload:[{}]", sensor, forwardPayload);
        RawMessage rawMessage = new RawMessage(
                forwardPayload.getSensorDestination().getNode().getId(),
                forwardPayload.getSensorDestination().getSensorId(),
                MESSAGE_TYPE.C_SET.ordinal(), //messageType
                0, //ack
                forwardPayload.getSensorDestination().getMessageType(),//subType
                sensor.getLastValue(),
                true);// isTxMessage
        ObjectFactory.getRawMessageQueue().putMessage(rawMessage);
    }

    @Override
    public void run() {
        for (ForwardPayload forwardPayload : forwardPayloads) {
            try {
                execute(forwardPayload);
            } catch (Exception ex) {
                _logger.error("Unable to execute ForwardPayload:[{}], Sensor:[{}]", forwardPayload, sensor, ex);
            }
        }

    }
}
