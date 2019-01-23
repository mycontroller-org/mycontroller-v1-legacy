/*
 * Copyright 2015-2018 Jeeva Kandasamy (jkandasa@gmail.com)
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.mycontroller.standalone.AppProperties.STATE;
import org.mycontroller.standalone.McThreadPoolFactory;
import org.mycontroller.standalone.eventbus.McEventBus;
import org.mycontroller.standalone.eventbus.MessageStatus;
import org.mycontroller.standalone.exceptions.MessageParserException;
import org.mycontroller.standalone.gateway.GatewayUtils;
import org.mycontroller.standalone.gateway.config.GatewayConfigEthernet;
import org.mycontroller.standalone.message.IMessage;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_STATUS;
import org.mycontroller.standalone.offheap.IQueue;
import org.mycontroller.standalone.provider.IMessageParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.2.0
 */
@Slf4j
public class EthernetDriver {
    private static final Logger _RAW_MSG_LOGGER = LoggerFactory.getLogger(GatewayUtils.RAW_MESSAGE_LOGGER);
    public static final int SOCKET_TIMEOUT = 1000 * 7;

    private Socket _socket = null;
    private GatewayConfigEthernet _config = null;
    private EthernetDataListener _listener;
    private IMessageParser<byte[]> _parser;
    private IQueue<IMessage> _queue;
    private String gatewayReference;

    public EthernetDriver(GatewayConfigEthernet _config, IMessageParser<byte[]> _parser, IQueue<IMessage> _queue) {
        this._config = _config;
        this._parser = _parser;
        this._queue = _queue;
        this.gatewayReference = GatewayUtils.gwLogReference(_config);
    }

    public void connect() {
        try {
            _socket = new Socket();
            _socket.setKeepAlive(true);
            _socket.connect(new InetSocketAddress(_config.getHost(), _config.getPort()), SOCKET_TIMEOUT);
            _listener = new EthernetDataListener(_socket, _config, this._parser, _queue);
            // Start listener in new thread via thread pool
            McThreadPoolFactory.execute(_listener);
            _logger.info("Connected successfully[{}:{}]", _config.getHost(), _config.getPort());
            _config.setStatus(STATE.UP, "Connected Successfully");
        } catch (Exception ex) {
            _logger.error("Exception, ", ex);
            _config.setStatus(STATE.DOWN, "ERROR: " + ex.getMessage());
        }
    }

    public void disconnect() {
        if (_listener != null) {
            _listener.setTerminate(true);
        }
        if (_socket != null) {
            try {
                _socket.close();
                _logger.info("EthernetDriver[{}:{}] closed", _config.getHost(), _config.getPort());
            } catch (Exception ex) {
                _logger.error("Exception,", ex);
            }
        }
        _config.setStatus(STATE.DOWN, "Stopped.");
    }

    public void write(IMessage message) throws MessageParserException {
        try {
            byte[] data = _parser.getGatewayData(message);
            // add raw message to a debug file
            if (_RAW_MSG_LOGGER.isDebugEnabled()) {
                MDC.put(GatewayUtils.RAW_MESSAGE_REFERENCE, gatewayReference);
                _RAW_MSG_LOGGER.debug("Tx: {}", new String(data));
            }
            _socket.getOutputStream().write(data);
            _socket.getOutputStream().flush();
        } catch (IOException ex) {
            _logger.error("Exception,", ex);
            _config.setStatus(STATE.DOWN, "ERROR: " + ex.getMessage());
        }
    }

}

@Slf4j
class EthernetDataListener implements Runnable {
    private static final Logger _RAW_MSG_LOGGER = LoggerFactory.getLogger(GatewayUtils.RAW_MESSAGE_LOGGER);

    private Socket _socket = null;
    private boolean terminate = false;
    private boolean terminated = false;
    private GatewayConfigEthernet _config = null;
    private IMessageParser<byte[]> _parser;
    private IQueue<IMessage> _queue;
    private String gatewayReference;

    public EthernetDataListener(Socket _socket, GatewayConfigEthernet _config, IMessageParser<byte[]> _parser,
            IQueue<IMessage> _queue) {
        this._socket = _socket;
        this._config = _config;
        this._parser = _parser;
        this._queue = _queue;
        this.gatewayReference = GatewayUtils.gwLogReference(_config);
    }

    @Override
    public void run() {
        BufferedReader buf = null;
        try {
            buf = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
        } catch (IOException ex) {
            _logger.error("Exception, ", ex);
        }
        while (!isTerminate()) {
            try {
                if (buf.ready()) {
                    String rawMessage = buf.readLine();
                    _logger.debug("Raw message: {}", rawMessage);
                    // add raw messages to a debug file
                    if (_RAW_MSG_LOGGER.isDebugEnabled()) {
                        MDC.put(GatewayUtils.RAW_MESSAGE_REFERENCE, gatewayReference);
                        _RAW_MSG_LOGGER.debug("Rx: {}", rawMessage);
                    }
                    IMessage message = _parser.getMessage(_config, rawMessage.getBytes());
                    if (message != null) {
                        if (message.getAck() == IMessage.ACK_RESPONSE) {
                            McEventBus.getInstance().publish(
                                    message.getEventTopic(),
                                    MessageStatus.builder()
                                            .status(MESSAGE_STATUS.ACK_RECEIVED).message("Ack received")
                                            .build());
                        } else {
                            _queue.add(message);
                        }
                    }
                }
                Thread.sleep(100);
            } catch (IOException | InterruptedException | MessageParserException ex) {
                _logger.error("Exception, ", ex);
            }
        }
        _logger.debug("EthernetDataListener Terminated...");
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
