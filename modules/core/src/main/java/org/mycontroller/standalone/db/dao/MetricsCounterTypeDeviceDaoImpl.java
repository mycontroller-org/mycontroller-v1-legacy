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

import org.mycontroller.standalone.api.jaxrs.model.ResourcePurgeConf;
import org.mycontroller.standalone.db.tables.MetricsCounterTypeDevice;
import org.mycontroller.standalone.exceptions.McDatabaseException;
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
public class MetricsCounterTypeDeviceDaoImpl extends BaseAbstractDaoImpl<MetricsCounterTypeDevice, Object> implements
        MetricsCounterTypeDeviceDao {

    public MetricsCounterTypeDeviceDaoImpl(ConnectionSource connectionSource)
            throws SQLException {
        super(connectionSource, MetricsCounterTypeDevice.class);
    }

    @Override
    public void deletePrevious(MetricsCounterTypeDevice metric) {
        deletePrevious(metric, null);
    }

    @Override
    public void deletePrevious(MetricsCounterTypeDevice metric, ResourcePurgeConf purgeConfig) {
        try {
            DeleteBuilder<MetricsCounterTypeDevice, Object> deleteBuilder = this.getDao().deleteBuilder();
            Where<MetricsCounterTypeDevice, Object> where = deleteBuilder.where();
            int whereCount = 0;

            if (metric.getAggregationType() != null) {
                where.eq(MetricsCounterTypeDevice.KEY_AGGREGATION_TYPE, metric.getAggregationType());
                whereCount++;
            }
            if (metric.getSensorVariable() != null && metric.getSensorVariable().getId() != null) {
                where.eq(MetricsCounterTypeDevice.KEY_SENSOR_VARIABLE_ID, metric.getSensorVariable().getId());
                whereCount++;
            } else {
                _logger.warn("Sensor variable id is not supplied!"
                        + " Cannot perform purge operation without sensor variable id.");
                return;
            }
            if (metric.getTimestamp() != null) {
                where.le(MetricsCounterTypeDevice.KEY_TIMESTAMP, metric.getTimestamp());
                whereCount++;
            }
            if (metric.getStart() != null) {
                where.ge(MetricsCounterTypeDevice.KEY_TIMESTAMP, metric.getStart());
                whereCount++;
            }
            if (metric.getEnd() != null) {
                where.le(MetricsCounterTypeDevice.KEY_TIMESTAMP, metric.getEnd());
                whereCount++;
            }
            if (purgeConfig != null && purgeConfig.getAvg() != null && purgeConfig.getAvg().getValue() != null) {
                updatePurgeCondition(where, MetricsCounterTypeDevice.KEY_VALUE,
                        purgeConfig.getAvg().getValueLong(), purgeConfig.getAvg().getOperator());
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
            throw new McDatabaseException(ex);
        }
    }

    @Override
    public void deleteBySensorVariableRefId(int sensorValueRefId) {
        try {
            DeleteBuilder<MetricsCounterTypeDevice, Object> deleteBuilder = this.getDao().deleteBuilder();
            deleteBuilder.where().eq(MetricsCounterTypeDevice.KEY_SENSOR_VARIABLE_ID, sensorValueRefId);
            int count = deleteBuilder.delete();
            _logger.debug("Metric-sensorValueRefId:[{}] deleted, Delete count:{}", sensorValueRefId, count);
        } catch (SQLException ex) {
            _logger.error("unable to delete metric-sensorValueRefId:[{}]", sensorValueRefId, ex);
            throw new McDatabaseException(ex);
        }
    }

    @Override
    public List<MetricsCounterTypeDevice> getAll(MetricsCounterTypeDevice metric) {
        try {
            QueryBuilder<MetricsCounterTypeDevice, Object> queryBuilder = this.getDao().queryBuilder();
            Where<MetricsCounterTypeDevice, Object> whereBuilder = queryBuilder.where();
            whereBuilder.eq(MetricsCounterTypeDevice.KEY_SENSOR_VARIABLE_ID,
                    metric.getSensorVariable().getId());
            if (metric.getAggregationType() != null) {
                whereBuilder.and().eq(MetricsCounterTypeDevice.KEY_AGGREGATION_TYPE,
                        metric.getAggregationType());
            }
            if (metric.getStart() != null) {
                whereBuilder.and().gt(MetricsCounterTypeDevice.KEY_TIMESTAMP,
                        metric.getStart());
            }
            if (metric.getEnd() != null) {
                whereBuilder.and().le(MetricsCounterTypeDevice.KEY_TIMESTAMP,
                        metric.getEnd());
            }
            queryBuilder.orderBy(MetricsCounterTypeDevice.KEY_TIMESTAMP, true);
            return queryBuilder.query();
        } catch (SQLException ex) {
            _logger.error("unable to get, metric:{}", metric, ex);
            throw new McDatabaseException(ex);
        }
    }

    @Override
    public MetricsCounterTypeDevice get(MetricsCounterTypeDevice metric) {
        try {
            return this.getDao().queryForFirst(
                    this.getDao().queryBuilder()
                            .where()
                            .eq(MetricsCounterTypeDevice.KEY_SENSOR_VARIABLE_ID, metric.getSensorVariable().getId())
                            .and().eq(MetricsCounterTypeDevice.KEY_AGGREGATION_TYPE, metric.getAggregationType())
                            .and().eq(MetricsCounterTypeDevice.KEY_TIMESTAMP, metric.getTimestamp()).prepare());
        } catch (SQLException ex) {
            _logger.error("unable to get, metric:{}", metric, ex);
            throw new McDatabaseException(ex);
        }
    }

    @Override
    public List<MetricsCounterTypeDevice> getAll(List<Object> ids) {
        return null;
    }

    @Override
    public List<MetricsCounterTypeDevice> getAggregationRequiredVariableIds(AGGREGATION_TYPE aggregationType,
            Long fromTimestamp, Long toTimestamp) {
        QueryBuilder<MetricsCounterTypeDevice, Object> queryBuilder = this.getDao().queryBuilder();
        try {
            return queryBuilder.distinct().selectColumns(MetricsCounterTypeDevice.KEY_SENSOR_VARIABLE_ID)
                    .where().eq(MetricsCounterTypeDevice.KEY_AGGREGATION_TYPE, aggregationType).and()
                    .gt(MetricsCounterTypeDevice.KEY_TIMESTAMP, fromTimestamp).and()
                    .le(MetricsCounterTypeDevice.KEY_TIMESTAMP, toTimestamp)
                    .query();
        } catch (SQLException ex) {
            _logger.error("Exception,", ex);
            throw new McDatabaseException(ex);
        }
    }

    @Override
    public long countOf(AGGREGATION_TYPE aggregationType, long start, long end) {
        QueryBuilder<MetricsCounterTypeDevice, Object> queryBuilder = getDao().queryBuilder();
        try {
            return queryBuilder.where().gt(MetricsCounterTypeDevice.KEY_TIMESTAMP, start).and()
                    .le(MetricsCounterTypeDevice.KEY_TIMESTAMP, end).and()
                    .eq(MetricsCounterTypeDevice.KEY_AGGREGATION_TYPE, aggregationType).countOf();
        } catch (Exception ex) {
            _logger.error("Unable to execute countOf query", ex);
            throw new McDatabaseException(ex);
        }
    }

    @Override
    public boolean isRecordFound(AGGREGATION_TYPE aggregationType, long start, long end) {
        QueryBuilder<MetricsCounterTypeDevice, Object> queryBuilder = getDao().queryBuilder();
        try {
            return queryBuilder.where().gt(MetricsCounterTypeDevice.KEY_TIMESTAMP, start).and()
                    .le(MetricsCounterTypeDevice.KEY_TIMESTAMP, end).and()
                    .eq(MetricsCounterTypeDevice.KEY_AGGREGATION_TYPE, aggregationType).queryForFirst() != null;
        } catch (Exception ex) {
            _logger.error("Unable to execute countOf query", ex);
            throw new McDatabaseException(ex);
        }
    }
}
