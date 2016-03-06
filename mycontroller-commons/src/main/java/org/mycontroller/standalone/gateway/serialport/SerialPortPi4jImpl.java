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

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class SerialPortPi4jImpl implements IGateway {
    private static Logger _logger = LoggerFactory.getLogger(SerialPortPi4jImpl.class.getName());
    private SerialDataListenerPi4j dataListenerPi4J;
    private GatewaySerial gateway = null;

    private Serial serial;

    public SerialPortPi4jImpl(GatewaySerial gateway) {
        this.gateway = gateway;
        this.initialize();
    }

    public synchronized void write(RawMessage rawMessage) {
        try {
            serial.write(rawMessage.getGWBytes());
        } catch (Exception ex) {
            gateway.setStatus(STATE.DOWN, "ERROR: " + ex.getMessage());
            gateway.updateGateway();
            _logger.error("exception on pi4j serialport,", ex);
        }
    }

    private void initialize() {
        try {
            // create an instance of the serial communications class
            this.serial = SerialFactory.createInstance();
            this.dataListenerPi4J = new SerialDataListenerPi4j(gateway);
            // create and register the serial data listener
            serial.addListener(dataListenerPi4J);
            // open the serial port
            serial.open(gateway.getPortName(), gateway.getBaudRate());
            _logger.debug("Serial port gateway initialized, Gateway[{}]", gateway);
            gateway.setStatus(STATE.UP, "Connected Successfully");
            gateway.updateGateway();
        } catch (Exception ex) {
            gateway.setStatus(STATE.DOWN, "ERROR: " + ex.getMessage());
            gateway.updateGateway();
            _logger.error("Failed to load serial port,", ex);
        }

    }

    public void close() {
        if (this.serial.isOpen()) {
            try {
                if (this.dataListenerPi4J != null) {
                    this.serial.removeListener(dataListenerPi4J);
                }
                this.serial.close();
                _logger.debug("serialPort{} closed", gateway.getPortName());
            } catch (Exception ex) {
                _logger.error("exception on pi4j serialport,", ex);
            }
        } else {
            _logger.debug("serialPort{} already closed, nothing to do.", gateway.getPortName());
        }
    }

    @Override
    public GatewaySerial getGateway() {
        return gateway;
    }

}
