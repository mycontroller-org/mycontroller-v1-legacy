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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.api.jaxrs.model.AllowedResources;
import org.mycontroller.standalone.api.jaxrs.model.Query;
import org.mycontroller.standalone.api.jaxrs.model.QueryResponse;
import org.mycontroller.standalone.api.jaxrs.model.ResourcePurgeConf.OPERATOR;
import org.mycontroller.standalone.db.tables.GatewayTable;
import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.db.tables.SensorVariable;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.Dao.CreateOrUpdateStatus;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableInfo;
import com.j256.ormlite.table.TableUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Slf4j
public abstract class BaseAbstractDaoImpl<Tdao, Tid> {

    private Dao<Tdao, Tid> dao;
    private TableInfo<Tdao, Tid> tableInfo;

    @SuppressWarnings("unchecked")
    public BaseAbstractDaoImpl(ConnectionSource connectionSource, Class<Tdao> entity) throws SQLException {
        dao = (Dao<Tdao, Tid>) DaoManager.createDao(connectionSource, entity);
        //Enable Auto commit
        //dao.setAutoCommit(connectionSource.getReadWriteConnection(), true);
        //Create Table if not exists
        //https://github.com/j256/ormlite-core/issues/20
        if (!hasTable(((BaseDaoImpl<?, ?>) dao).getTableInfo().getTableName())) {
            TableUtils.createTableIfNotExists(connectionSource, entity);
        }
        _logger.debug("Create Table If Not Exists, executed for {}", entity.getName());

        //Create TableInfo object
        tableInfo = new TableInfo<Tdao, Tid>(connectionSource, (BaseDaoImpl<Tdao, Tid>) dao, entity);
    }

    protected boolean hasTable(String tablename) {
        try {
            // test if the table already exists
            ((BaseDaoImpl<?, ?>) dao).countOf();
            return true;
        } catch (SQLException ex) {
            return false;
        }
    }

    public Dao<Tdao, Tid> getDao() {
        return dao;
    }

    public TableInfo<Tdao, Tid> getTableInfo() {
        return tableInfo;
    }

    private int addResourcesFilter(AllowedResources allowedResources, RESOURCE_TYPE type, Where<Tdao, Tid> where)
            throws SQLException {
        int count = 0;
        switch (type) {
            case GATEWAY:
                where.in(GatewayTable.KEY_ID, allowedResources.getGatewayIds());
                count++;
                break;
            case NODE:
                where.in(Node.KEY_GATEWAY_ID, allowedResources.getGatewayIds()).or()
                        .in(Node.KEY_ID, allowedResources.getNodeIds());
                count++;
                break;
            case SENSOR:
                where.in(Sensor.KEY_NODE_ID, allowedResources.getNodeIds()).or()
                        .in(Sensor.KEY_ID, allowedResources.getSensorIds());
                count++;
                break;
            case SENSOR_VARIABLE:
                where.in(SensorVariable.KEY_SENSOR_DB_ID, allowedResources.getSensorIds()).or()
                        .in(SensorVariable.KEY_ID, allowedResources.getSensorVariableIds());
                count++;
                break;
            default:
                break;

        }
        return count;
    }

