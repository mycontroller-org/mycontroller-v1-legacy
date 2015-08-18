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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fazecast.jSerialComm.SerialPort;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class SerialPortjSerialCommImpl implements ISerialPort {
    private static final Logger _logger = LoggerFactory.getLogger(SerialPortJsscImpl.class.getName());
    private SerialPort serialPort;

    public SerialPortjSerialCommImpl() {
        initialize();
    }

    @Override
    public void writeBytes(byte[] data) {
        serialPort.writeBytes(data, data.length);
    }

    @Override
    public void close() {
        if (this.serialPort.closePort()) {
            _logger.info("serialPort{} closed", serialPort.getDescriptivePortName());
        }
    }

    private void initialize() {
        SerialPort[] serialPorts = SerialPort.getCommPorts();
        _logger.info("Number of serial port available:{}", serialPorts.length);
        for (int portNo = 0; portNo < serialPorts.length; portNo++) {
            _logger.info("SerialPort[{}]:[{},{}]", portNo + 1, serialPorts[portNo].getSystemPortName(), serialPorts[portNo].getDescriptivePortName());
        }
        // create an instance of the serial communications class
        serialPort = SerialPort.getCommPort(ObjectFactory.getAppProperties().getSerialPortName());

        serialPort.openPort();//Open port
        if (!serialPort.isOpen()) {
            _logger.error("Unable to open serial port:[{}]", ObjectFactory.getAppProperties().getSerialPortName());
            return;
        }
        serialPort.setComPortParameters(
                ObjectFactory.getAppProperties().getSerialPortBaudRate(),
                8,  // data bits
                SerialPort.ONE_STOP_BIT,
                SerialPort.NO_PARITY);

        // create and register the serial data listener
        serialPort.addDataListener(new SerialDataListenerjSerialComm(serialPort));
        _logger.debug("Serial port initialized with the driver:{}, PortName:{}, BaudRate:{}",
                ObjectFactory.getAppProperties().getSerialPortDriver(),
                ObjectFactory.getAppProperties().getSerialPortName(),
                ObjectFactory.getAppProperties().getSerialPortBaudRate());

    }

}
