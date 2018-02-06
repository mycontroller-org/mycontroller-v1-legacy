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
package org.mycontroller.standalone.model;

import org.mycontroller.standalone.AppProperties.NETWORK_TYPE;
import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.McObjectManager;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.ForwardPayload;
import org.mycontroller.standalone.db.tables.GatewayTable;
import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.db.tables.ResourcesGroup;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.db.tables.Timer;
import org.mycontroller.standalone.rule.RuleUtils;
import org.mycontroller.standalone.rule.model.RuleDefinitionAbstract;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
public class ResourceModel {
    public static final String DISPLAY_KEY_GATEWAY = "[G]:";
    public static final String DISPLAY_KEY_NODE = "[N]:";
    public static final String DISPLAY_KEY_SENSOR = "[S]:";
    public static final String DISPLAY_KEY_SENSOR_VARIABLE = "[SV]:";
    public static final String DISPLAY_KEY_RESOURCES_GROUP = "[RG]:";
    public static final String DISPLAY_KEY_RULE_DIFINITION = "[RD]:";
    public static final String DISPLAY_KEY_TIMER = "[T]:";

    private RESOURCE_TYPE resourceType;
    private Integer resourceId;
    private NETWORK_TYPE networkType = null;
    private Object resource;

    public ResourceModel(RESOURCE_TYPE resourceType, Integer resourceId) {
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        switch (this.resourceType) {
            case GATEWAY:
                resource = DaoUtils.getGatewayDao().getById(this.resourceId);
                isValidResource();
                networkType = ((GatewayTable) resource).getNetworkType();
                break;
            case NODE:
                resource = DaoUtils.getNodeDao().getById(this.resourceId);
                isValidResource();
                networkType = ((Node) resource).getGatewayTable().getNetworkType();
                break;
            case SENSOR:
                resource = DaoUtils.getSensorDao().getById(this.resourceId);
                isValidResource();
                networkType = ((Sensor) resource).getNode().getGatewayTable().getNetworkType();
                break;
            case SENSOR_VARIABLE:
                resource = DaoUtils.getSensorVariableDao().get(this.resourceId);
                isValidResource();
                networkType = ((SensorVariable) resource).getSensor().getNode().getGatewayTable().getNetworkType();
                break;
            case RESOURCES_GROUP:
                resource = DaoUtils.getResourcesGroupDao().get(this.resourceId);
                isValidResource();
                break;
            case RULE_DEFINITION:
                resource = RuleUtils.getRuleDefinition(DaoUtils.getRuleDefinitionDao().getById(resourceId));
                isValidResource();
                break;
            case TIMER:
                resource = DaoUtils.getTimerDao().getById(resourceId);
                isValidResource();
                break;
            case FORWARD_PAYLOAD:
                resource = DaoUtils.getForwardPayloadDao().getById(resourceId);
                isValidResource();
                break;
            default:
                throw new RuntimeException("Not supported ResourceType:" + resourceType);
        }

    }

    private void isValidResource() {
        if (resource == null) {
            throw new RuntimeException("Resource not available! ResourceType:[" + resourceType.getText()
                    + "], ResourceId:[" + resourceId + "]");
        }
    }

    public ResourceModel(RESOURCE_TYPE resourceType, Object resource) {
        this.resourceType = resourceType;
        this.resource = resource;
        isValidResource();
        switch (this.resourceType) {
            case GATEWAY:
                networkType = ((GatewayTable) resource).getNetworkType();
                break;
            case NODE:
                networkType = ((Node) resource).getGatewayTable().getNetworkType();
                break;
            case SENSOR:
                networkType = ((Sensor) resource).getNode().getGatewayTable().getNetworkType();
                break;
            case SENSOR_VARIABLE:
                networkType = ((SensorVariable) resource).getSensor().getNode().getGatewayTable().getNetworkType();
                break;
            case RESOURCES_GROUP:
            case RULE_DEFINITION:
            case TIMER:
            case FORWARD_PAYLOAD:
                networkType = null;
                break;
            default:
                throw new RuntimeException("Not supported ResourceType:" + resourceType);
        }
    }

    public NETWORK_TYPE getNetworkType() {
        return networkType;
    }

    public void setNetworkType(NETWORK_TYPE networkType) {
        this.networkType = networkType;
    }

    public Object getResource() {
        return resource;
    }

    public RESOURCE_TYPE getResourceType() {
        return resourceType;
    }

    public Integer getResourceId() {
        return resourceId;
    }

    public String getResourceDetails() {
        isValidResource();
        StringBuilder builder = new StringBuilder();
        switch (this.resourceType) {
            case GATEWAY:
                updateDGateway((GatewayTable) this.resource, builder);
                break;
            case NODE:
                updateDNode((Node) this.resource, builder);
                break;
            case SENSOR:
                updateDSensor((Sensor) this.resource, builder);
                break;
            case SENSOR_VARIABLE:
                updateDSensorVariable((SensorVariable) this.resource, builder);
                break;
            case RESOURCES_GROUP:
                ResourcesGroup resourcesGroup = (ResourcesGroup) this.resource;
                builder.append("Resources group:[Name:").append(resourcesGroup.getName())
                        .append(", State:").append(resourcesGroup.getState().getText()).append("]");
                break;
            case RULE_DEFINITION:
                RuleDefinitionAbstract ruleDefinition = (RuleDefinitionAbstract) this.resource;
                builder.append("Rule definition:[Name:").append(ruleDefinition.getName()).append("]");
                break;
            case TIMER:
                Timer timer = (Timer) this.resource;
                builder.append("Timer:[Name:").append(timer.getName()).append("]");
                break;
            case FORWARD_PAYLOAD:
                ForwardPayload forwardPayload = (ForwardPayload) this.resource;
                updateDSensorVariable((SensorVariable) forwardPayload.getSource(), builder);
                builder.append(" >>> ");
                updateDSensorVariable((SensorVariable) forwardPayload.getDestination(), builder);
            default:
                break;
        }
        return builder.toString();
    }

