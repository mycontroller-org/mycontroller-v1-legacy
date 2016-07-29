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

import org.mycontroller.standalone.AppProperties.STATE;
import org.mycontroller.standalone.gateway.model.GatewaySerial;
import org.mycontroller.standalone.message.RawMessage;
import org.mycontroller.standalone.message.RawMessageQueue;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Slf4j
public class SerialDataListenerJssc implements SerialPortEventListener {
    private SerialPort serialPort;
    private GatewaySerial gateway;

    public SerialDataListenerJssc(SerialPort serialPort, GatewaySerial gateway) {
        this.serialPort = serialPort;
        this.gateway = gateway;
    }

    StringBuilder message = new StringBuilder();

    @Override
    public void serialEvent(SerialPortEvent event) {
        if (event.isRXCHAR() && event.getEventValue() > 0) {
            try {
                byte[] buffer = serialPort.readBytes();
                for (byte b : buffer) {
                    if ((b == SerialPortCommon.MESSAGE_SPLITTER) && message.length() > 0) {
                        String toProcess = message.toString();
                        _logger.debug("Received a message:[{}]", toProcess);
                        //Send Message to message factory
                        RawMessageQueue.getInstance().putMessage(RawMessage.builder()
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
            } catch (SerialPortException ex) {
                _logger.error("Serail Event Exception, ", ex);
                gateway.setStatus(STATE.DOWN, "ERROR: " + ex.getMessage());
                message.setLength(0);
            } catch (Exception ex) {
                _logger.error("Exception,", ex);
                message.setLength(0);
            }
        }
    }

}
