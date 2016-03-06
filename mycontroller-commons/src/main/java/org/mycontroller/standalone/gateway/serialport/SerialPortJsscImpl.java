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
import org.mycontroller.standalone.gateway.GatewaySerial;
import org.mycontroller.standalone.gateway.IGateway;
import org.mycontroller.standalone.message.RawMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class SerialPortJsscImpl implements IGateway {
    private static final Logger _logger = LoggerFactory.getLogger(SerialPortJsscImpl.class.getName());
    private SerialPort serialPort;
    private GatewaySerial gateway = null;

    public SerialPortJsscImpl(GatewaySerial gateway) {
        this.gateway = gateway;
        this.initialize();
    }

    @Override
    public synchronized void write(RawMessage rawMessage) {
        try {
            serialPort.writeBytes(rawMessage.getGWBytes());
        } catch (Exception ex) {
            gateway.setStatus(STATE.DOWN, "ERROR: " + ex.getMessage());
            gateway.updateGateway();
            _logger.error("Exception while writing data, ", ex);
        }
    }

    private void initialize() {
        String[] portNames = SerialPortList.getPortNames();
        _logger.debug("Number of serial port available:{}", portNames.length);
        for (int portNo = 0; portNo < portNames.length; portNo++) {
            _logger.debug("SerialPortJson[{}]:{}", portNo + 1, portNames[portNo]);
        }

        // create an instance of the serial communications class
        serialPort = new SerialPort(gateway.getPortName());
        try {
            serialPort.openPort();//Open port
            serialPort.setParams(
                    gateway.getBaudRate(),
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE,
                    SerialPort.DATABITS_8);
            int mask = SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS + SerialPort.MASK_DSR + SerialPort.MASK_ERR;//Prepare mask
            serialPort.setEventsMask(mask);//Set mask
            // create and register the serial data listener
            serialPort.addEventListener(new SerialDataListenerJssc(serialPort, gateway));//Add SerialPortEventListener
            _logger.debug("Serial port gateway initialized, Gateway[{}]", gateway);
            gateway.setStatus(STATE.UP, "Connected Successfully");
            gateway.updateGateway();
        } catch (SerialPortException ex) {
            gateway.setStatus(STATE.DOWN, "ERROR: " + ex.getMessage());
            gateway.updateGateway();
            if (ex.getMessage().contains("Port not found")) {
                _logger.error("Failed to load serial port: {}", ex.getMessage());
            } else {
                _logger.error("Failed to load serial port, ", ex);
            }
        }
    }

    public void close() {
        try {
            this.serialPort.closePort();
            _logger.debug("serialPort{} closed", serialPort.getPortName());
        } catch (SerialPortException ex) {
            if (ex.getMessage().contains("Port not opened")) {
                _logger.debug("unable to close the port, Error: {}", ex.getMessage());
            } else {
                _logger.error("unable to close the port{}", serialPort.getPortName(), ex);
            }
        }

    }

    @Override
    public GatewaySerial getGateway() {
        return gateway;
    }

}