    @SuppressWarnings("unchecked")
    public QueryResponse getQueryResponse(Query query)
            throws SQLException {
        _logger.debug("Input query: {}", query);
        QueryBuilder<Tdao, Tid> queryBuilder = this.getDao().queryBuilder();
        Where<Tdao, Tid> whereMain = this.getDao().queryBuilder().where();

        AllowedResources allowedResources = null;
        RESOURCE_TYPE allowedResourceType = null;

        //where.isNotNull(idColumn);
        int whereCount = 0;
        for (String key : query.getFilters().keySet()) {
            if (query.getFilters().get(key) != null) {
                if (key.equals(AllowedResources.KEY_ALLOWED_RESOURCE_TYPE)) {
                    continue;
                }
                if (query.getFilters().get(key) instanceof List<?>) {
                    for (Object value : (List<?>) query.getFilters().get(key)) {
                        if (value instanceof String) {//If it's string add one by one
                            whereMain.like(key, "%" + value + "%");
                            whereCount++;
                        } else {//If it's integer, float, long, etc., add it under IN type
                            whereMain.in(key, (List<?>) query.getFilters().get(key));
                            whereCount++;
                            break;
                        }
                    }
                } else if (query.getFilters().get(key) instanceof AllowedResources) {
                    if (query.getFilters().get(AllowedResources.KEY_ALLOWED_RESOURCE_TYPE) == null) {
                        _logger.error("'{}' is a mandetary field, when '{}' is defined!",
                                AllowedResources.KEY_ALLOWED_RESOURCE_TYPE, AllowedResources.KEY_ALLOWED_RESOURCES);
                        continue;
                    }
                    //Add resource filter for gateways, nodes, sensors
                    allowedResources = (AllowedResources) query.getFilters().get(key);
                    allowedResourceType = (RESOURCE_TYPE) query.getFilters().get(
                            AllowedResources.KEY_ALLOWED_RESOURCE_TYPE);
                } else {
                    whereMain.eq(key, query.getFilters().get(key));
                    whereCount++;
                }
            }
        }

        //Set filtered count result
        QueryBuilder<Tdao, Tid> queryBuilderFilteredCount = this.getDao().queryBuilder();
        if (whereCount != 0) {
            if (query.isAndQuery()) {
                whereMain.and(whereCount);
            } else {
                whereMain.or(whereCount);
            }
        } else if (allowedResourceType != null) {
            int count = addResourcesFilter(allowedResources, allowedResourceType, whereMain);
            if (count > 0) {
                whereMain.and(count);
            }
        } else {
            whereMain = null;
        }
        if (whereMain != null) {
            queryBuilderFilteredCount.setWhere(whereMain);
        }
        query.setFilteredCount(queryBuilderFilteredCount.countOf());

        // Add total count
        //-----------------
        QueryBuilder<Tdao, Tid> totalItemsBuilder = this.getDao().queryBuilder();
        int totalItemsAndCount = 0;
        if (query.getTotalCountAltColumn() != null && query.getFilters().get(query.getTotalCountAltColumn()) != null) {
            totalItemsBuilder.where().eq(query.getTotalCountAltColumn(),
                    query.getFilters().get(query.getTotalCountAltColumn()));
            totalItemsAndCount++;
        } else {
            List<Object> idColumnList = (List<Object>) query.getFilters().get(query.getIdColumn());
            if (idColumnList != null && !idColumnList.isEmpty()) {
                totalItemsBuilder.where().in(query.getIdColumn(),
                        (List<?>) query.getFilters().get(query.getIdColumn()));
                totalItemsAndCount++;
            }
        }

        if (allowedResourceType != null) {
            totalItemsAndCount += addResourcesFilter(allowedResources, allowedResourceType, totalItemsBuilder.where());
        }

        if (totalItemsAndCount > 1) {
            totalItemsBuilder.where().and(totalItemsAndCount);
            query.setTotalItems(totalItemsBuilder.countOf());
        } else if (totalItemsAndCount != 0) {
            query.setTotalItems(totalItemsBuilder.countOf());
        } else {
            query.setTotalItems(this.getDao().countOf());
        }
        //-----------------

        if (whereMain != null) {
            queryBuilder.setWhere(whereMain);
        }

        //Update offset and limit
        if (query.getPageLimit() > 0) {
            queryBuilder.limit(query.getPageLimit());
        }
        if (query.getStartingRow() > 0) {
            queryBuilder.offset(query.getStartingRow());
        }

        if (query.isOrderByRaw()) {
            queryBuilder.orderByRaw(query.getOrderBy() + query.getOrder());
        } else {
            queryBuilder.orderBy(query.getOrderBy(), query.getOrder().equalsIgnoreCase(Query.ORDER_ASC));
        }

        //Remove allowed resources from query, to avoid send list to user
        query.getFilters().put(AllowedResources.KEY_ALLOWED_RESOURCES, null);
        return QueryResponse.builder().data(queryBuilder.query()).query(query).build();
    }

    //Create new item
    public void create(Tdao tdao) {
        try {
            Integer count = this.getDao().create(tdao);
            _logger.debug("Created new item:[{}], Create count:{}", tdao, count);
        } catch (SQLException ex) {
            _logger.error("unable to add new item:[{}]", tdao, ex);
        }
    }

