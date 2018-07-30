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
package org.mycontroller.standalone.api;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptException;

import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.api.jaxrs.model.DataPointDouble;
import org.mycontroller.standalone.api.jaxrs.model.McHeatMap;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.MetricsBatteryUsage;
import org.mycontroller.standalone.db.tables.MetricsBinaryTypeDevice;
import org.mycontroller.standalone.db.tables.MetricsDoubleTypeDevice;
import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.db.tables.UidTag;
import org.mycontroller.standalone.exceptions.McBadRequestException;
import org.mycontroller.standalone.metrics.DATA_TYPE;
import org.mycontroller.standalone.metrics.MetricsUtils;
import org.mycontroller.standalone.metrics.MetricsUtils.METRIC_TYPE;
import org.mycontroller.standalone.metrics.model.Criteria;
import org.mycontroller.standalone.metrics.model.MetricDouble;
import org.mycontroller.standalone.model.ResourceCountModel;
import org.mycontroller.standalone.model.ResourceModel;
import org.mycontroller.standalone.scripts.McScript;
import org.mycontroller.standalone.scripts.McScriptEngine;
import org.mycontroller.standalone.scripts.McScriptException;
import org.mycontroller.standalone.settings.MetricsDataRetentionSettings;
import org.mycontroller.standalone.utils.McUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Slf4j
public class MetricApi {
    //Get count of resources
    public ResourceCountModel getResourceCount(RESOURCE_TYPE resourceType, Integer resourceId) {
        return new ResourceCountModel(resourceType, resourceId);
    }