    private void updateDGateway(GatewayTable gatewayTable, StringBuilder builder) {
        builder.append("Type:").append(this.networkType.getText())
                .append(", Id:").append(gatewayTable.getId())
                .append(", Name:").append(gatewayTable.getName());
    }

    private void updateDNode(Node node, StringBuilder builder) {
        builder.append("Type:").append(this.networkType.getText())
                .append(", Gateway:").append(node.getGatewayTable().getName())
                .append(", NodeEui:").append(node.getEui())
                .append(", Name:").append(node.getName());
    }

    private void updateDSensor(Sensor sensor, StringBuilder builder) {
        builder.append("Type:").append(this.networkType.getText())
                .append(", Gateway:").append(sensor.getNode().getGatewayTable().getName())
                .append(", NodeEui:").append(sensor.getNode().getEui())
                .append(", SensorId:").append(sensor.getSensorId())
                .append(", Name:").append(sensor.getName());
    }

    private void updateDSensorVariable(SensorVariable sensorVariable, StringBuilder builder) {
        builder.append("Type:").append(this.networkType.getText())
                .append(", Gateway:").append(sensorVariable.getSensor().getNode().getGatewayTable().getName())
                .append(", NodeEui:").append(sensorVariable.getSensor().getNode().getEui())
                .append(", SensorId:").append(sensorVariable.getSensor().getSensorId())
                .append(", VariableType:")
                .append(McObjectManager.getMcLocale().getString(sensorVariable.getVariableType().name()));
        if (sensorVariable.getName() != null) {
            builder.append(" (").append(sensorVariable.getName()).append(")");
        }
    }

    public String getResourceLessDetails() {
        isValidResource();
        StringBuilder builder = new StringBuilder();
        switch (this.resourceType) {
            case GATEWAY:
                updateLDGateway((GatewayTable) this.resource, builder);
                break;
            case NODE:
                updateLDNode((Node) this.resource, builder);
                break;
            case SENSOR:
                updateLDSensor((Sensor) this.resource, builder);
                break;
            case SENSOR_VARIABLE:
                updateLDSensorVariable((SensorVariable) this.resource, builder);
                break;
            case RESOURCES_GROUP:
                ResourcesGroup resourcesGroup = (ResourcesGroup) this.resource;
                builder.append(DISPLAY_KEY_RESOURCES_GROUP).append(resourcesGroup.getName());
                break;
            case RULE_DEFINITION:
                RuleDefinitionAbstract ruleDefinition = (RuleDefinitionAbstract) this.resource;
                builder.append(DISPLAY_KEY_RULE_DIFINITION).append(ruleDefinition.getName());
                break;
            case TIMER:
                Timer timer = (Timer) this.resource;
                builder.append(DISPLAY_KEY_TIMER).append(timer.getName());
                break;
            case FORWARD_PAYLOAD:
                ForwardPayload forwardPayload = (ForwardPayload) this.resource;
                updateLDSensorVariable((SensorVariable) forwardPayload.getSource(), builder);
                builder.append(" >>> ");
                updateLDSensorVariable((SensorVariable) forwardPayload.getDestination(), builder);
                break;
            default:
                break;
        }
        return builder.toString();
    }

    private void updateLDGateway(GatewayTable gatewayTable, StringBuilder builder) {
        builder.append(DISPLAY_KEY_GATEWAY).append(gatewayTable.getName());
    }

    private void updateLDNode(Node node, StringBuilder builder) {
        updateLDGateway(node.getGatewayTable(), builder);
        builder.append(" >> ").append(DISPLAY_KEY_NODE).append(node.getEui());
        if (node.getName() != null && node.getName().length() > 0) {
            builder.append(":").append(node.getName());
        }
    }

    private void updateLDSensor(Sensor sensor, StringBuilder builder) {
        updateLDNode(sensor.getNode(), builder);
        builder.append(" >> ").append(DISPLAY_KEY_SENSOR).append(sensor.getSensorId());
        if (sensor.getName() != null && sensor.getName().length() > 0) {
            builder.append(":").append(sensor.getName());
        } else if (sensor.getType() != null) {
            builder.append(":").append(McObjectManager.getMcLocale().getString(sensor.getType().name()));
        }
    }

    private void updateLDSensorVariable(SensorVariable sensorVariable, StringBuilder builder) {
        updateLDSensor(sensorVariable.getSensor(), builder);
        builder.append(" >> ").append(DISPLAY_KEY_SENSOR_VARIABLE)
                .append(McObjectManager.getMcLocale().getString(sensorVariable.getVariableType().name()));
        if (sensorVariable.getName() != null) {
            builder.append(" (").append(sensorVariable.getName()).append(")");
        }
    }
}
