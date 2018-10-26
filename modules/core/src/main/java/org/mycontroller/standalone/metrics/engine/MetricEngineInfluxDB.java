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

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.mycontroller.restclient.influxdb.InfluxDBClient;
import org.mycontroller.restclient.influxdb.model.Pong;
import org.mycontroller.restclient.influxdb.model.Query;
import org.mycontroller.restclient.influxdb.model.QueryResult;
import org.mycontroller.restclient.influxdb.model.Series;
import org.mycontroller.standalone.api.MetricApi;
import org.mycontroller.standalone.api.jaxrs.model.DataPointBase;
import org.mycontroller.standalone.api.jaxrs.model.DataPointBinary;
import org.mycontroller.standalone.api.jaxrs.model.DataPointCounter;
import org.mycontroller.standalone.api.jaxrs.model.DataPointDouble;
import org.mycontroller.standalone.api.jaxrs.model.ResourcePurgeConf;
import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.metrics.DATA_TYPE;
import org.mycontroller.standalone.metrics.MetricsUtils.METRIC_TYPE;
import org.mycontroller.standalone.metrics.engine.conf.MetricEngineConfigInfluxDB;
import org.mycontroller.standalone.metrics.model.Criteria;
import org.mycontroller.standalone.metrics.model.DataPointer;
import org.mycontroller.standalone.model.ResourceModel;
import org.mycontroller.standalone.utils.McUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Slf4j
public class MetricEngineInfluxDB implements IMetricEngine {
    private static final String TAG_INTERNAL_ID = "internal_id";
    private static final String TAG_TYPE = "type";
    private static final String MEASUREMENT_RESOURCE_DOUBLE = "mc_resource_double";
    private static final String MEASUREMENT_RESOURCE_COUNTER = "mc_resource_counter";
    private static final String MEASUREMENT_RESOURCE_BINARY = "mc_resource_binary";

    private static final String VALUE_COLUMN = "value";

    private static final TimeUnit timeUnit = TimeUnit.MILLISECONDS;

    private static final int FLUSH_POINTS = 200;
    private static final int FLUSH_DURATION = 2000;

    private InfluxDBClient _clientQuery = null;
    private InfluxDB _client = null;

    public MetricEngineInfluxDB(InfluxDB _client) {
        this._client = _client;
    }

    public MetricEngineInfluxDB(MetricEngineConfigInfluxDB _config) throws URISyntaxException {
        if (_config.getUsername() != null) {
            _client = InfluxDBFactory.connect(_config.getUrl(), _config.getUsername(), _config.getPassword());
            _clientQuery = new InfluxDBClient(_config.getUrl(), _config.getUsername(), _config.getPassword(),
                    _config.getDatabase(), _config.getTrustHostType());
        } else {
            _clientQuery = new InfluxDBClient(_config.getUrl(), _config.getDatabase(), _config.getTrustHostType());
            _client = InfluxDBFactory.connect(_config.getUrl());
        }
        _client.setDatabase(_config.getDatabase());
        _client.enableBatch(BatchOptions.DEFAULTS.actions(FLUSH_POINTS).flushDuration(FLUSH_DURATION));
        _logger.debug("MetricEngine, Influxdb client BatchSettings[flush, points:{}, duration:{} ms]",
                FLUSH_POINTS, FLUSH_DURATION);
    }

    private Point getPoint(String measurementName, long timestamp, Object value, Integer internalId, String type) {
        if (value instanceof Boolean) {
            return Point.measurement(MEASUREMENT_RESOURCE_DOUBLE)
                    .addField("value", (boolean) value)
                    .time(timestamp, timeUnit)
                    .tag(TAG_INTERNAL_ID, String.valueOf(internalId))
                    .tag(TAG_TYPE, type)
                    .build();
        } else {
            return Point.measurement(MEASUREMENT_RESOURCE_DOUBLE)
                    .addField("value", (Number) value)
                    .time(timestamp, timeUnit)
                    .tag(TAG_INTERNAL_ID, String.valueOf(internalId))
                    .tag(TAG_TYPE, type)
                    .build();
        }
    }

