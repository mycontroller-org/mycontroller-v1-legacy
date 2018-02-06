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
package org.mycontroller.standalone.message;

import java.util.List;

import org.mycontroller.standalone.db.ResourceOperation;
import org.mycontroller.standalone.db.tables.ForwardPayload;
import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.model.ResourceModel;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
public interface IMcActionEngine {

    void executeSendPayload(ResourceModel resourceModel, ResourceOperation operation);

    void executeRequestPayload(ResourceModel resourceModel);

    String sendAliveStatusRequest(Node node);

    void executeForwardPayload(ForwardPayload forwardPayload, String payload);

    void rebootNode(Node node);

    void uploadFirmware(Node node);

    void discover(Integer gateway);

    void updateNodeInformations(Integer gatewayId, List<Integer> nodeIds);

    String sendPayload(SensorVariable sensorVariable);

    void addNode(Node node);

    void updateNode(Node node);

    void addSensor(Sensor sensor);

    void updateSensor(Sensor sensor);

    String eraseConfiguration(Node node);

}
