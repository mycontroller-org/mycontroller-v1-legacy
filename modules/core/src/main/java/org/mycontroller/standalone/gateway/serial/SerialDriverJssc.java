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

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.2.0
 */
@Slf4j
public class SerialDriverJssc implements ISerialDriver {
    private static final Logger _RAW_MSG_LOGGER = LoggerFactory.getLogger(GatewayUtils.RAW_MESSAGE_LOGGER);

    private GatewayConfigSerial _config;
    private SerialPort _serialPort;
    private IMessageParser<byte[]> _parser;
    private IQueue<IMessage> _queue;
    private String gatewayReference;

    public SerialDriverJssc(GatewayConfigSerial _config, IMessageParser<byte[]> _parser,
            IQueue<IMessage> _queue) {
        this._config = _config;
        this._parser = _parser;
        this._queue = _queue;
        this.gatewayReference = GatewayUtils.gwLogReference(_config);
    }

    @Override
    public void connect() {
        String[] portNames = SerialPortList.getPortNames();
        _logger.debug("Number of serial port available:{}", portNames.length);
        for (int portNo = 0; portNo < portNames.length; portNo++) {
            _logger.debug("SerialPortJson[{}]:{}", portNo + 1, portNames[portNo]);
        }

        // create an instance of the serial communications class
        _serialPort = new SerialPort(_config.getPortName());
        try {
            _serialPort.openPort();//Open port
            _serialPort.setParams(
                    _config.getBaudRate(),
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE,
                    SerialPort.DATABITS_8);
            int mask = SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS + SerialPort.MASK_DSR
                    + SerialPort.MASK_ERR;//Prepare mask
            _serialPort.setEventsMask(mask);//Set mask
            // create and register the serial data listener
            // Add SerialPortEventListener
            _serialPort.addEventListener(new SerialDataListenerJssc(_serialPort, _config, _parser, _queue));
            _logger.debug("Serial port gateway initialized, GatewayTable[{}]", _config);
            _config.setStatus(STATE.UP, "Connected Successfully");
        } catch (SerialPortException ex) {
            _config.setStatus(STATE.DOWN, "ERROR: " + ex.getMessage());
            if (ex.getMessage().contains("Port not found")) {
                _logger.error("Failed to load serial port: {}", ex.getMessage());
            } else {
                _logger.error("Failed to load serial port, ", ex);
            }
        }
    }

    @Override
    public void disconnect() {
        try {
            _serialPort.closePort();
            _config.setStatus(STATE.DOWN, "Stopped.");
            _logger.debug("_serialPort{} closed", _serialPort.getPortName());
        } catch (SerialPortException ex) {
            if (ex.getMessage().contains("Port not opened")) {
                _logger.debug("unable to close the port, Error: {}", ex.getMessage());
            } else {
                _logger.error("unable to close the port{}", _serialPort.getPortName(), ex);
            }
            _config.setStatus(STATE.DOWN, "ERROR:" + ex.getMessage());
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
            _serialPort.writeBytes(data);
        } catch (Exception ex) {
            _config.setStatus(STATE.DOWN, "ERROR: " + ex.getMessage());
            _logger.error("Exception while writing data, ", ex);
        }
    }

}

@Slf4j
class SerialDataListenerJssc implements SerialPortEventListener {
    private static final Logger _RAW_MSG_LOGGER = LoggerFactory.getLogger(GatewayUtils.RAW_MESSAGE_LOGGER);

    private SerialPort _serialPort;
    private GatewayConfigSerial _config;
    private IMessageParser<byte[]> _parser;
    private IQueue<IMessage> _queue;
    private String gatewayReference;

    public SerialDataListenerJssc(SerialPort _serialPort, GatewayConfigSerial _config, IMessageParser<byte[]> _parser,
            IQueue<IMessage> _queue) {
        this._serialPort = _serialPort;
        this._config = _config;
        this._parser = _parser;
        this._queue = _queue;
        this.gatewayReference = GatewayUtils.gwLogReference(_config);
    }

    StringBuilder rawMessage = new StringBuilder();

    @Override
    public void serialEvent(SerialPortEvent event) {
        if (event.isRXCHAR() && event.getEventValue() > 0) {
            try {
                byte[] buffer = _serialPort.readBytes();
                for (byte b : buffer) {
                    if ((b == ISerialDriver.MESSAGE_SPLITTER) && rawMessage.length() > 0) {
                        String toProcess = rawMessage.toString();
                        _logger.debug("Received a rawMessage:[{}]", toProcess);

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
                        _logger.debug("Received MESSAGE_SPLITTER and current rawMessage length is ZERO! Nothing to do");
                    }
                }
            } catch (SerialPortException ex) {
                _logger.error("Serail Event Exception, ", ex);
                _config.setStatus(STATE.DOWN, "ERROR: " + ex.getMessage());
                rawMessage.setLength(0);
            } catch (Exception ex) {
                _logger.error("Exception, RawMessage: [{}]", rawMessage.toString(), ex);
                rawMessage.setLength(0);
            }
        }
    }

}
