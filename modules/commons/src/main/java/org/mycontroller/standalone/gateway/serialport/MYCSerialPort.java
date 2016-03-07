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

import org.mycontroller.standalone.TIME_REF;
import org.mycontroller.standalone.db.tables.Gateway;
import org.mycontroller.standalone.gateway.GatewaySerial;
import org.mycontroller.standalone.gateway.IGateway;
import org.mycontroller.standalone.gateway.GatewayException;
import org.mycontroller.standalone.message.RawMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
public class MYCSerialPort implements IGateway {
    private static final Logger _logger = LoggerFactory.getLogger(MYCSerialPort.class.getName());
    public static final long THREAD_TERMINATION_WAIT_TIME = TIME_REF.ONE_SECOND * 5; //Five seconds
    public static final int SERIAL_DATA_MAX_SIZE = 1000;
    private SerialPortMonitoringThread monitoringThread = null;

    public MYCSerialPort(Gateway gateway) {
        monitoringThread = new SerialPortMonitoringThread(gateway);
        new Thread(monitoringThread).start();
    }

    @Override
    public synchronized void write(RawMessage rawMessage) throws GatewayException {
        monitoringThread.write(rawMessage);
    }

    @Override
    public synchronized void close() {
        monitoringThread.setTerminate(true);
        long waitTime = THREAD_TERMINATION_WAIT_TIME;
        while (!monitoringThread.isTerminated() && waitTime > 0) {
            try {
                Thread.sleep(100);
                waitTime -= 100;
            } catch (InterruptedException ex) {
                _logger.error("Error,", ex);
            }
        }
        if (waitTime <= 0) {
            _logger.warn("Terminating abnormally SerialPortMonitoringThread!");
        }
        monitoringThread.close();
    }

    @Override
    public synchronized GatewaySerial getGateway() {
        return monitoringThread.getGateway();
    }

}
