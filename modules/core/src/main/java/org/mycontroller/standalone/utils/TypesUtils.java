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
package org.mycontroller.standalone.utils;

import java.util.ArrayList;
import java.util.List;

import org.mycontroller.restclient.core.TRUST_HOST_TYPE;
import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.AppProperties.MC_LANGUAGE;
import org.mycontroller.standalone.AppProperties.MC_TIME_FORMAT;
import org.mycontroller.standalone.AppProperties.NETWORK_TYPE;
import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.AppProperties.STATE;
import org.mycontroller.standalone.AppProperties.UNIT_CONFIG;
import org.mycontroller.standalone.McObjectManager;
import org.mycontroller.standalone.api.ForwardPayloadApi;
import org.mycontroller.standalone.api.jaxrs.model.Query;
import org.mycontroller.standalone.api.jaxrs.model.TypesIdNameMapper;
import org.mycontroller.standalone.auth.AuthUtils;
import org.mycontroller.standalone.auth.AuthUtils.PERMISSION_TYPE;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.NodeUtils.NODE_REGISTRATION_STATE;
import org.mycontroller.standalone.db.ResourceOperationUtils.SEND_PAYLOAD_OPERATIONS;
import org.mycontroller.standalone.db.ResourcesLogsUtils.LOG_DIRECTION;
import org.mycontroller.standalone.db.ResourcesLogsUtils.LOG_LEVEL;
import org.mycontroller.standalone.db.tables.ExternalServerTable;
import org.mycontroller.standalone.db.tables.Firmware;
import org.mycontroller.standalone.db.tables.FirmwareType;
import org.mycontroller.standalone.db.tables.FirmwareVersion;
import org.mycontroller.standalone.db.tables.ForwardPayload;
import org.mycontroller.standalone.db.tables.GatewayTable;
import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.db.tables.OperationTable;
import org.mycontroller.standalone.db.tables.ResourcesGroup;
import org.mycontroller.standalone.db.tables.Room;
import org.mycontroller.standalone.db.tables.RuleDefinitionTable;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.db.tables.SensorsVariablesMap;
import org.mycontroller.standalone.db.tables.Timer;
import org.mycontroller.standalone.db.tables.User;
import org.mycontroller.standalone.externalserver.ExternalServerFactory.EXTERNAL_SERVER_TYPE;
import org.mycontroller.standalone.gateway.GatewayUtils;
import org.mycontroller.standalone.gateway.GatewayUtils.GATEWAY_TYPE;
import org.mycontroller.standalone.message.McMessageUtils;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_INTERNAL;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_PRESENTATION;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_SET_REQ;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_STREAM;
import org.mycontroller.standalone.metrics.METRIC_ENGINE;
import org.mycontroller.standalone.metrics.MetricsUtils.METRIC_TYPE;
import org.mycontroller.standalone.model.ResourceModel;
import org.mycontroller.standalone.operation.OperationUtils.OPERATION_TYPE;
import org.mycontroller.standalone.rule.RuleUtils;
import org.mycontroller.standalone.rule.RuleUtils.CONDITION_TYPE;
import org.mycontroller.standalone.rule.RuleUtils.DAMPENING_TYPE;
import org.mycontroller.standalone.rule.RuleUtils.DATA_TYPE;
import org.mycontroller.standalone.rule.RuleUtils.OPERATOR;
import org.mycontroller.standalone.rule.RuleUtils.STRING_OPERATOR;
import org.mycontroller.standalone.timer.TimerUtils.FREQUENCY_TYPE;
import org.mycontroller.standalone.timer.TimerUtils.TIMER_TYPE;
import org.mycontroller.standalone.timer.TimerUtils.WEEK_DAY;
import org.mycontroller.standalone.units.UnitUtils;
import org.mycontroller.standalone.units.UnitUtils.UNIT_TYPE;

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
                        .displayName(McObjectManager.getMcLocale().getString(type.name())).build());
            } else if (metricTypes.contains(McMessageUtils.getMetricType(type).getText())) {
                typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.getText())
                        .displayName(McObjectManager.getMcLocale().getString(type.name())).build());
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
                        .displayName(McObjectManager.getMcLocale().getString(type.name())).build());
            }
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getMetricTypes() {
        METRIC_TYPE[] types = METRIC_TYPE.values();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (METRIC_TYPE type : types) {
            typesIdNameMappers.add(TypesIdNameMapper
                    .builder()
                    .id(type.getText())
                    //.displayName(McObjectManager.getMcLocale().getString(type.name()))
                    .build());
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getUnitTypes() {
        UNIT_TYPE[] types = UNIT_TYPE.values();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (UNIT_TYPE type : types) {
            typesIdNameMappers.add(TypesIdNameMapper
                    .builder()
                    .id(type.getText())
                    .displayName(type.getText()
                            + (type == UNIT_TYPE.U_NONE ? "" : " (" + UnitUtils.getUnit(type).getUnit() + ")"))
                    .build());
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getNodeTypes() {
        MESSAGE_TYPE_PRESENTATION[] types = MESSAGE_TYPE_PRESENTATION.values();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (MESSAGE_TYPE_PRESENTATION type : types) {
            if (type.name().contains(NODE_IDENTIFIER)) {
                typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.getText())
                        .displayName(McObjectManager.getMcLocale().getString(type.name())).build());
            }
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getNodeRegistrationStatuses() {
        NODE_REGISTRATION_STATE[] types = NODE_REGISTRATION_STATE.values();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (NODE_REGISTRATION_STATE type : types) {
            typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.getText())
                    .displayName(McObjectManager.getMcLocale().getString(type.name())).build());
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getRuleOperatorTypes(String conditionTypeString,
            String resourceTypeString) {
        OPERATOR[] operators = OPERATOR.values();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        CONDITION_TYPE conditionType = conditionTypeString != null ?
                CONDITION_TYPE.fromString(conditionTypeString) : null;
        RESOURCE_TYPE resourceType = resourceTypeString != null ?
                RESOURCE_TYPE.fromString(resourceTypeString) : null;

        if (conditionType != null &&
                (conditionType == CONDITION_TYPE.THRESHOLD
                        || conditionType == CONDITION_TYPE.COMPARE
                        || conditionType == CONDITION_TYPE.STATE)) {
            for (OPERATOR operator : operators) {
                switch (conditionType) {
                    case THRESHOLD:
                    case COMPARE:
                        typesIdNameMappers.add(TypesIdNameMapper.builder().id(operator.getText())
                                .displayName(operator.getText()).build());
                        break;
                    case STATE:
                        if (operator == OPERATOR.EQ || operator == OPERATOR.NEQ) {
                            typesIdNameMappers.add(TypesIdNameMapper.builder().id(operator.getText())
                                    .displayName(operator.getText()).build());
                        }
                        break;
                    default:
                        break;
                }

            }
        } else if (conditionType != null
                && conditionType == CONDITION_TYPE.STRING) {
            for (STRING_OPERATOR stringOperator : STRING_OPERATOR.values()) {
                typesIdNameMappers.add(TypesIdNameMapper.builder().id(stringOperator.getText())
                        .displayName(stringOperator.getText()).build());
            }
        } else if (resourceType != null) {
            for (OPERATOR operator : operators) {
                switch (resourceType) {
                    case GATEWAY:
                    case NODE:
                    case RESOURCES_GROUP:
                        if (operator == OPERATOR.EQ || operator == OPERATOR.NEQ) {
                            typesIdNameMappers
                                    .add(TypesIdNameMapper.builder().id(operator.getText())
                                            .displayName(operator.getText())
                                            .build());
                        }
                        break;
                    case SENSOR_VARIABLE:
                        typesIdNameMappers.add(TypesIdNameMapper.builder().id(operator.getText())
                                .displayName(operator.getText())
                                .build());
                        break;

                    case RULE_DEFINITION:
                    case SENSOR:
                    case TIMER:
                    default:
                        //Do not add anything...
                        break;
                }

            }
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getOperationTypes() {
        OPERATION_TYPE[] types = OPERATION_TYPE.values();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (OPERATION_TYPE notificationType : types) {
            typesIdNameMappers.add(TypesIdNameMapper.builder().id(notificationType.getText())
                    .displayName(notificationType.getText()).build());
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getThresholdDataTypes(String resourceTypeString) {
        DATA_TYPE[] types = DATA_TYPE.values();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        RESOURCE_TYPE resourceType = RESOURCE_TYPE.fromString(resourceTypeString);
        if (resourceType == null) {
            resourceType = RESOURCE_TYPE.SENSOR_VARIABLE;
        }
        for (DATA_TYPE type : types) {
            switch (resourceType) {
                case GATEWAY:
                case NODE:
                case RESOURCES_GROUP:
                    if (type == DATA_TYPE.VALUE) {
                        typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.getText())
                                .displayName(type.getText()).build());
                    }
                    break;
                case SENSOR_VARIABLE:
                    if (type == DATA_TYPE.VALUE || type == DATA_TYPE.SENSOR_VARIABLE) {
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

    public static ArrayList<TypesIdNameMapper> getRuleConditionTypes() {
        CONDITION_TYPE[] types = CONDITION_TYPE.values();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (CONDITION_TYPE type : types) {
            typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.getText()).displayName(type.getText())
                    .build());
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getRuleDampeningTypes() {
        DAMPENING_TYPE[] types = DAMPENING_TYPE.values();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (DAMPENING_TYPE type : types) {
            typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.getText()).displayName(type.getText())
                    .build());
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getPayloadOperations(String resourceTypeString) {
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        RESOURCE_TYPE resourceType = RESOURCE_TYPE.fromString(resourceTypeString);
        ArrayList<SEND_PAYLOAD_OPERATIONS> operations = new ArrayList<SEND_PAYLOAD_OPERATIONS>();
        if (resourceType == null) {
            resourceType = RESOURCE_TYPE.SENSOR_VARIABLE;
        }
        switch (resourceType) {
            case RULE_DEFINITION:
            case TIMER:
            case FORWARD_PAYLOAD:
                operations.add(SEND_PAYLOAD_OPERATIONS.ENABLE);
                operations.add(SEND_PAYLOAD_OPERATIONS.DISABLE);
                break;
            case GATEWAY:
                operations.add(SEND_PAYLOAD_OPERATIONS.ENABLE);
                operations.add(SEND_PAYLOAD_OPERATIONS.DISABLE);
                operations.add(SEND_PAYLOAD_OPERATIONS.RELOAD);
                operations.add(SEND_PAYLOAD_OPERATIONS.START);
                operations.add(SEND_PAYLOAD_OPERATIONS.STOP);
                break;
            case NODE:
                operations.add(SEND_PAYLOAD_OPERATIONS.REBOOT);
                break;
            case RESOURCES_GROUP:
                operations.add(SEND_PAYLOAD_OPERATIONS.ON);
                operations.add(SEND_PAYLOAD_OPERATIONS.OFF);
                break;
            case SENSOR_VARIABLE:
                operations.add(SEND_PAYLOAD_OPERATIONS.INCREMENT);
                operations.add(SEND_PAYLOAD_OPERATIONS.DECREMENT);
                operations.add(SEND_PAYLOAD_OPERATIONS.TOGGLE);
                break;
            default:
                break;
        }
        for (SEND_PAYLOAD_OPERATIONS operation : operations) {
            typesIdNameMappers.add(TypesIdNameMapper.builder().id("sp:" + operation.getText())
                    .displayName(operation.getText()).build());
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
                            typesIdNameMappers.add(TypesIdNameMapper.builder()
                                    .id(type.getText()).displayName(type.getText()).build());
                        }
                        break;
                    case RESOURCES_GROUP:
                    case RULE_DEFINITION:
                    case TIMER:
                    case SENSOR_VARIABLE:
                        if (type == STATE.ON || type == STATE.OFF) {
                            typesIdNameMappers.add(TypesIdNameMapper.builder()
                                    .id(type.getText()).displayName(type.getText()).build());
                        }
                        break;
                    default:
                        //Do not add anything
                        break;
                }
            } else {
                typesIdNameMappers.add(TypesIdNameMapper.builder()
                        .id(type.getText()).displayName(type.getText()).build());
            }
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getTimerTypes() {
        TIMER_TYPE[] types = TIMER_TYPE.values();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (TIMER_TYPE timerType : types) {
            typesIdNameMappers.add(TypesIdNameMapper.builder()
                    .id(timerType.getText()).displayName(McObjectManager.getMcLocale().getString(timerType.name()))
                    .build());
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getTimerFrequencyTypes() {
        FREQUENCY_TYPE[] types = FREQUENCY_TYPE.values();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (FREQUENCY_TYPE type : types) {
            typesIdNameMappers.add(TypesIdNameMapper.builder()
                    .id(type.getText()).displayName(type.getText()).build());
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getTrustHostTypes() {
        TRUST_HOST_TYPE[] types = TRUST_HOST_TYPE.values();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (TRUST_HOST_TYPE type : types) {
            typesIdNameMappers.add(TypesIdNameMapper.builder()
                    .id(type.getText()).displayName(McObjectManager.getMcLocale().getString(type.name())).build());
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getTimerWeekDays(boolean isAllDaysTicked) {
        WEEK_DAY[] types = WEEK_DAY.values();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (WEEK_DAY type : types) {
            typesIdNameMappers.add(TypesIdNameMapper.builder()
                    .id(type.getText()).displayName(type.getText()).ticked(isAllDaysTicked).build());
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getResourceTypes(User user, String resourceType, String operationType,
            String conditionTypeString) {
        RESOURCE_TYPE[] types = RESOURCE_TYPE.values();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        RESOURCE_TYPE resourceTypeFilter = null;
        OPERATION_TYPE operationTypeFilter = null;
        CONDITION_TYPE conditionType = CONDITION_TYPE.fromString(conditionTypeString);
        if (resourceType != null && resourceType.equalsIgnoreCase("Resource data")) {
            for (RESOURCE_TYPE type : types) {
                if (type == RESOURCE_TYPE.GATEWAY
                        || type == RESOURCE_TYPE.NODE
                        || type == RESOURCE_TYPE.SENSOR
                        || type == RESOURCE_TYPE.SENSOR_VARIABLE) {
                    typesIdNameMappers.add(TypesIdNameMapper.builder()
                            .id(type.getText()).displayName(type.getText()).build());

                }
            }
        } else if (conditionType != null) {
            for (RESOURCE_TYPE type : types) {
                switch (conditionType) {
                    case STATE:
                        if (type == RESOURCE_TYPE.GATEWAY
                                || type == RESOURCE_TYPE.NODE
                                || type == RESOURCE_TYPE.SENSOR_VARIABLE
                                || type == RESOURCE_TYPE.RESOURCES_GROUP) {
                            typesIdNameMappers.add(TypesIdNameMapper.builder()
                                    .id(type.getText()).displayName(type.getText()).build());
                        }
                        break;
                    default:
                        break;
                }
            }
        } else {
            if (resourceType != null) {
                resourceTypeFilter = RESOURCE_TYPE.fromString(resourceType);
            }
            if (operationType != null) {
                operationTypeFilter = OPERATION_TYPE.fromString(operationType);
            }

            for (RESOURCE_TYPE type : types) {
                if (resourceTypeFilter != null) {
                    if (!AuthUtils.isSuperAdmin(user)) {
                        if (type == RESOURCE_TYPE.RESOURCES_GROUP) {
                            break;
                        }
                    }
                    switch (resourceTypeFilter) {
                        case RULE_DEFINITION:
                        case TIMER:
                            if (type != RESOURCE_TYPE.SENSOR && type != RESOURCE_TYPE.RULE_DEFINITION
                                    && type != RESOURCE_TYPE.TIMER && type != RESOURCE_TYPE.UID_TAG) {
                                typesIdNameMappers.add(TypesIdNameMapper.builder()
                                        .id(type.getText()).displayName(type.getText()).build());
                            }
                            break;
                        case RESOURCES_GROUP:
                            if (type != RESOURCE_TYPE.RULE_DEFINITION && type != RESOURCE_TYPE.TIMER
                                    && type != RESOURCE_TYPE.SENSOR && type != RESOURCE_TYPE.UID_TAG
                                    && type != RESOURCE_TYPE.RESOURCES_GROUP) {
                                typesIdNameMappers.add(TypesIdNameMapper.builder()
                                        .id(type.getText()).displayName(type.getText()).build());
                            }
                            break;
                        case UID_TAG:
                            if (type != RESOURCE_TYPE.RULE_DEFINITION && type != RESOURCE_TYPE.TIMER
                                    && type != RESOURCE_TYPE.SENSOR && type != RESOURCE_TYPE.RESOURCES_GROUP
                                    && type != RESOURCE_TYPE.UID_TAG && type != RESOURCE_TYPE.SCRIPT) {
                                typesIdNameMappers.add(TypesIdNameMapper.builder()
                                        .id(type.getText()).displayName(type.getText()).build());
                            }
                            break;
                        default:
                            typesIdNameMappers.add(TypesIdNameMapper.builder()
                                    .id(type.getText()).displayName(type.getText()).build());
                            break;
                    }
                } else if (operationTypeFilter != null) {
                    switch (operationTypeFilter) {
                        case REQUEST_PAYLOAD:
                            if (type == RESOURCE_TYPE.SENSOR_VARIABLE) {
                                typesIdNameMappers.add(TypesIdNameMapper.builder()
                                        .id(type.getText()).displayName(type.getText()).build());
                            }
                            break;
                        case SEND_PAYLOAD:
                            if (type != RESOURCE_TYPE.SENSOR && type != RESOURCE_TYPE.SCRIPT
                                    && type != RESOURCE_TYPE.UID_TAG) {
                                typesIdNameMappers.add(TypesIdNameMapper.builder()
                                        .id(type.getText()).displayName(type.getText()).build());
                            }
                            break;
                        default:
                            break;
                    }
                } else {
                    typesIdNameMappers.add(TypesIdNameMapper.builder()
                            .id(type.getText()).displayName(type.getText()).build());
                }
            }

        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getResources(User user, String resourceType, String filter, long page,
            long pageLimit) {
        Query query = Query.builder()
                .page(page)
                .pageLimit(pageLimit)
                .build();
        if (resourceType == null) {
            return null;
        }
        RESOURCE_TYPE resourceTypeEnum = RESOURCE_TYPE.fromString(resourceType);
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        StringBuilder builder = new StringBuilder();
        switch (resourceTypeEnum) {
            case GATEWAY:
                List<GatewayTable> gateways = null;
                if (AuthUtils.isSuperAdmin(user)) {
                    gateways = DaoUtils.getGatewayDao().getAll(query, filter, null);
                } else {
                    gateways = DaoUtils.getGatewayDao().getAll(query, filter, user.getAllowedResources());
                }
                for (GatewayTable type : gateways) {
                    typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.getId()).displayName(type.getName())
                            .build());
                }
                break;
            case NODE:
                List<Node> nodes = null;
                if (AuthUtils.isSuperAdmin(user)) {
                    nodes = DaoUtils.getNodeDao().getAll(query, filter, null);
                } else {
                    nodes = DaoUtils.getNodeDao().getAll(query, filter, user.getAllowedResources());
                }
                for (Node type : nodes) {
                    builder.setLength(0);
                    builder.append(type.getGatewayTable().getName()).append(" -> ")
                            .append("[").append(type.getEui()).append("]").append(type.getName());
                    typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.getId())
                            .displayName(builder.toString()).build());
                }
                break;
            case SENSOR:
                List<Sensor> sensors = null;
                if (AuthUtils.isSuperAdmin(user)) {
                    sensors = DaoUtils.getSensorDao().getAll(query, filter, null);
                } else {
                    sensors = DaoUtils.getSensorDao().getAll(query, filter, user.getAllowedResources());
                }
                for (Sensor type : sensors) {
                    builder.setLength(0);
                    builder.append(type.getNode().getGatewayTable().getName()).append(" -> ")
                            .append("[").append(type.getNode().getEui()).append("]").append(type.getNode().getName())
                            .append(" -> [").append(type.getSensorId()).append("]").append(type.getName());
                    typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.getId())
                            .displayName(builder.toString()).build());
                }
                break;
            case SENSOR_VARIABLE:
                List<SensorVariable> sensorVariables = null;
                if (AuthUtils.isSuperAdmin(user)) {
                    sensorVariables = DaoUtils.getSensorVariableDao().getAll(query, filter, null);
                } else {
                    sensorVariables = DaoUtils.getSensorVariableDao()
                            .getAll(query, filter, user.getAllowedResources());
                }
                for (SensorVariable type : sensorVariables) {
                    builder.setLength(0);
                    builder.append(type.getSensor().getNode().getGatewayTable().getName())
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

    public static ArrayList<TypesIdNameMapper> getGateways(User user, String filter, long page, long pageLimit) {
        Query query = Query.builder()
                .page(page)
                .pageLimit(pageLimit)
                .build();
        List<GatewayTable> gateways = null;
        if (AuthUtils.isSuperAdmin(user)) {
            gateways = DaoUtils.getGatewayDao().getAll(query, filter, null);
        } else {
            gateways = DaoUtils.getGatewayDao().getAll(query, filter, user.getAllowedResources());
        }
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (GatewayTable gatewayTable : gateways) {
            typesIdNameMappers.add(TypesIdNameMapper.builder().id(gatewayTable.getId())
                    .displayName(new ResourceModel(RESOURCE_TYPE.GATEWAY, gatewayTable).getResourceLessDetails())
                    .build());
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getNodes(User user, Integer gatewayId, String filter, long page,
            long pageLimit) {
        Query query = Query.builder()
                .page(page)
                .pageLimit(pageLimit)
                .build();
        List<Node> nodes = null;
        if (AuthUtils.isSuperAdmin(user)) {
            if (gatewayId != null) {
                query.getFilters().put(Node.KEY_GATEWAY_ID, gatewayId);
                nodes = DaoUtils.getNodeDao().getAll(query, filter, null);
            } else {
                nodes = DaoUtils.getNodeDao().getAll(query, filter, null);
            }
        } else {
            nodes = DaoUtils.getNodeDao().getAll(query, filter, user.getAllowedResources());
        }

        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (Node node : nodes) {
            typesIdNameMappers.add(TypesIdNameMapper.builder().id(node.getId())
                    .displayName(new ResourceModel(RESOURCE_TYPE.NODE, node).getResourceLessDetails()).build());
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getExternalServers(User user) {
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        List<ExternalServerTable> externalServerTable = null;
        if (AuthUtils.isSuperAdmin(user)) {
            externalServerTable = DaoUtils.getExternalServerTableDao().getAll();
        } else {
            return typesIdNameMappers;
        }
        if (externalServerTable != null) {
            for (ExternalServerTable serverTable : externalServerTable) {
                typesIdNameMappers.add(TypesIdNameMapper.builder().id(serverTable.getId())
                        .displayName(serverTable.getName()).build());
            }
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

    public static ArrayList<TypesIdNameMapper> getForwardPayloads(User user) {
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        List<ForwardPayload> items = null;
        if (AuthUtils.isSuperAdmin(user)) {
            items = new ForwardPayloadApi().getAll();
        } else {
            return typesIdNameMappers;
        }
        for (ForwardPayload item : items) {
            typesIdNameMappers.add(TypesIdNameMapper.builder().id(item.getId())
                    .displayName(new ResourceModel(RESOURCE_TYPE.FORWARD_PAYLOAD, item).getResourceLessDetails())
                    .build());
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

    public static ArrayList<TypesIdNameMapper> getRuleDefinitions(User user) {
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        List<RuleDefinitionTable> ruleDefinitionTables = null;
        if (AuthUtils.isSuperAdmin(user)) {
            ruleDefinitionTables = DaoUtils.getRuleDefinitionDao().getAll();
        } else {
            //TODO: filter resource based on users role
            return typesIdNameMappers;
        }
        for (RuleDefinitionTable ruleDefinitionTable : ruleDefinitionTables) {
            typesIdNameMappers.add(TypesIdNameMapper
                    .builder()
                    .id(ruleDefinitionTable.getId())
                    .displayName(new ResourceModel(RESOURCE_TYPE.RULE_DEFINITION, RuleUtils
                            .getRuleDefinition(ruleDefinitionTable)).getResourceLessDetails()).build());
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getSensors(User user, Integer nodeId, Integer roomId,
            Boolean enableNoRoomFilter, String filter, long page, long pageLimit) {
        Query query = Query.builder()
                .page(page)
                .pageLimit(pageLimit)
                .build();
        List<Node> nodes = new ArrayList<Node>();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        if (enableNoRoomFilter == null) {
            enableNoRoomFilter = false;
        }
        List<Sensor> sensors = null;
        if (AuthUtils.isSuperAdmin(user)) {
            if (nodeId != null) {
                nodes.add(Node.builder().id(nodeId).build());
                query.getFilters().put(Sensor.KEY_NODE_ID, nodeId);
                sensors = DaoUtils.getSensorDao().getAll(query, filter, null);
            } else {
                sensors = DaoUtils.getSensorDao().getAll(query, filter, null);
            }
        } else {
            sensors = DaoUtils.getSensorDao().getAll(query, filter, user.getAllowedResources());
        }

        boolean include = false;
        for (Sensor sensor : sensors) {
            include = false;
            if (enableNoRoomFilter) {
                if (sensor.getRoom() == null) {
                    include = true;
                } else if (roomId != null && roomId.equals(sensor.getRoom().getId())) {
                    include = true;
                }
            } else if (roomId != null && roomId.equals(sensor.getRoom().getId())) {
                include = true;
            } else {
                include = true;
            }

            if (include) {
                typesIdNameMappers.add(TypesIdNameMapper.builder().id(sensor.getId()).subId(sensor.getSensorId())
                        .displayName(new ResourceModel(RESOURCE_TYPE.SENSOR, sensor).getResourceLessDetails())
                        .build());
            }

        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getRooms(Integer selfId) {
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        List<Room> rooms = DaoUtils.getRoomDao().getAll();
        if (rooms != null) {
            for (Room room : rooms) {
                if (selfId != null && selfId.equals(room.getId())) {
                    //Nothing to do
                } else {
                    typesIdNameMappers.add(TypesIdNameMapper.builder()
                            .id(room.getId())
                            .subId(room.getName())
                            .displayName(room.getFullPath())
                            .build());
                }
            }
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getSensorVariables(User user, Integer sensorId,
            Integer sensorVariableId, List<String> variableTypes, List<String> metricTypes, String filter, long page,
            long pageLimit) throws IllegalAccessException {
        Query query = Query.builder()
                .page(page)
                .pageLimit(pageLimit)
                .build();
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
            query.getFilters().put(SensorVariable.KEY_ID, sensorVariableId);
            sensorVariables = DaoUtils.getSensorVariableDao().getAll(query, filter, null);
        } else if (sensorId != null) {
            query.getFilters().put(SensorVariable.KEY_SENSOR_DB_ID, sensorId);
            sensorVariables = DaoUtils.getSensorVariableDao().getAll(query, filter, null);
        } else if (sensorVariables == null) {
            sensorVariables = DaoUtils.getSensorVariableDao().getAll(query, filter, null);
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
                        .displayName(McObjectManager.getMcLocale().getString(variableType.getVariableType().name()))
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
                    .add(TypesIdNameMapper.builder().id(type.getText())
                            .displayName(McObjectManager.getMcLocale().getString(type.name()))
                            .build());
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getMessageSubTypes(MESSAGE_TYPE messageType) {
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        switch (messageType) {
            case C_INTERNAL:
                MESSAGE_TYPE_INTERNAL[] typesInt = MESSAGE_TYPE_INTERNAL.values();
                for (MESSAGE_TYPE_INTERNAL type : typesInt) {
                    typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.getText())
                            .displayName(McObjectManager.getMcLocale().getString(type.name()))
                            .build());
                }
                break;
            case C_PRESENTATION:
                MESSAGE_TYPE_PRESENTATION[] typesPre = MESSAGE_TYPE_PRESENTATION.values();
                for (MESSAGE_TYPE_PRESENTATION type : typesPre) {
                    typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.getText())
                            .displayName(McObjectManager.getMcLocale().getString(type.name()))
                            .build());
                }
                break;
            case C_REQ:
            case C_SET:
                MESSAGE_TYPE_SET_REQ[] typesSetReq = MESSAGE_TYPE_SET_REQ.values();
                for (MESSAGE_TYPE_SET_REQ type : typesSetReq) {
                    typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.getText())
                            .displayName(McObjectManager.getMcLocale().getString(type.name()))
                            .build());
                }
                break;
            case C_STREAM:
                MESSAGE_TYPE_STREAM[] typesStr = MESSAGE_TYPE_STREAM.values();
                for (MESSAGE_TYPE_STREAM type : typesStr) {
                    typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.getText())
                            .displayName(McObjectManager.getMcLocale().getString(type.name()))
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
        MC_LANGUAGE selected = AppProperties.getInstance().getLanguage();
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

    public static ArrayList<TypesIdNameMapper> getGatewayTypes(String networkType) {
        NETWORK_TYPE nwType = null;
        if (networkType != null) {
            nwType = NETWORK_TYPE.fromString(networkType);
        }
        ArrayList<GATEWAY_TYPE> gatewayTypes = new ArrayList<GatewayUtils.GATEWAY_TYPE>();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        if (nwType != null) {
            switch (nwType) {
                case MY_SENSORS:
                    gatewayTypes.add(GATEWAY_TYPE.ETHERNET);
                    gatewayTypes.add(GATEWAY_TYPE.MQTT);
                    gatewayTypes.add(GATEWAY_TYPE.SERIAL);
                    break;
                case PHANT_IO:
                    gatewayTypes.add(GATEWAY_TYPE.PHANT_IO);
                    break;
                case MY_CONTROLLER:
                    gatewayTypes.add(GATEWAY_TYPE.MQTT);
                    break;
                case RF_LINK:
                    gatewayTypes.add(GATEWAY_TYPE.SERIAL);
                    break;
                case PHILIPS_HUE:
                    gatewayTypes.add(GATEWAY_TYPE.PHILIPS_HUE);
                    break;
                case WUNDERGROUND:
                    gatewayTypes.add(GATEWAY_TYPE.WUNDERGROUND);
                    break;
                default:
                    break;
            }
        } else {
            for (GATEWAY_TYPE type : GATEWAY_TYPE.values()) {
                gatewayTypes.add(type);
            }
        }
        for (GATEWAY_TYPE type : gatewayTypes) {
            typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.getText())
                    .displayName(McObjectManager.getMcLocale().getString(type.name())).build());
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

    public static ArrayList<TypesIdNameMapper> getExternalServerTypes() {
        EXTERNAL_SERVER_TYPE[] types = EXTERNAL_SERVER_TYPE.values();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (EXTERNAL_SERVER_TYPE type : types) {
            typesIdNameMappers.add(TypesIdNameMapper.builder().id(type.getText()).displayName(type.getText()).build());
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getMetricEngineTypes() {
        METRIC_ENGINE[] types = METRIC_ENGINE.values();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (METRIC_ENGINE type : types) {
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
        for (String key : McMessageUtils.HVAC_OPTIONS_FLOW_STATE.keySet()) {
            typesIdNameMappers.add(TypesIdNameMapper.builder().id(key)
                    .displayName(McMessageUtils.HVAC_OPTIONS_FLOW_STATE.get(key)).build());
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getHvacOptionsFlowMode() {
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (String key : McMessageUtils.HVAC_OPTIONS_FLOW_MODE.keySet()) {
            typesIdNameMappers.add(TypesIdNameMapper.builder().id(key)
                    .displayName(McMessageUtils.HVAC_OPTIONS_FLOW_MODE.get(key)).build());
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getHvacOptionsFanSpeed() {
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (String key : McMessageUtils.HVAC_OPTIONS_FAN_SPEED.keySet()) {
            typesIdNameMappers.add(TypesIdNameMapper.builder().id(key)
                    .displayName(McMessageUtils.HVAC_OPTIONS_FAN_SPEED.get(key)).build());
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

    public static ArrayList<TypesIdNameMapper> getOperations() {
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        List<OperationTable> operationTables = DaoUtils.getOperationDao().getAll();
        for (OperationTable operationTable : operationTables) {
            typesIdNameMappers.add(TypesIdNameMapper.builder().id(operationTable.getId())
                    .displayName(operationTable.getName()).build());
        }
        return typesIdNameMappers;
    }
}
