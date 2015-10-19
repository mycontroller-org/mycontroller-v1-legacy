/**
 * Copyright (C) 2015 Jeeva Kandasamy (jkandasa@gmail.com)
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

import org.mycontroller.standalone.db.tables.MetricsDoubleTypeDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.j256.ormlite.dao.Dao.CreateOrUpdateStatus;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class MetricsDoubleTypeDeviceDaoImpl extends BaseAbstractDao<MetricsDoubleTypeDevice, Object> implements
        MetricsDoubleTypeDeviceDao {
    private static final Logger _logger = LoggerFactory.getLogger(MetricsDoubleTypeDeviceDaoImpl.class);

    public MetricsDoubleTypeDeviceDaoImpl(ConnectionSource connectionSource)
            throws SQLException {
        super(connectionSource, MetricsDoubleTypeDevice.class);
    }

    @Override
    public void create(MetricsDoubleTypeDevice metric) {
        try {
            int count = this.getDao().create(metric);
            _logger.debug("Created Metric:[{}], Create count:{}", metric, count);

        } catch (SQLException ex) {
            _logger.error("unable to add Metric:[{}]", metric, ex);
        }
    }

    @Override
    public void createOrUpdate(MetricsDoubleTypeDevice metric) {
        try {
            CreateOrUpdateStatus status = this.getDao().createOrUpdate(metric);
            _logger.debug("CreateOrUpdate Metric:[{}],Create:{},Update:{},Lines Changed:{}",
                    metric, status.isCreated(), status.isUpdated(),
                    status.getNumLinesChanged());
        } catch (SQLException ex) {
            _logger.error("unable to createOrUpdate Metric:[{}]", metric, ex);
        }
    }

    @Override
    public void delete(MetricsDoubleTypeDevice metric) {
        try {
            int count = this.getDao().delete(metric);
            _logger.debug("Metric:[{}] deleted, Delete count:{}", metric, count);
        } catch (SQLException ex) {
            _logger.error("unable to delete metric:[{}]", metric, ex);
        }
    }

    @Override
    public void deletePrevious(MetricsDoubleTypeDevice metric) {
        try {
            DeleteBuilder<MetricsDoubleTypeDevice, Object> deleteBuilder = this.getDao().deleteBuilder();
            deleteBuilder.where().eq(MetricsDoubleTypeDevice.AGGREGATION_TYPE, metric.getAggregationType())
                    .and().lt(MetricsDoubleTypeDevice.TIMESTAMP, metric.getTimestamp());

            int count = this.getDao().delete(deleteBuilder.prepare());
            _logger.debug("Metric:[{}] deleted, Delete count:{}", metric, count);
        } catch (SQLException ex) {
            _logger.error("unable to delete metric:[{}]", metric, ex);
        }
    }

    @Override
    public void deleteBySensorRefId(int sensorValueRefId) {
        try {
            DeleteBuilder<MetricsDoubleTypeDevice, Object> deleteBuilder = this.getDao().deleteBuilder();
            deleteBuilder.where().eq(MetricsDoubleTypeDevice.SENSOR_VALUE_REF_ID, sensorValueRefId);
            int count = this.getDao().delete(deleteBuilder.prepare());
            _logger.debug("Metric-sensorValueRefId:[{}] deleted, Delete count:{}", sensorValueRefId, count);
        } catch (SQLException ex) {
            _logger.error("unable to delete metric-sensorValueRefId:[{}]", sensorValueRefId, ex);
        }
    }

    @Override
    public void update(MetricsDoubleTypeDevice metric) {
        try {
            int count = this.getDao().update(metric);
            _logger.debug("Metric:[{}] updated, Update count:{}", metric, count);
        } catch (SQLException ex) {
            _logger.error("unable to update metric:[{}]", metric, ex);
        }
    }

    @Override
    public List<MetricsDoubleTypeDevice> getAll(MetricsDoubleTypeDevice metric) {
        try {
            QueryBuilder<MetricsDoubleTypeDevice, Object> queryBuilder = this.getDao().queryBuilder();
            Where<MetricsDoubleTypeDevice, Object> whereBuilder = queryBuilder.where();
            whereBuilder.eq(MetricsDoubleTypeDevice.SENSOR_VALUE_REF_ID,
                    metric.getSensorValue().getId())
                    .and().eq(MetricsDoubleTypeDevice.AGGREGATION_TYPE,
                            metric.getAggregationType());
            if (metric.getTimestampFrom() != null) {
                whereBuilder.and().ge(MetricsDoubleTypeDevice.TIMESTAMP,
                        metric.getTimestampFrom());
            }
            if (metric.getTimestampTo() != null) {
                whereBuilder.and().le(MetricsDoubleTypeDevice.TIMESTAMP,
                        metric.getTimestampTo());
            }
            return queryBuilder.query();
        } catch (SQLException ex) {
            _logger.error("unable to get, metric:{}", metric, ex);
        }
        return null;
    }

    @Override
    public List<MetricsDoubleTypeDevice> getAll() {
        try {
            return this.getDao().queryForAll();
        } catch (SQLException ex) {
            _logger.error("unable to getAll", ex);
        }
        return null;
    }

    @Override
    public MetricsDoubleTypeDevice get(MetricsDoubleTypeDevice metric) {
        try {
            return this.getDao().queryForFirst(
                    this.getDao().queryBuilder()
                            .where().eq(MetricsDoubleTypeDevice.SENSOR_VALUE_REF_ID, metric.getSensorValue().getId())
                            .and().eq(MetricsDoubleTypeDevice.AGGREGATION_TYPE, metric.getAggregationType())
                            .and().eq(MetricsDoubleTypeDevice.TIMESTAMP, metric.getTimestamp()).prepare());
        } catch (SQLException ex) {
            _logger.error("unable to get, metric:{}", metric, ex);
        }
        return null;
    }
}
