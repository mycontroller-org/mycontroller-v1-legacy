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
import org.mycontroller.standalone.ObjectFactory;
import org.mycontroller.standalone.alarm.AlarmUtils.DAMPENING_TYPE;
import org.mycontroller.standalone.alarm.AlarmUtils.THRESHOLD_TYPE;
import org.mycontroller.standalone.alarm.AlarmUtils.TRIGGER_TYPE;
import org.mycontroller.standalone.alarm.AlarmUtils.NOTIFICATION_TYPE;
import org.mycontroller.standalone.api.jaxrs.mapper.TypesIdNameMapper;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.AlarmDefinition;
import org.mycontroller.standalone.db.tables.Firmware;
import org.mycontroller.standalone.db.tables.FirmwareType;
import org.mycontroller.standalone.db.tables.FirmwareVersion;
import org.mycontroller.standalone.db.tables.Gateway;
import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.db.tables.ResourcesGroup;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.db.tables.SensorsVariablesMap;
import org.mycontroller.standalone.db.tables.Timer;
import org.mycontroller.standalone.gateway.GatewayUtils;
import org.mycontroller.standalone.metrics.TypeUtils.METRIC_TYPE;
import org.mycontroller.standalone.model.ResourceModel;
import org.mycontroller.standalone.timer.TimerUtils.FREQUENCY;
import org.mycontroller.standalone.timer.TimerUtils.WEEK_DAY;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class TypesUtils {
    public static final String NODE_IDENTIFIER = "NODE";

    private TypesUtils() {

    }

    public static ArrayList<TypesIdNameMapper> getSensorValueTypes() {
        MESSAGE_TYPE_SET_REQ[] types = MESSAGE_TYPE_SET_REQ.values();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (MESSAGE_TYPE_SET_REQ type : types) {
            typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.ordinal()).displayName(type.name()).build());
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getSensorTypes() {
        MESSAGE_TYPE_PRESENTATION[] types = MESSAGE_TYPE_PRESENTATION.values();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (MESSAGE_TYPE_PRESENTATION type : types) {
            if (!type.name().contains(NODE_IDENTIFIER)) {
                typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.ordinal()).displayName(type.getText())
                        .build());
            }
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getNodeTypes() {
        MESSAGE_TYPE_PRESENTATION[] types = MESSAGE_TYPE_PRESENTATION.values();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (MESSAGE_TYPE_PRESENTATION type : types) {
            if (type.name().contains(NODE_IDENTIFIER)) {
                typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.ordinal()).displayName(type.getText())
                        .build());
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
                                    .add(TypesIdNameMapper.builder().id(triggerType.ordinal())
                                            .displayName(triggerType.getText())
                                            .build());
                        }
                        break;
                    case SENSOR_VARIABLE:
                        typesIdNameMappers.add(TypesIdNameMapper.builder().id(triggerType.ordinal())
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
            typesIdNameMappers.add(TypesIdNameMapper.builder().id(notificationType.ordinal())
                    .displayName(notificationType.getText()).build());
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getAlarmThresholdTypes() {
        THRESHOLD_TYPE[] types = THRESHOLD_TYPE.values();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (THRESHOLD_TYPE type : types) {
            typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.ordinal()).displayName(type.getText())
                    .build());
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getAlarmDampeningTypes() {
        DAMPENING_TYPE[] types = DAMPENING_TYPE.values();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (DAMPENING_TYPE type : types) {
            typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.ordinal()).displayName(type.getText())
                    .build());
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getStateTypes(String resourceTypeString) {
        STATE[] types = STATE.values();
        RESOURCE_TYPE resourceType = resourceTypeString != null ? RESOURCE_TYPE.fromString(resourceTypeString) : null;
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (STATE type : types) {
            if (resourceType != null) {
                switch (resourceType) {
                    case GATEWAY:
                    case NODE:
                        if (type == STATE.UP || type == STATE.DOWN || type == STATE.UNAVAILABLE) {
                            typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.ordinal())
                                    .displayName(type.getText()).build());
                        }
                        break;
                    case RESOURCES_GROUP:
                        if (type == STATE.ON || type == STATE.OFF) {
                            typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.ordinal())
                                    .displayName(type.getText()).build());
                        }
                        break;
                    default:
                        //Do not add anything
                        break;
                }
            } else {
                typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.ordinal()).displayName(type.getText())
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
        FREQUENCY[] types = FREQUENCY.values();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (FREQUENCY type : types) {
            typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.ordinal()).displayName(type.getText())
                    .build());
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getTimerWeekDays(boolean isAllDaysTicked) {
        WEEK_DAY[] types = WEEK_DAY.values();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (WEEK_DAY type : types) {
            typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.ordinal()).displayName(type.getText())
                    .ticked(isAllDaysTicked).build());
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getResourceTypes(String resourceType) {
        RESOURCE_TYPE[] types = RESOURCE_TYPE.values();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        RESOURCE_TYPE resourceTypeFilter = null;
        if (resourceType != null) {
            resourceTypeFilter = RESOURCE_TYPE.fromString(resourceType);
        }

        for (RESOURCE_TYPE type : types) {
            if (resourceTypeFilter != null) {
                switch (resourceTypeFilter) {
                    case ALARM_DEFINITION:
                    case TIMER:
                        if (type != RESOURCE_TYPE.ALARM_DEFINITION && type != RESOURCE_TYPE.TIMER
                                && type != RESOURCE_TYPE.SENSOR) {
                            typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.ordinal())
                                    .displayName(type.getText()).build());
                        }
                        break;
                    case RESOURCES_GROUP:
                        if (type != RESOURCE_TYPE.ALARM_DEFINITION && type != RESOURCE_TYPE.TIMER
                                && type != RESOURCE_TYPE.SENSOR
                                && type != RESOURCE_TYPE.RESOURCES_GROUP) {
                            typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.ordinal())
                                    .displayName(type.getText()).build());
                        }
                        break;
                    default:
                        typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.ordinal())
                                .displayName(type.getText()).build());
                        break;
                }
            } else {
                typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.ordinal()).displayName(type.getText())
                        .build());
            }
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getResources(String resourceType) {
        if (resourceType == null) {
            return null;
        }
        RESOURCE_TYPE resourceTypeEnum = RESOURCE_TYPE.fromString(resourceType);
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        StringBuilder builder = new StringBuilder();
        switch (resourceTypeEnum) {
            case GATEWAY:
                List<Gateway> gateways = DaoUtils.getGatewayDao().getAll();
                for (Gateway type : gateways) {
                    typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.getId()).displayName(type.getName())
                            .build());
                }
                break;
            case NODE:
                List<Node> nodes = DaoUtils.getNodeDao().getAll();
                for (Node type : nodes) {
                    builder.setLength(0);
                    builder.append(type.getGateway().getName()).append(" -> ")
                            .append("[").append(type.getEui()).append("]").append(type.getName());
                    typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.getId())
                            .displayName(builder.toString()).build());
                }
                break;
            case SENSOR:
                List<Sensor> sensors = DaoUtils.getSensorDao().getAll();
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
                List<SensorVariable> sensorVariables = DaoUtils.getSensorVariableDao().getAll();
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

    public static ArrayList<TypesIdNameMapper> getGateways() {
        List<Gateway> gateways = DaoUtils.getGatewayDao().getAll();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (Gateway gateway : gateways) {
            typesIdNameMappers.add(TypesIdNameMapper.builder().id(gateway.getId())
                    .displayName(new ResourceModel(RESOURCE_TYPE.GATEWAY, gateway).getResourceLessDetails()).build());
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getNodes(Integer gatewayId) {
        List<Node> nodes = null;
        if (gatewayId != null) {
            nodes = DaoUtils.getNodeDao().getAll(gatewayId);
        } else {
            nodes = DaoUtils.getNodeDao().getAll();
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

    public static ArrayList<TypesIdNameMapper> getTimers() {
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        List<Timer> timers = DaoUtils.getTimerDao().getAll();
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

    public static ArrayList<TypesIdNameMapper> getAlarmDefinitions() {
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        List<AlarmDefinition> alarmDefinitions = DaoUtils.getAlarmDefinitionDao().getAll();
        for (AlarmDefinition alarmDefinition : alarmDefinitions) {
            typesIdNameMappers.add(TypesIdNameMapper.builder().id(alarmDefinition.getId())
                    .displayName(new ResourceModel(RESOURCE_TYPE.ALARM_DEFINITION, alarmDefinition)
                            .getResourceLessDetails()).build());
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getSensors(Integer nodeId) {
        List<Node> nodes = new ArrayList<Node>();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        if (nodeId != null) {
            nodes.add(new Node(nodeId));
        } else {
            nodes = DaoUtils.getNodeDao().getAll();
        }
        for (Node node : nodes) {
            List<Sensor> sensors = DaoUtils.getSensorDao().getAll(node.getId());
            for (Sensor sensor : sensors) {
                typesIdNameMappers
                        .add(TypesIdNameMapper.builder().id(sensor.getId()).subId(sensor.getSensorId())
                                .displayName(new ResourceModel(RESOURCE_TYPE.SENSOR, sensor).getResourceLessDetails())
                                .build());
            }
        }

        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getSensorVariables(Integer sensorId) {
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        List<Sensor> sensors = new ArrayList<Sensor>();
        if (sensorId != null) {
            Sensor sensor = new Sensor();
            sensor.setId(sensorId);
            sensors.add(sensor);
        } else {
            sensors = DaoUtils.getSensorDao().getAll();
        }

        for (Sensor sensor : sensors) {
            List<SensorVariable> sensorVariables = DaoUtils.getSensorVariableDao().getAll(sensor.getId());
            for (SensorVariable sensorVariable : sensorVariables) {
                typesIdNameMappers.add(TypesIdNameMapper.builder().id(sensorVariable.getId()).displayName(
                        new ResourceModel(RESOURCE_TYPE.SENSOR_VARIABLE, sensorVariable).getResourceLessDetails())
                        .build());
            }
        }

        return typesIdNameMappers;
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
            Integer sensorId) {
        if (sensorType == null) {
            return null;
        }
        List<SensorsVariablesMap> variableTypes = DaoUtils.getSensorsVariablesMapDao().getAll(sensorType);
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (SensorsVariablesMap variableType : variableTypes) {
            typesIdNameMappers.add(TypesIdNameMapper.builder().id(variableType.getVariableType().getText())
                    .displayName(variableType.getVariableType().getText()).ticked(false).build());
        }
        if (sensorId != null) {
            List<SensorVariable> sensorVariables = DaoUtils.getSensorVariableDao().getAll(sensorId);
            for (SensorVariable sensorVariable : sensorVariables) {
                for (TypesIdNameMapper idNameMapper : typesIdNameMappers) {
                    if (idNameMapper.getDisplayName().equals(sensorVariable.getVariableType().getText())) {
                        idNameMapper.setSubId(sensorVariable.getId());
                        idNameMapper.setTicked(true);
                    }
                }
            }
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getGraphSensorVariableTypes(int sensorRefId) {
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        List<SensorVariable> sensorVariables = DaoUtils.getSensorVariableDao().getAll(sensorRefId);
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
                    .add(TypesIdNameMapper.builder().id(type.ordinal()).displayName(type.toString()).build());
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getMessageSubTypes(int messageType) {
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        switch (MESSAGE_TYPE.get(messageType)) {
            case C_INTERNAL:
                MESSAGE_TYPE_INTERNAL[] typesInt = MESSAGE_TYPE_INTERNAL.values();
                for (MESSAGE_TYPE_INTERNAL type : typesInt) {
                    typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.ordinal()).displayName(type.toString())
                            .build());
                }
                break;
            case C_PRESENTATION:
                MESSAGE_TYPE_PRESENTATION[] typesPre = MESSAGE_TYPE_PRESENTATION.values();
                for (MESSAGE_TYPE_PRESENTATION type : typesPre) {
                    typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.ordinal()).displayName(type.toString())
                            .build());
                }
                break;
            case C_REQ:
            case C_SET:
                MESSAGE_TYPE_SET_REQ[] typesSetReq = MESSAGE_TYPE_SET_REQ.values();
                for (MESSAGE_TYPE_SET_REQ type : typesSetReq) {
                    typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.ordinal()).displayName(type.toString())
                            .build());
                }
                break;
            case C_STREAM:
                MESSAGE_TYPE_STREAM[] typesStr = MESSAGE_TYPE_STREAM.values();
                for (MESSAGE_TYPE_STREAM type : typesStr) {
                    typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.ordinal()).displayName(type.toString())
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
            typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.getText()).displayName(type.getText()).build());
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
}
