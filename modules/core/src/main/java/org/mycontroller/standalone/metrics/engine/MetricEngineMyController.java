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
package org.mycontroller.standalone.metrics.engine;

import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.mycontroller.standalone.api.SystemApi;
import org.mycontroller.standalone.api.jaxrs.model.DataPointBase;
import org.mycontroller.standalone.api.jaxrs.model.DataPointBinary;
import org.mycontroller.standalone.api.jaxrs.model.DataPointCounter;
import org.mycontroller.standalone.api.jaxrs.model.DataPointDouble;
import org.mycontroller.standalone.api.jaxrs.model.DataPointGPS;
import org.mycontroller.standalone.api.jaxrs.model.ResourcePurgeConf;
import org.mycontroller.standalone.db.DB_QUERY;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.MetricsBatteryUsage;
import org.mycontroller.standalone.db.tables.MetricsBinaryTypeDevice;
import org.mycontroller.standalone.db.tables.MetricsCounterTypeDevice;
import org.mycontroller.standalone.db.tables.MetricsDoubleTypeDevice;
import org.mycontroller.standalone.db.tables.MetricsGPSTypeDevice;
import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.exceptions.McBadRequestException;
import org.mycontroller.standalone.metrics.MetricsUtils.AGGREGATION_TYPE;
import org.mycontroller.standalone.metrics.model.Criteria;
import org.mycontroller.standalone.metrics.model.DataPointer;
import org.mycontroller.standalone.metrics.model.Pong;
import org.mycontroller.standalone.model.ResourceModel;
import org.mycontroller.standalone.utils.McUtils;

import com.j256.ormlite.dao.GenericRawResults;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Slf4j
public class MetricEngineMyController implements IMetricEngine {
    @Override
    public void post(DataPointer data) {
        switch (data.getResourceModel().getResourceType()) {
            case NODE:
                Node node = (Node) data.getResourceModel().getResource();
                switch (data.getDataType()) {
                    case NODE_BATTERY_USAGE:    //Update battery level in to metrics table
                        MetricsBatteryUsage batteryUsage = MetricsBatteryUsage.builder()
                                .node(node)
                                .timestamp(System.currentTimeMillis())
                                .aggregationType(AGGREGATION_TYPE.RAW)
                                .avg(McUtils.getDouble(data.getPayload()))
                                .min(McUtils.getDouble(data.getPayload()))
                                .max(McUtils.getDouble(data.getPayload()))
                                .samples(1)
                                .build();
                        DaoUtils.getMetricsBatteryUsageDao().create(batteryUsage);
                        return;
                    default:
                        break;
                }
                break;
            case SENSOR_VARIABLE:
                SensorVariable sensorVariable = (SensorVariable) data.getResourceModel().getResource();
                switch (sensorVariable.getMetricType()) {
                    case DOUBLE:
                        DaoUtils.getMetricsDoubleTypeDeviceDao()
                                .create(MetricsDoubleTypeDevice.builder()
                                        .sensorVariable(sensorVariable)
                                        .aggregationType(AGGREGATION_TYPE.RAW)
                                        .timestamp(data.getTimestamp())
                                        .avg(McUtils.getDouble(data.getPayload()))
                                        .min(McUtils.getDouble(data.getPayload()))
                                        .max(McUtils.getDouble(data.getPayload()))
                                        .samples(1).build());

                        return;
                    case BINARY:
                        // check duplicate, if enabled and any update timestamp. Otherwise normal insert
                        if ((boolean) sensorVariable.getProperties().get(SensorVariable.KEY_IGNORE_DUPLICATE)
                                && sensorVariable.getId() != null) {
                            List<MetricsBinaryTypeDevice> metrics = DaoUtils.getMetricsBinaryTypeDeviceDao()
                                    .getAllLastN(sensorVariable, 2);
                            if (metrics.size() == 2
                                    && metrics.get(0).getState() == McUtils.getBoolean(data.getPayload())
                                    && metrics.get(0).getState() == metrics.get(1).getState()) {
                                DaoUtils.getMetricsBinaryTypeDeviceDao().updateTimestamp(sensorVariable.getId(),
                                        metrics.get(0).getTimestamp(), data.getTimestamp());
                                return;
                            }
                        }
                        // Do normal insert
                        DaoUtils.getMetricsBinaryTypeDeviceDao()
                                .create(MetricsBinaryTypeDevice.builder()
                                        .sensorVariable(sensorVariable)
                                        .timestamp(data.getTimestamp())
                                        .state(McUtils.getBoolean(data.getPayload())).build());
                        return;
                    case COUNTER:
                        DaoUtils.getMetricsCounterTypeDeviceDao()
                                .create(MetricsCounterTypeDevice.builder()
                                        .sensorVariable(sensorVariable)
                                        .aggregationType(AGGREGATION_TYPE.RAW)
                                        .timestamp(data.getTimestamp())
                                        .value(McUtils.getLong(data.getPayload()))
                                        .samples(1).build());
                        return;
                    case GPS:
                        try {
                            MetricsGPSTypeDevice gpsData = MetricsGPSTypeDevice.get(data.getPayload(),
                                    data.getTimestamp());
                            gpsData.setSensorVariable(sensorVariable);
                            DaoUtils.getMetricsGPSTypeDeviceDao().create(gpsData);
                        } catch (McBadRequestException ex) {
                            _logger.error("Exception,", ex);
                        }

                        return;
                    case NONE:
                        //For None type nothing to do.
                        return;
                    default:
                        break;
                }
            default:
                break;
        }
        throw new RuntimeException("Not supported operation for :" + data);
    }

