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
import org.mycontroller.standalone.gateway.IGateway;
import org.mycontroller.standalone.gateway.model.GatewaySerial;
import org.mycontroller.standalone.message.RawMessage;

import com.fazecast.jSerialComm.SerialPort;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Slf4j
public class SerialPortjSerialCommImpl implements IGateway {
    private SerialPort serialPort;
    private GatewaySerial gateway = null;

    public SerialPortjSerialCommImpl(GatewaySerial gateway) {
        this.gateway = gateway;
        initialize();
    }

    @Override
    public synchronized void write(RawMessage rawMessage) {
        try {
            serialPort.writeBytes(rawMessage.getGWBytes(), rawMessage.getGWBytes().length);
        } catch (Exception ex) {
            gateway.setStatus(STATE.DOWN, "ERROR: " + ex.getMessage());
            _logger.error("Error,", ex);
        }
    }

    @Override
    public void close() {
        if (this.serialPort.closePort()) {
            _logger.debug("serialPort{} closed", serialPort.getDescriptivePortName());
        } else {
            _logger.warn("Failed to close serialPort{}", serialPort.getDescriptivePortName());
        }
    }

    private void initialize() {
        SerialPort[] serialPorts = SerialPort.getCommPorts();
        _logger.debug("Number of serial port available:{}", serialPorts.length);
        for (int portNo = 0; portNo < serialPorts.length; portNo++) {
            _logger.debug("SerialPort[{}]:[{},{}]", portNo + 1, serialPorts[portNo].getSystemPortName(),
                    serialPorts[portNo].getDescriptivePortName());
        }

        // create an instance of the serial communications class
        serialPort = SerialPort.getCommPort(gateway.getPortName());

        serialPort.openPort();//Open port
        if (!serialPort.isOpen()) {
            _logger.error("Unable to open serial port:[{}]", gateway.getPortName());
            gateway.setStatus(STATE.DOWN, "ERROR: Unable to open!");
            return;
        }
        serialPort.setComPortParameters(
                gateway.getBaudRate(),
                8,  // data bits
                SerialPort.ONE_STOP_BIT,
                SerialPort.NO_PARITY);

        // create and register the serial data listener
        serialPort.addDataListener(new SerialDataListenerjSerialComm(serialPort, gateway));
        _logger.debug("Serial port initialized with {}", gateway);
        gateway.setStatus(STATE.UP, "Connected Successfully");
    }

    @Override
    public GatewaySerial getGateway() {
        return gateway;
    }

}
