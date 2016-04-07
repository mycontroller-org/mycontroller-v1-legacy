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

import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.AppProperties.STATE;
import org.mycontroller.standalone.db.tables.GatewayTable;
import org.mycontroller.standalone.gateway.GatewayException;
import org.mycontroller.standalone.gateway.GatewayUtils;
import org.mycontroller.standalone.gateway.GatewayUtils.SERIAL_PORT_DRIVER;
import org.mycontroller.standalone.gateway.IGateway;
import org.mycontroller.standalone.gateway.model.GatewaySerial;
import org.mycontroller.standalone.message.RawMessage;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
@Slf4j
public class SerialPortMonitoringThread implements Runnable, IGateway {

    private IGateway serialGateway = null;
    private boolean terminated = false;
    private boolean terminate = false;
    private long RETRY_WAIT_TIME;
    private GatewaySerial gateway = null;

    public SerialPortMonitoringThread(GatewayTable gatewayTable) {
        this.gateway = new GatewaySerial(gatewayTable);
        RETRY_WAIT_TIME = this.gateway.getRetryFrequency() * 1000;
        this.connect();
    }

    private void connect() {
        // - Start Serial port
        gateway.setRunningDriver(gateway.getDriver());
        if (gateway.getRunningDriver() == SERIAL_PORT_DRIVER.AUTO) {
            if (AppProperties.getOsArch().startsWith(GatewayUtils.OS_ARCH_ARM)) {
                gateway.setRunningDriver(SERIAL_PORT_DRIVER.PI4J);
            } else {
                gateway.setRunningDriver(SERIAL_PORT_DRIVER.JSERIALCOMM);
            }
        }
        // Open Serial Port
        switch (gateway.getRunningDriver()) {
            case JSERIALCOMM:
                serialGateway = new SerialPortjSerialCommImpl(this.gateway);
                break;
            case JSSC:
                serialGateway = new SerialPortJsscImpl(this.gateway);
                break;
            case PI4J:
                serialGateway = new SerialPortPi4jImpl(this.gateway);
                break;
            default:
                this.gateway.setStatus(STATE.DOWN, "Unknown serial port driver...["
                        + gateway.getRunningDriver() + "]");
                _logger.warn("Unknown serial port driver[{}], nothing to do..Gateway[{}]",
                        gateway.getRunningDriver(), gateway);
                throw new RuntimeException("Unkown serial port driver["
                        + gateway.getRunningDriver() + "] specified");
        }
    }

    private void reconnect() {
        // Close serial port
        this.close();
        serialGateway = null;
        // Reconnect serial port
        connect();
        if (gateway.getState() == STATE.UP) {
            _logger.info("Serial GatewayTable:[{}], Successfully reconnected!", gateway);
        } else {
            _logger.info("Serial GatewayTable[{}], Unable to reconnected! Will do next try after {} second(s)",
                    gateway, gateway.getRetryFrequency());
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
            _logger.debug("Serial GatewayTable:[{}]", gateway);
            if (!isTerminate() && gateway.getState() != STATE.UP) {
                _logger.info("Serial GatewayTable[{}] not connected, Reconnect initiated...", gateway);
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
    public synchronized void write(RawMessage rawMessage) throws GatewayException {
        if (gateway.getState() == STATE.UP) {
            serialGateway.write(rawMessage);
        } else {
            throw new GatewayException("GatewayTable not available! GatewayTable:[" + gateway.toString() + "]");
        }
    }

    @Override
    public synchronized void close() {
        try {
            serialGateway.close();
        } catch (Exception ex) {
            _logger.error("Error closing Serial GatewayTable[{}],", gateway, ex);
        }
    }

    @Override
    public synchronized GatewaySerial getGateway() {
        return gateway;
    }

}
