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

import org.mycontroller.standalone.db.tables.MetricsBinaryTypeDevice;
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
public class MetricsBinaryTypeDeviceDaoImpl extends BaseAbstractDao<MetricsBinaryTypeDevice, Object> implements
        MetricsBinaryTypeDeviceDao {
    private static final Logger _logger = LoggerFactory.getLogger(MetricsBinaryTypeDeviceDaoImpl.class);

    public MetricsBinaryTypeDeviceDaoImpl(ConnectionSource connectionSource)
            throws SQLException {
        super(connectionSource, MetricsBinaryTypeDevice.class);
    }

    @Override
    public void create(MetricsBinaryTypeDevice metric) {
        try {
            int count = this.getDao().create(metric);
            _logger.debug("Created Metric:[{}], Create count:{}", metric, count);

        } catch (SQLException ex) {
            _logger.error("unable to add Metric:[{}]", metric, ex);
        }
    }

    @Override
    public void createOrUpdate(MetricsBinaryTypeDevice metric) {
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
    public void delete(MetricsBinaryTypeDevice metric) {
        try {
            int count = this.getDao().delete(metric);
            _logger.debug("Metric:[{}] deleted, Delete count:{}", metric, count);
        } catch (SQLException ex) {
            _logger.error("unable to delete metric:[{}]", metric, ex);
        }
    }

    @Override
    public void deleteBySensorValueRefId(int sensorValueRefId) {
        try {
            DeleteBuilder<MetricsBinaryTypeDevice, Object> deleteBuilder = this.getDao().deleteBuilder();
            deleteBuilder.where().eq(MetricsBinaryTypeDevice.SENSOR_VALUE_REF_ID, sensorValueRefId);
            int count = deleteBuilder.delete();
            _logger.debug("Metric-sensorValueRefId:[{}] deleted, Delete count:{}", sensorValueRefId, count);
        } catch (SQLException ex) {
            _logger.error("unable to delete metric-sensorValueRefId:[{}]", sensorValueRefId, ex);
        }
    }

    @Override
    public void deletePrevious(MetricsBinaryTypeDevice metric) {
        try {
            DeleteBuilder<MetricsBinaryTypeDevice, Object> deleteBuilder = this.getDao().deleteBuilder();
            deleteBuilder.where().lt(MetricsBinaryTypeDevice.TIMESTAMP, metric.getTimestamp());
            int count = this.getDao().delete(deleteBuilder.prepare());
            _logger.debug("Metric:[{}] deleted, Delete count:{}", metric, count);
        } catch (SQLException ex) {
            _logger.error("unable to delete metric:[{}]", metric, ex);
        }
    }

    @Override
    public void update(MetricsBinaryTypeDevice metric) {
        try {
            int count = this.getDao().update(metric);
            _logger.debug("Metric:[{}] updated, Update count:{}", metric, count);
        } catch (SQLException ex) {
            _logger.error("unable to update metric:[{}]", metric, ex);
        }
    }

    @Override
    public List<MetricsBinaryTypeDevice> getAll(MetricsBinaryTypeDevice metric) {
        try {
            QueryBuilder<MetricsBinaryTypeDevice, Object> queryBuilder = this.getDao().queryBuilder();
            Where<MetricsBinaryTypeDevice, Object> where = queryBuilder.where();
            where.eq(MetricsBinaryTypeDevice.SENSOR_VALUE_REF_ID, metric.getSensorValue().getId());
            if (metric.getTimestampFrom() != null) {
                where.and().ge(MetricsBinaryTypeDevice.TIMESTAMP, metric.getTimestampFrom());
            }
            if (metric.getTimestampTo() != null) {
                where.and().le(MetricsBinaryTypeDevice.TIMESTAMP, metric.getTimestampTo());
            }
            return queryBuilder.query();
        } catch (SQLException ex) {
            _logger.error("unable to get, metric:{}", metric, ex);
        }
        return null;
    }

    @Override
    public MetricsBinaryTypeDevice get(MetricsBinaryTypeDevice metric) {
        try {
            return this.getDao().queryForFirst(
                    this.getDao().queryBuilder()
                            .where().eq(MetricsBinaryTypeDevice.SENSOR_VALUE_REF_ID, metric.getSensorValue().getId())
                            .and().eq(MetricsBinaryTypeDevice.TIMESTAMP, metric.getTimestamp()).prepare());
        } catch (SQLException ex) {
            _logger.error("unable to get, metric:{}", metric, ex);
        }
        return null;
    }
}