    @Override
    public void post(DataPointer data) {
        switch (data.getResourceModel().getResourceType()) {
            case NODE:
                Node node = (Node) data.getResourceModel().getResource();
                switch (data.getDataType()) {
                    case NODE_BATTERY_USAGE:    //Update battery level in to metrics table
                        Point point = getPoint(
                                MEASUREMENT_RESOURCE_DOUBLE,
                                data.getTimestamp(),
                                McUtils.getDouble(data.getPayload()),
                                node.getId(),
                                DATA_TYPE.NODE_BATTERY_USAGE.name());
                        _client.write(point);
                        return;
                    default:
                        break;
                }
                break;
            case SENSOR_VARIABLE:
                SensorVariable sensorVariable = (SensorVariable) data.getResourceModel().getResource();
                boolean writeSVdata = true;
                String measurement = null;
                String type = DATA_TYPE.SENSOR_VARIABLE.name();
                Object payload = null;
                switch (sensorVariable.getMetricType()) {
                    case DOUBLE:
                        measurement = MEASUREMENT_RESOURCE_DOUBLE;
                        payload = McUtils.getDouble(data.getPayload());
                        break;
                    case BINARY:
                        measurement = MEASUREMENT_RESOURCE_BINARY;
                        payload = McUtils.getBoolean(data.getPayload());
                        break;
                    case COUNTER:
                        measurement = MEASUREMENT_RESOURCE_COUNTER;
                        payload = McUtils.getLong(data.getPayload());
                        break;
                    case NONE:
                        //For None type nothing to do.
                        return;
                    default:
                        writeSVdata = false;
                        break;
                }
                if (writeSVdata) {
                    Point point = getPoint(
                            measurement,
                            data.getTimestamp(),
                            payload,
                            sensorVariable.getId(),
                            type);
                    _client.write(point);
                    return;
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
                        Query query = Query.builder()
                                .q(getQueryDouble(MEASUREMENT_RESOURCE_DOUBLE, DATA_TYPE.SENSOR_VARIABLE.name(),
                                        sensorVariable.getId(), criteria.getStart(),
                                        criteria.getEnd(), null, true))
                                .epoch("ms")
                                .build();
                        QueryResult response = _clientQuery.query(query);
                        if (response.getError() != null) {
                            _logger.warn("Query failed: {}, {}", criteria, response);
                            return DataPointDouble.builder().build();
                        }
                        if (response.getResults() == null
                                || response.getResults().get(0).getSeries() == null) {
                            return DataPointDouble.builder().build();
                        }
                        Series result = response.getResults().get(0).getSeries().get(0);
                        DataPointDouble metric = DataPointDouble.builder()
                                .min(McUtils.getDouble(result.getValues().get(0).get(1)))
                                .max(McUtils.getDouble(result.getValues().get(0).get(2)))
                                .avg(McUtils.getDouble(result.getValues().get(0).get(3)))
                                .samples((Integer) result.getValues().get(0).get(4))
                                .build();
                        metric.setStart(criteria.getStart());
                        metric.setEnd(criteria.getEnd());
                        return metric;
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
                        Query query = Query.builder()
                                .q(getQueryDouble(MEASUREMENT_RESOURCE_DOUBLE,
                                        DATA_TYPE.NODE_BATTERY_USAGE.name(),
                                        criteria.getResourceModel().getResourceId(), criteria.getStart(),
                                        criteria.getEnd(),
                                        criteria.getBucketDuration(), false))
                                .epoch("ms")
                                .build();
                        return getList(
                                METRIC_TYPE.DOUBLE,
                                _clientQuery.query(query),
                                criteria);
                    default:
                        break;
                }
                break;
            case SENSOR_VARIABLE:
                SensorVariable sensorVariable = (SensorVariable) criteria.getResourceModel().getResource();
                Query query = Query.builder().epoch("ms").build();
                switch (sensorVariable.getMetricType()) {
                    case BINARY:
                        query.setQ(getQueryBinary(MEASUREMENT_RESOURCE_BINARY,
                                DATA_TYPE.SENSOR_VARIABLE.name(),
                                sensorVariable.getId(), criteria.getStart(), criteria.getEnd(),
                                criteria.getBucketDuration()));
                        return getList(METRIC_TYPE.BINARY, _clientQuery.query(query), criteria);

                    case COUNTER:
                        query.setQ(getQueryCounter(MEASUREMENT_RESOURCE_COUNTER,
                                DATA_TYPE.SENSOR_VARIABLE.name(),
                                sensorVariable.getId(), criteria.getStart(), criteria.getEnd(),
                                criteria.getBucketDuration()));
                        return getList(METRIC_TYPE.COUNTER, _clientQuery.query(query), criteria);

                    case DOUBLE:
                        query.setQ(getQueryDouble(MEASUREMENT_RESOURCE_DOUBLE,
                                DATA_TYPE.SENSOR_VARIABLE.name(),
                                sensorVariable.getId(), criteria.getStart(), criteria.getEnd(),
                                criteria.getBucketDuration(), false));
                        return getList(METRIC_TYPE.DOUBLE, _clientQuery.query(query), criteria);

                    default:
                        throw new RuntimeException("Not supported metric type: " + sensorVariable.getMetricType());
                }
            default:
                break;
        }
        return new ArrayList<DataPointDouble>();
    }

