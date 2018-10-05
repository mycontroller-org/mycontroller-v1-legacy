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
import org.mycontroller.standalone.db.tables.ResourcesGroup;

import com.j256.ormlite.dao.Dao.CreateOrUpdateStatus;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Slf4j
public class ResourcesGroupDaoImpl extends BaseAbstractDaoImpl<ResourcesGroup, Integer> implements ResourcesGroupDao {

    public ResourcesGroupDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, ResourcesGroup.class);
    }

    @Override
    public void create(ResourcesGroup resourcesGroup) {
        try {
            int count = this.getDao().create(resourcesGroup);
            _logger.debug("Created ResourcesGroup:[{}], Create count:{}", resourcesGroup, count);

        } catch (SQLException ex) {
            _logger.error("unable to add ResourcesGroup:[{}]", resourcesGroup, ex);
        }
    }

    @Override
    public void createOrUpdate(ResourcesGroup resourcesGroup) {
        try {
            CreateOrUpdateStatus status = this.getDao().createOrUpdate(resourcesGroup);
            _logger.debug("CreateOrUpdate ResourcesGroup:[{}],Create:{},Update:{},Lines Changed:{}", resourcesGroup,
                    status.isCreated(),
                    status.isUpdated(), status.getNumLinesChanged());
        } catch (SQLException ex) {
            _logger.error("unable to CreateOrUpdate ResourcesGroup:[{}]", resourcesGroup, ex);
        }
    }

    private void delete(Integer id, String name) {
        try {
            int count = 0;
            if (id != null) {
                count = this.getDao().deleteById(id);
            } else if (name != null) {
                DeleteBuilder<ResourcesGroup, Integer> deleteBuilder = this.getDao().deleteBuilder();
                deleteBuilder.where().eq("name", name);
                count = deleteBuilder.delete();
            }
            _logger.debug("ResourcesGroup:[id:{}, name:{}] deleted, Delete count:{}", id, name, count);
        } catch (SQLException ex) {
            _logger.error("unable to delete resourcesGroup:[id:{}, name:{}]", id, name, ex);
        }
    }

    @Override
    public void delete(Integer id) {
        delete(id, null);
    }

    @Override
    public void delete(ResourcesGroup resourcesGroup) {
        delete(resourcesGroup.getId(), resourcesGroup.getName());
    }

    @Override
    public void update(ResourcesGroup resourcesGroup) {
        try {
            int count = this.getDao().update(resourcesGroup);
            _logger.debug("Updated ResourcesGroup:[{}], Update count:{}", resourcesGroup, count);
        } catch (SQLException ex) {
            _logger.error("unable to update resourcesGroup:[{}]", resourcesGroup, ex);
        }
    }

    @Override
    public List<ResourcesGroup> getAll() {
        try {
            return this.getDao().queryForAll();
        } catch (SQLException ex) {
            _logger.error("unable to get ResourcesGroup", ex);
            return null;
        }
    }

    @Override
    public ResourcesGroup get(ResourcesGroup resourcesGroup) {
        try {
            if (resourcesGroup.getId() != null) {
                return this.getDao().queryForId(resourcesGroup.getId());
            } else if (resourcesGroup.getName() != null) {

                QueryBuilder<ResourcesGroup, Integer> queryBuilder = this.getDao().queryBuilder();
                queryBuilder.where().eq("name", resourcesGroup.getName());
                return queryBuilder.queryForFirst();

            }
        } catch (SQLException ex) {
            _logger.error("unable to get ResourcesGroup", ex);
            return null;
        }
        return null;
    }

    @Override
    public ResourcesGroup get(Integer id) {
        try {
            return this.getDao().queryForId(id);
        } catch (SQLException ex) {
            _logger.error("unable to get ResourcesGroup", ex);
        }
        return null;
    }

    @Override
    public QueryResponse getAll(Query query) {
        try {
            query.setIdColumn(ResourcesGroup.KEY_ID);
            return super.getQueryResponse(query);
        } catch (SQLException ex) {
            _logger.error("unable to run query:[{}]", query, ex);
            return null;
        }
    }

    @Override
    public List<ResourcesGroup> getAll(List<Integer> ids) {
        return super.getAll(ResourcesGroup.KEY_ID, ids);
    }
}
