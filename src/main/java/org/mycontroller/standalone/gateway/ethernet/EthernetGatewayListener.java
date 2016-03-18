/**
 * Copyright (C) 2015-2016 Jeeva Kandasamy (jkandasa@gmail.com)
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
package org.mycontroller.standalone.gateway.ethernet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import org.mycontroller.standalone.ObjectManager;
import org.mycontroller.standalone.gateway.GatewayEthernet;
import org.mycontroller.standalone.message.RawMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
public class EthernetGatewayListener implements Runnable {
    private static final Logger _logger = LoggerFactory.getLogger(EthernetGatewayListener.class.getName());
    private Socket socket = null;
    private boolean terminate = false;
    private boolean terminated = false;
    private GatewayEthernet gateway = null;

    public EthernetGatewayListener(Socket socket, GatewayEthernet gateway) {
        this.socket = socket;
        this.gateway = gateway;
    }

    @Override
    public void run() {
        BufferedReader buf = null;
        try {
            buf = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException ex) {
            _logger.error("Exception, ", ex);
        }
        while (!isTerminate()) {
            try {
                if (buf.ready()) {
                    String message = buf.readLine();
                    _logger.debug("Message Received: {}", message);
                    ObjectManager.getRawMessageQueue().putMessage(new RawMessage(gateway.getId(), message));
                }
                Thread.sleep(100);
            } catch (IOException | InterruptedException ex) {
                _logger.error("Exception, ", ex);
            }
        }
        _logger.debug("EthernetGatewayListener Terminated...");
        this.terminated = true;
    }

    public boolean isTerminate() {
        return terminate;
    }

    public synchronized void setTerminate(boolean terminate) {
        this.terminate = terminate;
    }

    public boolean isTerminated() {
        return terminated;
    }
}
