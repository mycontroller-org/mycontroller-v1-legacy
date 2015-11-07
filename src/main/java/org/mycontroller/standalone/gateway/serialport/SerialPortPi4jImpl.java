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

import java.util.HashMap;

import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.ObjectFactory;
import org.mycontroller.standalone.api.jaxrs.mapper.GatewayInfo;
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
    private SerialDataListenerPi4j dataListenerPi4J;
    private GatewayInfo gatewayInfo = new GatewayInfo();

    private Serial serial;

    public SerialPortPi4jImpl() {
        this.initialize();
    }

    public synchronized void write(RawMessage rawMessage) {
        try {
            serial.write(rawMessage.getGWBytes());
        } catch (Exception ex) {
            gatewayInfo.getData().put(SerialPortCommon.IS_CONNECTED, false);
            _logger.error("exception on pi4j serialport,", ex);
        }
    }

    private void initialize() {
        try {
            //Update Gateway Info
            gatewayInfo.setType(ObjectFactory.getAppProperties().getGatewayType());
            gatewayInfo.setData(new HashMap<String, Object>());

            gatewayInfo.getData().put(SerialPortCommon.IS_CONNECTED, false);
            gatewayInfo.getData().put(SerialPortCommon.DRIVER_TYPE,
                    ObjectFactory.getAppProperties().getGatewaySerialPortDriver());
            gatewayInfo.getData().put(SerialPortCommon.SELECTED_DRIVER_TYPE,
                    AppProperties.SERIAL_PORT_DRIVER.PI4J.toString());
            gatewayInfo.getData().put(SerialPortCommon.PORT_NAME,
                    ObjectFactory.getAppProperties().getGatewaySerialPortName());
            gatewayInfo.getData().put(SerialPortCommon.BAUD_RATE,
                    ObjectFactory.getAppProperties().getGatewaySerialPortBaudRate());

            // create an instance of the serial communications class
            this.serial = SerialFactory.createInstance();
            this.dataListenerPi4J = new SerialDataListenerPi4j(gatewayInfo);
            // create and register the serial data listener
            serial.addListener(dataListenerPi4J);
            // open the serial port
            serial.open(ObjectFactory.getAppProperties().getGatewaySerialPortName(),
                    ObjectFactory.getAppProperties().getGatewaySerialPortBaudRate());
            _logger.debug("Serial port initialized with the driver:{}, PortName:{}, BaudRate:{}",
                    ObjectFactory.getAppProperties().getGatewaySerialPortDriver(),
                    ObjectFactory.getAppProperties().getGatewaySerialPortName(),
                    ObjectFactory.getAppProperties().getGatewaySerialPortBaudRate());
            gatewayInfo.getData().put(SerialPortCommon.CONNECTION_STATUS, "Connected Successfully");
            gatewayInfo.getData().put(SerialPortCommon.IS_CONNECTED, true);
            gatewayInfo.getData().put(SerialPortCommon.LAST_SUCCESSFUL_CONNECTION, System.currentTimeMillis());
        } catch (Exception ex) {
            gatewayInfo.getData().put(SerialPortCommon.CONNECTION_STATUS, "ERROR: " + ex.getMessage());
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
                _logger.debug("serialPort{} closed",
                        ObjectFactory.getAppProperties().getGatewaySerialPortName());
            } catch (Exception ex) {
                _logger.error("exception on pi4j serialport,", ex);
            }
        } else {
            _logger.debug("serialPort{} already closed, nothing to do.",
                    ObjectFactory.getAppProperties().getGatewaySerialPortName());
        }
    }

    @Override
    public GatewayInfo getGatewayInfo() {
        return gatewayInfo;
    }

}
