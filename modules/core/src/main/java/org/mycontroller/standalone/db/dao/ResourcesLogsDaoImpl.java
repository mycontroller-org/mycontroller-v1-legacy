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
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.db.tables.ResourcesLogs;
import org.mycontroller.standalone.exceptions.McDatabaseException;

import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Slf4j
public class ResourcesLogsDaoImpl extends BaseAbstractDaoImpl<ResourcesLogs, Integer> implements ResourcesLogsDao {

    public ResourcesLogsDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, ResourcesLogs.class);
    }

    @Override
    public void add(ResourcesLogs resourcesLogs) {
        try {
            int count = this.getDao().create(resourcesLogs);
            _logger.debug("Added a log:[{}], Create count:{}", resourcesLogs, count);
        } catch (SQLException ex) {
            _logger.error("unable to add a log:[{}]", resourcesLogs, ex);
            throw new McDatabaseException(ex);
        }
    }

    @Override
    public void delete(ResourcesLogs resourcesLogs) {
        try {
            int count = this.getDao().delete(resourcesLogs);
            _logger.debug("ResourcesLogs:[{}] deleted, Delete count:{}", resourcesLogs, count);

        } catch (SQLException ex) {
            _logger.error("unable to delete a log:[{}]", resourcesLogs, ex);
            throw new McDatabaseException(ex);
        }

    }

    @Override
    public void deleteAll(ResourcesLogs resourcesLogs) {
        try {
            DeleteBuilder<ResourcesLogs, Integer> deleteBuilder = this.getDao().deleteBuilder();

            Where<ResourcesLogs, Integer> where = deleteBuilder.where();
            //Add any where condition to avoid and append check
            where.isNotNull(ResourcesLogs.KEY_ID);
            //timestamp before
            if (resourcesLogs.getTimestamp() != null) {
                where.and().lt(ResourcesLogs.KEY_TIMESTAMP, resourcesLogs.getTimestamp());
            }
            //message contains
            if (resourcesLogs.getMessage() != null) {
                where.and().like(ResourcesLogs.KEY_MESSAGE, "%" + resourcesLogs.getMessage() + "%");
            }
            //message contains
            if (resourcesLogs.getMessage() != null) {
                where.and().like(ResourcesLogs.KEY_MESSAGE, "%" + resourcesLogs.getMessage() + "%");
            }
            //log level
            if (resourcesLogs.getLogLevel() != null) {
                where.and().eq(ResourcesLogs.KEY_LOG_LEVEL, resourcesLogs.getLogLevel());
            }
            //log direction
            if (resourcesLogs.getLogDirection() != null) {
                where.and().eq(ResourcesLogs.KEY_LOG_DIRECTION, resourcesLogs.getLogDirection());
            }
            //message type
            if (resourcesLogs.getMessageType() != null) {
                where.and().eq(ResourcesLogs.KEY_MESSAGE_TYPE, resourcesLogs.getMessageType());
            }
            //resource type and resource id
            if (resourcesLogs.getResourceType() != null && resourcesLogs.getResourceId() != null) {
                where.and().eq(ResourcesLogs.KEY_RESOURCE_TYPE, resourcesLogs.getResourceType()).and()
                        .eq(ResourcesLogs.KEY_RESOURCE_ID, resourcesLogs.getResourceId());
            } else if (resourcesLogs.getResourceType() != null) {
                where.and().eq(ResourcesLogs.KEY_RESOURCE_TYPE, resourcesLogs.getResourceType());
            }
            deleteBuilder.setWhere(where);
            int deletionCount = deleteBuilder.delete();
            _logger.debug("Deleted Resource:[{}], deletion count:{}", resourcesLogs, deletionCount);
        } catch (SQLException ex) {
            _logger.error("unable to delete Resource:[{}]", resourcesLogs, ex);
            throw new McDatabaseException(ex);
        }
    }

    @Override
    public void deleteAll(RESOURCE_TYPE resourceType, Integer resourceId) {
        try {
            DeleteBuilder<ResourcesLogs, Integer> deleteBuilder = this.getDao().deleteBuilder();
            this.updateWhereQuery(deleteBuilder.where(), resourceType, resourceId);

            int deleteCount = deleteBuilder.delete();
            _logger.debug("Deleted Resource:[Type:{}, KEY_ID:{}], delete count:{}", resourceType.getText(),
                    resourceId,
                    deleteCount);
        } catch (SQLException ex) {
            _logger.error("unable to delete Resource:[Type:{}, KEY_ID:{}]", resourceType.getText(), resourceId, ex);
            throw new McDatabaseException(ex);
        }
    }

    @Override
    public void deleteAll(RESOURCE_TYPE resourceType, Long timestamp) {
        try {
            DeleteBuilder<ResourcesLogs, Integer> deleteBuilder = this.getDao().deleteBuilder();
            if (resourceType != null && timestamp != null) {
                deleteBuilder.where().eq(ResourcesLogs.KEY_RESOURCE_TYPE, resourceType).and()
                        .le("timestamp", timestamp);
            } else if (timestamp != null) {
                deleteBuilder.where().le("timestamp", timestamp);
            }
            int deleteCount = deleteBuilder.delete();
            _logger.debug("Deleted Resource:[Type:{}, Timestamp:{}], delete count:{}",
                    resourceType, timestamp, deleteCount);
        } catch (SQLException ex) {
            _logger.error("unable to delete Resource:[Type:{}, Timestamp:{}]", resourceType.getText(), timestamp, ex);
            throw new McDatabaseException(ex);
        }
    }

    @Override
    public List<ResourcesLogs> getAll() {
        try {
            return this.getDao().queryForAll();
        } catch (SQLException ex) {
            _logger.error("unable to get all list", ex);
            throw new McDatabaseException(ex);
        }
    }

    @Override
    public List<ResourcesLogs> getAll(RESOURCE_TYPE resourceType, Integer resourceId) {
        try {
            QueryBuilder<ResourcesLogs, Integer> queryBuilder = this.getDao().queryBuilder();
            this.updateWhereQuery(queryBuilder.where(), resourceType, resourceId);
            return queryBuilder.query();
        } catch (SQLException ex) {
            _logger.error("unable to fetch Resource:[Type:{},KEY_ID:{}]", resourceType.getText(), resourceId, ex);
            throw new McDatabaseException(ex);
        }
    }

    private void updateWhereQuery(Where<ResourcesLogs, Integer> whereQuery, RESOURCE_TYPE resourceType,
            Integer resourceId)
            throws SQLException {
        whereQuery.eq(ResourcesLogs.KEY_RESOURCE_TYPE, resourceType).and()
                .eq(ResourcesLogs.KEY_RESOURCE_ID, resourceId);
        switch (resourceType) {
            case SENSOR:
                List<Integer> ids = DaoUtils.getSensorVariableDao().getSensorVariableIds(resourceId);
                if (!ids.isEmpty()) {
                    whereQuery.or().eq(ResourcesLogs.KEY_RESOURCE_TYPE, RESOURCE_TYPE.SENSOR_VARIABLE).and()
                            .in(ResourcesLogs.KEY_RESOURCE_ID, ids);
                }
                break;
            case NODE:
                Node node = DaoUtils.getNodeDao().getById(resourceId);
                List<Integer> sensorIds = DaoUtils.getSensorDao().getSensorIds(node.getEui(),
                        node.getGatewayTable().getId());
                if (!sensorIds.isEmpty()) {
                    whereQuery.or().eq(ResourcesLogs.KEY_RESOURCE_TYPE, RESOURCE_TYPE.SENSOR).and()
                            .in(ResourcesLogs.KEY_RESOURCE_ID, sensorIds);
                    for (Integer sensorId : sensorIds) {
                        List<Integer> sensorVariableIds = DaoUtils.getSensorVariableDao()
                                .getSensorVariableIds(sensorId);
                        if (!sensorVariableIds.isEmpty()) {
                            whereQuery.or()
                                    .eq(ResourcesLogs.KEY_RESOURCE_TYPE, RESOURCE_TYPE.SENSOR_VARIABLE).and()
                                    .in(ResourcesLogs.KEY_RESOURCE_ID, sensorVariableIds);
                        }
                    }
                }
                break;

            //TODO: add for alarm and timer logs

            default:
                break;
        }
    }

    @Override
    public QueryResponse getAll(Query query) {
        try {
            query.setIdColumn(ResourcesLogs.KEY_ID);
            return this.getQueryResponse(query);
        } catch (SQLException ex) {
            _logger.error("unable to run query:[{}]", query, ex);
            throw new McDatabaseException(ex);
        }
    }

    @Override
    public void delete(List<Integer> ids) {
        try {
            int count = this.getDao().deleteIds(ids);
            _logger.debug("Ids:[{}] deleted, Delete count:{}", ids, count);

        } catch (SQLException ex) {
            _logger.error("unable to delete logs:[{}]", ids, ex);
            throw new McDatabaseException(ex);
        }

    }
}