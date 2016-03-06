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
package org.mycontroller.standalone.gateway.ethernet;

import org.mycontroller.standalone.db.tables.Gateway;
import org.mycontroller.standalone.gateway.GatewayEthernet;
import org.mycontroller.standalone.gateway.IGateway;
import org.mycontroller.standalone.gateway.GatewayException;
import org.mycontroller.standalone.message.RawMessage;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
public class EthernetGatewayImpl implements IGateway {
    private EthernetGatewayActionThread monitoringThread = null;

    public EthernetGatewayImpl(Gateway gateway) {
        if (monitoringThread == null) {
            monitoringThread = new EthernetGatewayActionThread(new GatewayEthernet(gateway));
            new Thread(monitoringThread).start();
        }
    }

    @Override
    public synchronized void close() {
        monitoringThread.close();
    }

    @Override
    public synchronized void write(RawMessage rawMessage) throws GatewayException {
        monitoringThread.write(rawMessage);
    }

    @Override
    public GatewayEthernet getGateway() {
        if (monitoringThread != null) {
            return monitoringThread.getGateway();
        }
        return null;
    }

}
