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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptException;

import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.McUtils;
import org.mycontroller.standalone.api.jaxrs.json.McHeatMap;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.MetricsBatteryUsage;
import org.mycontroller.standalone.db.tables.MetricsBinaryTypeDevice;
import org.mycontroller.standalone.db.tables.MetricsDoubleTypeDevice;
import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.exceptions.McBadRequestException;
import org.mycontroller.standalone.metrics.MetricDouble;
import org.mycontroller.standalone.metrics.MetricsUtils.METRIC_TYPE;
import org.mycontroller.standalone.model.ResourceCountModel;
import org.mycontroller.standalone.model.ResourceModel;
import org.mycontroller.standalone.scripts.McScript;
import org.mycontroller.standalone.scripts.McScriptEngine;
import org.mycontroller.standalone.scripts.McScriptException;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */

public class MetricApi {
    //Get count of resources
    public ResourceCountModel getResourceCount(RESOURCE_TYPE resourceType, Integer resourceId) {
        return new ResourceCountModel(resourceType, resourceId);
    }

    public List<MetricsDoubleTypeDevice> getSensorVariableMetricsDouble(Integer sensorVariableId,
            Long timestampFrom, Long timestampTo) {
        return DaoUtils.getMetricsDoubleTypeDeviceDao().getAll(MetricsDoubleTypeDevice.builder()
                .timestampFrom(timestampFrom)
                .timestampTo(timestampTo)
                .sensorVariable(SensorVariable.builder().id(sensorVariableId).build())
                .build());
    }

    public MetricDouble getSensorVariableMetricDouble(SensorVariable sensorVariable, Long timestampFrom,
            Long timestampTo) {
        MetricsDoubleTypeDevice queryInput = MetricsDoubleTypeDevice.builder()
                .timestampFrom(timestampFrom)
                .timestampTo(timestampTo)
                .sensorVariable(sensorVariable)
                .build();

        MetricsDoubleTypeDevice metric = DaoUtils.getMetricsDoubleTypeDeviceDao().getMinMaxAvg(queryInput);

        MetricDouble metricDouble = null;
        //If current value not available, do not allow any value
        if (sensorVariable.getValue() == null || metric.getMin() == null) {
            metricDouble = MetricDouble.builder().variable(sensorVariable).build();
        } else {
            metricDouble = MetricDouble.builder()
                    .variable(sensorVariable)
                    .minimum(metric.getMin())
                    .average(metric.getAvg())
                    .maximum(metric.getMax())
                    .current(McUtils.getDouble(sensorVariable.getValue()))
                    .previous(McUtils.getDouble(sensorVariable.getPreviousValue()))
                    .build();
        }
        return metricDouble;
    }

    public List<MetricsBinaryTypeDevice> getSensorVariableMetricsBinary(Integer sensorVariableId,
            Long timestampFrom, Long timestampTo) {
        return DaoUtils.getMetricsBinaryTypeDeviceDao().getAll(MetricsBinaryTypeDevice.builder()
                .timestampFrom(timestampFrom)
                .timestampTo(timestampTo)
                .sensorVariable(SensorVariable.builder().id(sensorVariableId).build())
                .build());
    }

    public List<MetricsBatteryUsage> getMetricsBattery(Integer nodeId, Long timestampFrom, Long timestampTo,
            Boolean withMinMax) {
        MetricsBatteryUsage metricQueryBattery = MetricsBatteryUsage.builder()
                .timestampFrom(timestampFrom)
                .timestampTo(timestampTo)
                .node(Node.builder().id(nodeId).build())
                .build();
        return DaoUtils.getMetricsBatteryUsageDao().getAll(metricQueryBattery);
    }

    public List<McHeatMap> getHeatMapNodeBatteryLevel(List<Integer> nodeIds) {
        List<McHeatMap> mcHeatMap = new ArrayList<McHeatMap>();
        List<Node> nodes = null;
        if (nodeIds != null) {
            nodes = DaoUtils.getNodeDao().getAll(nodeIds);
        } else {
            nodes = DaoUtils.getNodeDao().getAll();
        }
        for (Node node : nodes) {
            if (node.getBatteryLevel() != null) {
                mcHeatMap.add(McHeatMap.builder()
                        .id(node.getId().longValue())
                        .altId(node.getId().longValue())
                        .value(McUtils.round((McUtils.getDouble(node.getBatteryLevel()) / 100.0), 2))
                        .tooltip(new ResourceModel(RESOURCE_TYPE.NODE, node).getResourceLessDetails())
                        .build());
            }
        }
        return mcHeatMap;
    }

    public List<McHeatMap> getHeatMapNodeState(List<Integer> nodeIds) {
        List<McHeatMap> mcHeatMap = new ArrayList<McHeatMap>();
        List<Node> nodes = null;
        if (nodeIds != null) {
            nodes = DaoUtils.getNodeDao().getAll(nodeIds);
        } else {
            nodes = DaoUtils.getNodeDao().getAll();
        }
        Double value = null;
        for (Node node : nodes) {
            value = null;
            switch (node.getState()) {
                case DOWN:
                    value = 0.0;
                    break;
                case UNAVAILABLE:
                    value = 0.5;
                    break;
                case UP:
                    value = 1.0;
                    break;
                default:
                    break;
            }
            mcHeatMap.add(McHeatMap.builder()
                    .id(node.getId().longValue())
                    .altId(node.getId().longValue())
                    .value(value)
                    .tooltip(new ResourceModel(RESOURCE_TYPE.NODE, node).getResourceLessDetails())
                    .build());
        }
        return mcHeatMap;
    }

    public List<McHeatMap> getHeatMapSensorVariableDouble(List<Integer> svIds, Double upperLimit) {
        if (upperLimit == null) {
            upperLimit = 100.0;
        }
        List<McHeatMap> mcHeatMap = new ArrayList<McHeatMap>();
        List<SensorVariable> sVariables = null;
        if (svIds != null) {
            sVariables = DaoUtils.getSensorVariableDao().getAll(svIds);
        } else {
            sVariables = DaoUtils.getSensorVariableDao().getAll(SensorVariable.KEY_METRIC, METRIC_TYPE.DOUBLE);
        }
        Double value = null;
        for (SensorVariable sVariable : sVariables) {
            if (sVariable.getValue() != null) {
                value = McUtils.round(McUtils.getDouble(sVariable.getValue()) / upperLimit, 2);

                mcHeatMap.add(McHeatMap.builder()
                        .id(sVariable.getId().longValue())
                        .altId(sVariable.getSensor().getId().longValue())
                        .value(value)
                        .tooltip(new ResourceModel(RESOURCE_TYPE.SENSOR_VARIABLE, sVariable).getResourceLessDetails())
                        .build());
            }
        }
        return mcHeatMap;
    }

    @SuppressWarnings("unchecked")
    public List<McHeatMap> getHeatMapScript(String scriptName) throws McBadRequestException, IllegalAccessException,
            IOException, McScriptException, ScriptException {
        if (scriptName == null) {
            throw new McBadRequestException("script file name missing!");
        }
        McScriptEngine mcScriptEngine = new McScriptEngine(McScript.getMcScript(scriptName));
        return (List<McHeatMap>) mcScriptEngine.executeScript();
    }
}
