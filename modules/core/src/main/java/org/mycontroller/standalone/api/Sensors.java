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
import java.util.List;

import org.mycontroller.standalone.McObjectManager;
import org.mycontroller.standalone.McUtils;
import org.mycontroller.standalone.api.jaxrs.json.Query;
import org.mycontroller.standalone.api.jaxrs.json.QueryResponse;
import org.mycontroller.standalone.api.jaxrs.json.SensorVariableJson;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.DeleteResourceUtils;
import org.mycontroller.standalone.db.SensorUtils;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.exceptions.McBadRequestException;
import org.mycontroller.standalone.exceptions.McDuplicateException;
import org.mycontroller.standalone.exceptions.McException;
import org.mycontroller.standalone.exceptions.McInvalidException;
import org.mycontroller.standalone.message.McMessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
public class Sensors {
    private static final Logger _logger = LoggerFactory.getLogger(Sensors.class);

    public QueryResponse getAll(Query query) {
        return DaoUtils.getSensorDao().getAll(query);
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
            throw new McException("Refer server logs");

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
            throw new McException("Refer server logs");
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

    public void updateVariableUnit(SensorVariableJson sensorVariableJson) throws McBadRequestException {
        SensorVariable sensorVariable = DaoUtils.getSensorVariableDao().get(sensorVariableJson.getId());
        if (sensorVariable != null) {
            sensorVariable.setUnit(sensorVariableJson.getUnit());
            //Update sensor unit
            DaoUtils.getSensorVariableDao().update(sensorVariable);
        } else {
            throw new McBadRequestException("null not allowed");
        }
    }
}
