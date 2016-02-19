/**
 * Copyright (C) 2015-2016 Jeeva Kandasamy (jkandasa@gmail.com)
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
package org.mycontroller.standalone.db.dao;

import java.sql.SQLException;
import java.util.List;

import org.mycontroller.standalone.NumericUtils;
import org.mycontroller.standalone.db.DB_TABLES;
import org.mycontroller.standalone.db.tables.MetricsDoubleTypeDevice;
import org.mycontroller.standalone.metrics.MetricsUtils.AGGREGATION_TYPE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class MetricsDoubleTypeDeviceDaoImpl extends BaseAbstractDaoImpl<MetricsDoubleTypeDevice, Object> implements
        MetricsDoubleTypeDeviceDao {
    private static final Logger _logger = LoggerFactory.getLogger(MetricsDoubleTypeDeviceDaoImpl.class);

    public MetricsDoubleTypeDeviceDaoImpl(ConnectionSource connectionSource)
            throws SQLException {
        super(connectionSource, MetricsDoubleTypeDevice.class);
    }

    @Override
    public void deletePrevious(MetricsDoubleTypeDevice metric) {
        try {
            DeleteBuilder<MetricsDoubleTypeDevice, Object> deleteBuilder = this.getDao().deleteBuilder();
            deleteBuilder.where().eq(MetricsDoubleTypeDevice.KEY_AGGREGATION_TYPE, metric.getAggregationType())
                    .and().le(MetricsDoubleTypeDevice.KEY_TIMESTAMP, metric.getTimestamp());

            int count = this.getDao().delete(deleteBuilder.prepare());
            _logger.debug("Metric:[{}] deleted, Delete count:{}", metric, count);
        } catch (SQLException ex) {
            _logger.error("unable to delete metric:[{}]", metric, ex);
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
            if (metric.getTimestampFrom() != null) {
                whereBuilder.and().gt(MetricsDoubleTypeDevice.KEY_TIMESTAMP,
                        metric.getTimestampFrom());
            }
            if (metric.getTimestampTo() != null) {
                whereBuilder.and().le(MetricsDoubleTypeDevice.KEY_TIMESTAMP,
                        metric.getTimestampTo());
            }
            return queryBuilder.query();
        } catch (SQLException ex) {
            _logger.error("unable to get, metric:{}", metric, ex);
        }
        return null;
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
        }
        return null;
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
            return null;
        }
    }

    @Override
    public MetricsDoubleTypeDevice getMinMaxAvg(Integer sensorVariableId) {
        StringBuilder query = new StringBuilder();
        try {
            //Query sample
            //SELECT MIN(MINREF) AS MIN FROM (SELECT MIN(MIN) AS MINREF FROM metrics_double_type_device WHERE 
            //sensorVariableId=7 UNION SELECT MIN(AVG) AS MINREF FROM metrics_double_type_device WHERE 
            //sensorVariableId=7 AND aggregationType=0) AS TABLE_MIN

            //Query to get minumum
            query.append("SELECT MIN(MINREF) AS MIN FROM (SELECT MIN(MIN) AS MINREF FROM ")
                    .append(DB_TABLES.METRICS_DOUBLE_TYPE_DEVICE).append(" WHERE ")
                    .append(MetricsDoubleTypeDevice.KEY_SENSOR_VARIABLE_ID).append("=").append(sensorVariableId)
                    .append(" UNION ")
                    .append("SELECT MIN(AVG) AS MINREF FROM ").append(DB_TABLES.METRICS_DOUBLE_TYPE_DEVICE)
                    .append(" WHERE ")
                    .append(MetricsDoubleTypeDevice.KEY_SENSOR_VARIABLE_ID).append("=").append(sensorVariableId)
                    .append(" AND ").append(MetricsDoubleTypeDevice.KEY_AGGREGATION_TYPE).append("=")
                    .append(AGGREGATION_TYPE.RAW.ordinal())
                    .append(") AS TABLE_MIN");

            if (_logger.isTraceEnabled()) {
                _logger.trace("Minimum sql query:{}", query);
            }
            //Get minimum value
            Double min = NumericUtils.getDouble(this.getDao().queryRaw(query.toString()).getResults().get(0)[0]);
            //reset query
            query.setLength(0);

            //Query sample
            //SELECT MAX(MAXREF) AS MAX FROM (SELECT MAX(MAX) AS MAXREF FROM metrics_double_type_device
            //WHERE sensorVariableId=7 UNION SELECT MAX(AVG) AS MAXREF FROM metrics_double_type_device 
            //WHERE sensorVariableId=7 AND aggregationType=0) AS TABLE_MAX

            //Query to get maximum
            query.append("SELECT MAX(MAXREF) AS MAX FROM (SELECT MAX(MAX) AS MAXREF FROM ")
                    .append(DB_TABLES.METRICS_DOUBLE_TYPE_DEVICE).append(" WHERE ")
                    .append(MetricsDoubleTypeDevice.KEY_SENSOR_VARIABLE_ID).append("=").append(sensorVariableId)
                    .append(" UNION ")
                    .append("SELECT MAX(AVG) AS MAXREF FROM ").append(DB_TABLES.METRICS_DOUBLE_TYPE_DEVICE)
                    .append(" WHERE ")
                    .append(MetricsDoubleTypeDevice.KEY_SENSOR_VARIABLE_ID).append("=").append(sensorVariableId)
                    .append(" AND ").append(MetricsDoubleTypeDevice.KEY_AGGREGATION_TYPE).append("=")
                    .append(AGGREGATION_TYPE.RAW.ordinal())
                    .append(") AS TABLE_MAX");

            if (_logger.isTraceEnabled()) {
                _logger.trace("Maximum sql query:{}", query);
            }

            //Get maximum value
            Double max = NumericUtils.getDouble(this.getDao().queryRaw(query.toString()).getResults().get(0)[0]);
            //reset query
            query.setLength(0);

            //Query sample
            //SELECT ROUND(SUM(avg * samples) / SUM(samples), 2) AS AVG FROM metrics_double_type_device
            //WHERE sensorVariableId=7 ) AS MASTER_TABLE

            //Query to get average
            query.append("SELECT ROUND(SUM(").append(MetricsDoubleTypeDevice.KEY_AVG).append(" * ")
                    .append(MetricsDoubleTypeDevice.KEY_SAMPLES).append(") / SUM(")
                    .append(MetricsDoubleTypeDevice.KEY_SAMPLES).append("), 2) AS AVG FROM ")
                    .append(DB_TABLES.METRICS_DOUBLE_TYPE_DEVICE).append(" WHERE ")
                    .append(MetricsDoubleTypeDevice.KEY_SENSOR_VARIABLE_ID).append("=").append(sensorVariableId);

            if (_logger.isTraceEnabled()) {
                _logger.trace("Average sql query:{}", query);
            }

            //Get average value
            Double avg = NumericUtils.getDouble(this.getDao().queryRaw(query.toString()).getResults().get(0)[0]);

            return MetricsDoubleTypeDevice.builder()
                    .min(min)
                    .max(max)
                    .avg(avg)
                    .build();
        } catch (SQLException ex) {
            _logger.error("Unable to execute query:{}", query, ex);
        }

        return null;
    }
}