    //Create or update item
    public void createOrUpdate(Tdao tdao) {
        try {
            CreateOrUpdateStatus status = this.getDao().createOrUpdate(tdao);
            _logger.debug("CreateOrUpdate item:[{}],Create:{},Update:{},Lines Changed:{}",
                    tdao, status.isCreated(), status.isUpdated(),
                    status.getNumLinesChanged());
        } catch (SQLException ex) {
            _logger.error("unable to CreateOrUpdate item:[{}]", tdao, ex);
        }
    }

    //delete item
    public void delete(Tdao tdao) {
        try {
            Integer count = this.getDao().delete(tdao);
            _logger.debug("item:[{}] deleted, Delete count:{}", tdao, count);
        } catch (SQLException ex) {
            _logger.error("unable to delete item:[{}]", tdao, ex);
        }
    }

    //Update item
    public void update(Tdao tdao) {
        try {
            Integer count = this.getDao().update(tdao);
            _logger.debug("Updated item:[{}], Update count:{}", tdao, count);
        } catch (SQLException ex) {
            _logger.error("unable to update item:[{}]", tdao, ex);
        }
    }

    public void updateId(Tdao tdao, Tid tid) {
        try {
            Integer count = this.getDao().updateId(tdao, tid);
            _logger.debug("Updated item:[{}, id:{}], Update count:{}", tdao, tid, count);
        } catch (SQLException ex) {
            _logger.error("unable to update item:[{}]", tdao, ex);
        }
    }

    //Update items with out where condition
    public void updateBulk(String setColName, Object setColValue) {
        this.updateBulk(setColName, setColValue, null, null);
    }

    //Update items with where condition.
    public void updateBulk(String setColName, Object setColValue, String whereColName, Object whereColValue) {
        try {
            UpdateBuilder<Tdao, Tid> updateBuilder = this.getDao().updateBuilder();
            updateBuilder.updateColumnValue(setColName, setColValue);
            if (whereColName != null) {
                if (whereColValue != null) {
                    updateBuilder.where().eq(whereColName, whereColValue);
                } else {
                    updateBuilder.where().isNull(whereColName);
                }
            }
            Integer updateCount = updateBuilder.update();
            _logger.debug("Updated column[{}] with value[{}] where column[{}] == value[{}], Updated row count:{}",
                    setColName, setColValue, whereColName, whereColValue, updateCount);
        } catch (SQLException ex) {
            _logger.error("unable to update column[{}] with value[{}] where column[{}] == value[{}]", setColName,
                    setColValue, whereColName, whereColValue, ex);
        }
    }

    //Get all items
    public List<Tdao> getAll() {
        try {
            return this.getDao().queryForAll();
        } catch (SQLException ex) {
            _logger.error("unable to get all items", ex);
            return null;
        }
    }

    //Get item with id
    public Tdao getById(Tid id) {
        try {
            return this.getDao().queryForId(id);
        } catch (SQLException ex) {
            _logger.error("unable to get item[id:{}]", id, ex);
            return null;
        }
    }

    public void deleteByIds(List<Tid> ids) {
        try {
            Integer count = this.getDao().deleteIds(ids);
            _logger.debug("Ids:[{}] deleted, Delete count:{}", ids, count);
        } catch (SQLException ex) {
            _logger.error("unable to delete Ids:[{}]", ids, ex);
        }
    }

    public void deleteById(Tid id) {
        try {
            this.getDao().deleteById(id);
        } catch (SQLException ex) {
            _logger.error("unable to delete item, id:[{}]", id, ex);
        }
    }

    public void delete(String key, Object value) {
        try {
            DeleteBuilder<Tdao, Tid> deleteBuilder = this.getDao().deleteBuilder();
            deleteBuilder.where().eq(key, value);
            int deleteCount = deleteBuilder.delete();
            _logger.debug("Deleted count:{}, for key:{}, value:{}", deleteCount, key, value);
        } catch (SQLException ex) {
            _logger.error("unable to delete item, key:{}, value:{}", key, value, ex);
        }
    }

    public void delete(String key, List<?> values) {
        try {
            DeleteBuilder<Tdao, Tid> deleteBuilder = this.getDao().deleteBuilder();
            deleteBuilder.where().in(key, values);
            int deleteCount = deleteBuilder.delete();
            _logger.debug("Deleted count:{}, for key:{}, values:{}", deleteCount, key, values);
        } catch (SQLException ex) {
            _logger.error("unable to delete item, key:{}, values:{}", key, values, ex);
        }
    }

