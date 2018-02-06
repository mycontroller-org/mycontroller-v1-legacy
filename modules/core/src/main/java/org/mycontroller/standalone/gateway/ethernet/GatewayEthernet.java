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

import org.mycontroller.standalone.AppProperties.STATE;
import org.mycontroller.standalone.db.tables.GatewayTable;
import org.mycontroller.standalone.exceptions.MessageParserException;
import org.mycontroller.standalone.gateway.GatewayAbstract;
import org.mycontroller.standalone.gateway.config.GatewayConfigEthernet;
import org.mycontroller.standalone.message.IMessage;
import org.mycontroller.standalone.offheap.IQueue;
import org.mycontroller.standalone.provider.IMessageParser;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.2.0
 */
public class GatewayEthernet extends GatewayAbstract {
    private GatewayConfigEthernet _config;
    private EthernetDriver _driver;

    public GatewayEthernet(GatewayTable gatewayTable, IMessageParser<byte[]> messageParser, IQueue<IMessage> queue) {
        super(new GatewayConfigEthernet(gatewayTable));
        _config = (GatewayConfigEthernet) config();
        _driver = new EthernetDriver(_config, messageParser, queue);
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
        disconnect();
        connect();
    }

    @Override
    public boolean isUp() {
        return _config.getState() == STATE.UP;
    }

}
