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

import org.mycontroller.standalone.db.tables.MetricsDoubleTypeDevice;
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
                    .and().lt(MetricsDoubleTypeDevice.KEY_TIMESTAMP, metric.getTimestamp());

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
                    metric.getSensorVariable().getId())
                    .and().eq(MetricsDoubleTypeDevice.KEY_AGGREGATION_TYPE,
                            metric.getAggregationType());
            if (metric.getTimestampFrom() != null) {
                whereBuilder.and().ge(MetricsDoubleTypeDevice.KEY_TIMESTAMP,
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
}
