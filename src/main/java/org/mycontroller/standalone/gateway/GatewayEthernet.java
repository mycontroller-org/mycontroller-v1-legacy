/**
 * Copyright (C) 2015-2016 Jeeva Kandasamy (jkandasa@gmail.com)
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
package org.mycontroller.standalone.gateway;

import org.mycontroller.standalone.MycUtils;
import org.mycontroller.standalone.db.tables.Gateway;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
public class GatewayEthernet extends GatewayBase {

    public GatewayEthernet(Gateway gateway) {
        super(gateway);
    }

    public GatewayEthernet() {
        super(new Gateway());
    }

    public String getHost() {
        return super.getVariable1();
    }

    public void setHost(String host) {
        super.setVariable1(host);
    }

    public Integer getPort() {
        return MycUtils.getInteger(super.getVariable2());
    }

    public void setPort(Integer port) {
        super.setVariable2(String.valueOf(port));
    }

    public Integer getAliveFrequency() {
        return MycUtils.getInteger(super.getVariable3());
    }

    public void setAliveFrequency(Integer aliveFrequency) {
        super.setVariable3(String.valueOf(aliveFrequency));
    }

}
