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
import org.mycontroller.standalone.gateway.config.GatewayConfigSerial;
import org.mycontroller.standalone.message.IMessage;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_STATUS;
import org.mycontroller.standalone.offheap.IQueue;
import org.mycontroller.standalone.provider.IMessageParser;
import org.mycontroller.standalone.utils.McUtils;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.2.0
 */
@Slf4j
public class SerialDriverJSerialComm implements ISerialDriver {
    private GatewayConfigSerial _config;
    private SerialPort _serialPort;
    private IMessageParser<byte[]> _parser;
    private IQueue<IMessage> _queue;

    public SerialDriverJSerialComm(GatewayConfigSerial _config, IMessageParser<byte[]> _parser,
            IQueue<IMessage> _queue) {
        this._config = _config;
        this._parser = _parser;
        this._queue = _queue;
    }

    @Override
    public void connect() {
        SerialPort[] serialPorts = SerialPort.getCommPorts();
        _logger.debug("Number of serial port available:{}", serialPorts.length);
        for (int portNo = 0; portNo < serialPorts.length; portNo++) {
            _logger.debug("SerialPort[{}]:[{},{}]", portNo + 1, serialPorts[portNo].getSystemPortName(),
                    serialPorts[portNo].getDescriptivePortName());
        }

        // create an instance of the serial communications class
        _serialPort = SerialPort.getCommPort(_config.getPortName());

        _serialPort.openPort();//Open port
        if (!_serialPort.isOpen()) {
            _logger.error("Unable to open serial port:[{}]", _config.getPortName());
            _config.setStatus(STATE.DOWN, "ERROR: Unable to open!");
            return;
        }
        _serialPort.setComPortParameters(
                _config.getBaudRate(),
                8,  // data bits
                SerialPort.ONE_STOP_BIT,
                SerialPort.NO_PARITY);

        // create and register the serial data listener
        _serialPort.addDataListener(new SerialDataListenerjSerialComm(_serialPort, _config, _parser, _queue));
        _logger.debug("Serial port initialized with {}", _config);
        _config.setStatus(STATE.UP, "Connected Successfully");
    }

    @Override
    public void disconnect() {
        try {
            if (_serialPort.closePort()) {
                _logger.debug("_serialPort{} closed", _serialPort.getDescriptivePortName());
            } else {
                _logger.warn("Failed to close _serialPort{}", _serialPort.getDescriptivePortName());
            }
            _config.setStatus(STATE.DOWN, "Stopped.");
        } catch (Exception ex) {
            _config.setStatus(STATE.DOWN, "ERROR:" + ex.getMessage());
        }
    }

    @Override
    public void write(IMessage message) {
        try {
            byte[] data = _parser.getGatewayData(message);
            _serialPort.writeBytes(data, data.length);
        } catch (Exception ex) {
            _config.setStatus(STATE.DOWN, "ERROR: " + ex.getMessage());
            _logger.error("Exception while writing data, ", ex);
        }
    }

}

@Slf4j
class SerialDataListenerjSerialComm implements SerialPortDataListener {
    private SerialPort _serialPort;
    private GatewayConfigSerial _config = null;
    private StringBuilder rawMessage = new StringBuilder();
    private boolean failedStatusWritten = false;
    private IMessageParser<byte[]> _parser;
    private IQueue<IMessage> _queue;

    public SerialDataListenerjSerialComm(SerialPort _serialPort, GatewayConfigSerial _config,
            IMessageParser<byte[]> _parser, IQueue<IMessage> _queue) {
        this._serialPort = _serialPort;
        this._config = _config;
        this._queue = _queue;
        this._parser = _parser;
    }

    @Override
    public int getListeningEvents() {
        return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
            return;
        }
        try {
            byte[] buffer = new byte[_serialPort.bytesAvailable()];
            _serialPort.readBytes(buffer, buffer.length);
            for (byte b : buffer) {
                if ((b == ISerialDriver.MESSAGE_SPLITTER) && rawMessage.length() > 0) {
                    String toProcess = rawMessage.toString();
                    _logger.debug("Received a rawMessage:[{}]", toProcess);

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
            failedStatusWritten = false;
        } catch (Exception ex) {
            if (ex.getMessage() != null) {
                _logger.error("Exception, ", ex);
            }
            if (!failedStatusWritten) {
                _config.setStatus(STATE.DOWN, "ERROR: " + ex.getMessage());
                failedStatusWritten = true;
                _logger.error("Exception, ", ex);
            }
            rawMessage.setLength(0);
            try {
                //If serial port removed in between throws 'java.lang.NegativeArraySizeException: null' continuously
                //This continuous exception eats CPU heavily, to reduce CPU usage on this state added Thread.sleep
                Thread.sleep(McUtils.TEN_MILLISECONDS);
            } catch (InterruptedException tE) {
                _logger.error("Exception,", tE);
            }
        }

    }
}
