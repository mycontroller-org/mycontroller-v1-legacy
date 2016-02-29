/**
 * Copyright (C) 2015-2016 Jeeva Kandasamy (jkandasa@gmail.com)
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
import java.util.Arrays;
import java.util.List;

import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.alarm.AlarmUtils.DAMPENING_TYPE;
import org.mycontroller.standalone.api.jaxrs.mapper.Query;
import org.mycontroller.standalone.api.jaxrs.mapper.QueryResponse;
import org.mycontroller.standalone.db.tables.AlarmDefinition;

import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class AlarmDefinitionDaoImpl extends BaseAbstractDaoImpl<AlarmDefinition, Integer> implements
        AlarmDefinitionDao {
    public AlarmDefinitionDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, AlarmDefinition.class);
    }

    @Override
    public void delete(RESOURCE_TYPE resourceType, Integer resourceId) {
        try {
            DeleteBuilder<AlarmDefinition, Integer> deleteBuilder = this.getDao().deleteBuilder();
            deleteBuilder.where().eq(AlarmDefinition.KEY_RESOURCE_TYPE, resourceType).and()
                    .eq(AlarmDefinition.KEY_RESOURCE_ID, resourceId);
            Integer count = deleteBuilder.delete();
            _logger.debug("deleted alarms with Resource:[Type:{}, KEY_ID:{}], Deletion Count:{}",
                    resourceType.getText(),
                    resourceId, count);
        } catch (SQLException ex) {
            _logger.error("unable to delete alarms with Resource:[Type:{}, KEY_ID:{}]", resourceType.getText(),
                    resourceId, ex);
        }
    }

    @Override
    public List<AlarmDefinition> getAll(RESOURCE_TYPE resourceType) {
        return getAll(resourceType, null);
    }

    @Override
    public List<AlarmDefinition> getAll(RESOURCE_TYPE resourceType, Integer resourceId) {
        return this.getAll(resourceType, resourceId, null);
    }

    @Override
    public List<AlarmDefinition> getAllEnabled(RESOURCE_TYPE resourceType, Integer resourceId) {
        return this.getAll(resourceType, resourceId, true);
    }

    @Override
    public List<AlarmDefinition> getAll(RESOURCE_TYPE resourceType, Integer resourceId, Boolean enabled) {
        try {
            QueryBuilder<AlarmDefinition, Integer> queryBuilder = this.getDao().queryBuilder();
            Where<AlarmDefinition, Integer> where = queryBuilder.where();
            where.eq(AlarmDefinition.KEY_RESOURCE_TYPE, resourceType);
            if (enabled != null) {
                where.and().eq(AlarmDefinition.KEY_ENABLED, enabled);
            }
            if (resourceId != null) {
                where.and().eq(AlarmDefinition.KEY_RESOURCE_ID, resourceId);
            }
            queryBuilder.setWhere(where);
            List<AlarmDefinition> alarmDefinitions = this.getDao().query(queryBuilder.prepare());
            return alarmDefinitions;
        } catch (SQLException ex) {
            _logger.error("unable to get all alarms:[Resource:[Type:{},KEY_ID:{}], Enabled:{}]",
                    resourceType.getText(), resourceId, enabled, ex);
            return null;
        }
    }

    @Override
    public void disableAllTriggered() {
        try {
            UpdateBuilder<AlarmDefinition, Integer> updateBuilder = getDao().updateBuilder();
            updateBuilder.updateColumnValue(AlarmDefinition.KEY_TRIGGERED, false).where()
                    .eq(AlarmDefinition.KEY_TRIGGERED, true);
            Integer count = updateBuilder.update();
            _logger.debug("Number of rows updated:[{}]", count);
        } catch (SQLException ex) {
            _logger.error("unable to update alarm triggered status", ex);

        }
    }

    @Override
    public QueryResponse getAll(Query query) {
        try {
            return this.getQueryResponse(query, AlarmDefinition.KEY_ID);
        } catch (SQLException ex) {
            _logger.error("unable to run query:[{}]", query, ex);
            return null;
        }
    }

    @Override
    public AlarmDefinition get(AlarmDefinition alarmDefinition) {
        return this.getById(alarmDefinition.getId());
    }

    @Override
    public List<AlarmDefinition> getAll(List<Integer> ids) {
        return getAll(AlarmDefinition.KEY_ID, ids);
    }

    @Override
    public long countOf(RESOURCE_TYPE resourceType, Integer resourceId) {
        return countOf(resourceType, Arrays.asList(resourceId));
    }

    @Override
    public long countOf(RESOURCE_TYPE resourceType, List<Integer> resourceIds) {
        try {
            QueryBuilder<AlarmDefinition, Integer> queryBuilder = this.getDao().queryBuilder();
            queryBuilder.where().eq(AlarmDefinition.KEY_RESOURCE_TYPE, resourceType).and()
                    .in(AlarmDefinition.KEY_RESOURCE_ID, resourceIds);
            return queryBuilder.countOf();
        } catch (SQLException ex) {
            _logger.error("unable to get alarm definitions count for resource[Type:{}, Id:{}]", resourceType,
                    resourceIds, ex);
        }
        return 0;
    }

    @Override
    public List<AlarmDefinition> getAll(DAMPENING_TYPE dampeningType) {
        return getAll(AlarmDefinition.KEY_DAMPENING_TYPE, dampeningType);
    }

    @Override
    public List<AlarmDefinition> getAllByResourceIds(RESOURCE_TYPE resourceType, List<Integer> resourceIds) {
        try {
            return this.getDao().queryBuilder().where().eq(AlarmDefinition.KEY_RESOURCE_TYPE, resourceType).and()
                    .in(AlarmDefinition.KEY_RESOURCE_ID, resourceIds).query();
        } catch (SQLException ex) {
            _logger.error("unable to get alarm definitions count for resource[Type:{}, Id:{}]", resourceType,
                    resourceIds, ex);
        }
        return null;
    }

    @Override
    public AlarmDefinition getByName(String name) {
        try {
            return this.getDao().queryBuilder().where().eq(AlarmDefinition.KEY_NAME, name).queryForFirst();
        } catch (SQLException ex) {
            _logger.error("unable to get alarm definition name:{},", name, ex);
        }
        return null;
    }
}
