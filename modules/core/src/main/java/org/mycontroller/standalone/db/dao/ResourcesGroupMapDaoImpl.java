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
import org.mycontroller.standalone.db.tables.ResourcesGroup;
import org.mycontroller.standalone.db.tables.ResourcesGroupMap;

import com.j256.ormlite.dao.Dao.CreateOrUpdateStatus;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
@Slf4j
public class ResourcesGroupMapDaoImpl extends BaseAbstractDaoImpl<ResourcesGroupMap, Integer> implements
        ResourcesGroupMapDao {

    public ResourcesGroupMapDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, ResourcesGroupMap.class);
    }

    @Override
    public void create(ResourcesGroupMap resourcesGroupMap) {
        try {
            Integer count = this.getDao().create(resourcesGroupMap);
            _logger.debug("Created ResourcesGroupMap:[{}], Create count:{}", resourcesGroupMap, count);
        } catch (SQLException ex) {
            _logger.error("unable to add ResourcesGroupMap:[{}]", resourcesGroupMap, ex);
        }
    }

    @Override
    public void createOrUpdate(ResourcesGroupMap resourcesGroupMap) {
        try {
            CreateOrUpdateStatus status = this.getDao().createOrUpdate(resourcesGroupMap);
            _logger.debug("CreateOrUpdate ResourcesGroupMap:[{}],Create:{},Update:{},Lines Changed:{}",
                    resourcesGroupMap,
                    status.isCreated(),
                    status.isUpdated(), status.getNumLinesChanged());
        } catch (SQLException ex) {
            _logger.error("unable to CreateOrUpdate ResourcesGroup:[{}]", resourcesGroupMap, ex);
        }
    }

    @Override
    public void update(ResourcesGroupMap resourcesGroupMap) {
        try {
            Integer count = this.getDao().update(resourcesGroupMap);
            _logger.debug("Updated ResourcesGroupMap:[{}], Create count:{}", resourcesGroupMap, count);
        } catch (SQLException ex) {
            _logger.error("unable to update ResourcesGroupMap:[{}]", resourcesGroupMap, ex);
        }
    }

    @Override
    public void delete(ResourcesGroupMap resourcesGroupMap) {
        delete(null, resourcesGroupMap.getId(), null, null);
    }

    @Override
    public void delete(Integer id) {
        delete(id, null, null, null);
    }

    @Override
    public void delete(RESOURCE_TYPE resourceType, Integer resourceId) {
        delete(null, null, resourceType, resourceId);
    }

    @Override
    public void delete(ResourcesGroup resourcesGroup) {
        delete(null, resourcesGroup.getId(), null, null);

    }

    private void delete(Integer id, Integer resourcesGroupId, RESOURCE_TYPE resourceType,
            Integer resourceId) {
        try {
            int deleteCount = 0;
            if (id != null) {
                deleteCount = this.getDao().deleteById(id);
            } else if (resourcesGroupId != null) {
                DeleteBuilder<ResourcesGroupMap, Integer> deleteBuilder = this.getDao().deleteBuilder();
                deleteBuilder.where().eq(ResourcesGroupMap.KEY_GROUP_ID, resourcesGroupId);
                deleteCount = deleteBuilder.delete();
            } else if (resourceType != null && resourceId != null) {
                DeleteBuilder<ResourcesGroupMap, Integer> deleteBuilder = this.getDao().deleteBuilder();
                deleteBuilder.where().eq(ResourcesGroupMap.KEY_RESOURCE_TYPE, resourceType).and()
                        .eq(ResourcesGroupMap.KEY_RESOURCE_ID, resourceId);
                deleteCount = deleteBuilder.delete();
            } else {
                _logger.warn("all values should not be null!");
            }
            _logger.debug("Deleted [id:{}, resourceGroupId:{}, resourceType:{}, resourceId:{}], delete count:{}",
                    id, resourcesGroupId, resourceType, resourceId, deleteCount);
        } catch (SQLException ex) {
            _logger.debug("unable to deleted [id:{}, resourceGroupId:{}, resourceType:{}, resourceId:{}]",
                    id, resourcesGroupId, resourceType, resourceId, ex);
        }
    }

    @Override
    public ResourcesGroupMap get(Integer id) {
        try {
            return this.getDao().queryForId(id);
        } catch (SQLException ex) {
            _logger.error("unable to get id:[{}]", id, ex);
        }
        return null;
    }

    @Override
    public List<ResourcesGroupMap> getAll(Integer resourceGroupId) {
        return getAllPrivate(resourceGroupId, null, null);
    }

    @Override
    public List<ResourcesGroupMap> getAll(RESOURCE_TYPE resourceType, Integer resourceId) {
        return getAllPrivate(null, resourceType, resourceId);
    }

    @Override
    public List<ResourcesGroupMap> getAll() {
        return getAllPrivate(null, null, null);
    }

    @Override
    public List<ResourcesGroupMap> getAll(ResourcesGroup resourcesGroup) {
        return getAllPrivate(resourcesGroup.getId(), null, null);
    }

    private List<ResourcesGroupMap> getAllPrivate(Integer resourceGroupId, RESOURCE_TYPE resourceType,
            Integer resourceId) {
        try {
            if (resourceGroupId == null && resourceType == null && resourceId == null) {
                return this.getDao().queryForAll();
            } else if (resourceGroupId != null) {
                QueryBuilder<ResourcesGroupMap, Integer> queryBuilder = this.getDao().queryBuilder();
                queryBuilder.where().eq(ResourcesGroupMap.KEY_GROUP_ID, resourceGroupId);
                return queryBuilder.query();
            } else if (resourceType != null && resourceId != null) {
                QueryBuilder<ResourcesGroupMap, Integer> queryBuilder = this.getDao().queryBuilder();
                queryBuilder.where().eq(ResourcesGroupMap.KEY_RESOURCE_TYPE, resourceType).and()
                        .eq(ResourcesGroupMap.KEY_RESOURCE_ID, resourceId);
                return queryBuilder.query();
            } else if (resourceType != null) {
                QueryBuilder<ResourcesGroupMap, Integer> queryBuilder = this.getDao().queryBuilder();
                queryBuilder.where().eq(ResourcesGroupMap.KEY_RESOURCE_TYPE, resourceType);
                return queryBuilder.query();
            }
            return null;
        } catch (SQLException ex) {
            _logger.error("unable to get all list", ex);
            return null;
        }
    }

    @Override
    public void delete(List<Integer> ids) {
        try {
            int deleteCount = this.getDao().deleteIds(ids);
            _logger.debug("Deleted [ids:{}], delete count:{}", ids, deleteCount);
        } catch (SQLException ex) {
            _logger.debug("unable to deleted [ids:{}]", ids, ex);
        }
    }

    @Override
    public long countOf(RESOURCE_TYPE resourceType, Integer resourceId) {
        try {
            QueryBuilder<ResourcesGroupMap, Integer> queryBuilder = this.getDao().queryBuilder();
            queryBuilder.where().eq(ResourcesGroupMap.KEY_RESOURCE_TYPE, resourceType).and()
                    .eq(ResourcesGroupMap.KEY_RESOURCE_ID, resourceId);
            return queryBuilder.countOf();
        } catch (SQLException ex) {
            _logger.error("unable to get items count for resource[Type:{}, Id:{}]", resourceType,
                    resourceId, ex);
        }
        return 0;
    }

    @Override
    public QueryResponse getAll(Query query) {
        try {
            query.setIdColumn(ResourcesGroupMap.KEY_ID);
            query.setTotalCountAltColumn(ResourcesGroupMap.KEY_GROUP_ID);
            return this.getQueryResponse(query);
        } catch (SQLException ex) {
            _logger.error("unable to run query:[{}]", query, ex);
            return null;
        }
    }
}