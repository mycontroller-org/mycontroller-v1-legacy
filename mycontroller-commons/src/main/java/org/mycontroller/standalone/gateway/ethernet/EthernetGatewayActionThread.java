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
package org.mycontroller.standalone.gateway.ethernet;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.mycontroller.standalone.ObjectFactory;
import org.mycontroller.standalone.TIME_REF;
import org.mycontroller.standalone.AppProperties.STATE;
import org.mycontroller.standalone.gateway.GatewayEthernet;
import org.mycontroller.standalone.gateway.GatewayException;
import org.mycontroller.standalone.gateway.IGateway.GATEWAY_STATUS;
import org.mycontroller.standalone.message.RawMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
public class EthernetGatewayActionThread implements Runnable {
    private static final Logger _logger = LoggerFactory.getLogger(EthernetGatewayActionThread.class.getName());
    private Socket socket = null;
    private EthernetGatewayListener ethernetGatewayListener = null;
    private boolean terminated = false;
    private boolean terminate = false;
    private boolean reconnect = false;
    private Thread ethernetGatewayListenerThread = null;
    public static final long RETRY_WAIT_TIME = TIME_REF.ONE_SECOND * 5;
    public static final long THREAD_TERMINATION_WAIT_TIME = TIME_REF.ONE_SECOND * 5;
    public static final int SOCKET_TIMEOUT = (int) (TIME_REF.ONE_SECOND * 7);
    private GatewayEthernet gateway = null;

    public EthernetGatewayActionThread(GatewayEthernet gateway) {
        this.gateway = gateway;
        try {
            socket = new Socket();
            socket.setKeepAlive(true);
            socket.connect(new InetSocketAddress(this.gateway.getHost(), this.gateway.getPort()), SOCKET_TIMEOUT);

            ethernetGatewayListener = new EthernetGatewayListener(socket, this.gateway);
            ethernetGatewayListenerThread = new Thread(ethernetGatewayListener);
            ethernetGatewayListenerThread.start();
            _logger.info("Connected successfully with EthernetGateway[{}:{}]",
                    gateway.getHost(), gateway.getPort());
            this.gateway.setStatus(STATE.UP, "Connected Successfully");
            this.gateway.updateGateway();
        } catch (Exception ex) {
            _logger.error("Exception, ", ex);
            this.gateway.setStatus(STATE.DOWN, "ERROR: " + ex.getMessage());
            this.gateway.updateGateway();
            reconnect = true;
        }
    }

    public synchronized void close() {
        if (ethernetGatewayListener != null) {
            ethernetGatewayListener.setTerminate(true);
        }

        this.setTerminate(true);
        long waitTime = THREAD_TERMINATION_WAIT_TIME;
        while (!this.isTerminated() && waitTime > 0) {
            try {
                Thread.sleep(100);
                waitTime -= 100;
            } catch (InterruptedException ex) {
                _logger.error("Error,", ex);
            }
        }
        if (waitTime <= 0) {
            _logger.warn("Terminating abnormally EthernetGatewayActionThread!");
        }
        if (socket != null) {
            try {
                socket.close();
                _logger.info("EthernetGateway[{}:{}] closed", this.gateway.getHost(), this.gateway.getPort());
            } catch (Exception ex) {
                _logger.error("Exception,", ex);
            }
        }
    }

    public synchronized void write(RawMessage rawMessage) throws GatewayException {
        try {
            socket.getOutputStream().write(rawMessage.getGWBytes());
            socket.getOutputStream().flush();
        } catch (IOException ex) {
            _logger.error("Exception,", ex);
            reconnect = true;
            this.gateway.setStatus(STATE.DOWN, "ERROR: " + ex.getMessage());
            this.gateway.updateGateway();
            throw new GatewayException(GATEWAY_STATUS.GATEWAY_ERROR.toString()
                    + ": There is no connection with EthernetGateway!");
        }
    }

    private void reconnect() {
        _logger.debug("Reconnecting to EthernetGateway...");
        if (ethernetGatewayListener != null) {
            ethernetGatewayListener.setTerminate(true);
            _logger.debug("Waiting to terminate previous EthernetGatewayListener...");
            long waitTime = 0;
            while (!ethernetGatewayListener.isTerminated() && waitTime <= (1000 * 5) && !isTerminate()) {
                try {
                    Thread.sleep(100);
                    waitTime += 100;
                } catch (InterruptedException ex) {
                    _logger.error("Error,", ex);
                }
            }
            _logger.debug("Completed: Terminate previous EthernetGatewayListener...");
            ethernetGatewayListener = null;
        }

        if (ethernetGatewayListenerThread != null) {
            if (ethernetGatewayListenerThread.isAlive()) {
                _logger.warn("EthernetGatewayListener Thread is running and about to start another thread. Fix this issue...");
            }
        }

        try {
            if (socket != null) {
                socket.close();
                socket = null;
            }
            socket = new Socket(gateway.getHost(), gateway.getPort());
            socket.setKeepAlive(true);
            ethernetGatewayListener = new EthernetGatewayListener(socket, this.gateway);
            ethernetGatewayListenerThread = new Thread(ethernetGatewayListener);
            ethernetGatewayListenerThread.start();
            reconnect = false;
            this.gateway.setStatus(STATE.UP, "Reconnected Successfully");
            this.gateway.updateGateway();
            _logger.info("Reconnected gateway successfully...");
        } catch (IOException ex) {
            _logger.error("Gateway Exception: {}", ex.getMessage());
            _logger.trace("Detailed exception trace", ex);
        }
    }

    private boolean checkAliveState() {
        return ObjectFactory.getIActionEngine(this.gateway.getNetworkType()).checkEthernetGatewayAliveState(
                this.gateway);
    }

    @Override
    public void run() {
        while (!isTerminate()) {
            try {
                if (reconnect) {
                    long retryWaitTime = RETRY_WAIT_TIME;
                    while (retryWaitTime > 0 && !isTerminate()) {
                        Thread.sleep(100);
                        retryWaitTime -= 100;
                    }
                    if (!isTerminate()) {
                        reconnect();
                    }
                } else {
                    long aliveInterval = this.gateway.getAliveFrequency() * 1000;
                    while (aliveInterval > 0 && !isTerminate() && !reconnect) {
                        Thread.sleep(100);
                        aliveInterval -= 100;
                    }
                    if (!isTerminate() && !reconnect) {
                        if (!checkAliveState()) {
                            reconnect = true;
                        }
                    }
                }

            } catch (Exception ex) {
                _logger.error("Exception,", ex);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    _logger.error("Exception,", e);
                }
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

    public synchronized void setTerminate(boolean terminate) {
        this.terminate = terminate;
    }

    public GatewayEthernet getGateway() {
        return this.gateway;
    }

}
