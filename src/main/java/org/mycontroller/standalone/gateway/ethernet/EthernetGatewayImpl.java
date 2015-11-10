/**
 * Copyright (C) 2015 Jeeva Kandasamy (jkandasa@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mycontroller.standalone.gateway.ethernet;

import org.mycontroller.standalone.api.jaxrs.mapper.GatewayInfo;
import org.mycontroller.standalone.gateway.IMySensorsGateway;
import org.mycontroller.standalone.gateway.MySensorsGatewayException;
import org.mycontroller.standalone.mysensors.RawMessage;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
public class EthernetGatewayImpl implements IMySensorsGateway {
    private EthernetGatewayActionThread monitoringThread = null;

    public EthernetGatewayImpl() {
        if (monitoringThread == null) {
            monitoringThread = new EthernetGatewayActionThread();
            new Thread(monitoringThread).start();
        }
    }

    @Override
    public synchronized void close() {
        monitoringThread.close();
    }

    @Override
    public synchronized void write(RawMessage rawMessage) throws MySensorsGatewayException {
        monitoringThread.write(rawMessage);
    }

    @Override
    public GatewayInfo getGatewayInfo() {
        if (monitoringThread != null) {
            return monitoringThread.getGatewayInfo();
        }
        return null;
    }

}
