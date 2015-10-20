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

import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.ObjectFactory;
import org.mycontroller.standalone.api.jaxrs.mapper.GatewayInfo;
import org.mycontroller.standalone.db.TIME_REF;
import org.mycontroller.standalone.gateway.IMySensorsGateway;
import org.mycontroller.standalone.gateway.MySensorsGatewayException;
import org.mycontroller.standalone.mysensors.RawMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
public class SerialPortMonitoringThread implements Runnable, IMySensorsGateway {
    private static final Logger _logger = LoggerFactory.getLogger(SerialPortMonitoringThread.class.getName());

    private IMySensorsGateway serialGateway = null;
    private boolean terminated = false;
    private boolean terminate = false;
    public static final long RETRY_WAIT_TIME = TIME_REF.ONE_SECOND * 30; //30 seconds

    public SerialPortMonitoringThread() {
        this.connect();
    }

    private void connect() {
        // - Start Serial port
        String serialPortDriver = ObjectFactory.getAppProperties().getSerialPortDriver();
        if (ObjectFactory.getAppProperties().getSerialPortDriver()
                .equalsIgnoreCase(AppProperties.SERIAL_PORT_DRIVER.AUTO.toString())) {
            if (AppProperties.getOsArch().startsWith("arm")) {
                serialPortDriver = AppProperties.SERIAL_PORT_DRIVER.PI4J.toString();
            } else {
                serialPortDriver = AppProperties.SERIAL_PORT_DRIVER.JSERIALCOMM.toString();
            }
        }
        //Open Serial Port
        if (serialPortDriver.equalsIgnoreCase(AppProperties.SERIAL_PORT_DRIVER.JSSC.toString())) {
            serialGateway = new SerialPortJsscImpl();
        } else if (serialPortDriver.equalsIgnoreCase(AppProperties.SERIAL_PORT_DRIVER.PI4J.toString())) {
            serialGateway = new SerialPortPi4jImpl();
        } else if (serialPortDriver.equalsIgnoreCase(AppProperties.SERIAL_PORT_DRIVER.JSERIALCOMM.toString())) {
            serialGateway = new SerialPortjSerialCommImpl();
        } else {
            _logger.warn("Unkown serial port driver[{}] specified",
                    ObjectFactory.getAppProperties().getSerialPortDriver());
            throw new RuntimeException("Unkown serial port driver["
                    + ObjectFactory.getAppProperties().getSerialPortDriver() + "] specified");
        }

    }

    private void reconnect() {
        //Close serial port
        this.close();
        serialGateway = null;
        //Reconnect serial port
        connect();
        if ((boolean) serialGateway.getGatewayInfo().getData().get(SerialPortCommon.IS_CONNECTED)) {
            _logger.info("Serial Port[Name:{}, Driver{}], Successfully reconnected!",
                    serialGateway.getGatewayInfo().getData().get(SerialPortCommon.PORT_NAME),
                    serialGateway.getGatewayInfo().getData().get(SerialPortCommon.SELECTED_DRIVER_TYPE));
        } else {
            _logger.warn("Serial Port[Name:{}, Driver{}], Unable to reconnected! Will do next try after {} seconds",
                    serialGateway.getGatewayInfo().getData().get(SerialPortCommon.PORT_NAME),
                    serialGateway.getGatewayInfo().getData().get(SerialPortCommon.SELECTED_DRIVER_TYPE),
                    RETRY_WAIT_TIME / 1000);
        }
    }

    @Override
    public void run() {
        while (!isTerminate()) {
            long waitTime = RETRY_WAIT_TIME;
            while (!isTerminate() && waitTime > 0) {
                try {
                    Thread.sleep(100);
                    waitTime -= 100;
                } catch (InterruptedException ex) {
                    _logger.error("Error,", ex);
                }
            }
            if (!isTerminate()
                    && !(boolean) serialGateway.getGatewayInfo().getData().get(SerialPortCommon.IS_CONNECTED)) {
                _logger.warn("Serial Port[Name:{}, Driver{}] not connected, Reconnect initiated...",
                        serialGateway.getGatewayInfo().getData().get(SerialPortCommon.PORT_NAME),
                        serialGateway.getGatewayInfo().getData().get(SerialPortCommon.SELECTED_DRIVER_TYPE));
                reconnect();
            }
        }
        this.terminated = true;
    }

    public boolean isTerminated() {
        return terminated;
    }

    public boolean isTerminate() {
        return terminate;
    }

    public void setTerminate(boolean terminate) {
        this.terminate = terminate;
    }

    @Override
    public synchronized void write(RawMessage rawMessage) throws MySensorsGatewayException {
        serialGateway.write(rawMessage);
    }

    @Override
    public synchronized void close() {
        try {
            serialGateway.close();
        } catch (Exception ex) {
            _logger.error("Error closing serial port Serial Port[Name:{}, Driver{}], Successfully reconnected!",
                    serialGateway.getGatewayInfo().getData().get(SerialPortCommon.PORT_NAME),
                    serialGateway.getGatewayInfo().getData().get(SerialPortCommon.SELECTED_DRIVER_TYPE), ex);
        }
    }

    @Override
    public synchronized GatewayInfo getGatewayInfo() {
        return serialGateway.getGatewayInfo();
    }

}
