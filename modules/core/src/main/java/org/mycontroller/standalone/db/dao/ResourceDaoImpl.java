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

import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.api.jaxrs.model.Query;
import org.mycontroller.standalone.api.jaxrs.model.QueryResponse;
import org.mycontroller.standalone.db.tables.Resource;

import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Slf4j
public class ResourceDaoImpl extends BaseAbstractDaoImpl<Resource, Integer>
        implements ResourceDao {
    public ResourceDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, Resource.class);
    }

    @Override
    public QueryResponse getAll(Query query) {
        try {
            query.setIdColumn(Resource.KEY_ID);
            return this.getQueryResponse(query);
        } catch (SQLException ex) {
            _logger.error("unable to run query:[{}]", query, ex);
            return null;
        }
    }

    @Override
    public Resource get(Resource tdao) {
        return this.getById(tdao.getId());
    }

    @Override
    public List<Resource> getAll(List<Integer> ids) {
        return getAll(Resource.KEY_ID, ids);
    }

    @Override
    public List<Resource> getAll(RESOURCE_TYPE resourceType, Integer resourceId, Boolean enabled) {
        try {
            QueryBuilder<Resource, Integer> queryBuilder = getDao().queryBuilder();
            Where<Resource, Integer> where = queryBuilder.where();
            int whereCount = 0;
            where.eq(Resource.KEY_RESOURCE_TYPE, resourceType);
            whereCount++;
            where.eq(Resource.KEY_RESOURCE_ID, resourceId);
            whereCount++;
            if (enabled != null) {

                where.eq(Resource.KEY_ENABLED, resourceId);
                whereCount++;
            }
            where.and(whereCount);
            queryBuilder.setWhere(where);
            return queryBuilder.query();
        } catch (SQLException ex) {
            _logger.error("get resource failed! input:[resourceType:{}, resourceId:{}, enabled:{}], ",
                    resourceType, resourceId, enabled, ex);
            return null;
        }
    }

    @Override
    public List<Resource> getAll(RESOURCE_TYPE resourceType, Integer resourceId) {
        return getAll(resourceType, resourceId, null);
    }

    private void updateEnabled(RESOURCE_TYPE resourceType, Integer resourceId, boolean enabled) {
        try {
            UpdateBuilder<Resource, Integer> updateBuilder = getDao().updateBuilder();
            Where<Resource, Integer> where = updateBuilder.where();
            int whereCount = 0;
            where.eq(Resource.KEY_RESOURCE_TYPE, resourceType);
            whereCount++;
            where.eq(Resource.KEY_RESOURCE_ID, resourceId);
            where.and(whereCount);
            updateBuilder.setWhere(where);
            updateBuilder.updateColumnValue(Resource.KEY_ENABLED, enabled);
            int updateCount = updateBuilder.update();
            _logger.debug("Update count:{}", updateCount);
        } catch (SQLException ex) {
            _logger.error("Failed to update! input:[resourceType:{}, resourceId:{}, enabled:{}], ",
                    resourceType, resourceId, enabled, ex);
        }

    }

    @Override
    public void enable(RESOURCE_TYPE resourceType, Integer resourceId) {
        updateEnabled(resourceType, resourceId, true);
    }

    @Override
    public void disable(RESOURCE_TYPE resourceType, Integer resourceId) {
        updateEnabled(resourceType, resourceId, false);
    }

    @Override
    public void delete(RESOURCE_TYPE resourceType, Integer resourceId) {
        try {
            DeleteBuilder<Resource, Integer> deleteBuilder = getDao().deleteBuilder();
            Where<Resource, Integer> where = deleteBuilder.where();
            int whereCount = 0;
            where.eq(Resource.KEY_RESOURCE_TYPE, resourceType);
            whereCount++;
            where.eq(Resource.KEY_RESOURCE_ID, resourceId);
            where.and(whereCount);
            deleteBuilder.setWhere(where);
            int deleteCount = deleteBuilder.delete();
            _logger.debug("Delete count:{}", deleteCount);
        } catch (SQLException ex) {
            _logger.error("Failed to delete! input:[resourceType:{}, resourceId:{}], ",
                    resourceType, resourceId, ex);
        }
    }

    @Override
    public Resource get(RESOURCE_TYPE resourceType, Integer resourceId) {
        List<Resource> resources = getAll(resourceType, resourceId);
        if (resources != null && !resources.isEmpty()) {
            return resources.get(0);
        }
        return null;
    }
}