    private List<?> getList(METRIC_TYPE type, QueryResult response, Criteria criteria) {
        if (response == null) {
            return null;
        }
        if (response.getError() != null || response.getResults() == null) {
            _logger.warn("Query failed:{}", response);
            return new ArrayList<DataPointDouble>();
        }

        //no data
        if (response.getResults().get(0).getSeries() == null) {
            return new ArrayList<DataPointDouble>();
        }
        List<List<Object>> valuesList = response.getResults().get(0).getSeries().get(0).getValues();
        switch (type) {
            case BINARY:
                List<DataPointBinary> metricDataBinary = new ArrayList<DataPointBinary>();
                for (List<Object> values : valuesList) {
                    metricDataBinary.add(DataPointBinary.get(McUtils.getBoolean(values.get(1)), (Long) values.get(0)));
                }
                return metricDataBinary;
            case COUNTER:
                List<DataPointCounter> metricDataCounter = new ArrayList<DataPointCounter>();
                boolean withStart = getBucketDuration(criteria.getBucketDuration()) == null ? false : true;
                long duration = MetricApi.getBucketDuration(criteria.getBucketDuration());
                for (List<Object> values : valuesList) {
                    if (withStart) {
                        int samples = (Integer) values.get(2);
                        if (samples > 0) {
                            metricDataCounter.add(DataPointCounter.get(
                                    McUtils.getLong(values.get(1)),
                                    (Integer) values.get(2),
                                    null,
                                    (Long) values.get(0),
                                    (Long) values.get(0) + duration));
                        } else {
                            DataPointCounter data = DataPointCounter.builder().samples(samples).build();
                            data.setEmpty(true);
                            data.setStart((Long) values.get(0));
                            data.setEnd((Long) values.get(0) + duration);
                            metricDataCounter.add(data);
                        }
                    } else {
                        metricDataCounter.add(DataPointCounter.get(
                                McUtils.getLong(values.get(1)),
                                1,
                                (Long) values.get(0),
                                null,
                                null));
                    }
                }
                return metricDataCounter;
            case DOUBLE:
                List<DataPointDouble> metricDataDouble = new ArrayList<DataPointDouble>();
                withStart = getBucketDuration(criteria.getBucketDuration()) == null ? false : true;
                duration = MetricApi.getBucketDuration(criteria.getBucketDuration());
                for (List<Object> values : valuesList) {
                    if (withStart) {
                        int samples = (Integer) values.get(4);
                        if (samples > 0) {
                            metricDataDouble.add(DataPointDouble.get(
                                    McUtils.getDouble(values.get(1)),
                                    McUtils.getDouble(values.get(2)),
                                    McUtils.getDouble(values.get(3)),
                                    samples,
                                    null,
                                    (Long) values.get(0),
                                    (Long) values.get(0) + duration));
                        } else {
                            DataPointDouble data = DataPointDouble.builder().samples(samples).build();
                            data.setEmpty(true);
                            data.setStart((Long) values.get(0));
                            data.setEnd((Long) values.get(0) + duration);
                            metricDataDouble.add(data);
                        }

                    } else {
                        Double value = McUtils.getDouble(values.get(1));
                        metricDataDouble.add(DataPointDouble.get(
                                value,
                                value,
                                value,
                                1,
                                (Long) values.get(0),
                                null,
                                null));
                    }
                }
                return metricDataDouble;
            default:
                break;

        }
        throw new RuntimeException("Not supported metric type, Metric: " + type + ", " + criteria);
    }

    private String getBucketDuration(String bucketDuration) {
        if (bucketDuration == null) {
            return null;
        }
        long duration = MetricApi.getBucketDuration(bucketDuration, -1);
        if (bucketDuration.endsWith("mn")) {
            return String.valueOf(duration / McUtils.ONE_MINUTE) + "m";
        } else if (bucketDuration.endsWith("m")) {
            return String.valueOf(duration / McUtils.ONE_DAY) + "d";
        } else if (bucketDuration.equalsIgnoreCase("raw")) {
            return null;
        } else {
            return bucketDuration;
        }
    }