    public MetricDouble getSensorVariableMetricDouble(SensorVariable sensorVariable, Long start,
            Long end) {
        Criteria criteria = Criteria.builder()
                .start(start)
                .end(end)
                .resourceModel(new ResourceModel(RESOURCE_TYPE.SENSOR_VARIABLE, sensorVariable))
                .build();
        //Query from metric engine

        DataPointDouble metric = (DataPointDouble) MetricsUtils.engine().get(criteria);
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

    //Utils methods
    public static Long getBucketDuration(String bucketDuration) {
        return getBucketDuration(bucketDuration, McUtils.ONE_MINUTE);
    }

    public static Long getBucketDuration(String bucketDuration, long defaultValue) {
        if (bucketDuration == null) {
            return McUtils.ONE_MINUTE;
        } else if (bucketDuration.endsWith("mn")) {
            return McUtils.getLong(bucketDuration.replace("mn", "")) * McUtils.ONE_MINUTE;
        } else if (bucketDuration.endsWith("h")) {
            return McUtils.getLong(bucketDuration.replace("h", "")) * McUtils.ONE_HOUR;
        } else if (bucketDuration.endsWith("d")) {
            return McUtils.getLong(bucketDuration.replace("d", "")) * McUtils.ONE_DAY;
        } else if (bucketDuration.endsWith("m")) {
            return McUtils.getLong(bucketDuration.replace("m", "")) * McUtils.ONE_DAY * 30;
        } else if (bucketDuration.equalsIgnoreCase("raw")) {
            return -1L;
        }
        return defaultValue;
    }

    public static Long getStart(Long start) {
        if (start == null) {
            start = System.currentTimeMillis() - McUtils.HOUR * 6;
        }
        return start;
    }

    public static Long getEnd(Long end) {
        if (end == null) {
            end = System.currentTimeMillis();
        }
        return end;
    }

    public static String getBucketDuration(Long start, Long end, METRIC_TYPE metricType) {
        start = getStart(start);
        end = getEnd(end);
        Long duration = end - start;
        if (metricType == METRIC_TYPE.DOUBLE) {
            MetricsDataRetentionSettings metricSettings = AppProperties.getInstance()
                    .getMetricsDataRetentionSettings();
            if (duration > metricSettings.getRetentionTwelveHours()) {
                return "1d";
            } else if (duration > metricSettings.getRetentionSixHours()) {
                return "12h";
            } else if (duration > metricSettings.getRetentionOneHour()) {
                return "6h";
            } else if (duration > metricSettings.getRetentionFiveMinutes()) {
                return "1h";
            } else if (duration > metricSettings.getRetentionOneMinute()) {
                return "5mn";
            } else if (duration >= McUtils.ONE_MINUTE * 6) {
                return "1mn";
            } else {
                return "raw";
            }
        } else if (metricType == METRIC_TYPE.COUNTER) {
            if (duration >= McUtils.DAY * 29) {
                return "1m";
            } else if (duration >= McUtils.DAY * 6) {
                return "1d";
            } else if (duration >= McUtils.ONE_HOUR * 23) {
                return "30mn";
            } else if (duration >= McUtils.ONE_HOUR * 11) {
                return "15mn";
            } else if (duration >= McUtils.ONE_HOUR * 5) {
                return "5mn";
            } else if (duration >= McUtils.ONE_MINUTE * 7) {
                return "1mn";
            } else {
                return "raw";
            }
        } else if (metricType == METRIC_TYPE.BINARY) {
            return "raw";
        }
        return "1mn";
    }

    public List<MetricsDoubleTypeDevice> getMetricsDoubleData(SensorVariable sensorVariable,
            Long start, Long end) {
        return DaoUtils.getMetricsDoubleTypeDeviceDao().getAll(
                MetricsDoubleTypeDevice.builder()
                        .sensorVariable(sensorVariable)
                        .start(start)
                        .end(end).build());
    }

    public List<MetricsBatteryUsage> getMetricsBatteryUsage(Node node,
            Long start, Long end) {
        return DaoUtils.getMetricsBatteryUsageDao().getAll(
                MetricsBatteryUsage.builder()
                        .node(node)
                        .start(start)
                        .end(end).build());
    }

    //Get metric data for boolean type
    public List<MetricsBinaryTypeDevice> getMetricsBinaryData(
            SensorVariable sensorVariable, Long start) {
        MetricsBinaryTypeDevice binaryTypeDevice = MetricsBinaryTypeDevice.builder()
                .sensorVariable(sensorVariable).build();
        if (start != null) {
            binaryTypeDevice.setStart(start);
        }
        return DaoUtils.getMetricsBinaryTypeDeviceDao().getAll(binaryTypeDevice);
    }

    public List<?> getMetricData(Integer resourceId, String resourceType, Long start, Long end, String duration,
            String bucketDuration, DATA_TYPE dataType) throws McBadRequestException {
        return getMetricData(getResourceModel(resourceId, resourceType, null), start, end, duration, bucketDuration,
                dataType);
    }

    public List<?> getMetricData(String uid, Long start, Long end, String duration, String bucketDuration,
            DATA_TYPE dataType)
            throws McBadRequestException {
        return getMetricData(getResourceModel(null, null, uid), start, end, duration, bucketDuration, dataType);
    }

    public ResourceModel getResourceModel(Integer resourceId, String resourceType, String uid)
            throws McBadRequestException {
        if (uid != null) {
            UidTag uidObj = new UidTagApi().getByUid(uid);
            if (uidObj == null || uidObj.getResource() == null) {
                throw new McBadRequestException(MessageFormat.format("Requested uid[{0}] not available!", uid));
            }
            resourceId = uidObj.getResourceId();
            resourceType = uidObj.getResourceType().getText();
        }

        if (uid == null && (resourceId == null || resourceType == null)) {
            throw new McBadRequestException(MessageFormat.format("Required fields are missing! resourceId:[{0}], "
                    + "resourceType:[{1}]", resourceId, resourceType));
        }
        try {
            return new ResourceModel(RESOURCE_TYPE.fromString(resourceType), resourceId);
        } catch (Exception ex) {
            throw new McBadRequestException(ex);
        }

    }

    public List<?> getMetricData(ResourceModel resourceModel, Long start, Long end, String duration,
            String bucketDuration, DATA_TYPE dataType) throws McBadRequestException {
        if (bucketDuration == null) {
            throw new McBadRequestException(MessageFormat.format(
                    "Required fields is missing! bucketDuration:[{0}]",
                    bucketDuration));
        }
        Long durationLong = null;
        if (duration != null) {
            durationLong = getBucketDuration(duration, -1);
        }
        if (duration != null && (start == null || end == null)) {
            if (durationLong == -1) {
                throw new McBadRequestException(MessageFormat.format("Invalid request! duration:[{0}]", duration));
            } else if (start == null && end == null) {
                end = System.currentTimeMillis();
                start = end - durationLong;
            } else if (start == null) {
                start = end - durationLong;
            } else if (end == null) {
                end = start + durationLong;
            }
        }

        //Update start and end
        start = getStart(start);
        end = getEnd(end);

        _logger.debug(
                "Metric request for (start:{}, end:{}, duration:{}, bucketDuration:{}, {})", start, end, duration,
                bucketDuration, resourceModel);
        if (dataType == null) {
            switch (resourceModel.getResourceType()) {
                case NODE:
                    dataType = DATA_TYPE.NODE_BATTERY_USAGE;
                    break;
                case SENSOR_VARIABLE:
                    dataType = DATA_TYPE.SENSOR_VARIABLE;
                    break;
                default:
                    break;
            }
        }
        long bucketDurationLong = getBucketDuration(bucketDuration);
        if ((end - start) < bucketDurationLong) {
            throw new McBadRequestException(
                    "'bucketDuration' must be lesser than 'end' - 'start' or 'duration'. Validation(bucketDuration:"
                            + bucketDurationLong + " ms, end - start:" + (end - start) + " ms, duration:"
                            + durationLong + " ms), Input(bucketDuration:" + bucketDuration + ", start:" + start
                            + ", end:" + end + ", duration:" + duration + ", dataType:" + dataType + ")");
        }
        //give result if bucketDuration and duration are equal.
        start--;

        return MetricsUtils.engine().list(Criteria.builder()
                .resourceModel(resourceModel)
                .start(start)
                .end(end)
                .bucketDuration(bucketDuration)
                .dataType(dataType)
                .build());
    }
}
