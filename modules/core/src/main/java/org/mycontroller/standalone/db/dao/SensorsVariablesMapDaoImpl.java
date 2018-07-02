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

import org.mycontroller.standalone.db.DbException;
import org.mycontroller.standalone.db.tables.SensorsVariablesMap;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_PRESENTATION;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_SET_REQ;

import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.support.ConnectionSource;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
@Slf4j
public class SensorsVariablesMapDaoImpl extends BaseAbstractDaoImpl<SensorsVariablesMap, Integer> implements
        SensorsVariablesMapDao {

    public SensorsVariablesMapDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, SensorsVariablesMap.class);
    }

    @Override
    public void create(SensorsVariablesMap sensorsVariablesMap) {
        try {
            Integer count = this.getDao().create(sensorsVariablesMap);
            _logger.debug("Created SensorsVariablesMap:[{}], Create count:{}", sensorsVariablesMap, count);
        } catch (SQLException ex) {
            _logger.error("unable to add SensorsVariablesMap:[{}]", sensorsVariablesMap, ex);
        }
    }

    @Override
    public void create(MESSAGE_TYPE_PRESENTATION sensorType, MESSAGE_TYPE_SET_REQ variableType) {
        this.create(SensorsVariablesMap.builder().sensorType(sensorType).variableType(variableType).build());
    }

    @Override
    public void delete(SensorsVariablesMap sensorsVariablesMap) {
        try {
            int deleteCount = this.getDao().delete(sensorsVariablesMap);
            _logger.debug("Deleted sensorsVariablesType:[{}], delete count:{}", sensorsVariablesMap, deleteCount);
        } catch (SQLException ex) {
            _logger.error("unable to delete, sensorsVariablesMap:{}", sensorsVariablesMap, ex);
        }
    }

    @Override
    public void delete(MESSAGE_TYPE_PRESENTATION sensorType) {
        try {
            DeleteBuilder<SensorsVariablesMap, Integer> deleteBuilder = this.getDao().deleteBuilder();
            deleteBuilder.where().eq(SensorsVariablesMap.KEY_SENSOR_TYPE, sensorType);
            int deleteCount = deleteBuilder.delete();
            _logger.debug("Deleted sensorType:[{}], delete count:{}", sensorType, deleteCount);
        } catch (SQLException ex) {
            _logger.error("unable to delete, sensorType:{}", sensorType, ex);
        }
    }

    @Override
    public List<SensorsVariablesMap> getAll(MESSAGE_TYPE_PRESENTATION sensorType) {
        try {
            if (sensorType == null) {
                return null;
            }
            return this.getDao().queryForEq(SensorsVariablesMap.KEY_SENSOR_TYPE, sensorType);
        } catch (SQLException ex) {
            _logger.error("unable to get all list wit sensorType:{}", sensorType, ex);
            return null;
        }
    }

    @Override
    public List<SensorsVariablesMap> getAll() {
        try {
            return this.getDao().queryForAll();
        } catch (SQLException ex) {
            _logger.error("unable to get all list", ex);
            return null;
        }
    }

    @Override
    public SensorsVariablesMap get(MESSAGE_TYPE_PRESENTATION sensorType, MESSAGE_TYPE_SET_REQ variableType) {
        try {
            nodeIdSensorIdnullCheck(sensorType, variableType);
            return this.getDao().queryForFirst(
                    this.getDao().queryBuilder()
                            .where().eq(SensorsVariablesMap.KEY_SENSOR_TYPE, sensorType)
                            .and().eq(SensorsVariablesMap.KEY_VARIABLE_TYPE, variableType).prepare());
        } catch (SQLException ex) {
            _logger.error("unable to get", ex);
        } catch (DbException dbEx) {
            _logger.error("unable to get, sensorType:{},variableType:{}", sensorType, variableType, dbEx);
        }
        return null;
    }

    @Override
    public SensorsVariablesMap get(SensorsVariablesMap sensorsVariablesMap) {
        try {
            if (sensorsVariablesMap.getId() != null) {
                return this.getDao().queryForId(sensorsVariablesMap.getId());
            } else {
                return this.get(sensorsVariablesMap.getSensorType(), sensorsVariablesMap.getVariableType());
            }
        } catch (SQLException ex) {
            _logger.error("unable to get", ex);
            return null;
        }
    }

    private void nodeIdSensorIdnullCheck(MESSAGE_TYPE_PRESENTATION sensorType, MESSAGE_TYPE_SET_REQ variableType)
            throws DbException {
        if (sensorType != null && variableType != null) {
            return;
        } else {
            throw new DbException("sensorType or NodeId should not be a NULL, sensorType:"
                    + sensorType + ",variableType:" + variableType);
        }
    }
}