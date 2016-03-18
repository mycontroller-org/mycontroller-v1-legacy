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
package org.mycontroller.standalone.model;

import org.mycontroller.standalone.ObjectManager;
import org.mycontroller.standalone.AppProperties.NETWORK_TYPE;
import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.AlarmDefinition;
import org.mycontroller.standalone.db.tables.Gateway;
import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.db.tables.ResourcesGroup;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.db.tables.Timer;

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
    public static final String DISPLAY_KEY_ALARM_DIFINITION = "[AD]:";
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
                networkType = ((Gateway) resource).getNetworkType();
                break;
            case NODE:
                resource = DaoUtils.getNodeDao().getById(this.resourceId);
                networkType = ((Node) resource).getGateway().getNetworkType();
                break;
            case SENSOR:
                resource = DaoUtils.getSensorDao().getById(this.resourceId);
                networkType = ((Sensor) resource).getNode().getGateway().getNetworkType();
                break;
            case SENSOR_VARIABLE:
                resource = DaoUtils.getSensorVariableDao().get(this.resourceId);
                networkType = ((SensorVariable) resource).getSensor().getNode().getGateway().getNetworkType();
                break;
            case RESOURCES_GROUP:
                resource = DaoUtils.getResourcesGroupDao().get(this.resourceId);
                break;
            case ALARM_DEFINITION:
                resource = DaoUtils.getAlarmDefinitionDao().getById(resourceId);
                break;
            case TIMER:
                resource = DaoUtils.getTimerDao().getById(resourceId);
                break;
            default:
                throw new RuntimeException("Not supported KEY_RESOURCE_TYPE:" + resourceType);
        }
        if (resource == null) {
            throw new RuntimeException("RESOURCE not available! KEY_RESOURCE_TYPE:" + resourceType + ", ResourceId:"
                    + resourceId);
        }
    }

    public ResourceModel(RESOURCE_TYPE resourceType, Object resource) {
        this.resourceType = resourceType;
        this.resource = resource;
        switch (this.resourceType) {
            case GATEWAY:
                networkType = ((Gateway) resource).getNetworkType();
                break;
            case NODE:
                networkType = ((Node) resource).getGateway().getNetworkType();
                break;
            case SENSOR:
                networkType = ((Sensor) resource).getNode().getGateway().getNetworkType();
                break;
            case SENSOR_VARIABLE:
                networkType = ((SensorVariable) resource).getSensor().getNode().getGateway().getNetworkType();
                break;
            case RESOURCES_GROUP:
            case ALARM_DEFINITION:
            case TIMER:
                networkType = null;
                break;
            default:
                throw new RuntimeException("Not supported KEY_RESOURCE_TYPE:" + resourceType);
        }
        if (resource == null) {
            throw new RuntimeException("RESOURCE not available! KEY_RESOURCE_TYPE:" + resourceType + ", ResourceId:"
                    + resourceId);
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
        if (this.resource == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();

        switch (this.resourceType) {
            case GATEWAY:
                Gateway gateway = (Gateway) this.resource;
                builder.append("Type:").append(this.networkType.getText())
                        .append(", Id:").append(gateway.getId())
                        .append(", Name:").append(gateway.getName());
                break;
            case NODE:
                Node node = (Node) this.resource;
                builder.append("Type:").append(this.networkType.getText())
                        .append(", Gateway:").append(node.getGateway().getName())
                        .append(", NodeEui:").append(node.getEui())
                        .append(", Name:").append(node.getName());
                break;
            case SENSOR:
                Sensor sensor = (Sensor) this.resource;
                builder.append("Type:").append(this.networkType.getText())
                        .append(", Gateway:").append(sensor.getNode().getGateway().getName())
                        .append(", NodeEui:").append(sensor.getNode().getEui())
                        .append(", SensorId:").append(sensor.getSensorId())
                        .append(", Name:").append(sensor.getName());
                break;
            case SENSOR_VARIABLE:
                SensorVariable sensorVariable = (SensorVariable) this.resource;
                builder.append("Type:").append(this.networkType.getText())
                        .append(", Gateway:").append(sensorVariable.getSensor().getNode().getGateway().getName())
                        .append(", NodeEui:").append(sensorVariable.getSensor().getNode().getEui())
                        .append(", SensorId:").append(sensorVariable.getSensor().getSensorId())
                        .append(", VariableType:")
                        .append(ObjectManager.getMcLocale().getString(sensorVariable.getVariableType().name()));
                break;
            case RESOURCES_GROUP:
                ResourcesGroup resourcesGroup = (ResourcesGroup) this.resource;
                builder.append("Resources group:[Name:").append(resourcesGroup.getName())
                        .append(", State:").append(resourcesGroup.getState().getText()).append("]");
                break;
            case ALARM_DEFINITION:
                AlarmDefinition alarmDefinition = (AlarmDefinition) this.resource;
                builder.append("Alarm definition:[Name:").append(alarmDefinition.getName()).append("]");
                break;
            case TIMER:
                Timer timer = (Timer) this.resource;
                builder.append("Timer:[Name:").append(timer.getName()).append("]");
                break;
            default:
                break;
        }
        return builder.toString();
    }

    public String getResourceLessDetails() {
        if (this.resource == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        switch (this.resourceType) {
            case GATEWAY:
                Gateway gateway = (Gateway) this.resource;
                builder.append(DISPLAY_KEY_GATEWAY).append(gateway.getName());
                break;
            case NODE:
                Node node = (Node) this.resource;
                builder.append(DISPLAY_KEY_GATEWAY).append(node.getGateway().getName()).append(" >> ")
                        .append(DISPLAY_KEY_NODE).append(node.getEui())
                        .append(":").append(node.getName());
                break;
            case SENSOR:
                Sensor sensor = (Sensor) this.resource;
                builder.append(DISPLAY_KEY_GATEWAY).append(sensor.getNode().getGateway().getName()).append(" >> ")
                        .append(DISPLAY_KEY_NODE).append(sensor.getNode().getEui()).append(":")
                        .append(sensor.getNode().getName()).append(" >> ").append(DISPLAY_KEY_SENSOR)
                        .append(sensor.getSensorId()).append(":").append(sensor.getName());
                break;
            case SENSOR_VARIABLE:
                SensorVariable sensorVariable = (SensorVariable) this.resource;
                builder.append(DISPLAY_KEY_GATEWAY)
                        .append(sensorVariable.getSensor().getNode().getGateway().getName())
                        .append(" >> ").append(DISPLAY_KEY_NODE).append(sensorVariable.getSensor().getNode().getEui())
                        .append(":").append(sensorVariable.getSensor().getNode().getName())
                        .append(" >> ").append(DISPLAY_KEY_SENSOR).append(sensorVariable.getSensor().getSensorId())
                        .append(":").append(sensorVariable.getSensor().getName())
                        .append(" >> ").append(DISPLAY_KEY_SENSOR_VARIABLE)
                        .append(ObjectManager.getMcLocale().getString(sensorVariable.getVariableType().name()));
                break;
            case RESOURCES_GROUP:
                ResourcesGroup resourcesGroup = (ResourcesGroup) this.resource;
                builder.append(DISPLAY_KEY_RESOURCES_GROUP).append(resourcesGroup.getName());
                break;
            case ALARM_DEFINITION:
                AlarmDefinition alarmDefinition = (AlarmDefinition) this.resource;
                builder.append(DISPLAY_KEY_ALARM_DIFINITION).append(alarmDefinition.getName());
                break;
            case TIMER:
                Timer timer = (Timer) this.resource;
                builder.append(DISPLAY_KEY_TIMER).append(timer.getName());
                break;
            default:
                break;
        }
        return builder.toString();
    }
}
