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
package org.mycontroller.standalone.api.jaxrs.model;

import java.util.HashMap;
import java.util.List;

import org.mycontroller.standalone.db.DaoUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(includeFieldNames = true)
/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
public class AllowedResources {
    public static final String KEY_ALLOWED_RESOURCES = "allowedResources";
    public static final String KEY_ALLOWED_RESOURCE_TYPE = "allowedResourceType";
    public static final String KEY_RESOURCE_TYPE = "resourceType";
    public static final String KEY_RESOURCE_ID = "resourceId";

    @NonNull
    private Integer userId;
    private List<Integer> gatewayIds;
    private List<Integer> nodeIds;
    private List<Integer> sensorIds;
    private List<Integer> sensorVariableIds;
    private List<String> mqttReadTopics;
    private List<String> mqttWriteTopics;

    public List<Integer> getGatewayIds() {
        if (gatewayIds == null || gatewayIds.size() == 0) {
            gatewayIds = DaoUtils.getRoleDao().getGatewayIds(userId);
            if (gatewayIds.isEmpty()) {
                gatewayIds.add(-1);
            }
        }
        return gatewayIds;
    }

    public List<Integer> getNodeIds() {
        if (nodeIds == null || nodeIds.size() == 0) {
            nodeIds = DaoUtils.getRoleDao().getNodeIds(userId);
            if (nodeIds.isEmpty()) {
                nodeIds.add(-1);
            }
        }
        return nodeIds;
    }

    public List<Integer> getSensorIds() {
        if (sensorIds == null || sensorIds.size() == 0) {
            sensorIds = DaoUtils.getRoleDao().getSensorIds(userId);
            if (sensorIds.isEmpty()) {
                sensorIds.add(-1);
            }
        }
        return sensorIds;
    }

    public List<Integer> getSensorVariableIds() {
        if (sensorVariableIds == null || sensorVariableIds.size() == 0) {
            sensorVariableIds = DaoUtils.getRoleDao().getSensorVariableIds(userId);
            if (sensorVariableIds.isEmpty()) {
                sensorVariableIds.add(-1);
            }
        }
        return sensorVariableIds;
    }

    private void updateMqttTopics() {
        if (mqttReadTopics == null || mqttReadTopics.size() == 0
                || mqttWriteTopics == null || mqttWriteTopics.size() == 0) {
            HashMap<String, List<String>> topics = DaoUtils.getRoleDao().getMqttTopics(userId);
            mqttReadTopics = topics.get("subscribe");
            mqttWriteTopics = topics.get("publish");
        }
    }

    public List<String> getMqttReadTopics() {
        updateMqttTopics();
        return mqttReadTopics;
    }

    public List<String> getMqttWriteTopics() {
        updateMqttTopics();
        return mqttWriteTopics;
    }

}
