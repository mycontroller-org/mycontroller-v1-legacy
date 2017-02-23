/*
 * Copyright 2015-2017 Jeeva Kandasamy (jkandasa@gmail.com)
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
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.script.ScriptException;

import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.api.jaxrs.model.DataPointBinary;
import org.mycontroller.standalone.api.jaxrs.model.DataPointCounter;
import org.mycontroller.standalone.api.jaxrs.model.DataPointDouble;
import org.mycontroller.standalone.api.jaxrs.model.DataPointGPS;
import org.mycontroller.standalone.api.jaxrs.model.McHeatMap;
import org.mycontroller.standalone.db.DB_QUERY;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.MetricsBatteryUsage;
import org.mycontroller.standalone.db.tables.MetricsBinaryTypeDevice;
import org.mycontroller.standalone.db.tables.MetricsCounterTypeDevice;
import org.mycontroller.standalone.db.tables.MetricsDoubleTypeDevice;
import org.mycontroller.standalone.db.tables.MetricsGPSTypeDevice;
import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.db.tables.UidTag;
import org.mycontroller.standalone.exceptions.McBadRequestException;
import org.mycontroller.standalone.metrics.MetricDouble;
import org.mycontroller.standalone.metrics.MetricsUtils.METRIC_TYPE;
import org.mycontroller.standalone.model.ResourceCountModel;
import org.mycontroller.standalone.model.ResourceModel;
import org.mycontroller.standalone.scripts.McScript;
import org.mycontroller.standalone.scripts.McScriptEngine;
import org.mycontroller.standalone.scripts.McScriptException;
import org.mycontroller.standalone.settings.MetricsDataRetentionSettings;
import org.mycontroller.standalone.utils.McUtils;

import com.j256.ormlite.dao.GenericRawResults;

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
        MetricsDoubleTypeDevice queryInput = MetricsDoubleTypeDevice.builder()
                .start(start)
                .end(end)
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

    //Metric private api's
    private List<?> getSensorVariableMetricsBinary(Integer sensorVariableId,
            Long start, Long end, Boolean isGeneric) {
        List<MetricsBinaryTypeDevice> metrics = DaoUtils.getMetricsBinaryTypeDeviceDao().getAll(
                MetricsBinaryTypeDevice.builder()
                        .start(start)
                        .end(end)
                        .sensorVariable(SensorVariable.builder().id(sensorVariableId).build())
                        .build());
        if (isGeneric) {
            List<DataPointBinary> genericMetrics = new ArrayList<DataPointBinary>();
            for (MetricsBinaryTypeDevice metric : metrics) {
                genericMetrics.add(DataPointBinary.get(metric, null, null));
            }
            return genericMetrics;
        } else {
            return metrics;
        }
    }

    private List<?> getSensorVariableMetricsGPS(Integer sensorVariableId,
            Long start, Long end, long bucketDuration, boolean getGeneric) {
        List<MetricsGPSTypeDevice> metricsFinal = new ArrayList<MetricsGPSTypeDevice>();
        List<DataPointGPS> metricsGenericFinal = new ArrayList<DataPointGPS>();
        MetricsGPSTypeDevice metricConfig = MetricsGPSTypeDevice.builder()
                .sensorVariable(SensorVariable.builder().id(sensorVariableId).build()).build();
        if (bucketDuration == -1) {
            metricConfig.setStart(start);
            metricConfig.setEnd(end);
            List<MetricsGPSTypeDevice> metrics = DaoUtils.getMetricsGPSTypeDeviceDao().getAll(metricConfig);
            if (getGeneric) {
                for (MetricsGPSTypeDevice metric : metrics) {
                    metricsGenericFinal.add(DataPointGPS.get(metric, null, null));
                }
            } else {
                metricsFinal = metrics;
            }
        } else {
            //TODO: add support for runtime aggregation
        }
        if (getGeneric) {
            return metricsGenericFinal;
        }
        return metricsFinal;
    }

    private List<?> getSensorVariableMetricsDouble(Integer sensorVariableId,
            Long start, Long end, long bucketDuration, boolean getGeneric) {
        start = getStart(start);
        end = getEnd(end);
        List<MetricsDoubleTypeDevice> metricsFinal = new ArrayList<MetricsDoubleTypeDevice>();
        List<DataPointDouble> metricsGenericFinal = new ArrayList<DataPointDouble>();
        _logger.debug("timestamp:[from:{}, to:{}], bucketDuration:{}, totalDuration:{}",
                start, end, bucketDuration, (end - start));
        if ((end - start) < bucketDuration) {
            return metricsFinal;
        }
        MetricsDoubleTypeDevice metricConfig = MetricsDoubleTypeDevice.builder()
                .sensorVariable(SensorVariable.builder().id(sensorVariableId).build())
                .build();
        if (bucketDuration == -1) {
            metricConfig.setStart(start);
            metricConfig.setEnd(end);
            List<MetricsDoubleTypeDevice> metrics = DaoUtils.getMetricsDoubleTypeDeviceDao().getAll(metricConfig);
            if (getGeneric) {
                for (MetricsDoubleTypeDevice metric : metrics) {
                    metricsGenericFinal.add(DataPointDouble.get(metric, null, null));
                }
            } else {
                metricsFinal = metrics;
            }
        } else {
            Long tmpEnd = start + bucketDuration;
            while (tmpEnd < end) {
                if (tmpEnd > end) {
                    break;
                }
                String sqlSelectQuery = MessageFormat.format(
                        DB_QUERY.getQuery(DB_QUERY.SELECT_METRICS_DOUBLE_BY_SENSOR_VARIABLE),
                        String.valueOf(sensorVariableId), String.valueOf(start),
                        String.valueOf(tmpEnd));
                _logger.debug("Sql query:[{}]", sqlSelectQuery);
                MetricsDoubleTypeDevice metric = null;
                try {
                    GenericRawResults<MetricsDoubleTypeDevice> rawResult = DaoUtils
                            .getMetricsDoubleTypeDeviceDao().getDao().queryRaw(sqlSelectQuery,
                                    DaoUtils.getMetricsDoubleTypeDeviceDao().getDao().getRawRowMapper());
                    metric = rawResult.getFirstResult();
                    _logger.debug("Metric:[{}]", metric);
                } catch (SQLException ex) {
                    _logger.error("Exception,", ex);
                }
                if (metric != null && !getGeneric) {
                    metricsFinal.add(metric);
                }
                if (getGeneric) {
                    metricsGenericFinal.add(DataPointDouble.get(metric, start, tmpEnd));
                }
                start = tmpEnd;
                tmpEnd += bucketDuration;
            }
        }
        if (getGeneric) {
            return metricsGenericFinal;
        }
        return metricsFinal;
    }

    private List<?> getSensorVariableMetricsCounter(Integer sensorVariableId,
            Long start, Long end, String bucketDurationString, boolean getGeneric) {
        start = getStart(start);
        end = getEnd(end);
        List<MetricsCounterTypeDevice> metricsFinal = new ArrayList<MetricsCounterTypeDevice>();
        List<DataPointCounter> metricsGenericFinal = new ArrayList<DataPointCounter>();
        _logger.debug("timestamp:[from:{}, to:{}], bucketDurationString:{}",
                start, end, bucketDurationString);
        MetricsCounterTypeDevice metricConfig = MetricsCounterTypeDevice.builder()
                .sensorVariable(SensorVariable.builder().id(sensorVariableId).build())
                .build();
        if (bucketDurationString.equalsIgnoreCase("raw")) {
            metricConfig.setStart(start);
            metricConfig.setEnd(end);
            List<MetricsCounterTypeDevice> metrics = DaoUtils.getMetricsCounterTypeDeviceDao().getAll(metricConfig);
            if (getGeneric) {
                for (MetricsCounterTypeDevice metric : metrics) {
                    metricsGenericFinal.add(DataPointCounter.get(metric, null, null));
                }
            } else {
                metricsFinal = metrics;
            }
        } else {
            Calendar calendarFrom = Calendar.getInstance();
            Calendar calendarTo = Calendar.getInstance();
            calendarFrom.setTime(new Date(start));
            calendarTo.setTime(new Date(end));
            String[] bucket = bucketDurationString.trim().split("(?<=\\d)(?=\\D)");
            if (bucket.length != 2) {
                _logger.warn("Invalid bucketDuration string: {}, result:{}", bucketDurationString, bucket);
                return metricsFinal;
            }
            Integer increment = McUtils.getInteger(bucket[0]);
            Integer incrementRef = null;
            String bucketString = bucket[1].toLowerCase();

            switch (bucketString) {
                case "m":
                    calendarFrom.set(Calendar.DAY_OF_MONTH, 1);
                    calendarTo.set(Calendar.DAY_OF_MONTH, 1);
                    incrementRef = Calendar.MONTH;
                case "d":
                    calendarFrom.set(Calendar.HOUR_OF_DAY, 0);
                    calendarTo.set(Calendar.HOUR_OF_DAY, 0);
                    if (incrementRef == null) {
                        incrementRef = Calendar.DATE;
                    }
                case "h":
                    calendarFrom.set(Calendar.MINUTE, 0);
                    calendarTo.set(Calendar.MINUTE, 0);
                    if (incrementRef == null) {
                        incrementRef = Calendar.HOUR;
                    }
                case "mn":
                    calendarFrom.set(Calendar.MILLISECOND, 0);
                    calendarTo.set(Calendar.MILLISECOND, 0);
                    calendarFrom.set(Calendar.SECOND, 0);
                    calendarTo.set(Calendar.SECOND, 0);
                    if (incrementRef == null) {
                        incrementRef = Calendar.MINUTE;
                    }
            }
            while (calendarFrom.before(calendarTo) || calendarFrom.equals(calendarTo)) {
                long timestampTmpFrom = calendarFrom.getTimeInMillis();
                calendarFrom.add(incrementRef, increment);
                long endTmp = calendarFrom.getTimeInMillis();

                String sqlSelectQuery = MessageFormat.format(
                        DB_QUERY.getQuery(DB_QUERY.SELECT_METRICS_COUNTER_BY_SENSOR_VARIABLE),
                        String.valueOf(sensorVariableId), String.valueOf(timestampTmpFrom),
                        String.valueOf(endTmp));
                _logger.debug("Sql query:[{}]", sqlSelectQuery);
                MetricsCounterTypeDevice metric = null;
                try {
                    GenericRawResults<MetricsCounterTypeDevice> rawResult = DaoUtils
                            .getMetricsCounterTypeDeviceDao().getDao().queryRaw(sqlSelectQuery,
                                    DaoUtils.getMetricsCounterTypeDeviceDao().getDao().getRawRowMapper());
                    metric = rawResult.getFirstResult();
                    _logger.debug("Metric:[{}]", metric);
                } catch (SQLException ex) {
                    _logger.error("Exception,", ex);
                }
                if (metric != null && !getGeneric) {
                    metricsFinal.add(metric);
                }
                if (getGeneric) {
                    metricsGenericFinal.add(DataPointCounter.get(metric, start, endTmp));
                }
                if ((bucketString.equals("mn") || bucketString.equals("h"))
                        && endTmp > System.currentTimeMillis()) {
                    break;
                }
            }
        }
        if (getGeneric) {
            return metricsGenericFinal;
        }
        return metricsFinal;
    }

    private List<?> getMetricsBattery(Integer nodeId, Long start, Long end,
            Long bucketDuration, boolean getGeneric) {
        start = getStart(start);
        end = getEnd(end);
        List<MetricsBatteryUsage> metricsFinal = new ArrayList<MetricsBatteryUsage>();
        List<DataPointDouble> metricsGenericFinal = new ArrayList<DataPointDouble>();
        _logger.debug("timestamp:[from:{}, to:{}], bucketDuration:{}, totalDuration:{}",
                start, end, bucketDuration, (end - start));
        if ((end - start) < bucketDuration) {
            return metricsFinal;
        }
        MetricsBatteryUsage metricConfig = MetricsBatteryUsage.builder()
                .node(Node.builder().id(nodeId).build())
                .build();
        if (bucketDuration == -1) {
            metricConfig.setStart(start);
            metricConfig.setEnd(end);
            List<MetricsBatteryUsage> metrics = DaoUtils.getMetricsBatteryUsageDao().getAll(metricConfig);
            if (getGeneric) {
                for (MetricsBatteryUsage metric : metrics) {
                    metricsGenericFinal.add(DataPointDouble.get(metric, null, null));
                }
            } else {
                metricsFinal = metrics;
            }
        } else {
            Long tmpEnd = start + bucketDuration;
            while (tmpEnd < end) {

                start = tmpEnd;
                if (tmpEnd > end) {
                    break;
                }
                tmpEnd += bucketDuration;
                String sqlSelectQuery = MessageFormat.format(
                        DB_QUERY.getQuery(DB_QUERY.SELECT_METRICS_BATTERY_BY_NODE),
                        String.valueOf(nodeId), String.valueOf(start), String.valueOf(tmpEnd));
                _logger.debug("Sql query:[{}]", sqlSelectQuery);
                MetricsBatteryUsage metric = null;
                try {
                    GenericRawResults<MetricsBatteryUsage> rawResult = DaoUtils
                            .getMetricsBatteryUsageDao().getDao().queryRaw(sqlSelectQuery,
                                    DaoUtils.getMetricsBatteryUsageDao().getDao().getRawRowMapper());
                    metric = rawResult.getFirstResult();
                    _logger.debug("Metric:[{}]", metric);
                } catch (SQLException ex) {
                    _logger.error("Exception,", ex);
                }
                if (metric != null && !getGeneric) {
                    metricsFinal.add(metric);
                }
                if (getGeneric) {
                    metricsGenericFinal.add(DataPointDouble.get(metric, start, tmpEnd));
                }

                metricConfig.setStart(start);
                start = tmpEnd;
                if (tmpEnd > end) {
                    break;
                }
            }
        }
        if (getGeneric) {
            return metricsGenericFinal;
        }
        return metricsFinal;
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
            String bucketDuration, Boolean isGeneric) throws McBadRequestException {
        return getMetricData(getResourceModel(resourceId, resourceType, null), start, end, duration, bucketDuration,
                isGeneric);
    }

    public List<?> getMetricData(Integer resourceId, String resourceType, Long start, Long end, String duration,
            String bucketDuration) throws McBadRequestException {
        return getMetricData(getResourceModel(resourceId, resourceType, null), start, end, duration, bucketDuration,
                true);
    }

    public List<?> getMetricData(String uid, Long start, Long end, String duration, String bucketDuration,
            Boolean isGeneric) throws McBadRequestException {
        return getMetricData(getResourceModel(null, null, uid), start, end, duration, bucketDuration,
                isGeneric);
    }

    public List<?> getMetricData(String uid, Long start, Long end, String duration, String bucketDuration)
            throws McBadRequestException {
        return getMetricData(getResourceModel(null, null, uid), start, end, duration, bucketDuration, true);
    }

    public List<?> getMetricData(ResourceModel resourceModel, Long start, Long end, String duration,
            String bucketDuration) throws McBadRequestException {
        return getMetricData(resourceModel, start, end, duration, bucketDuration, true);
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
            String bucketDuration, Boolean isGeneric) throws McBadRequestException {
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
                "Metric request for (start:{}, end:{}, duration:{}, bucketDuration:{}, isGeneric:{}, {})", start, end,
                duration, bucketDuration, isGeneric, resourceModel);
        long bucketDurationLong = getBucketDuration(bucketDuration);
        if ((end - start) < bucketDurationLong) {
            throw new McBadRequestException(
                    "'bucketDuration' must be lesser than 'end' - 'start' or 'duration'. Validation(bucketDuration:"
                            + bucketDurationLong + " ms, end - start:" + (end - start) + " ms, duration:"
                            + durationLong + " ms), Input(bucketDuration:" + bucketDuration + ", start:" + start
                            + ", end:" + end + ", duration:" + duration + ")");
        }
        //give result if bucketDuration and duration are equal.
        start--;

        switch (resourceModel.getResourceType()) {
            case NODE:
                return getMetricsBattery(resourceModel.getResourceId(), start, end, bucketDurationLong, isGeneric);
            case SENSOR_VARIABLE:
                SensorVariable sVariable = (SensorVariable) resourceModel.getResource();
                switch (sVariable.getMetricType()) {
                    case BINARY:
                        return getSensorVariableMetricsBinary(resourceModel.getResourceId(), start, end, isGeneric);
                    case COUNTER:
                        return getSensorVariableMetricsCounter(resourceModel.getResourceId(), start, end,
                                bucketDuration, isGeneric);
                    case DOUBLE:
                        return getSensorVariableMetricsDouble(resourceModel.getResourceId(), start, end,
                                bucketDurationLong, isGeneric);
                    case GPS:
                        return getSensorVariableMetricsGPS(resourceModel.getResourceId(), start, end,
                                bucketDurationLong, isGeneric);
                    default:
                        break;
                }
                break;
            default:
                break;

        }
        throw new McBadRequestException(
                MessageFormat
                        .format("Metric not available for request! {resourceId:[{0}], resourceType:[{1}], start:[{2}],"
                                + " end:[{3}], duration:[{4}], bucketDuration:[{5}]], isGeneric:[{6}] ]}",
                                String.valueOf(resourceModel.getResourceId()), resourceModel.getResourceType()
                                        .getText(), String.valueOf(start), String.valueOf(end),
                                duration, bucketDuration, String.valueOf(isGeneric)));
    }
}