    @Override
    public DataPointBase get(Criteria criteria) {
        switch (criteria.getResourceModel().getResourceType()) {
            case SENSOR_VARIABLE:
                SensorVariable sensorVariable = (SensorVariable) criteria.getResourceModel().getResource();
                switch (sensorVariable.getMetricType()) {
                    case DOUBLE:
                        MetricsDoubleTypeDevice metric = DaoUtils.getMetricsDoubleTypeDeviceDao().getMinMaxAvg(
                                MetricsDoubleTypeDevice.builder()
                                        .start(criteria.getStart())
                                        .end(criteria.getEnd())
                                        .sensorVariable(sensorVariable)
                                        .build());
                        return DataPointDouble.get(metric, criteria.getStart(), criteria.getEnd());
                    default:
                        break;
                }
            default:
                break;
        }
        throw new RuntimeException("Selected query not implemented! " + criteria);
    }

    @Override
    public List<?> list(Criteria criteria) {
        switch (criteria.getResourceModel().getResourceType()) {
            case NODE:
                switch (criteria.getDataType()) {
                    case NODE_BATTERY_USAGE:    //Update battery level in to metrics table
                        return getMetricsBattery(criteria);
                    default:
                        break;
                }
                break;
            case SENSOR_VARIABLE:
                SensorVariable sensorVariable = (SensorVariable) criteria.getResourceModel().getResource();
                switch (sensorVariable.getMetricType()) {
                    case BINARY:
                        return getSensorVariableMetricsBinary(criteria);
                    case COUNTER:
                        return getSensorVariableMetricsCounter(criteria);
                    case DOUBLE:
                        return getSensorVariableMetricsDouble(criteria);
                    case GPS:
                        return getSensorVariableMetricsGPS(criteria);
                    default:
                        throw new RuntimeException("Not supported metric type: " + sensorVariable.getMetricType());
                }
            default:
                break;
        }
        return new ArrayList<DataPointDouble>();

    }

    //Metric private api's
    private List<DataPointBinary> getSensorVariableMetricsBinary(Criteria criteria) {
        List<MetricsBinaryTypeDevice> metrics = DaoUtils.getMetricsBinaryTypeDeviceDao().getAll(
                MetricsBinaryTypeDevice
                        .builder()
                        .start(criteria.getStart())
                        .end(criteria.getEnd())
                        .sensorVariable(
                                SensorVariable.builder().id(criteria.getResourceModel().getResourceId()).build())
                        .build());
        List<DataPointBinary> genericMetrics = new ArrayList<DataPointBinary>();
        for (MetricsBinaryTypeDevice metric : metrics) {
            genericMetrics.add(DataPointBinary.get(metric, null, null));
        }
        return genericMetrics;
    }

    private List<DataPointGPS> getSensorVariableMetricsGPS(Criteria criteria) {
        List<DataPointGPS> metricsFinal = new ArrayList<DataPointGPS>();
        MetricsGPSTypeDevice metricConfig = MetricsGPSTypeDevice.builder()
                .sensorVariable(SensorVariable.builder().id(criteria.getResourceModel().getResourceId()).build())
                .build();
        if (criteria.getBucketDuration().equalsIgnoreCase("raw")) {
            metricConfig.setStart(criteria.getStart());
            metricConfig.setEnd(criteria.getEnd());
            List<MetricsGPSTypeDevice> metrics = DaoUtils.getMetricsGPSTypeDeviceDao().getAll(metricConfig);
            for (MetricsGPSTypeDevice metric : metrics) {
                metricsFinal.add(DataPointGPS.get(metric, null, null));
            }
        } else {
            //TODO: add support for runtime aggregation
        }
        return metricsFinal;
    }

