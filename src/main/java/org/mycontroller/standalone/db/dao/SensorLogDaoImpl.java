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

import org.mycontroller.standalone.db.tables.SensorLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class SensorLogDaoImpl extends BaseAbstractDao<SensorLog, Integer> implements SensorLogDao {

    private static final Logger _logger = LoggerFactory.getLogger(SensorLogDaoImpl.class);

    public SensorLogDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, SensorLog.class);
    }

    @Override
    public void add(SensorLog sensorLog) {
        try {
            int count = this.getDao().create(sensorLog);
            _logger.debug("Added a log:[{}], Create count:{}", sensorLog, count);

        } catch (SQLException ex) {
            _logger.error("unable to add a log:[{}]", sensorLog, ex);
        }

    }

    @Override
    public void delete(SensorLog sensorLog) {
        try {
            int count = this.getDao().delete(sensorLog);
            _logger.debug("Log:[{}] deleted, Delete count:{}", sensorLog, count);

        } catch (SQLException ex) {
            _logger.error("unable to delete a log:[{}]", sensorLog, ex);
        }

    }

    @Override
    public void deleteBySensorId(int sensorRefId) {
        try {
            DeleteBuilder<SensorLog, Integer> deleteBuilder = this.getDao().deleteBuilder();
            deleteBuilder.where().eq(SensorLog.SENSOR_REF_ID, sensorRefId);
            int deleteCount = deleteBuilder.delete();
            _logger.debug("Deleted sensorRefId:[{}], delete count:{}", sensorRefId, deleteCount);
        } catch (SQLException ex) {
            _logger.error("unable to sensorRefId, SensorLog:{}", sensorRefId, ex);
        }

    }

    @Override
    public void deleteAllBefore(SensorLog sensorLog) {
        try {
            DeleteBuilder<SensorLog, Integer> deleteBuilder = this.getDao().deleteBuilder();
            if (sensorLog.getSensor() != null) {
                deleteBuilder.where().eq(SensorLog.SENSOR_REF_ID, sensorLog.getSensor().getId())
                        .and().le("timestamp", sensorLog.getTimestamp())
                        .and().eq("logType", sensorLog.getLogType());
            } else {
                if (sensorLog.getLogType() != null) {
                    deleteBuilder.where().le("timestamp", sensorLog.getTimestamp())
                            .and().eq("logType", sensorLog.getLogType());
                } else {
                    deleteBuilder.where().le("timestamp", sensorLog.getTimestamp());
                }
            }

            int deleteCount = deleteBuilder.delete();
            _logger.debug("Deleted SensorLog:[{}], delete count:{}", sensorLog, deleteCount);
        } catch (SQLException ex) {
            _logger.error("unable to delete SensorLog, SensorLog:{}", sensorLog, ex);
        }

    }

    @Override
    public List<SensorLog> getAll() {
        try {
            return this.getDao().queryForAll();
        } catch (SQLException ex) {
            _logger.error("unable to get all list", ex);
            return null;
        }
    }

    @Override
    public List<SensorLog> getAll(int sensorRefId) {
        try {
            QueryBuilder<SensorLog, Integer> queryBuilder = this.getDao().queryBuilder();
            queryBuilder.where().eq(SensorLog.SENSOR_REF_ID, sensorRefId);
            return queryBuilder.query();
        } catch (SQLException ex) {
            _logger.error("unable to fetch sensorRefId{}", sensorRefId, ex);
        }
        return null;

    }

}