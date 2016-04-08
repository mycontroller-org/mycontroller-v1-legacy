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
package org.mycontroller.standalone.gateway.serialport;

import java.io.IOException;

import org.mycontroller.standalone.AppProperties.STATE;
import org.mycontroller.standalone.McObjectManager;
import org.mycontroller.standalone.gateway.model.GatewaySerial;
import org.mycontroller.standalone.message.RawMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataEventListener;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class SerialDataListenerPi4j implements SerialDataEventListener {
    private static Logger _logger = LoggerFactory.getLogger(SerialDataListenerPi4j.class.getName());
    StringBuilder message = new StringBuilder();
    private GatewaySerial gateway;

    public SerialDataListenerPi4j(GatewaySerial gateway) {
        this.gateway = gateway;

    }

    @Override
    public void dataReceived(SerialDataEvent event) {
        try {
            byte[] buffer = event.getBytes();
            for (byte b : buffer) {
                if ((b == SerialPortCommon.MESSAGE_SPLITTER) && message.length() > 0) {
                    String toProcess = message.toString();
                    _logger.debug("Received a message:[{}]", toProcess);
                    //Send Message to message factory
                    McObjectManager.getRawMessageQueue().putMessage(RawMessage.builder()
                            .gatewayId(gateway.getId())
                            .data(toProcess)
                            .networkType(gateway.getNetworkType())
                            .build());
                    message.setLength(0);
                } else if (b != SerialPortCommon.MESSAGE_SPLITTER) {
                    _logger.trace("Received a char:[{}]", ((char) b));
                    message.append((char) b);
                } else if (message.length() >= MYCSerialPort.SERIAL_DATA_MAX_SIZE) {
                    _logger.warn(
                            "Serial receive buffer size reached to MAX level[{} chars], "
                            + "Now clearing the buffer. Existing data:[{}]",
                            MYCSerialPort.SERIAL_DATA_MAX_SIZE, message.toString());
                    message.setLength(0);
                } else {
                    _logger.debug("Received MESSAGE_SPLITTER and current message length is ZERO! Nothing to do");
                }
            }
        } catch (IOException ex) {
            _logger.error("exception on pi4j data event, ", ex);
            gateway.setStatus(STATE.DOWN, "ERROR: " + ex.getMessage());
            message.setLength(0);
        } catch (Exception ex) {
            _logger.error("Exception,", ex.getMessage());
            message.setLength(0);
        }
    }
}
