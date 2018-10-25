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

import org.mycontroller.standalone.db.tables.MetricsGPSTypeDevice;
import org.mycontroller.standalone.metrics.MetricsUtils.AGGREGATION_TYPE;

import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Slf4j
public class MetricsGPSTypeDeviceDaoImpl extends BaseAbstractDaoImpl<MetricsGPSTypeDevice, Object> implements
        MetricsGPSTypeDeviceDao {

    public MetricsGPSTypeDeviceDaoImpl(ConnectionSource connectionSource)
            throws SQLException {
        super(connectionSource, MetricsGPSTypeDevice.class);
    }

    @Override
    public void deletePrevious(MetricsGPSTypeDevice metric) {
        try {
            DeleteBuilder<MetricsGPSTypeDevice, Object> deleteBuilder = this.getDao().deleteBuilder();
            Where<MetricsGPSTypeDevice, Object> where = deleteBuilder.where();
            int whereCount = 0;

            if (metric.getAggregationType() != null) {
                where.eq(MetricsGPSTypeDevice.KEY_AGGREGATION_TYPE, metric.getAggregationType());
                whereCount++;
            }
            if (metric.getSensorVariable() != null && metric.getSensorVariable().getId() != null) {
                where.eq(MetricsGPSTypeDevice.KEY_SENSOR_VARIABLE_ID, metric.getSensorVariable().getId());
                whereCount++;
            } else {
                _logger.warn("Sensor variable id is not supplied!"
                        + " Cannot perform purge operation without sensor variable id.");
                return;
            }
            if (metric.getTimestamp() != null) {
                where.le(MetricsGPSTypeDevice.KEY_TIMESTAMP, metric.getTimestamp());
                whereCount++;
            }
            if (metric.getStart() != null) {
                where.ge(MetricsGPSTypeDevice.KEY_TIMESTAMP, metric.getStart());
                whereCount++;
            }
            if (metric.getEnd() != null) {
                where.le(MetricsGPSTypeDevice.KEY_TIMESTAMP, metric.getEnd());
                whereCount++;
            }

            if (whereCount > 0) {
                where.and(whereCount);
                deleteBuilder.setWhere(where);
            }
            int count = deleteBuilder.delete();
            _logger.debug("Metric:[{}] deleted, Delete count:{}", metric, count);
        } catch (SQLException ex) {
            _logger.error("unable to delete metric:[{}]", metric, ex);
        }
    }

    @Override
    public void deleteBySensorVariableRefId(int sensorValueRefId) {
        try {
            DeleteBuilder<MetricsGPSTypeDevice, Object> deleteBuilder = this.getDao().deleteBuilder();
            deleteBuilder.where().eq(MetricsGPSTypeDevice.KEY_SENSOR_VARIABLE_ID, sensorValueRefId);
            int count = deleteBuilder.delete();
            _logger.debug("Metric-sensorValueRefId:[{}] deleted, Delete count:{}", sensorValueRefId, count);
        } catch (SQLException ex) {
            _logger.error("unable to delete metric-sensorValueRefId:[{}]", sensorValueRefId, ex);
        }
    }

    @Override
    public List<MetricsGPSTypeDevice> getAll(MetricsGPSTypeDevice metric) {
        try {
            QueryBuilder<MetricsGPSTypeDevice, Object> queryBuilder = this.getDao().queryBuilder();
            Where<MetricsGPSTypeDevice, Object> whereBuilder = queryBuilder.where();
            whereBuilder.eq(MetricsGPSTypeDevice.KEY_SENSOR_VARIABLE_ID,
                    metric.getSensorVariable().getId());
            if (metric.getAggregationType() != null) {
                whereBuilder.and().eq(MetricsGPSTypeDevice.KEY_AGGREGATION_TYPE,
                        metric.getAggregationType());
            }
            if (metric.getStart() != null) {
                whereBuilder.and().gt(MetricsGPSTypeDevice.KEY_TIMESTAMP,
                        metric.getStart());
            }
            if (metric.getEnd() != null) {
                whereBuilder.and().le(MetricsGPSTypeDevice.KEY_TIMESTAMP,
                        metric.getEnd());
            }
            queryBuilder.orderBy(MetricsGPSTypeDevice.KEY_TIMESTAMP, true);
            return queryBuilder.query();
        } catch (SQLException ex) {
            _logger.error("unable to get, metric:{}", metric, ex);
        }
        return null;
    }

    @Override
    public MetricsGPSTypeDevice get(MetricsGPSTypeDevice metric) {
        try {
            return this.getDao().queryForFirst(
                    this.getDao().queryBuilder()
                            .where()
                            .eq(MetricsGPSTypeDevice.KEY_SENSOR_VARIABLE_ID, metric.getSensorVariable().getId())
                            .and().eq(MetricsGPSTypeDevice.KEY_AGGREGATION_TYPE, metric.getAggregationType())
                            .and().eq(MetricsGPSTypeDevice.KEY_TIMESTAMP, metric.getTimestamp()).prepare());
        } catch (SQLException ex) {
            _logger.error("unable to get, metric:{}", metric, ex);
        }
        return null;
    }

    @Override
    public List<MetricsGPSTypeDevice> getAll(List<Object> ids) {
        return null;
    }

    @Override
    public List<MetricsGPSTypeDevice> getAggregationRequiredVariableIds(AGGREGATION_TYPE aggregationType,
            Long start, Long end) {
        QueryBuilder<MetricsGPSTypeDevice, Object> queryBuilder = this.getDao().queryBuilder();
        try {
            return queryBuilder.distinct().selectColumns(MetricsGPSTypeDevice.KEY_SENSOR_VARIABLE_ID)
                    .where().eq(MetricsGPSTypeDevice.KEY_AGGREGATION_TYPE, aggregationType).and()
                    .gt(MetricsGPSTypeDevice.KEY_TIMESTAMP, start).and()
                    .le(MetricsGPSTypeDevice.KEY_TIMESTAMP, end)
                    .query();
        } catch (SQLException ex) {
            _logger.error("Exception,", ex);
            return null;
        }
    }
}
