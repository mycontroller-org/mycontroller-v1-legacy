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
import java.util.ArrayList;
import java.util.List;

import org.mycontroller.standalone.db.tables.MetricsBinaryTypeDevice;
import org.mycontroller.standalone.db.tables.SensorVariable;

import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Slf4j
public class MetricsBinaryTypeDeviceDaoImpl extends BaseAbstractDaoImpl<MetricsBinaryTypeDevice, Object> implements
        MetricsBinaryTypeDeviceDao {

    public MetricsBinaryTypeDeviceDaoImpl(ConnectionSource connectionSource)
            throws SQLException {
        super(connectionSource, MetricsBinaryTypeDevice.class);
    }

    @Override
    public void deleteBySensorVariableRefId(int sensorVarRefId) {
        try {
            DeleteBuilder<MetricsBinaryTypeDevice, Object> deleteBuilder = this.getDao().deleteBuilder();
            deleteBuilder.where().eq(MetricsBinaryTypeDevice.KEY_SENSOR_VARIABLE_ID, sensorVarRefId);
            int count = deleteBuilder.delete();
            _logger.debug("Metric-sensorVariableRefId:[{}] deleted, Delete count:{}", sensorVarRefId, count);
        } catch (SQLException ex) {
            _logger.error("unable to delete metric-sensorVariableRefId:[{}]", sensorVarRefId, ex);
        }
    }

    @Override
    public void deletePrevious(MetricsBinaryTypeDevice metric) {

        try {
            DeleteBuilder<MetricsBinaryTypeDevice, Object> deleteBuilder = this.getDao().deleteBuilder();
            Where<MetricsBinaryTypeDevice, Object> where = deleteBuilder.where();
            int whereCount = 0;
            if (metric.getSensorVariable() != null && metric.getSensorVariable().getId() != null) {
                where.eq(MetricsBinaryTypeDevice.KEY_SENSOR_VARIABLE_ID, metric.getSensorVariable().getId());
                whereCount++;
            } else {
                _logger.warn("Sensor variable id is not supplied!"
                        + " Cannot perform purge operation without sensor variable id.");
                return;
            }
            if (metric.getTimestamp() != null) {
                where.le(MetricsBinaryTypeDevice.KEY_TIMESTAMP, metric.getTimestamp());
                whereCount++;
            }
            if (metric.getStart() != null) {
                where.ge(MetricsBinaryTypeDevice.KEY_TIMESTAMP, metric.getStart());
                whereCount++;
            }
            if (metric.getEnd() != null) {
                where.le(MetricsBinaryTypeDevice.KEY_TIMESTAMP, metric.getEnd());
                whereCount++;
            }
            if (metric.getState() != null) {
                where.eq(MetricsBinaryTypeDevice.KEY_STATE, metric.getState());
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
    public List<MetricsBinaryTypeDevice> getAll(MetricsBinaryTypeDevice metric) {
        try {
            QueryBuilder<MetricsBinaryTypeDevice, Object> queryBuilder = this.getDao().queryBuilder();
            Where<MetricsBinaryTypeDevice, Object> where = queryBuilder.where();
            where.eq(MetricsBinaryTypeDevice.KEY_SENSOR_VARIABLE_ID, metric.getSensorVariable().getId());
            if (metric.getStart() != null) {
                where.and().ge(MetricsBinaryTypeDevice.KEY_TIMESTAMP, metric.getStart());
            }
            if (metric.getEnd() != null) {
                where.and().le(MetricsBinaryTypeDevice.KEY_TIMESTAMP, metric.getEnd());
            }
            queryBuilder.orderBy(MetricsBinaryTypeDevice.KEY_TIMESTAMP, true);
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

    @Override
    public List<MetricsBinaryTypeDevice> getAllLastN(SensorVariable sensorVariable, long lastN) {
        try {
            QueryBuilder<MetricsBinaryTypeDevice, Object> _qb = getDao().queryBuilder();
            _qb.where().eq(MetricsBinaryTypeDevice.KEY_SENSOR_VARIABLE_ID, sensorVariable.getId());
            _qb.orderBy(MetricsBinaryTypeDevice.KEY_TIMESTAMP, false);
            _qb.limit(lastN);
            return _qb.query();
        } catch (SQLException ex) {
            _logger.error("SQL Exception", ex);
        }
        return new ArrayList<MetricsBinaryTypeDevice>();
    }

    @Override
    public void updateTimestamp(int sensorVariableId, long timestampOld, long timestanpNew) {
        try {
            UpdateBuilder<MetricsBinaryTypeDevice, Object> _ub = getDao().updateBuilder();
            _ub.where().eq(MetricsBinaryTypeDevice.KEY_SENSOR_VARIABLE_ID, sensorVariableId).and()
                    .eq(MetricsBinaryTypeDevice.KEY_TIMESTAMP, timestampOld);
            _ub.updateColumnValue(MetricsBinaryTypeDevice.KEY_TIMESTAMP, timestanpNew);
            int updateCount = _ub.update();
            _logger.debug("Update count:{}", updateCount);
        } catch (SQLException ex) {
            _logger.error("SQL Exception", ex);
        }
    }
}
