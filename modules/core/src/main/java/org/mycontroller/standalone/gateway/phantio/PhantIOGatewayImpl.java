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
package org.mycontroller.standalone.gateway.phantio;

import org.mycontroller.standalone.AppProperties.STATE;
import org.mycontroller.standalone.db.tables.GatewayTable;
import org.mycontroller.standalone.gateway.GatewayException;
import org.mycontroller.standalone.gateway.GatewayUtils;
import org.mycontroller.standalone.gateway.IGateway;
import org.mycontroller.standalone.gateway.model.GatewayPhantIO;
import org.mycontroller.standalone.message.RawMessage;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Slf4j
public class PhantIOGatewayImpl implements IGateway {
    private PhantIOGatewayPoller phantIOGatewayPoller = null;

    public PhantIOGatewayImpl(GatewayTable gatewayTable) {
        GatewayPhantIO gateway = (GatewayPhantIO) GatewayUtils.getGateway(gatewayTable);
        try {
            phantIOGatewayPoller = new PhantIOGatewayPoller(gateway);
            new Thread(phantIOGatewayPoller).start();
            _logger.info("Connected successfully with phantIOGatewayPoller[{}, PublicKey:{}]",
                    gateway.getUrl(), gateway.getPublicKey());
            gateway.setStatus(STATE.UP, "Connected Successfully");
        } catch (Exception ex) {
            _logger.error("Exception, ", ex);
            gateway.setStatus(STATE.DOWN, "ERROR: " + ex.getMessage());
        }
    }

    @Override
    public synchronized void close() {
        phantIOGatewayPoller.setTerminate(true);
    }

    @Override
    public synchronized void write(RawMessage rawMessage) throws GatewayException {
        phantIOGatewayPoller.write(rawMessage);
    }

    @Override
    public GatewayPhantIO getGateway() {
        return phantIOGatewayPoller.getGateway();
    }

}
