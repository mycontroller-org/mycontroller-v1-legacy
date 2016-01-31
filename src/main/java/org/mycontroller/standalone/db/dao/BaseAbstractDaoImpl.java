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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.mycontroller.standalone.api.jaxrs.mapper.Query;
import org.mycontroller.standalone.api.jaxrs.mapper.QueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.Dao.CreateOrUpdateStatus;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public abstract class BaseAbstractDaoImpl<Tdao, Tid> {
    public static final Logger _logger = LoggerFactory.getLogger(BaseAbstractDaoImpl.class);

    private Dao<Tdao, Tid> dao;

    @SuppressWarnings("unchecked")
    public BaseAbstractDaoImpl(ConnectionSource connectionSource, Class<Tdao> entity) throws SQLException {
        dao = (Dao<Tdao, Tid>) DaoManager.createDao(connectionSource, entity);
        //Enable Auto commit
        //dao.setAutoCommit(connectionSource.getReadWriteConnection(), true);
        //Create Table if not exists
        TableUtils.createTableIfNotExists(connectionSource, entity);
        _logger.debug("Create Table If Not Exists, executed for {}", entity.getName());
    }

    public Dao<Tdao, Tid> getDao() {
        return dao;
    }

    public QueryResponse getQueryResponse(Query query, String idColumn, String isAlterdTotalCountKey)
            throws SQLException {
        QueryBuilder<Tdao, Tid> queryBuilder = this.getDao().queryBuilder();
        Where<Tdao, Tid> where = this.getDao().queryBuilder().where();
        //where.isNotNull(idColumn);
        boolean addAnd = false;
        for (String key : query.getFilters().keySet()) {
            if (query.getFilters().get(key) != null) {
                if (query.getFilters().get(key) instanceof List<?>) {
                    for (Object value : (List<?>) query.getFilters().get(key)) {
                        if (value instanceof String) {//If it's string add one by one
                            if (addAnd) {
                                where.and();
                            }
                            where.like(key, "%" + value + "%");
                            addAnd = true;
                        } else {//If it's integer, float, long, etc., add it under IN type
                            if (addAnd) {
                                where.and();
                            }
                            where.in(key, (List<?>) query.getFilters().get(key));
                            addAnd = true;
                            break;
                        }
                    }
                } else {
                    if (addAnd) {
                        where.and();
                    }
                    where.eq(key, query.getFilters().get(key));
                    addAnd = true;
                }
            }
        }

        //Set filtered count result
        QueryBuilder<Tdao, Tid> queryBuilderFilteredCount = this.getDao().queryBuilder();
        if (addAnd) {
            queryBuilderFilteredCount.setWhere(where);
        }
        query.setFilteredCount(queryBuilderFilteredCount.countOf());

        //Total count
        QueryBuilder<Tdao, Tid> totalItemsBuilder = this.getDao().queryBuilder();
        if (isAlterdTotalCountKey != null) {
            if (query.getFilters().get(isAlterdTotalCountKey) != null) {
                totalItemsBuilder.where().eq(isAlterdTotalCountKey, query.getFilters().get(isAlterdTotalCountKey));
                query.setTotalItems(totalItemsBuilder.countOf());
            } else {
                query.setTotalItems(this.getDao().countOf());
            }
        } else {
            if (query.getFilters().get(idColumn) != null) {
                totalItemsBuilder.where().in(idColumn, (List<?>) query.getFilters().get(idColumn));
                query.setTotalItems(totalItemsBuilder.countOf());
            } else {
                query.setTotalItems(this.getDao().countOf());
            }
        }
        if (addAnd) {
            queryBuilder.setWhere(where);
        }

        queryBuilder.offset(query.getStartingRow()).limit(query.getPageLimit())
                .orderBy(query.getOrderBy(), query.getOrder().equalsIgnoreCase(Query.ORDER_ASC));
        return QueryResponse.builder().data(queryBuilder.query()).query(query).build();
    }

    public QueryResponse getQueryResponse(Query query, String idColumn)
            throws SQLException {
        return this.getQueryResponse(query, idColumn, null);
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

    public void delete(String key, List<Object> values) {
        try {
            DeleteBuilder<Tdao, Tid> deleteBuilder = this.getDao().deleteBuilder();
            deleteBuilder.where().in(key, values);
            int deleteCount = deleteBuilder.delete();
            _logger.debug("Deleted count:{}, for key:{}, values:{}", deleteCount, key, values);
        } catch (SQLException ex) {
            _logger.error("unable to delete item, key:{}, values:{}", key, values, ex);
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
            _logger.error("unable to get all items value:{}", value, ex);
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

    public Long countOf(String key, Object value) {
        try {
            return this.getDao().queryBuilder().where().eq(key, value).countOf();
        } catch (SQLException ex) {
            _logger.error("unable to get count key:{}, value:{}", key, value, ex);
            return null;
        }
    }

    public long countOf(HashMap<String, List<Object>> columnValues) {
        try {
            QueryBuilder<Tdao, Tid> queryBuilder = this.getDao().queryBuilder();
            Where<Tdao, Tid> where = queryBuilder.where();
            boolean appendAND = false;
            for (String key : columnValues.keySet()) {
                if (appendAND) {
                    where.and();
                } else {
                    appendAND = true;
                }
                where.in(key, columnValues.get(key));
            }
            return queryBuilder.countOf();
        } catch (SQLException ex) {
            _logger.error("unable to get count for query, input[{}]", columnValues, ex);
        }
        return 0;
    }

}
