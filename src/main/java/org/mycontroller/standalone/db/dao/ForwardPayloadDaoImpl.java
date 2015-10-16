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

import org.mycontroller.standalone.db.tables.ForwardPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class ForwardPayloadDaoImpl extends BaseAbstractDao<ForwardPayload, Integer> implements ForwardPayloadDao {
    private static final Logger _logger = LoggerFactory.getLogger(ForwardPayloadDaoImpl.class);

    public ForwardPayloadDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, ForwardPayload.class);
    }

    @Override
    public void create(ForwardPayload forwardPayload) {
        try {
            Integer count = this.getDao().create(forwardPayload);
            _logger.debug("Created ForwardPayload:[{}], Create count:{}", forwardPayload, count);
        } catch (SQLException ex) {
            _logger.error("unable to add ForwardPayload:[{}]", forwardPayload, ex);
        }
    }

    @Override
    public void delete(Integer id) {
        ForwardPayload forwardPayload = new ForwardPayload(id);
        try {
            Integer count = this.getDao().delete(forwardPayload);
            _logger.debug("ForwardPayload:[{}] deleted, Delete count:{}", forwardPayload, count);
        } catch (SQLException ex) {
            _logger.error("unable to delete ForwardPayload:[{}]", forwardPayload, ex);
        }
    }

    @Override
    public void deleteBySensorRefId(Integer sensorRefId) {
        try {
            DeleteBuilder<ForwardPayload, Integer> deleteBuilder = this.getDao().deleteBuilder();
            deleteBuilder.where().eq(ForwardPayload.SENSOR_REF_ID, sensorRefId).or()
                    .eq(ForwardPayload.FORWARD_SENSOR_REF_ID, sensorRefId);
            Integer deleteCount = deleteBuilder.delete();
            _logger.debug("Deleted sensorRefId:[{}], delete count:{}", sensorRefId, deleteCount);
        } catch (SQLException ex) {
            _logger.error("unable to delete sensorRefId:{}", sensorRefId, ex);
        }
    }

    @Override
    public List<ForwardPayload> getAll() {
        try {
            return this.getDao().queryForAll();
        } catch (SQLException ex) {
            _logger.error("unable to get all list", ex);
            return null;
        }
    }

    @Override
    public List<ForwardPayload> getAll(Integer sensorRefId) {
        return this.getAll(sensorRefId, null);
    }

    @Override
    public List<ForwardPayload> getAll(Integer sensorRefId, Integer sourceType) {
        try {
            QueryBuilder<ForwardPayload, Integer> queryBuilder = this.getDao().queryBuilder();
            if (sourceType != null) {
                queryBuilder.where().eq(ForwardPayload.SENSOR_REF_ID, sensorRefId).and()
                        .eq(ForwardPayload.SOURCE_TYPE, sourceType);
            } else {
                queryBuilder.where().eq(ForwardPayload.SENSOR_REF_ID, sensorRefId);
            }
            return queryBuilder.query();
        } catch (SQLException ex) {
            _logger.error("unable to fetch sensorRefId{}, sourceType", sensorRefId, sourceType, ex);
        }
        return null;
    }

}
