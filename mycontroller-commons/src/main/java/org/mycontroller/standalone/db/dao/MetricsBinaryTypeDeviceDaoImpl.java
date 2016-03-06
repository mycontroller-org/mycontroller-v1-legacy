/*
 * Copyright 2015-2016 Jeeva Kandasamy (jkandasa@gmail.com)
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

import org.mycontroller.standalone.db.tables.MetricsBinaryTypeDevice;
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
public class MetricsBinaryTypeDeviceDaoImpl extends BaseAbstractDaoImpl<MetricsBinaryTypeDevice, Object> implements
        MetricsBinaryTypeDeviceDao {
    private static final Logger _logger = LoggerFactory.getLogger(MetricsBinaryTypeDeviceDaoImpl.class);

    public MetricsBinaryTypeDeviceDaoImpl(ConnectionSource connectionSource)
            throws SQLException {
        super(connectionSource, MetricsBinaryTypeDevice.class);
    }

    @Override
    public void deleteBySensorValueRefId(int sensorValueRefId) {
        try {
            DeleteBuilder<MetricsBinaryTypeDevice, Object> deleteBuilder = this.getDao().deleteBuilder();
            deleteBuilder.where().eq(MetricsBinaryTypeDevice.KEY_SENSOR_VARIABLE_ID, sensorValueRefId);
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
            deleteBuilder.where().lt(MetricsBinaryTypeDevice.KEY_TIMESTAMP, metric.getTimestamp());
            int count = this.getDao().delete(deleteBuilder.prepare());
            _logger.debug("Metric:[{}] deleted, Delete count:{}", metric, count);
        } catch (SQLException ex) {
            _logger.error("unable to delete metric:[{}]", metric, ex);
        }
    }

    @Override
    public List<MetricsBinaryTypeDevice> getAll(MetricsBinaryTypeDevice metric) {
        try {
            QueryBuilder<MetricsBinaryTypeDevice, Object> queryBuilder = this.getDao().queryBuilder();
            Where<MetricsBinaryTypeDevice, Object> where = queryBuilder.where();
            where.eq(MetricsBinaryTypeDevice.KEY_SENSOR_VARIABLE_ID, metric.getSensorVariable().getId());
            if (metric.getTimestampFrom() != null) {
                where.and().ge(MetricsBinaryTypeDevice.KEY_TIMESTAMP, metric.getTimestampFrom());
            }
            if (metric.getTimestampTo() != null) {
                where.and().le(MetricsBinaryTypeDevice.KEY_TIMESTAMP, metric.getTimestampTo());
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
                            .where()
                            .eq(MetricsBinaryTypeDevice.KEY_SENSOR_VARIABLE_ID, metric.getSensorVariable().getId())
                            .and().eq(MetricsBinaryTypeDevice.KEY_TIMESTAMP, metric.getTimestamp()).prepare());
        } catch (SQLException ex) {
            _logger.error("unable to get, metric:{}", metric, ex);
        }
        return null;
    }

    @Override
    public List<MetricsBinaryTypeDevice> getAll(List<Object> ids) {
        return null;
    }
}
