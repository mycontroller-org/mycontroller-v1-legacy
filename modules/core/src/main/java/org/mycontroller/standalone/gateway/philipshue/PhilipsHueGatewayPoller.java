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

import org.mycontroller.standalone.gateway.model.GatewayPhilipsHue;
import org.mycontroller.standalone.message.RawMessage;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Fraid(https://github.com/Fraid)
 */
@Slf4j
public class PhilipsHueGatewayPoller implements Runnable {
    private boolean terminate = false;
    private boolean terminated = false;

    public PhilipsHueGatewayPoller() {
    }

    private GatewayPhilipsHue gateway = null;

    public PhilipsHueGatewayPoller(GatewayPhilipsHue gateway) throws Exception {
        this.gateway = gateway;
        //TODO I guess client init here...
    }

    @Override
    public void run() {
    }

    public void write(RawMessage rawMessage) {
        if (gateway.getAuthorizedUser() != null && gateway.getAuthorizedUser().length() > 0) {
            _logger.debug("Send data: {}, {}", this.gateway, rawMessage);
            //TODO Call Rest Client here
        } else {
            _logger.warn("Private key not set for this {}", gateway);
        }
    }

    public GatewayPhilipsHue getGateway() {
        return gateway;
    }

    public void close() {

    }

}
