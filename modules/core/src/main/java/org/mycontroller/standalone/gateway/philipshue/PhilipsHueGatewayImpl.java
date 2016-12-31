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
package org.mycontroller.standalone.gateway.philipshue;

import org.mycontroller.standalone.AppProperties.STATE;
import org.mycontroller.standalone.db.tables.GatewayTable;
import org.mycontroller.standalone.gateway.GatewayException;
import org.mycontroller.standalone.gateway.GatewayUtils;
import org.mycontroller.standalone.gateway.IGateway;
import org.mycontroller.standalone.gateway.model.GatewayPhilipsHue;
import org.mycontroller.standalone.message.RawMessage;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Fraid(https://github.com/Fraid)
 */
@Slf4j
public class PhilipsHueGatewayImpl implements IGateway {

    public PhilipsHueGatewayImpl() {
    }

    private PhilipsHueGatewayPoller philipsHueGatewayPoller = null;

    public PhilipsHueGatewayImpl(GatewayTable gatewayTable) {
        GatewayPhilipsHue gateway = (GatewayPhilipsHue) GatewayUtils.getGateway(gatewayTable);
        try {
            philipsHueGatewayPoller = new PhilipsHueGatewayPoller(gateway);
            new Thread(philipsHueGatewayPoller).start();
            _logger.info("Connected successfully with philipsHueGatewayPoller[{}, PublicKey:{}]",
                    gateway.getUrl(), gateway.getAuthorizedUser());
            gateway.setStatus(STATE.UP, "Connected Successfully");
        } catch (Exception ex) {
            _logger.error("Exception, ", ex);
            gateway.setStatus(STATE.DOWN, "ERROR: " + ex.getMessage());
        }
    }

    @Override
    public synchronized void close() {
        philipsHueGatewayPoller.setTerminate(true);
    }

    @Override
    public synchronized void write(RawMessage rawMessage) throws GatewayException {
        philipsHueGatewayPoller.write(rawMessage);
    }

    @Override
    public GatewayPhilipsHue getGateway() {
        return philipsHueGatewayPoller.getGateway();
    }

}
