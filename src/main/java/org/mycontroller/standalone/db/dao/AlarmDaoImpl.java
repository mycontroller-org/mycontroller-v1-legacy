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

import org.mycontroller.standalone.db.tables.Alarm;

import com.j256.ormlite.dao.Dao.CreateOrUpdateStatus;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class AlarmDaoImpl extends BaseAbstractDao<Alarm, Integer> implements AlarmDao {
    public AlarmDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, Alarm.class);
    }

    @Override
    public void create(Alarm alarm) {
        try {
            Integer count = this.getDao().create(alarm);
            _logger.debug("Created Alarm:[{}], Create count:{}", alarm, count);
        } catch (SQLException ex) {
            _logger.error("unable to add Alarm:[{}]", alarm, ex);
        }
    }

    @Override
    public void createOrUpdate(Alarm alarm) {
        try {
            CreateOrUpdateStatus status = this.getDao().createOrUpdate(alarm);
            _logger.debug("CreateOrUpdate Alarm:[{}],Create:{},Update:{},Lines Changed:{}",
                    alarm, status.isCreated(), status.isUpdated(),
                    status.getNumLinesChanged());
        } catch (SQLException ex) {
            _logger.error("unable to CreateOrUpdate Alarm:[{}]", alarm, ex);
        }
    }

    @Override
    public void delete(Alarm alarm) {
        try {
            Integer count = this.getDao().delete(alarm);
            _logger.debug("Alarm:[{}] deleted, Delete count:{}", alarm, count);
        } catch (SQLException ex) {
            _logger.error("unable to delete alarm:[{}]", alarm, ex);
        }
    }

    @Override
    public void delete(Integer id) {
        try {
            this.getDao().deleteById(id);
        } catch (SQLException ex) {
            _logger.error("unable to delete alarm:id:[{}]", id, ex);
        }
    }

    @Override
    public void deleteBySensorRefId(Integer sensorRefId) {
        try {
            DeleteBuilder<Alarm, Integer> deleteBuilder = this.getDao().deleteBuilder();
            deleteBuilder.where().eq(Alarm.SENSOR_REF_ID, sensorRefId);
            Integer count = deleteBuilder.delete();
            _logger.debug("deleted alarms with sensorRefId:[{}], Deletion Count:{}", sensorRefId, count);
        } catch (SQLException ex) {
            _logger.error("unable to delete alarms with sensorRefId:[{}]", sensorRefId, ex);
        }
    }

    @Override
    public void update(Alarm alarm) {
        try {
            Integer count = this.getDao().update(alarm);
            _logger.debug("Updated Alarm:[{}], Update count:{}", alarm, count);
        } catch (SQLException ex) {
            _logger.error("unable to update alarm:[{}]", alarm, ex);
        }

    }

    @Override
    public List<Alarm> getAll() {
        try {
            return this.getDao().queryForAll();
        } catch (SQLException ex) {
            _logger.error("unable to get all Nodes", ex);
            return null;
        }
    }

    @Override
    public List<Alarm> getAll(Integer sensorRefId) {
        return getAll(sensorRefId, null);
    }

    @Override
    public List<Alarm> getAll(Integer sensorRefId, Boolean enabled) {
        return this.getAll(sensorRefId, null, enabled);
    }

    @Override
    public List<Alarm> getAllEnabled(Integer sensorRefId, Integer variableType) {
        return this.getAll(sensorRefId, variableType, true);
    }

    @Override
    public List<Alarm> getAll(Integer sensorRefId, Integer variableType, Boolean enabled) {
        try {
            QueryBuilder<Alarm, Integer> queryBuilder = this.getDao().queryBuilder();
            Where<Alarm, Integer> where = queryBuilder.where();
            where.eq(Alarm.SENSOR_REF_ID, sensorRefId);
            if (enabled != null) {
                where.and().eq(Alarm.ENABLED, enabled);
            }
            if (variableType != null) {
                where.and().eq(Alarm.VARIABLE_TYPE, variableType);
            }
            queryBuilder.setWhere(where);
            List<Alarm> alarms = this.getDao().query(queryBuilder.prepare());
            return alarms;
        } catch (SQLException ex) {
            _logger.error("unable to get all alarms:[selsorRefId:{}, variableType:{}, Enabled:{}]",
                    sensorRefId, variableType, enabled, ex);
            return null;
        }
    }

    @Override
    public Alarm get(Integer id) {
        try {
            return this.getDao().queryForId(id);
        } catch (SQLException ex) {
            _logger.error("unable to get Alarm[id:{}]", id, ex);
            return null;
        }
    }

    @Override
    public void disableAllTriggered() {
        try {
            UpdateBuilder<Alarm, Integer> updateBuilder = getDao().updateBuilder();
            updateBuilder.updateColumnValue(Alarm.TRIGGERED, false).where().eq(Alarm.TRIGGERED, true);
            Integer count = updateBuilder.update();
            _logger.debug("Number of rows updated:[{}]", count);
        } catch (SQLException ex) {
            _logger.error("unable to update alarm triggered status", ex);

        }

    }

}