    public void delete(HashMap<String, Object> map) {
        try {
            DeleteBuilder<Tdao, Tid> deleteBuilder = this.getDao().deleteBuilder();
            Where<Tdao, Tid> where = deleteBuilder.where();
            int whereCount = 0;
            for (String key : map.keySet()) {
                if (map.get(key) instanceof List) {
                    where.in(key, map.get(key));
                    whereCount++;
                } else {
                    where.eq(key, map.get(key));
                    whereCount++;
                }
            }
            if (whereCount > 1) {
                where.and(whereCount);
            }
            deleteBuilder.setWhere(where);
            int deleteCount = deleteBuilder.delete();
            _logger.debug("Deleted count:{}, for map:{}", deleteCount, map);
        } catch (SQLException ex) {
            _logger.error("unable to delete item, map:{}", map, ex);
        }
    }

    public List<Tdao> getAll(String key, List<Tid> ids) {
        try {
            if (ids != null && !ids.isEmpty()) {
                return this.getDao().queryBuilder().where().in(key, ids).query();
            }
            return new ArrayList<Tdao>();
        } catch (SQLException ex) {
            _logger.error("unable to get all items ids:{}", ids, ex);
            return null;
        }
    }

    public List<Tdao> getAll(String key, Object value) {
        try {
            return this.getDao().queryBuilder().where().eq(key, value).query();
        } catch (SQLException ex) {
            _logger.error("unable to get all items key:{}, value:{}", key, value, ex);
            return null;
        }
    }

    public Tdao get(String key, Object value) {
        try {
            return this.getDao().queryBuilder().where().eq(key, value).queryForFirst();
        } catch (SQLException ex) {
            _logger.error("unable to get all items key:{}, value:{}", key, value, ex);
            return null;
        }
    }

    public Long countOf() {
        try {
            return this.getDao().countOf();
        } catch (SQLException ex) {
            _logger.error("unable to get count,", ex);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public long countOf(String key, Object data) {
        try {
            if (data instanceof List) {
                return this.getDao().queryBuilder().where().in(key, ((List<Object>) data)).countOf();
            } else {
                return this.getDao().queryBuilder().where().eq(key, data).countOf();
            }
        } catch (SQLException ex) {
            _logger.error("unable to get count key:{}, data:{}", key, data, ex);
            return 0;
        }
    }

    public long countOf(HashMap<String, Object> columnValues) {
        try {
            QueryBuilder<Tdao, Tid> queryBuilder = this.getDao().queryBuilder();
            Where<Tdao, Tid> where = queryBuilder.where();
            int andCount = 0;
            for (String key : columnValues.keySet()) {
                if (columnValues.get(key) instanceof List<?>) {
                    where.in(key, columnValues.get(key));
                } else {
                    where.eq(key, columnValues.get(key));
                }
                andCount++;
            }
            if (andCount > 1) {
                where.and(andCount);
            }
            queryBuilder.setWhere(where);
            return queryBuilder.countOf();
        } catch (SQLException ex) {
            _logger.error("unable to get count for query, input[{}]", columnValues, ex);
        }
        return 0;
    }

    @SuppressWarnings("unchecked")
    public List<Tdao> getAllData(Query query) {
        try {
            QueryResponse response = getQueryResponse(query);
            if (response.getData() != null) {
                return (List<Tdao>) response.getData();
            }
        } catch (SQLException ex) {
            _logger.error("Error while processing for {}", query, ex);
        }

        return new ArrayList<Tdao>();
    }

    protected void updatePurgeCondition(Where<?, Object> where, String key, Object value,
            OPERATOR operator) throws SQLException {
        switch (operator) {
            case EQ:
                where.eq(key, value);
                break;
            case GT:
                where.gt(key, value);
                break;
            case LT:
                where.lt(key, value);
                break;
            case GE:
                where.ge(key, value);
                break;
            case LE:
                where.le(key, value);
                break;
            case NE:
                where.ne(key, value);
                break;
            default:
                where.eq(key, value);
                break;
        }
    }

}
