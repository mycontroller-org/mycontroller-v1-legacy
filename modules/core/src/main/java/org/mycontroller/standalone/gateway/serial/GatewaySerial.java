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

import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.AppProperties.STATE;
import org.mycontroller.standalone.db.tables.GatewayTable;
import org.mycontroller.standalone.exceptions.MessageParserException;
import org.mycontroller.standalone.gateway.GatewayAbstract;
import org.mycontroller.standalone.gateway.GatewayUtils;
import org.mycontroller.standalone.gateway.GatewayUtils.SERIAL_PORT_DRIVER;
import org.mycontroller.standalone.gateway.config.GatewayConfigSerial;
import org.mycontroller.standalone.message.IMessage;
import org.mycontroller.standalone.offheap.IQueue;
import org.mycontroller.standalone.provider.IMessageParser;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.2.0
 */
@Slf4j
public class GatewaySerial extends GatewayAbstract {
    private GatewayConfigSerial _config;
    private ISerialDriver _driver;

    public GatewaySerial(GatewayTable gatewayTable, IMessageParser<byte[]> parser, IQueue<IMessage> queue) {
        super(new GatewayConfigSerial(gatewayTable));
        _config = (GatewayConfigSerial) config();
        // - Start Serial port
        _config.setRunningDriver(_config.getDriver());
        if (_config.getRunningDriver() == SERIAL_PORT_DRIVER.AUTO) {
            if (AppProperties.getOsArch().startsWith(GatewayUtils.OS_ARCH_ARM)) {
                //Refer https://github.com/mycontroller-org/mycontroller/issues/299
                //gateway.setRunningDriver(SERIAL_PORT_DRIVER.PI4J);
                _config.setRunningDriver(SERIAL_PORT_DRIVER.JSERIALCOMM);
            } else {
                _config.setRunningDriver(SERIAL_PORT_DRIVER.JSERIALCOMM);
            }
        }
        // Open Serial Port
        switch (_config.getRunningDriver()) {
            case JSERIALCOMM:
                _driver = new SerialDriverJSerialComm(_config, parser, queue);
                break;
            case JSSC:
                _driver = new SerialDriverJssc(_config, parser, queue);
                break;
            case PI4J:
                _driver = new SerialDriverPI4J(_config, parser, queue);
                break;
            default:
                _config.setStatus(STATE.DOWN, "Unknown serial port _driver...["
                        + _config.getRunningDriver() + "]");
                _logger.warn("Unknown serial port _driver[{}], nothing to do..Gateway[{}]",
                        _config.getRunningDriver(), _config);
                throw new RuntimeException("Unkown serial port _driver["
                        + _config.getRunningDriver() + "] specified");
        }
    }

    @Override
    public void write(IMessage message) throws MessageParserException {
        _driver.write(message);

    }

    @Override
    public void connect() {
        _driver.connect();
    }

    @Override
    public void disconnect() {
        _driver.disconnect();

    }

    @Override
    public void reconnect() {
        _driver.disconnect();
        _driver.connect();

    }

    @Override
    public boolean isUp() {
        return _config.getState() == STATE.UP;
    }

}
