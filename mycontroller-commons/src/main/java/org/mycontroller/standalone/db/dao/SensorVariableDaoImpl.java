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
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mycontroller.standalone.MYCMessages.MESSAGE_TYPE_SET_REQ;
import org.mycontroller.standalone.db.DbException;
import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.metrics.MetricsUtils;

import com.j256.ormlite.dao.Dao.CreateOrUpdateStatus;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.support.ConnectionSource;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
public class SensorVariableDaoImpl extends BaseAbstractDaoImpl<SensorVariable, Integer> implements SensorVariableDao {
    private static final Logger _logger = LoggerFactory.getLogger(SensorVariableDaoImpl.class);

    public SensorVariableDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, SensorVariable.class);
    }

    @Override
    public void create(SensorVariable sensorVariable) {
        try {
            this.nodeIdSensorIdnullCheck(sensorVariable);
            Integer count = this.getDao().create(sensorVariable);
            _logger.debug("Created SensorVariable:[{}], Create count:{}", sensorVariable, count);
        } catch (SQLException ex) {
            _logger.error("unable to add SensorVariable:[{}]", sensorVariable, ex);
        } catch (DbException dbEx) {
            _logger.error("unable to create, sensorValue:{}", sensorVariable, dbEx);
        }
    }

    @Override
    public void createOrUpdate(SensorVariable sensorVariable) {
        try {
            this.nodeIdSensorIdnullCheck(sensorVariable);
            CreateOrUpdateStatus status = this.getDao().createOrUpdate(sensorVariable);
            _logger.debug("CreateOrUpdate SensorVariable:[{}],Create:{},Update:{},Lines Changed:{}",
                    sensorVariable, status.isCreated(), status.isUpdated(),
                    status.getNumLinesChanged());
        } catch (SQLException ex) {
            _logger.error("CreateOrUpdate failed, SensorVariable:[{}]", sensorVariable, ex);
        } catch (DbException dbEx) {
            _logger.error("unable to createOrUpdate, sensorValue:{}", sensorVariable, dbEx);
        }
    }

    @Override
    public void delete(SensorVariable sensorVariable) {
        try {
            int deleteCount = this.getDao().delete(sensorVariable);
            _logger.debug("Deleted senosor:[{}], delete count:{}", sensorVariable, deleteCount);
        } catch (SQLException ex) {
            _logger.error("unable to delete, sensorValue:{}", sensorVariable, ex);
        }
    }

    @Override
    public void update(SensorVariable sensorVariable) {
        try {
            this.nodeIdSensorIdnullCheck(sensorVariable);
            UpdateBuilder<SensorVariable, Integer> updateBuilder = this.getDao().updateBuilder();

            updateBuilder.updateColumnValue(SensorVariable.KEY_UNIT, sensorVariable.getUnit());

            if (sensorVariable.getValue() != null) {
                updateBuilder.updateColumnValue(SensorVariable.KEY_VALUE, sensorVariable.getValue());
            }
            if (sensorVariable.getPreviousValue() != null) {
                updateBuilder.updateColumnValue(SensorVariable.KEY_PREVIOUS_VALUE, sensorVariable.getPreviousValue());
            }
            if (sensorVariable.getTimestamp() != null) {
                updateBuilder.updateColumnValue(SensorVariable.KEY_TIMESTAMP, sensorVariable.getTimestamp());
            }
            if (sensorVariable.getMetricType() != null) {
                updateBuilder.updateColumnValue(SensorVariable.KEY_METRIC, sensorVariable.getMetricType());
            }
            if (sensorVariable.getVariableType() != null) {
                updateBuilder.updateColumnValue(SensorVariable.KEY_VARIABLE_TYPE, sensorVariable.getVariableType());
            }

            if (sensorVariable.getId() != null) {
                updateBuilder.where().eq(SensorVariable.KEY_ID, sensorVariable.getId());
            } else {
                updateBuilder.where().eq(SensorVariable.KEY_SENSOR_DB_ID, sensorVariable.getSensor().getId()).and()
                        .eq(SensorVariable.KEY_VARIABLE_TYPE, sensorVariable.getVariableType());
            }
            int updateCount = updateBuilder.update();
            _logger.debug("Updated senosorValue:[{}], update count:{}", sensorVariable, updateCount);
        } catch (SQLException ex) {
            _logger.error("unable to get", ex);
        } catch (DbException dbEx) {
            _logger.error("unable to update, sensorValue:{}", sensorVariable, dbEx);
        }
    }

    @Override
    public List<SensorVariable> getAllBySensorId(Integer sensorRefId) {
        try {
            if (sensorRefId == null) {
                return new ArrayList<SensorVariable>();
            }
            return this.getDao().queryForEq(SensorVariable.KEY_SENSOR_DB_ID, sensorRefId);
        } catch (SQLException ex) {
            _logger.error("unable to get all list with sensorRefId:{}", sensorRefId, ex);
            return null;
        }
    }

    @Override
    public List<SensorVariable> getAllBySensorIds(List<Integer> sensorRefIds) {
        try {
            if (sensorRefIds == null) {
                return new ArrayList<SensorVariable>();
            }
            return this.getDao().queryBuilder().selectColumns(SensorVariable.KEY_ID, SensorVariable.KEY_SENSOR_DB_ID)
                    .where().in(SensorVariable.KEY_SENSOR_DB_ID, sensorRefIds).query();
        } catch (SQLException ex) {
            _logger.error("unable to get all list with sensorRefIds:{}", sensorRefIds, ex);
            return null;
        }
    }

    @Override
    public List<SensorVariable> getByVariableType(MESSAGE_TYPE_SET_REQ variableType) {
        try {
            if (variableType == null) {
                return null;
            }
            return this.getDao().queryForEq(SensorVariable.KEY_VARIABLE_TYPE, variableType);
        } catch (SQLException ex) {
            _logger.error("unable to get all list with variableType: {}", variableType, ex);
            return null;
        }
    }

    @Override
    public List<SensorVariable> getAllDoubleMetric(Integer sensorRefId) {
        try {
            if (sensorRefId == null) {
                return null;
            }
            QueryBuilder<SensorVariable, Integer> queryBuilder = this.getDao().queryBuilder();
            queryBuilder.where().eq(SensorVariable.KEY_SENSOR_DB_ID, sensorRefId).and()
                    .eq(SensorVariable.KEY_METRIC, MetricsUtils.METRIC_TYPE.DOUBLE);
            return queryBuilder.query();
        } catch (SQLException ex) {
            _logger.error("unable to get all list with sensorRefId:{}, MetricType:{}", sensorRefId,
                    MetricsUtils.METRIC_TYPE.DOUBLE, ex);
            return null;
        }
    }

    @Override
    public List<SensorVariable> getAll() {
        try {
            return this.getDao().queryForAll();
        } catch (SQLException ex) {
            _logger.error("unable to get all list", ex);
            return null;
        }
    }

    @Override
    public SensorVariable get(Integer sensorRefId, MESSAGE_TYPE_SET_REQ messageVariableType) {
        try {
            nodeIdSensorIdnullCheck(sensorRefId, messageVariableType);
            return this.getDao().queryForFirst(
                    this.getDao().queryBuilder()
                            .where().eq(SensorVariable.KEY_SENSOR_DB_ID, sensorRefId)
                            .and().eq(SensorVariable.KEY_VARIABLE_TYPE, messageVariableType).prepare());
        } catch (SQLException ex) {
            _logger.error("unable to get", ex);
        } catch (DbException dbEx) {
            _logger.error("unable to get, nodeId:{},sensorId:{}", sensorRefId, messageVariableType, dbEx);
        }
        return null;
    }

    @Override
    public SensorVariable get(SensorVariable sensorVariable) {
        try {
            if (sensorVariable.getId() != null) {
                return this.getDao().queryForId(sensorVariable.getId());
            } else {
                return this.get(sensorVariable.getSensor().getId(), sensorVariable.getVariableType());
            }
        } catch (SQLException ex) {
            _logger.error("unable to get", ex);
            return null;
        }
    }

    @Override
    public SensorVariable get(int id) {
        try {
            return this.getDao().queryForId(id);
        } catch (SQLException ex) {
            _logger.error("unable to get", ex);
            return null;
        }
    }

    private void nodeIdSensorIdnullCheck(Integer sensorRefId, MESSAGE_TYPE_SET_REQ messageVariableType)
            throws DbException {
        if (sensorRefId != null && messageVariableType != null) {
            return;
        } else {
            throw new DbException("SensorId or NodeId should not be a NULL, sensorRefId:" + sensorRefId
                    + ",messageVariableTypeId:" + messageVariableType);
        }
    }

    private void nodeIdSensorIdnullCheck(SensorVariable sensorVariable) throws DbException {
        if (sensorVariable != null && sensorVariable.getSensor() != null && sensorVariable.getVariableType() != null) {
            return;
        } else {
            throw new DbException("SensorVariable or Sensor or VariableType should not be a NULL, SensorVariable:"
                    + sensorVariable);
        }
    }

    @Override
    public List<Integer> getSensorVariableIds(Integer sensorRefId) {
        List<SensorVariable> variables = this.getAllBySensorId(sensorRefId);
        List<Integer> ids = new ArrayList<Integer>();
        //TODO: should modify by query (RAW query)
        for (SensorVariable sensorVariable : variables) {
            ids.add(sensorVariable.getId());
        }
        return ids;
    }

    @Override
    public List<SensorVariable> getAll(List<Integer> ids) {
        return super.getAll(SensorVariable.KEY_ID, ids);
    }

}