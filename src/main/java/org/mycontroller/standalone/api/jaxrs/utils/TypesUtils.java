/**
 * Copyright (C) 2015 Jeeva Kandasamy (jkandasa@gmail.com)
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
import java.util.Arrays;
import java.util.List;

import org.mycontroller.standalone.api.jaxrs.mapper.KeyValueJson;
import org.mycontroller.standalone.api.jaxrs.mapper.TypesIdNameMapper;
import org.mycontroller.standalone.db.AlarmUtils.DAMPENING_TYPE;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.SensorUtils;
import org.mycontroller.standalone.db.AlarmUtils.TRIGGER;
import org.mycontroller.standalone.db.AlarmUtils.TYPE;
import org.mycontroller.standalone.db.TimerUtils.FREQUENCY;
import org.mycontroller.standalone.db.TimerUtils.WEEK_DAY;
import org.mycontroller.standalone.db.TypeUtils.METRIC_TYPE;
import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.db.tables.SensorValue;
import org.mycontroller.standalone.db.tables.SensorsVariablesMap;
import org.mycontroller.standalone.mysensors.MyMessages.MESSAGE_TYPE;
import org.mycontroller.standalone.mysensors.MyMessages.MESSAGE_TYPE_INTERNAL;
import org.mycontroller.standalone.mysensors.MyMessages.MESSAGE_TYPE_PRESENTATION;
import org.mycontroller.standalone.mysensors.MyMessages.MESSAGE_TYPE_SET_REQ;
import org.mycontroller.standalone.mysensors.MyMessages.MESSAGE_TYPE_STREAM;

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
            typesIdNameMappers.add(new TypesIdNameMapper(type.ordinal(), type.name()));
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getSensorTypes() {
        MESSAGE_TYPE_PRESENTATION[] types = MESSAGE_TYPE_PRESENTATION.values();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (MESSAGE_TYPE_PRESENTATION type : types) {
            if (!type.name().contains(NODE_IDENTIFIER)) {
                typesIdNameMappers.add(new TypesIdNameMapper(type.ordinal(), type.name()));
            }
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getNodeTypes() {
        MESSAGE_TYPE_PRESENTATION[] types = MESSAGE_TYPE_PRESENTATION.values();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (MESSAGE_TYPE_PRESENTATION type : types) {
            if (type.name().contains(NODE_IDENTIFIER)) {
                typesIdNameMappers.add(new TypesIdNameMapper(type.ordinal(), type.name()));
            }
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getAlarmTriggerTypes() {
        TRIGGER[] triggers = TRIGGER.values();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (TRIGGER trigger : triggers) {
            typesIdNameMappers.add(new TypesIdNameMapper(trigger.ordinal(), trigger.value()));
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getAlarmTypes() {
        TYPE[] types = TYPE.values();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (TYPE type : types) {
            typesIdNameMappers.add(new TypesIdNameMapper(type.ordinal(), type.value()));
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getAlarmDampeningTypes() {
        DAMPENING_TYPE[] types = DAMPENING_TYPE.values();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (DAMPENING_TYPE type : types) {
            typesIdNameMappers.add(new TypesIdNameMapper(type.ordinal(), type.value()));
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getTimerTypes() {
        org.mycontroller.standalone.db.TimerUtils.TYPE[] types = org.mycontroller.standalone.db.TimerUtils.TYPE
                .values();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (org.mycontroller.standalone.db.TimerUtils.TYPE type : types) {
            typesIdNameMappers.add(new TypesIdNameMapper(type.ordinal(), type.value()));
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getTimerFrequencies() {
        FREQUENCY[] types = FREQUENCY.values();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (FREQUENCY type : types) {
            typesIdNameMappers.add(new TypesIdNameMapper(type.ordinal(), type.value()));
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getTimerDays(boolean isAllDaysTicked) {
        WEEK_DAY[] types = WEEK_DAY.values();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (WEEK_DAY type : types) {
            typesIdNameMappers.add(new TypesIdNameMapper(type.ordinal(), type.value(), isAllDaysTicked));
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getNodes() {
        List<Node> nodes = DaoUtils.getNodeDao().getAll();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (Node node : nodes) {
            typesIdNameMappers.add(new TypesIdNameMapper(node.getId(), "[" + node.getId() + "] " + node.getName()));
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getSensors(int nodeId) {
        List<Sensor> sensors = DaoUtils.getSensorDao().getAll(nodeId);
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (Sensor sensor : sensors) {
            typesIdNameMappers.add(new TypesIdNameMapper(sensor.getId(), sensor.getSensorId(), "["
                    + sensor.getSensorId() + "] " + sensor.getName()));
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

    public static ArrayList<TypesIdNameMapper> getSensorVariableTypes(int sensorType, String tickDisplayNames) {
        List<SensorsVariablesMap> variableTypes = DaoUtils.getSensorsVariablesMapDao().getAll(sensorType);
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        List<String> tickNames = null;
        if (tickDisplayNames != null) {
            tickNames = Arrays.asList(tickDisplayNames.split(SensorUtils.VARIABLE_TYPE_SPLITER));
        }
        for (SensorsVariablesMap variableType : variableTypes) {
            boolean isTicked = false;
            if (tickDisplayNames != null) {
                if (tickNames.contains(variableType.getVariableTypeString())) {
                    isTicked = true;
                }
            }
            typesIdNameMappers.add(new TypesIdNameMapper(
                    variableType.getVariableType(), variableType.getVariableTypeString(), isTicked));
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getGraphSensorVariableTypes(int sensorRefId) {
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        List<SensorValue> sensorValues = DaoUtils.getSensorValueDao().getAll(sensorRefId);
        for (SensorValue sensorValue : sensorValues) {
            if (sensorValue.getMetricType() != METRIC_TYPE.NONE.ordinal()) {
                typesIdNameMappers.add(new TypesIdNameMapper(
                        sensorValue.getId(),
                        sensorValue.getSensor().getId(),
                        MESSAGE_TYPE_SET_REQ.get(sensorValue.getVariableType()).toString()));
            }
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getSensorVariableTypesAll(int sensorTypeId) {
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        List<SensorsVariablesMap> variablesMap = DaoUtils.getSensorsVariablesMapDao().getAll(sensorTypeId);
        String variables = null;
        for (SensorsVariablesMap variableMap : variablesMap) {
            if (variables != null) {
                variables += SensorUtils.VARIABLE_TYPE_SPLITER + variableMap.getVariableTypeString();
            } else {
                variables = variableMap.getVariableTypeString();
            }
        }

        MESSAGE_TYPE_SET_REQ[] types = MESSAGE_TYPE_SET_REQ.values();
        for (MESSAGE_TYPE_SET_REQ type : types) {
            typesIdNameMappers.add(new TypesIdNameMapper(
                    type.ordinal(),
                    type.name(),
                    variables != null ? variables.contains(type.toString()) : false));
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getMessageTypes() {
        MESSAGE_TYPE[] types = MESSAGE_TYPE.values();
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        for (MESSAGE_TYPE type : types) {
            typesIdNameMappers.add(new TypesIdNameMapper(type.ordinal(), type.toString()));
        }
        return typesIdNameMappers;
    }

    public static ArrayList<TypesIdNameMapper> getMessageSubTypes(int messageType) {
        ArrayList<TypesIdNameMapper> typesIdNameMappers = new ArrayList<TypesIdNameMapper>();
        switch (MESSAGE_TYPE.get(messageType)) {
            case C_INTERNAL:
                MESSAGE_TYPE_INTERNAL[] typesInt = MESSAGE_TYPE_INTERNAL.values();
                for (MESSAGE_TYPE_INTERNAL type : typesInt) {
                    typesIdNameMappers.add(new TypesIdNameMapper(type.ordinal(), type.toString()));
                }
                break;
            case C_PRESENTATION:
                MESSAGE_TYPE_PRESENTATION[] typesPre = MESSAGE_TYPE_PRESENTATION.values();
                for (MESSAGE_TYPE_PRESENTATION type : typesPre) {
                    typesIdNameMappers.add(new TypesIdNameMapper(type.ordinal(), type.toString()));
                }
                break;
            case C_REQ:
            case C_SET:
                MESSAGE_TYPE_SET_REQ[] typesSetReq = MESSAGE_TYPE_SET_REQ.values();
                for (MESSAGE_TYPE_SET_REQ type : typesSetReq) {
                    typesIdNameMappers.add(new TypesIdNameMapper(type.ordinal(), type.toString()));
                }
                break;
            case C_STREAM:
                MESSAGE_TYPE_STREAM[] typesStr = MESSAGE_TYPE_STREAM.values();
                for (MESSAGE_TYPE_STREAM type : typesStr) {
                    typesIdNameMappers.add(new TypesIdNameMapper(type.ordinal(), type.toString()));
                }
                break;
            default:
                break;
        }

        return typesIdNameMappers;
    }

    public static List<KeyValueJson> getVariableMapperList() {
        List<KeyValueJson> keyValueJsons = new ArrayList<KeyValueJson>();
        StringBuilder builder = new StringBuilder();
        MESSAGE_TYPE_PRESENTATION[] types = MESSAGE_TYPE_PRESENTATION.values();
        for (MESSAGE_TYPE_PRESENTATION type : types) {
            if (!type.name().contains(NODE_IDENTIFIER)) {
                builder.setLength(0);
                List<SensorsVariablesMap> variablesMap = DaoUtils.getSensorsVariablesMapDao().getAll(type.ordinal());
                if (variablesMap != null) {
                    for (SensorsVariablesMap sensorsVariablesMap : variablesMap) {
                        if (builder.length() != 0) {
                            builder.append(SensorUtils.VARIABLE_TYPE_SPLITER).append(
                                    sensorsVariablesMap.getVariableTypeString());
                        } else {
                            builder.append(sensorsVariablesMap.getVariableTypeString());
                        }
                    }
                }
                keyValueJsons.add(new KeyValueJson(
                        type.toString(),
                        builder.toString(),
                        type.ordinal(),
                        org.mycontroller.standalone.api.jaxrs.mapper.KeyValueJson.TYPE.VARIABLE_MAPPER));
            }
        }
        return keyValueJsons;
    }

    public static void updateVariableMap(KeyValueJson keyValue) {
        //Delete existing map
        int sensorType = MESSAGE_TYPE_PRESENTATION.valueOf(keyValue.getKey()).ordinal();
        DaoUtils.getSensorsVariablesMapDao().delete(sensorType);
        if (keyValue.getValue() != null && keyValue.getValue().length() > 0) {
            //Create New Map
            String[] variables = keyValue.getValue().split(SensorUtils.VARIABLE_TYPE_SPLITER);
            for (String variable : variables) {
                DaoUtils.getSensorsVariablesMapDao().create(
                        sensorType,
                        MESSAGE_TYPE_SET_REQ.valueOf(variable).ordinal());
            }
        }
    }
}
