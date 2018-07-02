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

import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.api.jaxrs.model.AllowedResources;
import org.mycontroller.standalone.api.jaxrs.model.Query;
import org.mycontroller.standalone.api.jaxrs.model.QueryResponse;
import org.mycontroller.standalone.auth.AuthUtils;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.DbException;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_SET_REQ;
import org.mycontroller.standalone.metrics.MetricsUtils;

import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
@Slf4j
public class SensorVariableDaoImpl extends BaseAbstractDaoImpl<SensorVariable, Integer> implements SensorVariableDao {

    public SensorVariableDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, SensorVariable.class);
    }

    @Override
    public void create(SensorVariable sensorVariable) {
        try {
            this.nodeIdSensorIdnullCheck(sensorVariable);
            super.create(sensorVariable);
        } catch (DbException dbEx) {
            _logger.error("unable to createOrUpdate, sensorValue:{}", sensorVariable, dbEx);
        }
    }

    @Override
    public void createOrUpdate(SensorVariable sensorVariable) {
        try {
            this.nodeIdSensorIdnullCheck(sensorVariable);
            super.createOrUpdate(sensorVariable);
        } catch (DbException dbEx) {
            _logger.error("unable to createOrUpdate, sensorValue:{}", sensorVariable, dbEx);
        }
    }

    @Override
    public void update(SensorVariable sensorVariable) {
        try {
            this.nodeIdSensorIdnullCheck(sensorVariable);
            UpdateBuilder<SensorVariable, Integer> updateBuilder = this.getDao().updateBuilder();

            updateBuilder.updateColumnValue(SensorVariable.KEY_UNIT_TYPE, sensorVariable.getUnitType());

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
            if (sensorVariable.getReadOnly() != null) {
                updateBuilder.updateColumnValue(SensorVariable.KEY_READ_ONLY, sensorVariable.getReadOnly());
            }
            if (sensorVariable.getOffset() != null) {
                updateBuilder.updateColumnValue(SensorVariable.KEY_OFFSET, sensorVariable.getOffset());
            }
            if (sensorVariable.getPriority() != null) {
                updateBuilder.updateColumnValue(SensorVariable.KEY_PRIORITY, sensorVariable.getPriority());
            }
            if (sensorVariable.getProperties() != null) {
                updateBuilder.updateColumnValue(SensorVariable.KEY_PROPERTIES, sensorVariable.getProperties());
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
    public List<Integer> getSensorVariableIds(Integer sId) {
        List<SensorVariable> variables = this.getAllBySensorId(sId);
        List<Integer> ids = new ArrayList<Integer>();
        //TODO: should modify by query (RAW query)
        for (SensorVariable sensorVariable : variables) {
            ids.add(sensorVariable.getId());
        }
        return ids;
    }

    @Override
    public List<SensorVariable> getAll(List<Integer> ids) {
        try {
            if (ids != null && !ids.isEmpty()) {
                QueryBuilder<SensorVariable, Integer> queryBuilder = this.getDao().queryBuilder();
                Where<SensorVariable, Integer> where = queryBuilder.where();
                where.in(SensorVariable.KEY_ID, ids);
                queryBuilder.setWhere(where);
                queryBuilder.orderBy(SensorVariable.KEY_PRIORITY, true);
                return queryBuilder.query();
            }
            return new ArrayList<SensorVariable>();
        } catch (SQLException ex) {
            _logger.error("unable to get all items ids:{}", ids, ex);
            return null;
        }
    }

    public List<SensorVariable> getAll(Query query, String filter, AllowedResources allowedResources) {
        AuthUtils.updateQueryFilter(query.getFilters(), RESOURCE_TYPE.SENSOR_VARIABLE, allowedResources);
        if (query.getFilters().get(SensorVariable.KEY_SENSOR_DB_ID) == null) {
            query.setAndQuery(false);
            if (filter != null) {
                List<Sensor> sensors = DaoUtils.getSensorDao().getAll(query, filter, null);
                if (sensors.size() > 0) {
                    ArrayList<Integer> sensorIds = new ArrayList<Integer>();
                    for (Sensor sensor : sensors) {
                        sensorIds.add(sensor.getId());
                    }
                    query.getFilters().put(SensorVariable.KEY_SENSOR_DB_ID, sensors);
                }
                MESSAGE_TYPE_SET_REQ type = MESSAGE_TYPE_SET_REQ.fromString(filter);
                if (type != null) {
                    query.getFilters().put(SensorVariable.KEY_VARIABLE_TYPE, type);
                }
            }
        }
        //Remove keys
        query.getFilters().remove(Sensor.KEY_NODE_ID);

        query.setIdColumn(SensorVariable.KEY_ID);
        query.setOrderBy(SensorVariable.KEY_TIMESTAMP);
        query.setOrder(Query.ORDER_DESC);
        return super.getAllData(query);
    }

    @Override
    public QueryResponse getAll(Query query) {
        try {
            return super.getQueryResponse(query);
        } catch (SQLException ex) {
            _logger.error("Error while processing for {}", query, ex);
            return null;
        }
    }

}