    private String getQueryBinary(String measurement, String type, Integer internalId,
            Long start, Long end, String bucketDuration) {
        //select "value" from "1_2_state"
        //where time > 1491275029761000000 AND time <= 1491275937583000000 GROUP BY time(2m)
        StringBuilder builder = new StringBuilder();
        String bkDuration = getBucketDuration(bucketDuration);
        builder.append("SELECT ");
        updateColumnName(VALUE_COLUMN, builder);

        builder.append(" FROM ");
        updateColumnName(measurement, builder);

        builder.append(" WHERE ").append(TAG_INTERNAL_ID).append(" = '").append(internalId)
                .append("' AND ").append(TAG_TYPE).append(" = '").append(type).append("'")
                .append(" AND time > ").append(start).append("000000")
                .append(" AND time <= ").append(end).append("000000");
        if (bkDuration != null) {
            builder.append(" GROUP BY time(").append(bkDuration).append(")");
        }
        return builder.toString();
    }

    private String getQueryCounter(String measurement, String type, Integer internalId,
            Long start, Long end, String bucketDuration) {
        //select sum("value"), count("value") from "1_2_voltage"
        //where time > 1491275029761000000 AND time <= 1491275937583000000 GROUP BY time(2m)
        StringBuilder builder = new StringBuilder();
        String bkDuration = getBucketDuration(bucketDuration);
        builder.append("SELECT ");
        if (bkDuration != null) {
            updateColumnName("sum", VALUE_COLUMN, builder);
            builder.append(", ");
            updateColumnName("count", VALUE_COLUMN, builder);
        } else {
            updateColumnName(VALUE_COLUMN, builder);
        }

        builder.append(" FROM ");
        updateColumnName(measurement, builder);

        builder.append(" WHERE ").append(TAG_INTERNAL_ID).append(" = '").append(internalId)
                .append("' AND ").append(TAG_TYPE).append(" = '").append(type).append("'")
                .append(" AND time > ").append(start).append("000000")
                .append(" AND time <= ").append(end).append("000000");
        if (bkDuration != null) {
            builder.append(" GROUP BY time(").append(bkDuration).append(")");
        }
        return builder.toString();
    }

    private String getQueryDouble(String measurement, String type, Integer internalId,
            Long start, Long end, String bucketDuration, boolean forceMinMax) {
        //select mean("value"), min("value"), max("value"), count("value"), percentile("value",95) from "1_2_voltage"
        //where time > 1491275029761000000 AND time <= 1491275937583000000 GROUP BY time(2m)
        StringBuilder builder = new StringBuilder();
        String bkDuration = getBucketDuration(bucketDuration);
        builder.append("SELECT ");
        if (bkDuration != null || forceMinMax) {
            updateColumnName("min", VALUE_COLUMN, builder);
            builder.append(", ");
            updateColumnName("max", VALUE_COLUMN, builder);
            builder.append(", ");
            updateColumnName("mean", VALUE_COLUMN, builder);
            builder.append(", ");
            updateColumnName("count", VALUE_COLUMN, builder);
        } else {
            updateColumnName(VALUE_COLUMN, builder);
        }

        builder.append(" FROM ");
        updateColumnName(measurement, builder);

        builder.append(" WHERE ").append(TAG_INTERNAL_ID).append(" = '").append(internalId)
                .append("' AND ").append(TAG_TYPE).append(" = '").append(type).append("'")
                .append(" AND time > ").append(start).append("000000")
                .append(" AND time <= ").append(end).append("000000");
        if (bkDuration != null) {
            builder.append(" GROUP BY time(").append(bkDuration).append(")");
        }
        return builder.toString();
    }

    private void updateColumnName(String column, StringBuilder builder) {
        updateColumnName(null, column, builder);
    }

    private void updateColumnName(String function, String column, StringBuilder builder) {
        updateColumnName(function, column, null, builder);
    }

    private void updateColumnName(String function, String column, Object value, StringBuilder builder) {
        if (function != null) {
            builder.append(function).append("(\"").append(column);
            if (value != null) {
                builder.append(", ").append(value);
            }
            builder.append("\")");
        } else if (column != null) {
            builder.append("\"").append(column).append("\"");
        }
    }

