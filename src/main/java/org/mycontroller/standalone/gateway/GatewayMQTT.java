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

import org.mycontroller.standalone.db.tables.Gateway;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
public class GatewayMQTT extends GatewayBase {

    public GatewayMQTT() {
        super(new Gateway());
    }

    public GatewayMQTT(Gateway gateway) {
        super(gateway);
    }

    public String getBrokerHost() {
        return super.getVariable1();
    }

    public void setBrokerHost(String brokerHost) {
        super.setVariable1(brokerHost);
    }

    public String getClientId() {
        return super.getVariable2();
    }

    public void setClientId(String clientId) {
        super.setVariable2(clientId);
    }

    public String getTopicPublish() {
        return super.getVariable3();
    }

    public void setTopicPublish(String topicPublish) {
        super.setVariable3(topicPublish);
    }

    public String getTopicSubscribe() {
        return super.getVariable4();
    }

    public void setTopicSubscribe(String topicSubscribe) {
        super.setVariable4(topicSubscribe);
    }

    public String getUserName() {
        return super.getVariable5();
    }

    public void setUserName(String userName) {
        super.setVariable5(userName);
    }

    public String getPassword() {
        return super.getVariable6();
    }

    public void setPassword(String password) {
        super.setVariable6(password);
    }

}
