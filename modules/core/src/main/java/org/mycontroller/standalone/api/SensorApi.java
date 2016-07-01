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
package org.mycontroller.standalone.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.mycontroller.standalone.McObjectManager;
import org.mycontroller.standalone.api.jaxrs.json.Query;
import org.mycontroller.standalone.api.jaxrs.json.QueryResponse;
import org.mycontroller.standalone.api.jaxrs.json.SensorVariableJson;
import org.mycontroller.standalone.db.DB_QUERY;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.DeleteResourceUtils;
import org.mycontroller.standalone.db.SensorUtils;
import org.mycontroller.standalone.db.tables.Room;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.exceptions.McBadRequestException;
import org.mycontroller.standalone.exceptions.McDuplicateException;
import org.mycontroller.standalone.exceptions.McException;
import org.mycontroller.standalone.exceptions.McInvalidException;
import org.mycontroller.standalone.message.McMessage;
import org.mycontroller.standalone.message.McMessageUtils;
import org.mycontroller.standalone.metrics.MetricsUtils.METRIC_TYPE;
import org.mycontroller.standalone.utils.McUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Slf4j
public class SensorApi {

    public QueryResponse getAll(HashMap<String, Object> filters) {
        Query query = Query.get(filters);
        if (query.getOrderBy().equalsIgnoreCase(Sensor.KEY_NODE_EUI)) {
            query.setOrderByRawQuery(DB_QUERY.ORDER_BY_NODE_EUI);
        } else if (query.getOrderBy().equalsIgnoreCase(Sensor.KEY_NODE_NAME)) {
            query.setOrderByRawQuery(DB_QUERY.ORDER_BY_NODE_NAME);
        }
        return DaoUtils.getSensorDao().getAll(query);
    }

    public Sensor get(HashMap<String, Object> filters) {
        QueryResponse response = getAll(filters);
        @SuppressWarnings("unchecked")
        List<Sensor> items = (List<Sensor>) response.getData();
        if (items != null && !items.isEmpty()) {
            return items.get(0);
        }
        return null;
    }

    public Sensor get(int id) {
        return DaoUtils.getSensorDao().getById(id);
    }

    public void deleteIds(List<Integer> ids) {
        DeleteResourceUtils.deleteSensors(ids);
    }

    public void update(Sensor sensor) throws McException {
        Sensor availabilityCheck = DaoUtils.getSensorDao().get(sensor.getNode().getId(), sensor.getSensorId());
        if (availabilityCheck != null && sensor.getId() != availabilityCheck.getId()) {
            throw new McDuplicateException("A sensor available with this sensor id!");
        }
        try {
            if (McMessageUtils.validateSensorIdByProvider(sensor)) {
                DaoUtils.getSensorDao().update(sensor);
                // Update Variable Types
                SensorUtils.updateSensorVariables(sensor);
            }
        } catch (Exception ex) {
            throw new McException(ex);
        }

    }

    public void add(Sensor sensor) throws McException {
        Sensor availabilityCheck = DaoUtils.getSensorDao().get(sensor.getNode().getId(), sensor.getSensorId());
        if (availabilityCheck != null) {
            throw new McDuplicateException("A sensor available with this sensor id!");
        }
        try {
            //Take variable types reference
            List<String> variableTypes = sensor.getVariableTypes();
            if (McMessageUtils.validateSensorIdByProvider(sensor)) {
                DaoUtils.getSensorDao().create(sensor);
                sensor = DaoUtils.getSensorDao().get(sensor.getNode().getId(), sensor.getSensorId());
                // Update Variable Types
                sensor.setVariableTypes(variableTypes);
                //Update into database
                SensorUtils.updateSensorVariables(sensor);
            }
        } catch (Exception ex) {
            _logger.error("Exception,", ex);
            throw new McException(ex);
        }

    }

    public List<SensorVariableJson> getVariables(List<Integer> ids) {
        List<SensorVariable> sensorVariables = DaoUtils.getSensorVariableDao().getAll(ids);
        List<SensorVariableJson> sensorVariableJson = new ArrayList<SensorVariableJson>();
        //Convert to SensorVariableJson
        if (sensorVariables != null) {
            for (SensorVariable sensorVariable : sensorVariables) {
                sensorVariableJson.add(new SensorVariableJson(sensorVariable));
            }
        }
        return sensorVariableJson;
    }

