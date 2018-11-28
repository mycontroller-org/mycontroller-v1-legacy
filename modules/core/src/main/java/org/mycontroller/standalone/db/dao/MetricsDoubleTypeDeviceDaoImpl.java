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
package org.mycontroller.standalone.db.dao;

import java.sql.SQLException;
import java.util.List;

import org.mycontroller.standalone.api.jaxrs.model.ResourcePurgCondition;
import org.mycontroller.standalone.api.jaxrs.model.ResourcePurgeConf;
import org.mycontroller.standalone.db.DB_TABLES;
import org.mycontroller.standalone.db.tables.MetricsDoubleTypeDevice;
import org.mycontroller.standalone.exceptions.McDatabaseException;
import org.mycontroller.standalone.metrics.MetricsUtils.AGGREGATION_TYPE;
import org.mycontroller.standalone.utils.McUtils;

import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Slf4j
public class MetricsDoubleTypeDeviceDaoImpl extends BaseAbstractDaoImpl<MetricsDoubleTypeDevice, Object> implements
        MetricsDoubleTypeDeviceDao {

    public MetricsDoubleTypeDeviceDaoImpl(ConnectionSource connectionSource)
            throws SQLException {
        super(connectionSource, MetricsDoubleTypeDevice.class);
    }

    @Override
    public void deletePrevious(MetricsDoubleTypeDevice metric) {
        deletePrevious(metric, null);
    }

    private void _deletePrevious(
            MetricsDoubleTypeDevice metric,
            ResourcePurgCondition condition,
            String valueKey) throws SQLException {
        DeleteBuilder<MetricsDoubleTypeDevice, Object> deleteBuilder = this.getDao().deleteBuilder();
        Where<MetricsDoubleTypeDevice, Object> where = deleteBuilder.where();
        int whereCount = 0;

        if (metric.getAggregationType() != null) {
            where.eq(MetricsDoubleTypeDevice.KEY_AGGREGATION_TYPE, metric.getAggregationType());
            whereCount++;
        }
        if (metric.getSensorVariable() != null && metric.getSensorVariable().getId() != null) {
            where.eq(MetricsDoubleTypeDevice.KEY_SENSOR_VARIABLE_ID, metric.getSensorVariable().getId());
            whereCount++;
        } else {
            _logger.warn("Sensor variable id is not supplied!"
                    + " Cannot perform purge operation without sensor variable id.");
            return;
        }
        if (metric.getTimestamp() != null) {
            where.le(MetricsDoubleTypeDevice.KEY_TIMESTAMP, metric.getTimestamp());
            whereCount++;
        }
        if (metric.getStart() != null) {
            where.ge(MetricsDoubleTypeDevice.KEY_TIMESTAMP, metric.getStart());
            whereCount++;
        }
        if (metric.getEnd() != null) {
            where.le(MetricsDoubleTypeDevice.KEY_TIMESTAMP, metric.getEnd());
            whereCount++;
        }
        if (condition != null && condition.getValue() != null) {    // purge value
            updatePurgeCondition(where, valueKey, condition.getValueDouble(), condition.getOperator());
            whereCount++;
        }
        if (whereCount > 0) {
            where.and(whereCount);
            deleteBuilder.setWhere(where);
        }
        int count = deleteBuilder.delete();
        _logger.debug("Metric:[{}] deleted, delete count:{}, statement:{}", metric, count,
                deleteBuilder.prepareStatementString());

    }

    @Override
    public void deletePrevious(MetricsDoubleTypeDevice metric, ResourcePurgeConf purgeConfig) {
        try {
            if (purgeConfig != null && purgeConfig.getValue() != null) {
                if (purgeConfig.getAvg() != null && purgeConfig.getAvg().getValue() != null) {
                    _deletePrevious(metric, purgeConfig.getAvg(), MetricsDoubleTypeDevice.KEY_AVG);
                }
                if (purgeConfig.getMin() != null && purgeConfig.getMin().getValue() != null) {
                    _deletePrevious(metric, purgeConfig.getMin(), MetricsDoubleTypeDevice.KEY_MIN);
                }
                if (purgeConfig.getMax() != null && purgeConfig.getMax().getValue() != null) {
                    _deletePrevious(metric, purgeConfig.getMax(), MetricsDoubleTypeDevice.KEY_MAX);
                }
            } else {
                _deletePrevious(metric, null, null);
            }
        } catch (SQLException ex) {
            _logger.error("unable to delete metric:[{}], {}", metric, purgeConfig, ex);
            throw new McDatabaseException(ex);
        }
    }

    @Override
    public void deleteBySensorVariableRefId(int sensorValueRefId) {
        try {
            DeleteBuilder<MetricsDoubleTypeDevice, Object> deleteBuilder = this.getDao().deleteBuilder();
            deleteBuilder.where().eq(MetricsDoubleTypeDevice.KEY_SENSOR_VARIABLE_ID, sensorValueRefId);
            int count = deleteBuilder.delete();
            _logger.debug("Metric-sensorValueRefId:[{}] deleted, Delete count:{}", sensorValueRefId, count);
        } catch (SQLException ex) {
            _logger.error("unable to delete metric-sensorValueRefId:[{}]", sensorValueRefId, ex);
            throw new McDatabaseException(ex);
        }
    }

    @Override
    public List<MetricsDoubleTypeDevice> getAll(MetricsDoubleTypeDevice metric) {
        try {
            QueryBuilder<MetricsDoubleTypeDevice, Object> queryBuilder = this.getDao().queryBuilder();
            Where<MetricsDoubleTypeDevice, Object> whereBuilder = queryBuilder.where();
            whereBuilder.eq(MetricsDoubleTypeDevice.KEY_SENSOR_VARIABLE_ID,
                    metric.getSensorVariable().getId());
            if (metric.getAggregationType() != null) {
                whereBuilder.and().eq(MetricsDoubleTypeDevice.KEY_AGGREGATION_TYPE,
                        metric.getAggregationType());
            }
            if (metric.getStart() != null) {
                whereBuilder.and().gt(MetricsDoubleTypeDevice.KEY_TIMESTAMP,
                        metric.getStart());
            }
            if (metric.getEnd() != null) {
                whereBuilder.and().le(MetricsDoubleTypeDevice.KEY_TIMESTAMP,
                        metric.getEnd());
            }
            queryBuilder.orderBy(MetricsDoubleTypeDevice.KEY_TIMESTAMP, true);
            return queryBuilder.query();
        } catch (SQLException ex) {
            _logger.error("unable to get, metric:{}", metric, ex);
            throw new McDatabaseException(ex);
        }
    }

    @Override
    public MetricsDoubleTypeDevice get(MetricsDoubleTypeDevice metric) {
        try {
            return this.getDao().queryForFirst(
                    this.getDao().queryBuilder()
                            .where()
                            .eq(MetricsDoubleTypeDevice.KEY_SENSOR_VARIABLE_ID, metric.getSensorVariable().getId())
                            .and().eq(MetricsDoubleTypeDevice.KEY_AGGREGATION_TYPE, metric.getAggregationType())
                            .and().eq(MetricsDoubleTypeDevice.KEY_TIMESTAMP, metric.getTimestamp()).prepare());
        } catch (SQLException ex) {
            _logger.error("unable to get, metric:{}", metric, ex);
            throw new McDatabaseException(ex);
        }
    }

    @Override
    public List<MetricsDoubleTypeDevice> getAll(List<Object> ids) {
        return null;
    }

    @Override
    public List<MetricsDoubleTypeDevice> getAggregationRequiredVariableIds(AGGREGATION_TYPE aggregationType,
            Long fromTimestamp, Long toTimestamp) {
        QueryBuilder<MetricsDoubleTypeDevice, Object> queryBuilder = this.getDao().queryBuilder();
        try {
            return queryBuilder.distinct().selectColumns(MetricsDoubleTypeDevice.KEY_SENSOR_VARIABLE_ID)
                    .where().eq(MetricsDoubleTypeDevice.KEY_AGGREGATION_TYPE, aggregationType).and()
                    .gt(MetricsDoubleTypeDevice.KEY_TIMESTAMP, fromTimestamp).and()
                    .le(MetricsDoubleTypeDevice.KEY_TIMESTAMP, toTimestamp)
                    .query();
        } catch (SQLException ex) {
            _logger.error("Exception,", ex);
            throw new McDatabaseException(ex);
        }
    }

    @Override
    public MetricsDoubleTypeDevice getMinMaxAvg(MetricsDoubleTypeDevice metric) {
        StringBuilder query = new StringBuilder();
        StringBuilder queryTimestamp = new StringBuilder();
        //timestamp from / to
        if (metric.getStart() != null) {
            queryTimestamp.append(" AND ").append(MetricsDoubleTypeDevice.KEY_TIMESTAMP).append(" > ")
                    .append(metric.getStart());
        }
        if (metric.getEnd() != null) {
            queryTimestamp.append(" AND ").append(MetricsDoubleTypeDevice.KEY_TIMESTAMP).append(" <= ")
                    .append(metric.getEnd());
        }
        try {
            //Query sample
            //SELECT MIN(MINREF) AS MIN FROM (SELECT MIN(MIN) AS MINREF FROM metrics_double_type_device WHERE
            //sensorVariableId=7 UNION SELECT MIN(AVG) AS MINREF FROM metrics_double_type_device WHERE
            //sensorVariableId=7 AND aggregationType=0 AND timestamp > fromTime AND timestamp <= toTime) AS TABLE_MIN

            //Query to get minumum
            query.append("SELECT MIN(MINREF) AS MIN FROM (SELECT MIN(MIN) AS MINREF FROM ")
                    .append(DB_TABLES.METRICS_DOUBLE_TYPE_DEVICE).append(" WHERE ")
                    .append(MetricsDoubleTypeDevice.KEY_SENSOR_VARIABLE_ID).append("=")
                    .append(metric.getSensorVariable().getId());
            if (queryTimestamp.length() > 0) {
                query.append(queryTimestamp);
            }
            query.append(" UNION ")
                    .append("SELECT MIN(AVG) AS MINREF FROM ").append(DB_TABLES.METRICS_DOUBLE_TYPE_DEVICE)
                    .append(" WHERE ")
                    .append(MetricsDoubleTypeDevice.KEY_SENSOR_VARIABLE_ID).append("=")
                    .append(metric.getSensorVariable().getId())
                    .append(" AND ").append(MetricsDoubleTypeDevice.KEY_AGGREGATION_TYPE).append("=")
                    .append(AGGREGATION_TYPE.RAW.ordinal());

            if (queryTimestamp.length() > 0) {
                query.append(queryTimestamp);
            }
            query.append(") AS TABLE_MIN");

            if (_logger.isTraceEnabled()) {
                _logger.trace("Minimum sql query:{}", query);
            }
            //Get minimum value
            Double min = McUtils.getDouble(this.getDao().queryRaw(query.toString()).getResults().get(0)[0]);

            //reset query
            query.setLength(0);

            //Query sample
            //SELECT MAX(MAXREF) AS MAX FROM (SELECT MAX(MAX) AS MAXREF FROM metrics_double_type_device
            //WHERE sensorVariableId=7 UNION SELECT MAX(AVG) AS MAXREF FROM metrics_double_type_device
            //WHERE sensorVariableId=7 AND aggregationType=0
            //AND timestamp > fromTime AND timestamp <= toTime) AS TABLE_MAX

            //Query to get maximum
            query.append("SELECT MAX(MAXREF) AS MAX FROM (SELECT MAX(MAX) AS MAXREF FROM ")
                    .append(DB_TABLES.METRICS_DOUBLE_TYPE_DEVICE).append(" WHERE ")
                    .append(MetricsDoubleTypeDevice.KEY_SENSOR_VARIABLE_ID).append("=")
                    .append(metric.getSensorVariable().getId());
            if (queryTimestamp.length() > 0) {
                query.append(queryTimestamp);
            }
            query.append(" UNION ")
                    .append("SELECT MAX(AVG) AS MAXREF FROM ").append(DB_TABLES.METRICS_DOUBLE_TYPE_DEVICE)
                    .append(" WHERE ")
                    .append(MetricsDoubleTypeDevice.KEY_SENSOR_VARIABLE_ID).append("=")
                    .append(metric.getSensorVariable().getId())
                    .append(" AND ").append(MetricsDoubleTypeDevice.KEY_AGGREGATION_TYPE).append("=")
                    .append(AGGREGATION_TYPE.RAW.ordinal());
            //timestamp from / to
            if (queryTimestamp.length() > 0) {
                query.append(queryTimestamp);
            }

            query.append(") AS TABLE_MAX");

            if (_logger.isTraceEnabled()) {
                _logger.trace("Maximum sql query:{}", query);
            }

            //Get maximum value
            Double max = McUtils.getDouble(this.getDao().queryRaw(query.toString()).getResults().get(0)[0]);

            //reset query
            query.setLength(0);

            //Query sample
            //SELECT ROUND(SUM(avg * samples) / SUM(samples), 2) AS AVG FROM metrics_double_type_device
            //WHERE sensorVariableId=7 AND timestamp > fromTime AND timestamp <= toTime) AS MASTER_TABLE

            //Query to get average
            query.append("SELECT ROUND(SUM(").append(MetricsDoubleTypeDevice.KEY_AVG).append(" * ")
                    .append(MetricsDoubleTypeDevice.KEY_SAMPLES).append(") / SUM(")
                    .append(MetricsDoubleTypeDevice.KEY_SAMPLES).append("), 2) AS AVG FROM ")
                    .append(DB_TABLES.METRICS_DOUBLE_TYPE_DEVICE).append(" WHERE ")
                    .append(MetricsDoubleTypeDevice.KEY_SENSOR_VARIABLE_ID).append("=")
                    .append(metric.getSensorVariable().getId());
            //timestamp from / to
            if (queryTimestamp.length() > 0) {
                query.append(queryTimestamp.toString());
            }

            if (_logger.isTraceEnabled()) {
                _logger.trace("Average sql query:{}", query);
            }

            //Get average value
            Double avg = McUtils.getDouble(this.getDao().queryRaw(query.toString()).getResults().get(0)[0]);

            return MetricsDoubleTypeDevice.builder()
                    .min(min)
                    .max(max)
                    .avg(avg)
                    .build();
        } catch (SQLException ex) {
            _logger.error("Unable to execute query:{}", query, ex);
            throw new McDatabaseException(ex);
        }
    }

    @Override
    public long countOf(AGGREGATION_TYPE aggregationType, long start, long end) {
        QueryBuilder<MetricsDoubleTypeDevice, Object> queryBuilder = getDao().queryBuilder();
        try {
            return queryBuilder.where().gt(MetricsDoubleTypeDevice.KEY_TIMESTAMP, start).and()
                    .le(MetricsDoubleTypeDevice.KEY_TIMESTAMP, end).and()
                    .eq(MetricsDoubleTypeDevice.KEY_AGGREGATION_TYPE, aggregationType).countOf();
        } catch (Exception ex) {
            _logger.error("Unable to execute countOf query", ex);
            throw new McDatabaseException(ex);
        }
    }

    @Override
    public boolean isRecordFound(AGGREGATION_TYPE aggregationType, long start, long end) {
        QueryBuilder<MetricsDoubleTypeDevice, Object> queryBuilder = getDao().queryBuilder();
        try {
            return queryBuilder.where()
                    .gt(MetricsDoubleTypeDevice.KEY_TIMESTAMP, start).and()
                    .le(MetricsDoubleTypeDevice.KEY_TIMESTAMP, end).and()
                    .eq(MetricsDoubleTypeDevice.KEY_AGGREGATION_TYPE, aggregationType)
                    .queryForFirst() != null;
        } catch (Exception ex) {
            _logger.error("Unable to execute countOf query", ex);
            throw new McDatabaseException(ex);
        }
    }
}
