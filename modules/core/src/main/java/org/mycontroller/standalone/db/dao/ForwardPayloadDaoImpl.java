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

import org.mycontroller.standalone.api.jaxrs.model.Query;
import org.mycontroller.standalone.api.jaxrs.model.QueryResponse;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.ForwardPayload;
import org.mycontroller.standalone.exceptions.McDatabaseException;

import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.support.ConnectionSource;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Slf4j
public class ForwardPayloadDaoImpl extends BaseAbstractDaoImpl<ForwardPayload, Integer> implements ForwardPayloadDao {

    public ForwardPayloadDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, ForwardPayload.class);
    }

    @Override
    public List<ForwardPayload> getAllBySourceSensor(Integer sourceSensorId, Boolean enabled) {
        List<Integer> ids = DaoUtils.getSensorVariableDao().getSensorVariableIds(sourceSensorId);
        try {
            QueryBuilder<ForwardPayload, Integer> queryBuilder = this.getDao().queryBuilder();
            if (enabled != null) {
                queryBuilder.where().in(ForwardPayload.KEY_SOURCE_ID, ids).and()
                        .eq(ForwardPayload.KEY_ENABLED, enabled);
            } else {
                queryBuilder.where().in(ForwardPayload.KEY_SOURCE_ID, ids);
            }
            return queryBuilder.query();
        } catch (SQLException ex) {
            _logger.error("unable to get all list", ex);
            throw new McDatabaseException(ex);
        }
    }

    @Override
    public List<ForwardPayload> getAllBySourceSensor(Integer sourceSensorId) {
        return getAllBySourceSensor(sourceSensorId, null);
    }

    @Override
    public List<ForwardPayload> getAllByDestinationSensor(Integer destinationSensorId) {
        List<Integer> ids = DaoUtils.getSensorVariableDao().getSensorVariableIds(destinationSensorId);
        try {
            QueryBuilder<ForwardPayload, Integer> queryBuilder = this.getDao().queryBuilder();
            queryBuilder.where().in(ForwardPayload.KEY_DESTINATION_ID, ids);
            return queryBuilder.query();
        } catch (SQLException ex) {
            _logger.error("unable to get all list", ex);
            throw new McDatabaseException(ex);
        }
    }

    @Override
    public void deleteBySensorId(Integer sensorId) {
        try {
            List<Integer> ids = DaoUtils.getSensorVariableDao().getSensorVariableIds(sensorId);
            DeleteBuilder<ForwardPayload, Integer> deleteBuilder = this.getDao().deleteBuilder();
            deleteBuilder.where().in(ForwardPayload.KEY_SOURCE_ID, ids).or()
                    .in(ForwardPayload.KEY_DESTINATION_ID, ids);
            Integer deleteCount = deleteBuilder.delete();
            _logger.debug("Deleted SensorId:[{}], delete count:{}", sensorId, deleteCount);
        } catch (SQLException ex) {
            _logger.error("unable to delete SensorId:{}", sensorId, ex);
            throw new McDatabaseException(ex);
        }
    }

    @Override
    public void enable(List<Integer> ids) {
        this.enableDisable(ids, true);
    }

    @Override
    public void disable(List<Integer> ids) {
        this.enableDisable(ids, false);
    }

    private void enableDisable(List<Integer> ids, boolean enabled) {
        try {
            UpdateBuilder<ForwardPayload, Integer> updateBuilder = this.getDao().updateBuilder();
            updateBuilder.updateColumnValue(ForwardPayload.KEY_ENABLED, enabled).where()
                    .in(ForwardPayload.KEY_ID, ids);
            int updateCount = updateBuilder.update();
            _logger.debug("Updated rows count:{}", updateCount);
        } catch (SQLException ex) {
            _logger.error("Failed to update, Ids:{}", ids, ex);
            throw new McDatabaseException(ex);
        }

    }

    @Override
    public List<ForwardPayload> getAll(Integer sensorVariableId) {
        try {
            return this.getDao().queryForEq(ForwardPayload.KEY_SOURCE_ID, sensorVariableId);
        } catch (SQLException ex) {
            _logger.error("unable to featch getAll for selected id, ", ex);
            throw new McDatabaseException(ex);
        }
    }

    @Override
    public List<ForwardPayload> getAllEnabled(Integer sensorVariableId) {
        try {
            QueryBuilder<ForwardPayload, Integer> queryBuilder = this.getDao().queryBuilder();

            queryBuilder.where().eq(ForwardPayload.KEY_ENABLED, true).and()
                    .eq(ForwardPayload.KEY_SOURCE_ID, sensorVariableId);
            return queryBuilder.query();
        } catch (SQLException ex) {
            _logger.error("unable to featch getAll for selected id, ", ex);
            throw new McDatabaseException(ex);
        }
    }

    @Override
    public QueryResponse getAll(Query query) {
        try {
            query.setIdColumn(ForwardPayload.KEY_ID);
            return this.getQueryResponse(query);
        } catch (SQLException ex) {
            _logger.error("unable to run query:[{}]", query, ex);
            throw new McDatabaseException(ex);
        }
    }

    @Override
    public ForwardPayload get(ForwardPayload tdao) {
        return this.getById(tdao.getId());
    }

    @Override
    public List<ForwardPayload> getAll(List<Integer> ids) {
        return getAll(ForwardPayload.KEY_ID, ids);
    }

}
