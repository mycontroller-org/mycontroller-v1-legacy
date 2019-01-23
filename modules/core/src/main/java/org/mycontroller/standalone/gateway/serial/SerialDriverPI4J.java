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
package org.mycontroller.standalone.gateway.serial;

import java.io.IOException;

import org.mycontroller.standalone.AppProperties.STATE;
import org.mycontroller.standalone.eventbus.McEventBus;
import org.mycontroller.standalone.eventbus.MessageStatus;
import org.mycontroller.standalone.gateway.GatewayUtils;
import org.mycontroller.standalone.gateway.config.GatewayConfigSerial;
import org.mycontroller.standalone.message.IMessage;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_STATUS;
import org.mycontroller.standalone.offheap.IQueue;
import org.mycontroller.standalone.provider.IMessageParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataEventListener;
import com.pi4j.io.serial.SerialFactory;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.2.0
 */
@Slf4j
public class SerialDriverPI4J implements ISerialDriver {
    private static final Logger _RAW_MSG_LOGGER = LoggerFactory.getLogger(GatewayUtils.RAW_MESSAGE_LOGGER);

    private GatewayConfigSerial _config;
    private SerialDataListenerPi4j _listener;
    private Serial _serial;
    private IMessageParser<byte[]> _parser;
    private IQueue<IMessage> _queue;
    private String gatewayReference;

    public SerialDriverPI4J(GatewayConfigSerial _config, IMessageParser<byte[]> _parser, IQueue<IMessage> _queue) {
        this._config = _config;
        this._parser = _parser;
        this._queue = _queue;
        this.gatewayReference = GatewayUtils.gwLogReference(_config);
    }

    @Override
    public void connect() {
        try {
            // create an instance of the _serial communications class
            _serial = SerialFactory.createInstance();
            _listener = new SerialDataListenerPi4j(_config, _parser, _queue);
            // create and register the _serial data _listener
            _serial.addListener(_listener);
            // open the _serial port
            _serial.open(_config.getPortName(), _config.getBaudRate());
            _logger.debug("Serial port _config initialized, GatewayTable[{}]", _config);
            _config.setStatus(STATE.UP, "Connected Successfully");
        } catch (Exception ex) {
            _config.setStatus(STATE.DOWN, "ERROR: " + ex.getMessage());
            _logger.error("Failed to load _serial port,", ex);
        }

    }

    @Override
    public void disconnect() {
        if (_serial.isOpen()) {
            try {
                if (_listener != null) {
                    _serial.removeListener(_listener);
                }
                this._serial.close();
                _logger.debug("serialPort{} closed", _config.getPortName());
            } catch (Exception ex) {
                _logger.error("exception on pi4j serialport,", ex);
            }
        } else {
            _logger.debug("serialPort{} already closed, nothing to do.", _config.getPortName());
        }
    }

    @Override
    public void write(IMessage message) {
        try {
            byte[] data = _parser.getGatewayData(message);
            // add raw message to a debug file
            if (_RAW_MSG_LOGGER.isDebugEnabled()) {
                MDC.put(GatewayUtils.RAW_MESSAGE_REFERENCE, gatewayReference);
                _RAW_MSG_LOGGER.debug("Tx: {}", new String(data));
            }
            _serial.write(data);
            _config.setStatus(STATE.DOWN, "Stopped.");
        } catch (Exception ex) {
            _config.setStatus(STATE.DOWN, "ERROR: " + ex.getMessage());
            _logger.error("exception on pi4j serialport,", ex);
        }
    }

}

@Slf4j
class SerialDataListenerPi4j implements SerialDataEventListener {
    private static final Logger _RAW_MSG_LOGGER = LoggerFactory.getLogger(GatewayUtils.RAW_MESSAGE_LOGGER);

    StringBuilder rawMessage = new StringBuilder();
    private GatewayConfigSerial _config;
    private IMessageParser<byte[]> _parser;
    private IQueue<IMessage> _queue;
    private String gatewayReference;

    public SerialDataListenerPi4j(
            GatewayConfigSerial _config,
            IMessageParser<byte[]> _parser,
            IQueue<IMessage> _queue) {
        this._config = _config;
        this._parser = _parser;
        this._queue = _queue;
        this.gatewayReference = GatewayUtils.gwLogReference(_config);
    }

    @Override
    public void dataReceived(SerialDataEvent event) {
        try {
            byte[] buffer = event.getBytes();
            for (byte b : buffer) {
                if ((b == ISerialDriver.MESSAGE_SPLITTER) && rawMessage.length() > 0) {
                    String toProcess = rawMessage.toString();
                    _logger.debug("Received a message:[{}]", toProcess);

                    // add raw messages to a debug file
                    if (_RAW_MSG_LOGGER.isDebugEnabled()) {
                        MDC.put(GatewayUtils.RAW_MESSAGE_REFERENCE, gatewayReference);
                        _RAW_MSG_LOGGER.debug("Rx: {}", toProcess);
                    }

                    //Send Message
                    IMessage message = _parser.getMessage(_config, toProcess.getBytes());
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

                    rawMessage.setLength(0);
                } else if (b != ISerialDriver.MESSAGE_SPLITTER) {
                    _logger.trace("Received a char:[{}]", ((char) b));
                    rawMessage.append((char) b);
                } else if (rawMessage.length() >= ISerialDriver.SERIAL_DATA_MAX_SIZE) {
                    _logger.warn(
                            "Serial receive buffer size reached to MAX level[{} chars], "
                                    + "Now clearing the buffer. Existing data:[{}]",
                            ISerialDriver.SERIAL_DATA_MAX_SIZE, rawMessage.toString());
                    rawMessage.setLength(0);
                } else {
                    _logger.debug("Received MESSAGE_SPLITTER and current message length is ZERO! Nothing to do");
                }
            }
        } catch (IOException ex) {
            _logger.error("exception on pi4j data event, ", ex);
            _config.setStatus(STATE.DOWN, "ERROR: " + ex.getMessage());
            rawMessage.setLength(0);
        } catch (Exception ex) {
            _logger.error("Exception, RawMessage: [{}]", rawMessage.toString(), ex.getMessage());
            rawMessage.setLength(0);
        }
    }
}