    private List<DataPointDouble> getSensorVariableMetricsDouble(Criteria criteria) {
        List<DataPointDouble> metricsFinal = new ArrayList<DataPointDouble>();
        MetricsDoubleTypeDevice metricConfig = MetricsDoubleTypeDevice.builder()
                .sensorVariable(SensorVariable.builder().id(criteria.getResourceModel().getResourceId()).build())
                .build();
        long bucketDuration = criteria.getBucketDurationLong();
        long start = criteria.getStart();
        long end = criteria.getEnd();
        if (bucketDuration == -1) {
            metricConfig.setStart(criteria.getStart());
            metricConfig.setEnd(criteria.getEnd());
            List<MetricsDoubleTypeDevice> metrics = DaoUtils.getMetricsDoubleTypeDeviceDao().getAll(metricConfig);
            for (MetricsDoubleTypeDevice metric : metrics) {
                metricsFinal.add(DataPointDouble.get(metric, null, null));
            }
        } else {
            Long tmpEnd = start + bucketDuration;
            while (tmpEnd < end) {
                if (tmpEnd > end) {
                    break;
                }
                String sqlSelectQuery = MessageFormat.format(
                        DB_QUERY.getQuery(DB_QUERY.SELECT_METRICS_DOUBLE_BY_SENSOR_VARIABLE),
                        String.valueOf(criteria.getResourceModel().getResourceId()), String.valueOf(start),
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
                metricsFinal.add(DataPointDouble.get(metric, start, tmpEnd));
                start = tmpEnd;
                tmpEnd += bucketDuration;
            }
        }
        return metricsFinal;
    }

    private List<DataPointCounter> getSensorVariableMetricsCounter(Criteria criteria) {
        List<DataPointCounter> metricsFinal = new ArrayList<DataPointCounter>();
        MetricsCounterTypeDevice metricConfig = MetricsCounterTypeDevice.builder()
                .sensorVariable(SensorVariable.builder().id(criteria.getResourceModel().getResourceId()).build())
                .build();
        if (criteria.getBucketDuration().equalsIgnoreCase("raw")) {
            metricConfig.setStart(criteria.getStart());
            metricConfig.setEnd(criteria.getEnd());
            List<MetricsCounterTypeDevice> metrics = DaoUtils.getMetricsCounterTypeDeviceDao().getAll(metricConfig);
            for (MetricsCounterTypeDevice metric : metrics) {
                metricsFinal.add(DataPointCounter.get(metric, null, null));
            }

        } else {
            Calendar calendarFrom = Calendar.getInstance();
            Calendar calendarTo = Calendar.getInstance();
            calendarFrom.setTime(new Date(criteria.getStart()));
            calendarTo.setTime(new Date(criteria.getEnd()));
            String[] bucket = criteria.getBucketDuration().trim().split("(?<=\\d)(?=\\D)");
            if (bucket.length != 2) {
                _logger.warn("Invalid bucketDuration string: {}, result:{}", criteria.getBucketDuration(), bucket);
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
                        String.valueOf(criteria.getResourceModel().getResourceId()), String.valueOf(timestampTmpFrom),
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
                metricsFinal.add(DataPointCounter.get(metric, timestampTmpFrom, endTmp));

                if ((bucketString.equals("mn") || bucketString.equals("h"))
                        && endTmp > System.currentTimeMillis()) {
                    break;
                }
            }
        }
        return metricsFinal;
    }

    private List<DataPointDouble> getMetricsBattery(Criteria criteria) {
        List<DataPointDouble> metricsFinal = new ArrayList<DataPointDouble>();
        MetricsBatteryUsage metricConfig = MetricsBatteryUsage.builder()
                .node(Node.builder().id(criteria.getResourceModel().getResourceId()).build())
                .build();
        long bucketDuration = criteria.getBucketDurationLong();
        long start = criteria.getStart();
        long end = criteria.getEnd();
        if (bucketDuration == -1) {
            metricConfig.setStart(start);
            metricConfig.setEnd(end);
            List<MetricsBatteryUsage> metrics = DaoUtils.getMetricsBatteryUsageDao().getAll(metricConfig);
            for (MetricsBatteryUsage metric : metrics) {
                metricsFinal.add(DataPointDouble.get(metric, null, null));
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
                        String.valueOf(criteria.getResourceModel().getResourceId()), String.valueOf(start),
                        String.valueOf(tmpEnd));
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

                metricsFinal.add(DataPointDouble.get(metric, start, tmpEnd));

                metricConfig.setStart(start);
                start = tmpEnd;
                if (tmpEnd > end) {
                    break;
                }
            }
        }
        return metricsFinal;
    }

