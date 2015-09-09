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
package org.mycontroller.standalone.gateway.serialport;

import java.io.IOException;

import org.mycontroller.standalone.ObjectFactory;
import org.mycontroller.standalone.gateway.IMySensorsGateway;
import org.mycontroller.standalone.mysensors.RawMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class SerialPortPi4jImpl implements IMySensorsGateway {
    private static Logger _logger = LoggerFactory.getLogger(SerialPortPi4jImpl.class.getName());

    private Serial serial;

    public SerialPortPi4jImpl() {
        this.initialize();
    }

    public synchronized void write(RawMessage rawMessage) {
        try {
            serial.write(rawMessage.getGWBytes());
        } catch (IllegalStateException ilEx) {
            _logger.error("exception on pi4j serialport,", ilEx);
        } catch (IOException ioEx) {
            _logger.error("exception on pi4j serialport,", ioEx);
        }
    }

    private void initialize() {
        try {
            // create an instance of the serial communications class
            this.serial = SerialFactory.createInstance();
            SerialDataListenerPi4j dataListenerPi4J = new SerialDataListenerPi4j();
            // create and register the serial data listener
            serial.addListener(dataListenerPi4J);
            // open the serial port
            serial.open(ObjectFactory.getAppProperties().getSerialPortName(),
                    ObjectFactory.getAppProperties().getSerialPortBaudRate());
            _logger.debug("Serial port initialized with the driver:{}, PortName:{}, BaudRate:{}",
                    ObjectFactory.getAppProperties().getSerialPortDriver(),
                    ObjectFactory.getAppProperties().getSerialPortName(),
                    ObjectFactory.getAppProperties().getSerialPortBaudRate());
        } catch (Exception ex) {
            _logger.error("Failed to load serial port,", ex);
        }

    }

    public void close() {
        if (this.serial.isOpen()) {
            try {
                this.serial.close();
                _logger.debug("serialPort{} closed",
                        ObjectFactory.getAppProperties().getSerialPortName());
            } catch (IllegalStateException ilEx) {
                _logger.error("exception on pi4j serialport,", ilEx);
            } catch (IOException ioEx) {
                _logger.error("exception on pi4j serialport,", ioEx);
            }
        } else {
            _logger.debug("serialPort{} already closed, nothing to do.",
                    ObjectFactory.getAppProperties().getSerialPortName());
        }
    }

}
