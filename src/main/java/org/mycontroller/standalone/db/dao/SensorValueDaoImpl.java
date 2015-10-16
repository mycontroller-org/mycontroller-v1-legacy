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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mycontroller.standalone.db.DbException;
import org.mycontroller.standalone.db.TypeUtils;
import org.mycontroller.standalone.db.tables.SensorValue;

import com.j256.ormlite.dao.Dao.CreateOrUpdateStatus;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.support.ConnectionSource;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
public class SensorValueDaoImpl extends BaseAbstractDao<SensorValue, Integer> implements SensorValueDao {
    private static final Logger _logger = LoggerFactory.getLogger(SensorValueDaoImpl.class);

    public SensorValueDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, SensorValue.class);
    }

    @Override
    public void create(SensorValue sensorValue) {
        try {
            this.nodeIdSensorIdnullCheck(sensorValue);
            Integer count = this.getDao().create(sensorValue);
            _logger.debug("Created SensorValue:[{}], Create count:{}", sensorValue, count);
        } catch (SQLException ex) {
            _logger.error("unable to add SensorValue:[{}]", sensorValue, ex);
        } catch (DbException dbEx) {
            _logger.error("unable to create, sensorValue:{}", sensorValue, dbEx);
        }
    }

    @Override
    public void createOrUpdate(SensorValue sensorValue) {
        try {
            this.nodeIdSensorIdnullCheck(sensorValue);
            CreateOrUpdateStatus status = this.getDao().createOrUpdate(sensorValue);
            _logger.debug("CreateOrUpdate SensorValue:[{}],Create:{},Update:{},Lines Changed:{}",
                    sensorValue, status.isCreated(), status.isUpdated(),
                    status.getNumLinesChanged());
        } catch (SQLException ex) {
            _logger.error("CreateOrUpdate failed, SensorValue:[{}]", sensorValue, ex);
        } catch (DbException dbEx) {
            _logger.error("unable to createOrUpdate, sensorValue:{}", sensorValue, dbEx);
        }
    }

    @Override
    public void delete(SensorValue sensorValue) {
        try {
            int deleteCount = this.getDao().delete(sensorValue);
            _logger.debug("Deleted senosor:[{}], delete count:{}", sensorValue, deleteCount);
        } catch (SQLException ex) {
            _logger.error("unable to delete, sensorValue:{}", sensorValue, ex);
        }
    }

    @Override
    public void update(SensorValue sensorValue) {
        try {
            this.nodeIdSensorIdnullCheck(sensorValue);
            UpdateBuilder<SensorValue, Integer> updateBuilder = this.getDao().updateBuilder();

            updateBuilder.updateColumnValue(SensorValue.UNIT, sensorValue.getUnit());

            if (sensorValue.getLastValue() != null) {
                updateBuilder.updateColumnValue(SensorValue.LAST_VALUE, sensorValue.getLastValue());
            }
            if (sensorValue.getTimestamp() != null) {
                updateBuilder.updateColumnValue(SensorValue.TIMESTAMP, sensorValue.getTimestamp());
            }
            if (sensorValue.getMetricType() != null) {
                updateBuilder.updateColumnValue(SensorValue.METRIC, sensorValue.getMetricType());
            }
            if (sensorValue.getVariableType() != null) {
                updateBuilder.updateColumnValue(SensorValue.VARIABLE_TYPE, sensorValue.getVariableType());
            }

            if (sensorValue.getId() != null) {
                updateBuilder.where().eq(SensorValue.ID, sensorValue.getId());
            } else {
                updateBuilder.where().eq(SensorValue.SENSOR_REF_ID, sensorValue.getSensor().getId()).and()
                        .eq(SensorValue.VARIABLE_TYPE, sensorValue.getVariableType());
            }
            int updateCount = updateBuilder.update();
            _logger.debug("Updated senosorValue:[{}], update count:{}", sensorValue, updateCount);
        } catch (SQLException ex) {
            _logger.error("unable to get", ex);
        } catch (DbException dbEx) {
            _logger.error("unable to update, sensorValue:{}", sensorValue, dbEx);
        }
    }

    @Override
    public List<SensorValue> getAll(Integer sensorRefId) {
        try {
            if (sensorRefId == null) {
                return null;
            }
            return this.getDao().queryForEq(SensorValue.SENSOR_REF_ID, sensorRefId);
        } catch (SQLException ex) {
            _logger.error("unable to get all list wit sensorRefId:{}", sensorRefId, ex);
            return null;
        }
    }

    @Override
    public List<SensorValue> getByVariableType(Integer variableType) {
        try {
            if (variableType == null) {
                return null;
            }
            return this.getDao().queryForEq(SensorValue.VARIABLE_TYPE, variableType);
        } catch (SQLException ex) {
            _logger.error("unable to get all list with variableType: {}", variableType, ex);
            return null;
        }
    }

    @Override
    public List<SensorValue> getAllDoubleMetric(Integer sensorRefId) {
        try {
            if (sensorRefId == null) {
                return null;
            }
            QueryBuilder<SensorValue, Integer> queryBuilder = this.getDao().queryBuilder();
            queryBuilder.where().eq(SensorValue.SENSOR_REF_ID, sensorRefId).and()
                    .eq(SensorValue.METRIC, TypeUtils.METRIC_TYPE.DOUBLE.ordinal());
            return queryBuilder.query();
        } catch (SQLException ex) {
            _logger.error("unable to get all list with sensorRefId:{}, MetricType:{}", sensorRefId,
                    TypeUtils.METRIC_TYPE.DOUBLE, ex);
            return null;
        }
    }

    @Override
    public List<SensorValue> getAll() {
        try {
            return this.getDao().queryForAll();
        } catch (SQLException ex) {
            _logger.error("unable to get all list", ex);
            return null;
        }
    }

    @Override
    public SensorValue get(Integer sensorRefId, Integer messageVariableTypeId) {
        try {
            nodeIdSensorIdnullCheck(sensorRefId, messageVariableTypeId);
            return this.getDao().queryForFirst(
                    this.getDao().queryBuilder()
                            .where().eq(SensorValue.SENSOR_REF_ID, sensorRefId)
                            .and().eq(SensorValue.VARIABLE_TYPE, messageVariableTypeId).prepare());
        } catch (SQLException ex) {
            _logger.error("unable to get", ex);
        } catch (DbException dbEx) {
            _logger.error("unable to get, nodeId:{},sensorId:{}", sensorRefId, messageVariableTypeId, dbEx);
        }
        return null;
    }

    @Override
    public SensorValue get(SensorValue sensorValue) {
        try {
            if (sensorValue.getId() != null) {
                return this.getDao().queryForId(sensorValue.getId());
            } else {
                return this.get(sensorValue.getSensor().getId(), sensorValue.getVariableType());
            }
        } catch (SQLException ex) {
            _logger.error("unable to get", ex);
            return null;
        }
    }

    private void nodeIdSensorIdnullCheck(Integer sensorRefId, Integer messageVariableTypeId) throws DbException {
        if (sensorRefId != null && messageVariableTypeId != null) {
            return;
        } else {
            throw new DbException("SensorId or NodeId should not be a NULL, sensorRefId:" + sensorRefId
                    + ",messageVariableTypeId:"
                    + messageVariableTypeId);
        }
    }

    private void nodeIdSensorIdnullCheck(SensorValue sensorValue) throws DbException {
        if (sensorValue != null && sensorValue.getSensor() != null && sensorValue.getVariableType() != null) {
            return;
        } else {
            throw new DbException("SensorValue or Sensor or VariableType should not be a NULL, SensorValue:"
                    + sensorValue);
        }
    }
}