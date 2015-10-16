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

import org.mycontroller.standalone.db.tables.MetricsOnOffTypeDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.j256.ormlite.dao.Dao.CreateOrUpdateStatus;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.support.ConnectionSource;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class MetricsOnOffTypeDeviceDaoImpl extends BaseAbstractDao<MetricsOnOffTypeDevice, Object> implements
        MetricsOnOffTypeDeviceDao {
    private static final Logger _logger = LoggerFactory.getLogger(MetricsOnOffTypeDeviceDaoImpl.class);

    public MetricsOnOffTypeDeviceDaoImpl(ConnectionSource connectionSource)
            throws SQLException {
        super(connectionSource, MetricsOnOffTypeDevice.class);
    }

    @Override
    public void create(MetricsOnOffTypeDevice metric) {
        try {
            int count = this.getDao().create(metric);
            _logger.debug("Created Metric:[{}], Create count:{}", metric, count);

        } catch (SQLException ex) {
            _logger.error("unable to add Metric:[{}]", metric, ex);
        }
    }

    @Override
    public void createOrUpdate(MetricsOnOffTypeDevice metric) {
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
    public void delete(MetricsOnOffTypeDevice metric) {
        try {
            int count = this.getDao().delete(metric);
            _logger.debug("Metric:[{}] deleted, Delete count:{}", metric, count);
        } catch (SQLException ex) {
            _logger.error("unable to delete metric:[{}]", metric, ex);
        }
    }

    @Override
    public void deleteBySensorRefId(int sensorValueRefId) {
        try {
            DeleteBuilder<MetricsOnOffTypeDevice, Object> deleteBuilder = this.getDao().deleteBuilder();
            deleteBuilder.where().lt(MetricsOnOffTypeDevice.SENSOR_VALUE_REF_ID, sensorValueRefId);
            int count = this.getDao().delete(deleteBuilder.prepare());
            _logger.debug("Metric-sensorValueRefId:[{}] deleted, Delete count:{}", sensorValueRefId, count);
        } catch (SQLException ex) {
            _logger.error("unable to delete metric-sensorValueRefId:[{}]", sensorValueRefId, ex);
        }
    }

    @Override
    public void deletePrevious(MetricsOnOffTypeDevice metric) {
        try {
            DeleteBuilder<MetricsOnOffTypeDevice, Object> deleteBuilder = this.getDao().deleteBuilder();
            deleteBuilder.where().lt(MetricsOnOffTypeDevice.TIMESTAMP, metric.getTimestamp());
            int count = this.getDao().delete(deleteBuilder.prepare());
            _logger.debug("Metric:[{}] deleted, Delete count:{}", metric, count);
        } catch (SQLException ex) {
            _logger.error("unable to delete metric:[{}]", metric, ex);
        }
    }

    @Override
    public void update(MetricsOnOffTypeDevice metric) {
        try {
            int count = this.getDao().update(metric);
            _logger.debug("Metric:[{}] updated, Update count:{}", metric, count);
        } catch (SQLException ex) {
            _logger.error("unable to update metric:[{}]", metric, ex);
        }
    }

    @Override
    public List<MetricsOnOffTypeDevice> getAll(MetricsOnOffTypeDevice metric) {
        try {
            return this.getDao().query(
                    this.getDao()
                            .queryBuilder()
                            .where().eq(MetricsOnOffTypeDevice.SENSOR_VALUE_REF_ID,
                                    metric.getSensorValue().getId())
                            .and().between(MetricsOnOffTypeDevice.TIMESTAMP,
                                    metric.getTimestampFrom(), metric.getTimestampTo())
                            .prepare());
        } catch (SQLException ex) {
            _logger.error("unable to get, metric:{}", metric, ex);
        }
        return null;
    }

    @Override
    public List<MetricsOnOffTypeDevice> getAllAfter(MetricsOnOffTypeDevice metric) {
        try {
            return this.getDao().query(
                    this.getDao()
                            .queryBuilder()
                            .where().eq(MetricsOnOffTypeDevice.SENSOR_VALUE_REF_ID, metric.getSensorValue().getId())
                            .and().ge(MetricsOnOffTypeDevice.TIMESTAMP, metric.getTimestamp())
                            .prepare());
        } catch (SQLException ex) {
            _logger.error("unable to get, metric:{}", metric, ex);
        }
        return null;
    }

    @Override
    public MetricsOnOffTypeDevice get(MetricsOnOffTypeDevice metric) {
        try {
            return this.getDao().queryForFirst(
                    this.getDao().queryBuilder()
                            .where().eq(MetricsOnOffTypeDevice.SENSOR_VALUE_REF_ID, metric.getSensorValue().getId())
                            .and().eq(MetricsOnOffTypeDevice.TIMESTAMP, metric.getTimestamp()).prepare());
        } catch (SQLException ex) {
            _logger.error("unable to get, metric:{}", metric, ex);
        }
        return null;
    }
}
