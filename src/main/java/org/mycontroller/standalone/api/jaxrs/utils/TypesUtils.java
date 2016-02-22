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
package org.mycontroller.standalone.api.jaxrs.utils;

import java.util.ArrayList;
import java.util.List;

import org.mycontroller.standalone.AppProperties.MC_LANGUAGE;
import org.mycontroller.standalone.AppProperties.MC_TIME_FORMAT;
import org.mycontroller.standalone.AppProperties.NETWORK_TYPE;
import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.AppProperties.STATE;
import org.mycontroller.standalone.AppProperties.UNIT_CONFIG;
import org.mycontroller.standalone.MYCMessages.MESSAGE_TYPE;
import org.mycontroller.standalone.MYCMessages.MESSAGE_TYPE_INTERNAL;
import org.mycontroller.standalone.MYCMessages.MESSAGE_TYPE_PRESENTATION;
import org.mycontroller.standalone.MYCMessages.MESSAGE_TYPE_SET_REQ;
import org.mycontroller.standalone.MYCMessages.MESSAGE_TYPE_STREAM;
import org.mycontroller.standalone.MYCMessages;
import org.mycontroller.standalone.ObjectFactory;
import org.mycontroller.standalone.alarm.AlarmUtils.DAMPENING_TYPE;
import org.mycontroller.standalone.alarm.AlarmUtils.THRESHOLD_TYPE;
import org.mycontroller.standalone.alarm.AlarmUtils.TRIGGER_TYPE;
import org.mycontroller.standalone.api.jaxrs.mapper.TypesIdNameMapper;
import org.mycontroller.standalone.auth.AuthUtils;
import org.mycontroller.standalone.auth.AuthUtils.PERMISSION_TYPE;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.PayloadOperationUtils.SEND_PAYLOAD_OPERATIONS;
import org.mycontroller.standalone.db.ResourcesLogsUtils.LOG_DIRECTION;
import org.mycontroller.standalone.db.ResourcesLogsUtils.LOG_LEVEL;
import org.mycontroller.standalone.db.tables.AlarmDefinition;
import org.mycontroller.standalone.db.tables.Firmware;
import org.mycontroller.standalone.db.tables.FirmwareType;
import org.mycontroller.standalone.db.tables.FirmwareVersion;
import org.mycontroller.standalone.db.tables.Gateway;
import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.db.tables.Notification;
import org.mycontroller.standalone.db.tables.ResourcesGroup;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.db.tables.SensorsVariablesMap;
import org.mycontroller.standalone.db.tables.Timer;
import org.mycontroller.standalone.db.tables.User;
import org.mycontroller.standalone.gateway.GatewayUtils;
import org.mycontroller.standalone.metrics.MetricsUtils.METRIC_TYPE;
import org.mycontroller.standalone.model.ResourceModel;
import org.mycontroller.standalone.notification.NotificationUtils.NOTIFICATION_TYPE;
import org.mycontroller.standalone.timer.TimerUtils.FREQUENCY_TYPE;
import org.mycontroller.standalone.timer.TimerUtils.WEEK_DAY;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class TypesUtils {
    public static final String NODE_IDENTIFIER = "NODE";

    private TypesUtils() {

    }

    public static ArrayList<TypesIdNameMapper> getSensorVariableTypes(List<String> metricTypes) {
        MESSAGE_TYPE_SET_REQ[] types = MESSAGE_TYPE_SET_REQ.values();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (MESSAGE_TYPE_SET_REQ type : types) {
            if (metricTypes.isEmpty()) {
                typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.getText())
                        .displayName(ObjectFactory.getMcLocale().getString(type.name())).build());
            } else if (metricTypes.contains(MYCMessages.getMetricType(type).getText())) {
                typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.getText())
                        .displayName(ObjectFactory.getMcLocale().getString(type.name())).build());
            }
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getSensorTypes() {
        MESSAGE_TYPE_PRESENTATION[] types = MESSAGE_TYPE_PRESENTATION.values();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (MESSAGE_TYPE_PRESENTATION type : types) {
            if (!type.name().contains(NODE_IDENTIFIER)) {
                typesIdNameMappers.add(TypesIdNameMapper
                        .builder()
                        .id(type.getText())
                        .displayName(ObjectFactory.getMcLocale().getString(type.name())).build());
            }
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getNodeTypes() {
        MESSAGE_TYPE_PRESENTATION[] types = MESSAGE_TYPE_PRESENTATION.values();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (MESSAGE_TYPE_PRESENTATION type : types) {
            if (type.name().contains(NODE_IDENTIFIER)) {
                typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.getText())
                        .displayName(ObjectFactory.getMcLocale().getString(type.name())).build());
            }
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getAlarmTriggerTypes(String resourceTypeString) {
        TRIGGER_TYPE[] triggers = TRIGGER_TYPE.values();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        RESOURCE_TYPE resourceType = resourceTypeString != null ? RESOURCE_TYPE.fromString(resourceTypeString) : null;

        for (TRIGGER_TYPE triggerType : triggers) {
            if (resourceType != null) {
                switch (resourceType) {
                    case GATEWAY:
                    case NODE:
                    case RESOURCES_GROUP:
                        if (triggerType == TRIGGER_TYPE.EQUAL || triggerType == TRIGGER_TYPE.NOT_EQUAL) {
                            typesIdNameMappers
                                    .add(TypesIdNameMapper.builder().id(triggerType.getText())
                                            .displayName(triggerType.getText())
                                            .build());
                        }
                        break;
                    case SENSOR_VARIABLE:
                        typesIdNameMappers.add(TypesIdNameMapper.builder().id(triggerType.getText())
                                .displayName(triggerType.getText())
                                .build());
                        break;

                    case ALARM_DEFINITION:
                    case SENSOR:
                    case TIMER:
                    default:
                        //Do not add anything...
                        break;
                }
            } else {
                typesIdNameMappers.add(TypesIdNameMapper.builder().id(triggerType.ordinal())
                        .displayName(triggerType.getText())
                        .build());
            }
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getAlarmNotificationTypes() {
        NOTIFICATION_TYPE[] types = NOTIFICATION_TYPE.values();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (NOTIFICATION_TYPE notificationType : types) {
            typesIdNameMappers.add(TypesIdNameMapper.builder().id(notificationType.getText())
                    .displayName(notificationType.getText()).build());
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getAlarmThresholdTypes(String resourceTypeString) {
        THRESHOLD_TYPE[] types = THRESHOLD_TYPE.values();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        RESOURCE_TYPE resourceType = RESOURCE_TYPE.fromString(resourceTypeString);
        if (resourceType == null) {
            resourceType = RESOURCE_TYPE.SENSOR_VARIABLE;
        }
        for (THRESHOLD_TYPE type : types) {
            switch (resourceType) {
                case GATEWAY:
                case NODE:
                case RESOURCES_GROUP:
                    if (type == THRESHOLD_TYPE.VALUE) {
                        typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.getText())
                                .displayName(type.getText()).build());
                    }
                    break;
                case SENSOR_VARIABLE:
                    if (type == THRESHOLD_TYPE.VALUE || type == THRESHOLD_TYPE.SENSOR_VARIABLE) {
                        typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.getText())
                                .displayName(type.getText()).build());
                    }
                    break;

                default:
                    typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.getText())
                            .displayName(type.getText()).build());
                    break;
            }

        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getAlarmDampeningTypes() {
        DAMPENING_TYPE[] types = DAMPENING_TYPE.values();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (DAMPENING_TYPE type : types) {
            typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.getText()).displayName(type.getText())
                    .build());
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getPayloadOperations(String resourceTypeString) {
        SEND_PAYLOAD_OPERATIONS[] types = SEND_PAYLOAD_OPERATIONS.values();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        RESOURCE_TYPE resourceType = RESOURCE_TYPE.fromString(resourceTypeString);
        if (resourceType == null) {
            resourceType = RESOURCE_TYPE.SENSOR_VARIABLE;
        }
        for (SEND_PAYLOAD_OPERATIONS type : types) {
            switch (resourceType) {
                case ALARM_DEFINITION:
                case TIMER:
                    if (type == SEND_PAYLOAD_OPERATIONS.ENABLE || type == SEND_PAYLOAD_OPERATIONS.DISABLE) {
                        typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.getText())
                                .displayName(type.getText()).build());
                    }
                    break;
                case GATEWAY:
                    if (type == SEND_PAYLOAD_OPERATIONS.ENABLE || type == SEND_PAYLOAD_OPERATIONS.DISABLE
                            || type == SEND_PAYLOAD_OPERATIONS.RELOAD || type == SEND_PAYLOAD_OPERATIONS.START
                            || type == SEND_PAYLOAD_OPERATIONS.STOP) {
                        typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.getText())
                                .displayName(type.getText()).build());
                    }
                    break;
                case NODE:
                    if (type == SEND_PAYLOAD_OPERATIONS.REBOOT) {
                        typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.getText())
                                .displayName(type.getText()).build());
                    }
                    break;
                case RESOURCES_GROUP:
                    if (type == SEND_PAYLOAD_OPERATIONS.ON || type == SEND_PAYLOAD_OPERATIONS.OFF) {
                        typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.getText())
                                .displayName(type.getText()).build());
                    }
                    break;
                case SENSOR_VARIABLE:
                    if (type == SEND_PAYLOAD_OPERATIONS.TOGGLE || type == SEND_PAYLOAD_OPERATIONS.INCREMENT
                            || type == SEND_PAYLOAD_OPERATIONS.DECREMENT) {
                        typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.getText())
                                .displayName(type.getText()).build());
                    }
                    break;

                default:
                    break;
            }

        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getStateTypes(String resourceTypeString) {
        STATE[] types = STATE.values();
        RESOURCE_TYPE resourceType = RESOURCE_TYPE.fromString(resourceTypeString);
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (STATE type : types) {
            if (resourceType != null) {
                switch (resourceType) {
                    case GATEWAY:
                    case NODE:
                        if (type == STATE.UP || type == STATE.DOWN || type == STATE.UNAVAILABLE) {
                            typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.getText())
                                    .displayName(type.getText()).build());
                        }
                        break;
                    case RESOURCES_GROUP:
                        if (type == STATE.ON || type == STATE.OFF) {
                            typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.getText())
                                    .displayName(type.getText()).build());
                        }
                        break;
                    case ALARM_DEFINITION:
                    case TIMER:
                        if (type == STATE.ON || type == STATE.OFF) {
                            typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.getText())
                                    .displayName(type.getText()).build());
                        }
                        break;
                    default:
                        //Do not add anything
                        break;
                }
            } else {
                typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.getText()).displayName(type.getText())
                        .build());
            }
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getTimerTypes() {
        org.mycontroller.standalone.timer.TimerUtils.TIMER_TYPE[] types = org.mycontroller.standalone.timer.TimerUtils.TIMER_TYPE
                .values();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (org.mycontroller.standalone.timer.TimerUtils.TIMER_TYPE timerType : types) {
            typesIdNameMappers.add(TypesIdNameMapper.builder().id(timerType.ordinal())
                    .displayName(timerType.getText()).build());
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getTimerFrequencyTypes() {
        FREQUENCY_TYPE[] types = FREQUENCY_TYPE.values();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (FREQUENCY_TYPE type : types) {
            typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.getText()).displayName(type.getText())
                    .build());
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getTimerWeekDays(boolean isAllDaysTicked) {
        WEEK_DAY[] types = WEEK_DAY.values();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (WEEK_DAY type : types) {
            typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.getText()).displayName(type.getText())
                    .ticked(isAllDaysTicked).build());
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getResourceTypes(User user, String resourceType, Boolean isSendPayload) {
        RESOURCE_TYPE[] types = RESOURCE_TYPE.values();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        RESOURCE_TYPE resourceTypeFilter = null;
        if (resourceType != null) {
            resourceTypeFilter = RESOURCE_TYPE.fromString(resourceType);
        }

        for (RESOURCE_TYPE type : types) {
            if (resourceTypeFilter != null) {
                if (!AuthUtils.isSuperAdmin(user)) {
                    if (type == RESOURCE_TYPE.RESOURCES_GROUP) {
                        break;
                    }
                }
                switch (resourceTypeFilter) {
                    case ALARM_DEFINITION:
                    case TIMER:
                        if (isSendPayload && type != RESOURCE_TYPE.SENSOR) {
                            typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.getText())
                                    .displayName(type.getText()).build());
                        } else if (type != RESOURCE_TYPE.SENSOR
                                && type != RESOURCE_TYPE.ALARM_DEFINITION
                                && type != RESOURCE_TYPE.TIMER) {
                            typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.getText())
                                    .displayName(type.getText()).build());
                        }

                        break;
                    case RESOURCES_GROUP:
                        if (type != RESOURCE_TYPE.ALARM_DEFINITION && type != RESOURCE_TYPE.TIMER
                                && type != RESOURCE_TYPE.SENSOR
                                && type != RESOURCE_TYPE.RESOURCES_GROUP) {
                            typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.getText())
                                    .displayName(type.getText()).build());
                        }
                        break;
                    default:
                        typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.getText())
                                .displayName(type.getText()).build());
                        break;
                }
            } else {
                typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.getText()).displayName(type.getText())
                        .build());
            }
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getResources(User user, String resourceType) {
        if (resourceType == null) {
            return null;
        }
        RESOURCE_TYPE resourceTypeEnum = RESOURCE_TYPE.fromString(resourceType);
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        StringBuilder builder = new StringBuilder();
        switch (resourceTypeEnum) {
            case GATEWAY:
                List<Gateway> gateways = null;
                if (AuthUtils.isSuperAdmin(user)) {
                    gateways = DaoUtils.getGatewayDao().getAll();
                } else {
                    gateways = DaoUtils.getGatewayDao().getAll(user.getAllowedResources().getGatewayIds());
                }
                for (Gateway type : gateways) {
                    typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.getId()).displayName(type.getName())
                            .build());
                }
                break;
            case NODE:
                List<Node> nodes = null;
                if (AuthUtils.isSuperAdmin(user)) {
                    nodes = DaoUtils.getNodeDao().getAll();
                } else {
                    nodes = DaoUtils.getNodeDao().getAll(user.getAllowedResources().getNodeIds());
                }
                for (Node type : nodes) {
                    builder.setLength(0);
                    builder.append(type.getGateway().getName()).append(" -> ")
                            .append("[").append(type.getEui()).append("]").append(type.getName());
                    typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.getId())
                            .displayName(builder.toString()).build());
                }
                break;
            case SENSOR:
                List<Sensor> sensors = null;
                if (AuthUtils.isSuperAdmin(user)) {
                    sensors = DaoUtils.getSensorDao().getAll();
                } else {
                    sensors = DaoUtils.getSensorDao().getAll(user.getAllowedResources().getSensorIds());
                }
                for (Sensor type : sensors) {
                    builder.setLength(0);
                    builder.append(type.getNode().getGateway().getName()).append(" -> ")
                            .append("[").append(type.getNode().getEui()).append("]").append(type.getNode().getName())
                            .append(" -> [").append(type.getSensorId()).append("]").append(type.getName());
                    typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.getId())
                            .displayName(builder.toString()).build());
                }
                break;
            case SENSOR_VARIABLE:
                List<SensorVariable> sensorVariables = null;
                if (AuthUtils.isSuperAdmin(user)) {
                    sensorVariables = DaoUtils.getSensorVariableDao().getAll();
                } else {
                    sensorVariables = DaoUtils.getSensorVariableDao().getAll(
                            user.getAllowedResources().getSensorVariableIds());
                }
                for (SensorVariable type : sensorVariables) {
                    builder.setLength(0);
                    builder.append(type.getSensor().getNode().getGateway().getName())
                            .append(" -> [").append(type.getSensor().getNode().getEui()).append("]")
                            .append(type.getSensor().getNode().getName())
                            .append(" -> [").append(type.getSensor().getSensorId()).append("]")
                            .append(type.getSensor().getName())
                            .append(" -> ").append(type.getVariableType().getText());
                    typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.getId())
                            .displayName(builder.toString()).build());
                }
                break;

            default:
                return null;
        }

        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getGateways(User user) {
        List<Gateway> gateways = null;
        if (AuthUtils.isSuperAdmin(user)) {
            gateways = DaoUtils.getGatewayDao().getAll();
        } else {
            gateways = DaoUtils.getGatewayDao().getAll(user.getAllowedResources().getGatewayIds());
        }
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (Gateway gateway : gateways) {
            typesIdNameMappers.add(TypesIdNameMapper.builder().id(gateway.getId())
                    .displayName(new ResourceModel(RESOURCE_TYPE.GATEWAY, gateway).getResourceLessDetails()).build());
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getNodes(User user, Integer gatewayId) {
        List<Node> nodes = null;
        if (AuthUtils.isSuperAdmin(user)) {
            if (gatewayId != null) {
                nodes = DaoUtils.getNodeDao().getAllByGatewayId(gatewayId);
            } else {
                nodes = DaoUtils.getNodeDao().getAll();
            }
        } else {
            nodes = DaoUtils.getNodeDao().getAll(user.getAllowedResources().getNodeIds());
        }

        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (Node node : nodes) {
            typesIdNameMappers.add(TypesIdNameMapper.builder().id(node.getId())
                    .displayName(new ResourceModel(RESOURCE_TYPE.NODE, node).getResourceLessDetails()).build());
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getResourcesGroups() {
        List<ResourcesGroup> resourcesGroups = DaoUtils.getResourcesGroupDao().getAll();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (ResourcesGroup resourcesGroup : resourcesGroups) {
            typesIdNameMappers.add(TypesIdNameMapper
                    .builder().id(resourcesGroup.getId()).displayName(
                            new ResourceModel(RESOURCE_TYPE.RESOURCES_GROUP, resourcesGroup)
                                    .getResourceLessDetails()).build());
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getTimers(User user) {
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        List<Timer> timers = null;
        if (AuthUtils.isSuperAdmin(user)) {
            timers = DaoUtils.getTimerDao().getAll();
        } else {
            //TODO: filter resource based on users role
            return typesIdNameMappers;
        }
        for (Timer timer : timers) {
            typesIdNameMappers.add(TypesIdNameMapper.builder().id(timer.getId())
                    .displayName(new ResourceModel(RESOURCE_TYPE.TIMER, timer).getResourceLessDetails()).build());
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getFirmwareTypes() {
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        List<FirmwareType> firmwareTypes = DaoUtils.getFirmwareTypeDao().getAll();
        for (FirmwareType firmwareType : firmwareTypes) {
            typesIdNameMappers.add(TypesIdNameMapper.builder().id(firmwareType.getId())
                    .displayName(firmwareType.getName()).build());
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getFirmwareVersions() {
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        List<FirmwareVersion> firmwareVersions = DaoUtils.getFirmwareVersionDao().getAll();
        for (FirmwareVersion firmwareVersion : firmwareVersions) {
            typesIdNameMappers.add(TypesIdNameMapper.builder().id(firmwareVersion.getId())
                    .displayName(firmwareVersion.getVersion()).build());
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getFirmwares() {
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        List<Firmware> firmwares = DaoUtils.getFirmwareDao().getAll();
        for (Firmware firmware : firmwares) {
            typesIdNameMappers.add(TypesIdNameMapper.builder().id(firmware.getId())
                    .displayName(firmware.getType().getName() + ":" + firmware.getVersion().getVersion()).build());
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getAlarmDefinitions(User user) {
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        List<AlarmDefinition> alarmDefinitions = null;
        if (AuthUtils.isSuperAdmin(user)) {
            alarmDefinitions = DaoUtils.getAlarmDefinitionDao().getAll();
        } else {
            //TODO: filter resource based on users role
            return typesIdNameMappers;
        }
        for (AlarmDefinition alarmDefinition : alarmDefinitions) {
            typesIdNameMappers.add(TypesIdNameMapper.builder().id(alarmDefinition.getId())
                    .displayName(new ResourceModel(RESOURCE_TYPE.ALARM_DEFINITION, alarmDefinition)
                            .getResourceLessDetails()).build());
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getSensors(User user, Integer nodeId) {
        List<Node> nodes = new ArrayList<Node>();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        List<Sensor> sensors = null;
        if (AuthUtils.isSuperAdmin(user)) {
            if (nodeId != null) {
                nodes.add(Node.builder().id(nodeId).build());
                sensors = DaoUtils.getSensorDao().getAllByNodeId(nodeId);
            } else {
                sensors = DaoUtils.getSensorDao().getAll();
            }
        } else {
            sensors = DaoUtils.getSensorDao().getAll(user.getAllowedResources().getSensorIds());
        }

        for (Sensor sensor : sensors) {
            typesIdNameMappers
                    .add(TypesIdNameMapper.builder().id(sensor.getId()).subId(sensor.getSensorId())
                            .displayName(new ResourceModel(RESOURCE_TYPE.SENSOR, sensor).getResourceLessDetails())
                            .build());
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getSensorVariables(User user, Integer sensorId,
            Integer sensorVariableId, List<String> variableTypes, List<String> metricTypes)
            throws IllegalAccessException {
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        List<SensorVariable> sensorVariables = null;
        if (!AuthUtils.isSuperAdmin(user)) {
            if (sensorVariableId != null) {
                if (!DaoUtils.getSensorVariableDao().getAllBySensorId(sensorId).contains(sensorVariableId)) {
                    throw new IllegalAccessException("You do not have access for this resource!");
                }
            } else if (sensorId != null) {
                if (!DaoUtils.getSensorVariableDao().getAllBySensorId(sensorId).contains(sensorVariableId)) {
                    throw new IllegalAccessException("You do not have access for this resource!");
                }
            } else {
                sensorVariables = DaoUtils.getSensorVariableDao().getAll(
                        user.getAllowedResources().getSensorVariableIds());
            }
        }

        if (sensorVariableId != null) {
            sensorVariables = DaoUtils.getSensorVariableDao().getAll(
                    user.getAllowedResources().getSensorVariableIds());
        } else if (sensorId != null) {
            sensorVariables = DaoUtils.getSensorVariableDao().getAllBySensorId(sensorId);
        } else if (sensorVariables == null) {
            sensorVariables = DaoUtils.getSensorVariableDao().getAll();
        }

        if (sensorVariables != null) {
            for (SensorVariable sensorVariable : sensorVariables) {
                if (!variableTypes.isEmpty()) {
                    if (variableTypes.contains(sensorVariable.getVariableType().getText())) {
                        updateSensorVariable(typesIdNameMappers, metricTypes, sensorVariable);
                    }
                } else {
                    updateSensorVariable(typesIdNameMappers, metricTypes, sensorVariable);
                }
            }
        }
        return typesIdNameMappers;
    }

    private static void updateSensorVariable(ArrayList<TypesIdNameMapper> typesIdNameMappers,
            List<String> metricTypes,
            SensorVariable sensorVariable) {
        if (metricTypes.isEmpty()) {
            typesIdNameMappers.add(TypesIdNameMapper.builder().id(sensorVariable.getId()).displayName(
                    new ResourceModel(RESOURCE_TYPE.SENSOR_VARIABLE, sensorVariable)
                            .getResourceLessDetails()).build());
        } else if (metricTypes.contains(sensorVariable.getMetricType().getText())) {
            typesIdNameMappers.add(TypesIdNameMapper.builder().id(sensorVariable.getId()).displayName(
                    new ResourceModel(RESOURCE_TYPE.SENSOR_VARIABLE, sensorVariable)
                            .getResourceLessDetails()).build());
        }
    }

    public static ArrayList<String> getGraphInterpolateTypes() {
        ArrayList<String> types = new ArrayList<String>();
        types.add("linear");
        types.add("basis");
        types.add("cardinal");
        types.add("monotone");
        types.add("bundle");
        types.add("step-before");
        types.add("step-after");
        types.add("basis-open");
        types.add("basis-closed");
        types.add("cardinal-open");
        types.add("cardinal-closed");
        return types;
    }

    public static ArrayList<String> getConfigUnitTypes() {
        ArrayList<String> types = new ArrayList<String>();
        UNIT_CONFIG[] configTypes = UNIT_CONFIG.values();
        for (UNIT_CONFIG configType : configTypes) {
            types.add(configType.getText());
        }
        return types;
    }

    public static ArrayList<TypesIdNameMapper> getSensorVariableTypes(MESSAGE_TYPE_PRESENTATION sensorType,
            Integer sensorId, List<String> metricTypes) {
        if (sensorType == null) {
            return getSensorVariableTypes(metricTypes);
        } else {
            ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
            List<SensorsVariablesMap> variableTypes = DaoUtils.getSensorsVariablesMapDao().getAll(sensorType);
            for (SensorsVariablesMap variableType : variableTypes) {
                typesIdNameMappers.add(TypesIdNameMapper.builder().id(variableType.getVariableType().getText())
                        .displayName(ObjectFactory.getMcLocale().getString(variableType.getVariableType().name()))
                        .ticked(false).build());
            }
            if (sensorId != null) {
                List<SensorVariable> sensorVariables = DaoUtils.getSensorVariableDao().getAllBySensorId(sensorId);
                for (SensorVariable sensorVariable : sensorVariables) {
                    for (TypesIdNameMapper idNameMapper : typesIdNameMappers) {
                        if (idNameMapper.getId().equals(sensorVariable.getVariableType().getText())) {
                            idNameMapper.setSubId(sensorVariable.getId());
                            idNameMapper.setTicked(true);
                        }
                    }
                }
            }
            return typesIdNameMappers;
        }

    }

    public static ArrayList<TypesIdNameMapper> getGraphSensorVariableTypes(int sensorRefId) {
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        List<SensorVariable> sensorVariables = DaoUtils.getSensorVariableDao().getAllBySensorId(sensorRefId);
        for (SensorVariable sensorVariable : sensorVariables) {
            if (sensorVariable.getMetricType() != METRIC_TYPE.NONE) {
                typesIdNameMappers.add(TypesIdNameMapper.builder().id(sensorVariable.getId())
                        .subId(sensorVariable.getSensor().getId())
                        .displayName(sensorVariable.getVariableType().getText()).build());
            }
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getSensorVariableMapperByType(MESSAGE_TYPE_PRESENTATION sensorType) {
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        List<SensorsVariablesMap> variablesMap = DaoUtils.getSensorsVariablesMapDao().getAll(sensorType);
        ArrayList<String> variables = new ArrayList<String>();
        for (SensorsVariablesMap variableMap : variablesMap) {
            variables.add(variableMap.getVariableType().getText());
        }

        MESSAGE_TYPE_SET_REQ[] types = MESSAGE_TYPE_SET_REQ.values();
        for (MESSAGE_TYPE_SET_REQ type : types) {
            typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.getText()).displayName(type.getText())
                    .ticked(variables != null ? variables.contains(type.getText()) : false).build());
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getMessageTypes() {
        MESSAGE_TYPE[] types = MESSAGE_TYPE.values();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (MESSAGE_TYPE type : types) {
            typesIdNameMappers
                    .add(TypesIdNameMapper.builder().id(type.getText()).displayName(type.toString()).build());
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getMessageSubTypes(int messageType) {
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        switch (MESSAGE_TYPE.get(messageType)) {
            case C_INTERNAL:
                MESSAGE_TYPE_INTERNAL[] typesInt = MESSAGE_TYPE_INTERNAL.values();
                for (MESSAGE_TYPE_INTERNAL type : typesInt) {
                    typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.getText()).displayName(type.toString())
                            .build());
                }
                break;
            case C_PRESENTATION:
                MESSAGE_TYPE_PRESENTATION[] typesPre = MESSAGE_TYPE_PRESENTATION.values();
                for (MESSAGE_TYPE_PRESENTATION type : typesPre) {
                    typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.getText()).displayName(type.toString())
                            .build());
                }
                break;
            case C_REQ:
            case C_SET:
                MESSAGE_TYPE_SET_REQ[] typesSetReq = MESSAGE_TYPE_SET_REQ.values();
                for (MESSAGE_TYPE_SET_REQ type : typesSetReq) {
                    typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.getText()).displayName(type.toString())
                            .build());
                }
                break;
            case C_STREAM:
                MESSAGE_TYPE_STREAM[] typesStr = MESSAGE_TYPE_STREAM.values();
                for (MESSAGE_TYPE_STREAM type : typesStr) {
                    typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.getText()).displayName(type.toString())
                            .build());
                }
                break;
            default:
                break;
        }

        return typesIdNameMappers;
    }

    public static List<TypesIdNameMapper> getVariableMapperList() {
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        ArrayList<String> variables = new ArrayList<String>();
        MESSAGE_TYPE_PRESENTATION[] types = MESSAGE_TYPE_PRESENTATION.values();
        for (MESSAGE_TYPE_PRESENTATION type : types) {
            if (!type.name().contains(NODE_IDENTIFIER)) {
                variables.clear();
                List<SensorsVariablesMap> variablesMap = DaoUtils.getSensorsVariablesMapDao().getAll(type);
                if (variablesMap != null) {
                    for (SensorsVariablesMap sensorsVariablesMap : variablesMap) {
                        variables.add(sensorsVariablesMap.getVariableType().getText());
                    }
                }
                typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.getText()).displayName(type.getText())
                        .value(variables.clone()).build());
            }
        }
        return typesIdNameMappers;
    }

    public static void updateVariableMap(TypesIdNameMapper idNameMapper) {
        // Delete existing map
        MESSAGE_TYPE_PRESENTATION sensorType = MESSAGE_TYPE_PRESENTATION.fromString(String.valueOf(idNameMapper
                .getDisplayName()));
        DaoUtils.getSensorsVariablesMapDao().delete(sensorType);
        @SuppressWarnings("unchecked")
        List<String> variables = (List<String>) idNameMapper.getValue();
        if (variables != null && variables.size() > 0) {
            // Create New Map
            for (String variable : variables) {
                DaoUtils.getSensorsVariablesMapDao().create(sensorType,
                        MESSAGE_TYPE_SET_REQ.fromString(variable));
            }
        }
    }

    public static ArrayList<TypesIdNameMapper> getLanguages() {
        MC_LANGUAGE[] languages = MC_LANGUAGE.values();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        MC_LANGUAGE selected = ObjectFactory.getAppProperties().getLanguage();
        for (MC_LANGUAGE language : languages) {
            if (selected == language) {
                typesIdNameMappers.add(TypesIdNameMapper.builder().id(language.name().toLowerCase())
                        .displayName(language.getText()).ticked(true).build());
            } else {
                typesIdNameMappers.add(TypesIdNameMapper.builder().id(language.name().toLowerCase())
                        .displayName(language.getText()).build());
            }

        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getTimeFormats() {
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        MC_TIME_FORMAT[] formats = MC_TIME_FORMAT.values();
        for (MC_TIME_FORMAT format : formats) {
            typesIdNameMappers
                    .add(TypesIdNameMapper.builder().id(format.name()).displayName(format.getText()).build());
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getGatewayTypes() {
        GatewayUtils.TYPE[] types = GatewayUtils.TYPE.values();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (GatewayUtils.TYPE type : types) {
            typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.getText())
                    .displayName(ObjectFactory.getMcLocale().getString(type.name())).build());
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getGatewayNetworkTypes() {
        NETWORK_TYPE[] types = NETWORK_TYPE.values();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (NETWORK_TYPE type : types) {
            typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.getText()).displayName(type.getText()).build());
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getGatewaySerialDrivers() {
        GatewayUtils.SERIAL_PORT_DRIVER[] types = GatewayUtils.SERIAL_PORT_DRIVER.values();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (GatewayUtils.SERIAL_PORT_DRIVER type : types) {
            typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.getText()).displayName(type.getText()).build());
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getHvacOptionsFlowState() {
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (String key : MYCMessages.HVAC_OPTIONS_FLOW_STATE.keySet()) {
            typesIdNameMappers.add(TypesIdNameMapper.builder().id(key)
                    .displayName(MYCMessages.HVAC_OPTIONS_FLOW_STATE.get(key)).build());
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getHvacOptionsFlowMode() {
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (String key : MYCMessages.HVAC_OPTIONS_FLOW_MODE.keySet()) {
            typesIdNameMappers.add(TypesIdNameMapper.builder().id(key)
                    .displayName(MYCMessages.HVAC_OPTIONS_FLOW_MODE.get(key)).build());
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getHvacOptionsFanSpeed() {
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (String key : MYCMessages.HVAC_OPTIONS_FAN_SPEED.keySet()) {
            typesIdNameMappers.add(TypesIdNameMapper.builder().id(key)
                    .displayName(MYCMessages.HVAC_OPTIONS_FAN_SPEED.get(key)).build());
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getRolePermissions() {
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (PERMISSION_TYPE permission : PERMISSION_TYPE.values()) {
            if (permission != PERMISSION_TYPE.SUPER_ADMIN) {
                typesIdNameMappers.add(TypesIdNameMapper.builder().id(permission.ordinal())
                        .displayName(permission.getText()).build());
            }
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getResourceLogsMessageTypes() {
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (MESSAGE_TYPE type : MESSAGE_TYPE.values()) {
            typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.getText())
                    .displayName(type.getText()).build());
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getResourceLogsLogDirections() {
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (LOG_DIRECTION type : LOG_DIRECTION.values()) {
            typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.getText())
                    .displayName(type.getText()).build());
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getResourceLogsLogLevels() {
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (LOG_LEVEL type : LOG_LEVEL.values()) {
            typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.getText())
                    .displayName(type.getText()).build());
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getNotifications() {
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        List<Notification> notifications = DaoUtils.getNotificationDao().getAll();
        for (Notification notification : notifications) {
            typesIdNameMappers.add(TypesIdNameMapper.builder().id(notification.getId())
                    .displayName(notification.getName()).build());
        }
        return typesIdNameMappers;
    }
}