    @Override
    public void purge(ResourceModel resourceModel, ResourcePurgeConf purgeConf) {
        switch (resourceModel.getResourceType()) {
            case NODE:
                break;
            case SENSOR_VARIABLE:
                SensorVariable sVar = (SensorVariable) resourceModel.getResource();
                switch (sVar.getMetricType()) {

                    case BINARY:
                        MetricsBinaryTypeDevice metricBinary = new MetricsBinaryTypeDevice();
                        metricBinary.setSensorVariable(sVar);
                        metricBinary.setStart(purgeConf.getStart());
                        metricBinary.setEnd(purgeConf.getEnd());
                        metricBinary.setState(McUtils.getBoolean(purgeConf.getValue()));
                        DaoUtils.getMetricsBinaryTypeDeviceDao().deletePrevious(metricBinary);
                        break;
                    case COUNTER:
                        MetricsCounterTypeDevice metricCounter = new MetricsCounterTypeDevice();
                        metricCounter.setSensorVariable(sVar);
                        metricCounter.setStart(purgeConf.getStart());
                        metricCounter.setEnd(purgeConf.getEnd());
                        metricCounter.setValue(McUtils.getLong(purgeConf.getValue()));
                        metricCounter.setAggregationType(null);
                        DaoUtils.getMetricsCounterTypeDeviceDao().deletePrevious(metricCounter, purgeConf);
                        break;
                    case DOUBLE:
                        MetricsDoubleTypeDevice metricDouble = new MetricsDoubleTypeDevice();
                        metricDouble.setSensorVariable(sVar);
                        metricDouble.setStart(purgeConf.getStart());
                        metricDouble.setEnd(purgeConf.getEnd());
                        metricDouble.setAggregationType(null);
                        DaoUtils.getMetricsDoubleTypeDeviceDao().deletePrevious(metricDouble, purgeConf);
                        break;
                    default:
                        //Nothing to do
                        break;
                }
                break;
            default:
                break;

        }
    }

    @Override
    public void purge(ResourceModel resourceModel) {
        SensorVariable sVar = (SensorVariable) resourceModel.getResource();
        switch (sVar.getMetricType()) {
            case DOUBLE:
                DaoUtils.getMetricsDoubleTypeDeviceDao().deleteBySensorVariableRefId(sVar.getId());
                break;
            case BINARY:
                DaoUtils.getMetricsBinaryTypeDeviceDao().deleteBySensorVariableRefId(sVar.getId());
                break;
            case COUNTER:
                DaoUtils.getMetricsCounterTypeDeviceDao().deleteBySensorVariableRefId(sVar.getId());
                break;
            default:
                break;
        }
    }

    @Override
    public void purgeEverything() {
        DaoUtils.getMetricsBatteryUsageDao().deletePrevious(
                MetricsBatteryUsage.builder().timestamp(System.currentTimeMillis()).build());
        DaoUtils.getMetricsBinaryTypeDeviceDao().deletePrevious(
                MetricsBinaryTypeDevice.builder().timestamp(System.currentTimeMillis()).build());
        DaoUtils.getMetricsCounterTypeDeviceDao().deletePrevious(
                MetricsCounterTypeDevice.builder().timestamp(System.currentTimeMillis()).build());
        DaoUtils.getMetricsDoubleTypeDeviceDao().deletePrevious(
                MetricsDoubleTypeDevice.builder().timestamp(System.currentTimeMillis()).build());
        DaoUtils.getMetricsGPSTypeDeviceDao().deletePrevious(
                MetricsGPSTypeDevice.builder().timestamp(System.currentTimeMillis()).build());
    }

    @Override
    public Pong ping() {
        return Pong.builder()
                .reachable(DaoUtils.isDaoInitialized())
                .version(new SystemApi().getAbout().getApplicationVersion())
                .error(DaoUtils.isDaoInitialized() ? null : "looks like DAO not initialized yet!")
                .build();
    }

    @Override
    public void close() {
        // nothing to do
    }

}
