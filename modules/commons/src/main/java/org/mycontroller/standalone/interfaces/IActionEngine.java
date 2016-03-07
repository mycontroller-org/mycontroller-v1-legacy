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
package org.mycontroller.standalone.interfaces;

import org.mycontroller.standalone.db.PayloadOperation;
import org.mycontroller.standalone.db.tables.AlarmDefinition;
import org.mycontroller.standalone.db.tables.ForwardPayload;
import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.db.tables.Timer;
import org.mycontroller.standalone.gateway.GatewayEthernet;
import org.mycontroller.standalone.model.ResourceModel;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
public interface IActionEngine {
    void executeAlarm(AlarmDefinition alarmDefinition);

    void executeTimer(ResourceModel resourceModel, Timer timer);

    void executeSendPayload(ResourceModel resourceModel, PayloadOperation operation);

    void sendAliveStatusRequest(Node node);

    boolean checkEthernetGatewayAliveState(GatewayEthernet gatewayEthernet);

    void executeForwardPayload(ForwardPayload forwardPayload, String payload);

    void rebootNode(Node node);

    void uploadFirmware(Node node);

    void discover(Integer gateway);

    void sendPayload(SensorVariable sensorVariable);

    void addNode(Node node);

    void updateNode(Node node);

    void addSensor(Sensor sensor);

    void updateSensor(Sensor sensor);

    void eraseConfiguration(Node node);

}