    public SensorVariableJson getVariable(Integer id) {
        SensorVariable sensorVariable = DaoUtils.getSensorVariableDao().get(id);
        //Convert to SensorVariableJson
        return new SensorVariableJson(sensorVariable);
    }

    public void sendpayload(SensorVariableJson sensorVariableJson) throws McInvalidException, McBadRequestException {
        SensorVariable sensorVariable = DaoUtils.getSensorVariableDao().get(sensorVariableJson.getId());
        if (sensorVariable != null) {
            switch (sensorVariable.getMetricType()) {
                case BINARY:
                    if (McUtils.getBoolean(sensorVariableJson.getValue() == null)) {
                        throw new McInvalidException("Invalid value: " + sensorVariableJson.getValue());
                    }
                    break;
                case DOUBLE:
                    if (McUtils.getDouble(sensorVariableJson.getValue()) == null) {
                        throw new McInvalidException("Invalid value: " + sensorVariableJson.getValue());
                    }
                    break;

                default:
                    break;
            }

            sensorVariable.setValue(String.valueOf(sensorVariableJson.getValue()));
            McObjectManager.getMcActionEngine().sendPayload(sensorVariable);
        } else {
            throw new McBadRequestException("null not allowed");
        }
    }

    public void updateVariable(SensorVariableJson sensorVariableJson) throws McBadRequestException {
        SensorVariable sensorVariable = DaoUtils.getSensorVariableDao().get(sensorVariableJson.getId());
        if (sensorVariable != null) {
            if (!sensorVariable.getMetricType().getText().equalsIgnoreCase(sensorVariableJson.getMetricType())) {
                //clear existing data
                switch (sensorVariable.getMetricType()) {
                    case DOUBLE:
                        DaoUtils.getMetricsDoubleTypeDeviceDao().deleteBySensorVariableRefId(sensorVariable.getId());
                        break;
                    case BINARY:
                        DaoUtils.getMetricsBinaryTypeDeviceDao().deleteBySensorVariableRefId(sensorVariable.getId());
                        break;
                    case COUNTER:
                        DaoUtils.getMetricsCounterTypeDeviceDao().deleteBySensorVariableRefId(sensorVariable.getId());
                        break;
                    default:
                        break;
                }
                sensorVariable.setMetricType(METRIC_TYPE.fromString(sensorVariableJson.getMetricType()));
            }
            //Update Unit type
            sensorVariable.setUnitType(sensorVariableJson.getUnitType());
            //Update sensor variable readOnly option
            sensorVariable.setReadOnly(sensorVariableJson.getReadOnly());
            //Update offset
            sensorVariable.setOffset(sensorVariableJson.getOffset());
            //Update priority
            sensorVariable.setPriority(sensorVariableJson.getPriority());
            //Update Graph settings
            sensorVariable.setGraphProperties(sensorVariableJson.getGraphProperties());

            //Update sensor variable
            DaoUtils.getSensorVariableDao().update(sensorVariable);
        } else {
            throw new McBadRequestException("null not allowed");
        }
    }

    public Sensor getSensor(String sensorName, String... roomsName) {
        RoomApi roomApi = new RoomApi();
        Room room = roomApi.getRoom(roomsName);
        return getSensor(sensorName, room.getId());
    }

    public Sensor getSensor(String sensorName, Integer roomId) {
        return DaoUtils.getSensorDao().getByRoomId(sensorName, roomId);
    }

    public Sensor getSensor(String sensorName) {
        return DaoUtils.getSensorDao().getByRoomId(sensorName, null);
    }

    private SensorVariable getSensorVariable(Sensor sensor, String variableType) {
        for (SensorVariable sv : sensor.getVariables()) {
            if (sv.getVariableType().getText().equalsIgnoreCase(variableType)) {
                return sv;
            }
        }
        return null;
    }

    public SensorVariable getSensorVariable(String sensorName, String variableType, String... roomsName) {
        return getSensorVariable(getSensor(sensorName, roomsName), variableType);
    }

    public SensorVariable getSensorVariable(String sensorName, String variableType, Integer roomId) {
        return getSensorVariable(getSensor(sensorName, roomId), variableType);
    }

    public void sendRawMessage(McMessage mcMessage) throws McBadRequestException {
        mcMessage.setTxMessage(true);
        mcMessage.setScreeningDone(false);
        if (mcMessage.validate()) {
            McMessageUtils.sendToProviderBridge(mcMessage);
        } else {
            throw new McBadRequestException("Required field is missing! " + mcMessage);
        }
    }
}
