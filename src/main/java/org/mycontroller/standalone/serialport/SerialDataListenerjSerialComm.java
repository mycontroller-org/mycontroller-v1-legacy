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
package org.mycontroller.standalone.serialport;

import org.mycontroller.standalone.ObjectFactory;
import org.mycontroller.standalone.mysensors.RawMessage;
import org.mycontroller.standalone.mysensors.RawMessageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class SerialDataListenerjSerialComm implements SerialPortDataListener {
    private static final Logger _logger = LoggerFactory.getLogger(SerialDataListenerjSerialComm.class.getName());

    private SerialPort serialPort;
    private StringBuilder message = new StringBuilder();

    public SerialDataListenerjSerialComm(SerialPort serialPort) {
        this.serialPort = serialPort;
    }

    @Override
    public int getListeningEvents() {
        return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
            return;
        }
        try {
            byte[] buffer = new byte[serialPort.bytesAvailable()];
            serialPort.readBytes(buffer, buffer.length);
            for (byte b : buffer) {
                if ((b == '\r' || b == '\n') && message.length() > 0) {
                    String toProcess = message.toString();
                    _logger.debug("Received a message:[{}]", toProcess);
                    //Send Message to message factory
                    ObjectFactory.getRawMessageQueue().putMessage(new RawMessage(toProcess));
                    message.setLength(0);
                }
                else {
                    if (b != '\n') {
                        _logger.trace("Received a char:[{}]", ((char) b));
                        message.append((char) b);
                    }
                }
            }
        } catch (RawMessageException rEx) {
            _logger.warn(rEx.getMessage());
            message.setLength(0);
        } catch (Exception ex) {
            _logger.error("error, ", ex);
            message.setLength(0);
        }

    }

}
