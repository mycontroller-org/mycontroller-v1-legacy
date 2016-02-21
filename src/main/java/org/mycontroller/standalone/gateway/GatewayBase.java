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

import org.mycontroller.standalone.AppProperties.NETWORK_TYPE;
import org.mycontroller.standalone.AppProperties.STATE;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.Gateway;
import org.mycontroller.standalone.gateway.GatewayUtils.TYPE;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
public class GatewayBase {
    private Gateway gateway = null;

    public GatewayBase(Gateway gateway) {
        this.gateway = gateway;
    }

    public void updateGateway() {
        DaoUtils.getGatewayDao().update(this.gateway);
    }

    public Gateway getGateway() {
        return gateway;
    }

    public Integer getId() {
        return gateway.getId();
    }

    public void setId(Integer id) {
        gateway.setId(id);
    }

    public Boolean getEnabled() {
        return gateway.getEnabled();
    }

    public void setEnabled(Boolean enabled) {
        gateway.setEnabled(enabled);
    }

    public String getName() {
        return gateway.getName();
    }

    public void setName(String name) {
        gateway.setName(name);
    }

    public TYPE getType() {
        return gateway.getType();
    }

    public void setType(TYPE type) {
        gateway.setType(type);
    }

    public NETWORK_TYPE getNetworkType() {
        return gateway.getNetworkType();
    }

    public void setNetworkType(NETWORK_TYPE subType) {
        gateway.setNetworkType(subType);
    }

    public Long getTimestamp() {
        return gateway.getTimestamp();
    }

    public void setTimestamp(Long timestamp) {
        gateway.setTimestamp(timestamp);
    }

    public String getVariable1() {
        return gateway.getVariable1();
    }

    public void setVariable1(String variable1) {
        gateway.setVariable1(variable1);
        ;
    }

    public String getVariable2() {
        return gateway.getVariable2();
    }

    public void setVariable2(String variable2) {
        gateway.setVariable2(variable2);
    }

    public String getVariable3() {
        return gateway.getVariable3();
    }

    public void setVariable3(String variable3) {
        gateway.setVariable3(variable3);
    }

    public String getVariable4() {
        return gateway.getVariable4();
    }

    public void setVariable4(String variable4) {
        gateway.setVariable4(variable4);
    }

    public String getVariable5() {
        return gateway.getVariable5();
    }

    public void setVariable5(String variable5) {
        gateway.setVariable5(variable5);
    }

    public String getVariable6() {
        return gateway.getVariable6();
    }

    public void setVariable6(String variable6) {
        gateway.setVariable6(variable6);
    }

    public String getVariable7() {
        return gateway.getVariable7();
    }

    public void setVariable7(String variable7) {
        gateway.setVariable7(variable7);
    }

    public String getVariable8() {
        return gateway.getVariable8();
    }

    public void setVariable8(String variable8) {
        gateway.setVariable8(variable8);
    }

    public String getVariable9() {
        return gateway.getVariable9();
    }

    public void setVariable9(String variable9) {
        gateway.setVariable9(variable9);
    }

    public String getVariable10() {
        return gateway.getVariable10();
    }

    public void setVariable10(String variable10) {
        gateway.setVariable10(variable10);
    }

    public String getStatusMessage() {
        return gateway.getStatusMessage();
    }

    public void setStatusMessage(String connectionStatus) {
        gateway.setStatusMessage(connectionStatus);
    }

    public Long getStatusSince() {
        return gateway.getStatusSince();
    }

    public void setStatusSince(Long statusSince) {
        gateway.setStatusSince(statusSince);
    }

    public void setStatus(STATE state) {
        gateway.setState(state);
    }

    public STATE getStatus() {
        return gateway.getState();
    }

    public void setStatus(STATE state, String message) {
        gateway.setState(state);
        gateway.setStatusMessage(message);
    }

    public String getConnectionDetails() {
        return gateway.getConnectionDetails();
    }

    public String toString() {
        return gateway.toString();
    }

}
