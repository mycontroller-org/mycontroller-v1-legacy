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
package org.mycontroller.standalone.mysensors;

import org.mycontroller.standalone.ObjectFactory;
import org.mycontroller.standalone.gateway.MySensorsGatewayException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class ProcessRawMessageUtils {
    private static final Logger _logger = LoggerFactory.getLogger(ProcessRawMessageUtils.class.getName());

    private ProcessRawMessageUtils() {

    }

    public synchronized static void sendMessage(String rawMessage) throws MySensorsGatewayException {
        try {
            sendMessage(new RawMessage(rawMessage));
        } catch (RawMessageException ex) {
            _logger.error("Error, ", ex);
        }
        _logger.debug("Message sent to gateway:[{}]", rawMessage);
    }

    public synchronized static void sendMessage(RawMessage rawMessage) throws MySensorsGatewayException {
        ObjectFactory.getMySensorsGateway().write(rawMessage);
        try {
            Thread.sleep(3);//3ms sleep
        } catch (InterruptedException ex) {
            _logger.error("Exception on thread sleep,", ex);
        }
    }
}