    private String getDeleteQuery(String measurement, String type, Integer internalId, ResourcePurgeConf purgeConf) {
        StringBuilder builder = new StringBuilder();
        builder.append("DELETE FROM \"").append(measurement).append("\" WHERE \"")
                .append(TAG_TYPE).append("\" = '").append(type)
                .append("' AND \"").append(TAG_INTERNAL_ID).append("\" = '").append(internalId).append("'");
        if (purgeConf.getValue() != null && purgeConf.getAvg() != null && purgeConf.getAvg().getValue() != null) {
            builder.append(" AND \"value\" ").append(purgeConf.getAvg().getOperator()).append(" ")
                    .append(purgeConf.getAvg().getValue());
        }
        if (purgeConf.getStart() != null) {
            builder.append(" AND time").append(" >= ").append(purgeConf.getStart()).append("000000");
        }
        if (purgeConf.getEnd() != null) {
            builder.append(" AND time").append(" <= ").append(purgeConf.getEnd()).append("000000");
        }
        return builder.toString();
    }

    @Override
    public void purge(ResourceModel resourceModel, ResourcePurgeConf purgeConf) {
        String queryString = null;
        switch (resourceModel.getResourceType()) {
            case NODE:
                Node node = (Node) resourceModel.getResource();
                queryString = getDeleteQuery(MEASUREMENT_RESOURCE_DOUBLE, DATA_TYPE.NODE_BATTERY_USAGE.name(),
                        node.getId(), purgeConf);
                break;
            case SENSOR_VARIABLE:
                SensorVariable sVar = (SensorVariable) resourceModel.getResource();
                switch (sVar.getMetricType()) {
                    case BINARY:
                        queryString = getDeleteQuery(MEASUREMENT_RESOURCE_BINARY, DATA_TYPE.SENSOR_VARIABLE.name(),
                                sVar.getId(), purgeConf);
                        break;
                    case COUNTER:
                        queryString = getDeleteQuery(MEASUREMENT_RESOURCE_COUNTER, DATA_TYPE.SENSOR_VARIABLE.name(),
                                sVar.getId(), purgeConf);
                        break;
                    case DOUBLE:
                        queryString = getDeleteQuery(MEASUREMENT_RESOURCE_DOUBLE, DATA_TYPE.SENSOR_VARIABLE.name(),
                                sVar.getId(), purgeConf);
                        break;
                    default:
                        //Nothing to do
                        break;
                }
                break;
            default:
                break;
        }
        if (queryString != null) {
            Query query = Query.builder()
                    .q(queryString)
                    .epoch("ms")
                    .build();
            QueryResult response = _clientQuery.queryManagement(query);
            if (response.getResults().get(0).getError() != null) {
                _logger.warn("Failed to execute query[{}], ", query, response);
            }
        }
    }

    @Override
    public void purge(ResourceModel resourceModel) {
        purge(resourceModel, ResourcePurgeConf.builder().build());
    }

    private void purgeMeasurement(String measurement) {
        Query query = Query.builder()
                .epoch("ms")
                .q("DROP MEASUREMENT \"" + measurement + "\"")
                .build();
        //Purge measurements
        QueryResult response = _clientQuery.queryManagement(query);
        _logger.debug("{}", response);
    }

    @Override
    public void purgeEverything() {
        purgeMeasurement(MEASUREMENT_RESOURCE_BINARY);  //Purge binary
        purgeMeasurement(MEASUREMENT_RESOURCE_COUNTER); //Purge counter
        purgeMeasurement(MEASUREMENT_RESOURCE_DOUBLE);  //Purge double
    }

    @Override
    public org.mycontroller.standalone.metrics.model.Pong ping() {
        org.mycontroller.standalone.metrics.model.Pong pong = null;
        try {
            Pong influxPong = _clientQuery.ping();
            pong = org.mycontroller.standalone.metrics.model.Pong.builder()
                    .reachable(influxPong.isReachable())
                    .version(influxPong.getVersion())
                    .build();
            _logger.info("Ping response of influxDB {}", influxPong);
        } catch (Exception ex) {
            pong = org.mycontroller.standalone.metrics.model.Pong.builder()
                    .reachable(false)
                    .error(ex.getMessage())
                    .build();
            _logger.debug("Error, ", ex);
        }
        return pong;
    }

    @Override
    public void close() {
        if (_client != null) {
            _client.close();
            _logger.debug("Influxdb client connection closed.");
        }
    }
}
