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

import org.mycontroller.standalone.db.tables.Timer;
import org.mycontroller.standalone.scheduler.SchedulerUtils;

import com.j256.ormlite.dao.Dao.CreateOrUpdateStatus;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class TimerDaoImpl extends BaseAbstractDao<Timer, Integer> implements TimerDao {
    public TimerDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, Timer.class);
    }

    @Override
    public void create(Timer timer) {
        try {
            int count = this.getDao().create(timer);
            SchedulerUtils.loadTimerJob(timer);
            _logger.debug("Created Timer:[{}], Create count:{}", timer, count);
        } catch (SQLException ex) {
            _logger.error("unable to add Timer:[{}]", timer, ex);
        }
    }

    @Override
    public void createOrUpdate(Timer timer) {
        try {
            CreateOrUpdateStatus status = this.getDao().createOrUpdate(timer);
            SchedulerUtils.reloadTimerJob(timer);
            _logger.debug("CreateOrUpdate Timer:[{}],Create:{},Update:{},Lines Changed:{}",
                    timer, status.isCreated(), status.isUpdated(),
                    status.getNumLinesChanged());
        } catch (SQLException ex) {
            _logger.error("unable to CreateOrUpdate Timer:[{}]", timer, ex);
        }
    }

    @Override
    public void delete(Timer timer) {
        try {
            int count = this.getDao().delete(timer);
            SchedulerUtils.unloadTimerJob(timer);
            _logger.debug("Timer:[{}] deleted, Delete count:{}", timer, count);
        } catch (SQLException ex) {
            _logger.error("unable to delete timer:[{}]", timer, ex);
        }
    }

    @Override
    public void delete(int id) {
        delete(get(id));
    }

    @Override
    public void update(Timer timer) {
        try {
            int count = this.getDao().update(timer);
            _logger.debug("Updated Timer:[{}], Update count:{}", timer, count);
        } catch (SQLException ex) {
            _logger.error("unable to update timer:[{}]", timer, ex);
        }

    }

    @Override
    public List<Timer> getAll() {
        try {
            return this.getDao().queryForAll();
        } catch (SQLException ex) {
            _logger.error("unable to get all Nodes", ex);
            return null;
        }
    }

    @Override
    public List<Timer> getAll(int sensorRefId) {
        return getAll(sensorRefId, null);
    }

    @Override
    public void deleteBySensorRefId(int sensorRefId) {
        try {
            DeleteBuilder<Timer, Integer> deleteBuilder = this.getDao().deleteBuilder();
            deleteBuilder.where().eq(Timer.SENSOR_REF_ID, sensorRefId);
            int count = deleteBuilder.delete();
            _logger.debug("deleted timers with sensorRefId:[{}], Deletion Count:{}", sensorRefId, count);
        } catch (SQLException ex) {
            _logger.error("unable to delete timers with sensorRefId:[{}]", sensorRefId, ex);
        }
    }

    @Override
    public List<Timer> getAll(int sensorRefId, Boolean enabled) {
        try {
            QueryBuilder<Timer, Integer> queryBuilder = this.getDao().queryBuilder();
            if (enabled != null) {
                queryBuilder.where().eq(Timer.ENABLED, enabled).and().eq(Timer.SENSOR_REF_ID, sensorRefId);
            } else {
                queryBuilder.where().eq(Timer.SENSOR_REF_ID, sensorRefId);
            }
            List<Timer> timers = this.getDao().query(queryBuilder.prepare());
            return timers;
        } catch (SQLException ex) {
            _logger.error("unable to get all timers:[selsorRefId:{}, Enabled:{}]", sensorRefId, enabled, ex);
            return null;
        }
    }

    @Override
    public List<Timer> getAllEnabled() {
        try {
            QueryBuilder<Timer, Integer> queryBuilder = this.getDao().queryBuilder();
            queryBuilder.where().eq(Timer.ENABLED, true);
            List<Timer> timers = this.getDao().query(queryBuilder.prepare());
            return timers;
        } catch (SQLException ex) {
            _logger.error("unable to get all enabled timers", ex);
            return null;
        }
    }

    @Override
    public Timer get(int id) {
        try {
            return this.getDao().queryForId(id);
        } catch (SQLException ex) {
            _logger.error("unable to get Timer[id:{}]", id, ex);
            return null;
        }
    }

}
