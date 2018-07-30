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
import org.mycontroller.standalone.db.tables.Settings;

import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Slf4j
public class SettingsDaoImpl extends BaseAbstractDaoImpl<Settings, Integer> implements SettingsDao {

    public SettingsDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, Settings.class);
    }

    @Override
    public Settings get(Settings settings) {
        return getById(settings.getId());
    }

    @Override
    public List<Settings> getAll(List<Integer> ids) {
        return getAll(Settings.KEY_ID, ids);
    }

    @Override
    public Settings get(Integer userId, String key, String subKey) {
        try {
            Where<Settings, Integer> where = this.getDao().queryBuilder().where();
            int andCount = 0;
            if (userId == null) {
                where.isNull(Settings.KEY_USER_ID);
                andCount++;
            } else {
                where.eq(Settings.KEY_USER_ID, userId);
                andCount++;
            }
            where.eq(Settings.KEY_KEY, key);
            andCount++;
            where.eq(Settings.KEY_SUB_KEY, subKey);
            andCount++;
            where.and(andCount);
            QueryBuilder<Settings, Integer> queryBuilder = this.getDao().queryBuilder();
            queryBuilder.setWhere(where);
            return queryBuilder.queryForFirst();
        } catch (SQLException ex) {
            _logger.error("unable to get item for userId:{}, key:{}, subKey:{}", userId, key, subKey, ex);
            return null;
        }
    }

    @Override
    public void update(String key, String subKey, String value, String altValue) {
        try {
            //Create it if not available
            createIfNotAvailable(key, subKey);
            UpdateBuilder<Settings, Integer> updateBuilder = this.getDao().updateBuilder();
            updateBuilder.where().eq(Settings.KEY_KEY, key).and().eq(Settings.KEY_SUB_KEY, subKey);
            updateBuilder.updateColumnValue(Settings.KEY_VALUE, value);
            updateBuilder.updateColumnValue(Settings.KEY_ALT_VALUE, altValue);
            int count = updateBuilder.update();
            _logger.debug("update count:{}", count);
        } catch (SQLException ex) {
            _logger.error("unable to get item for key:{}, subKey:{}", key, subKey, ex);
        }
    }

    @Override
    public void update(String key, String subKey, String value) {
        try {
            //Create it if not available
            createIfNotAvailable(key, subKey);
            UpdateBuilder<Settings, Integer> updateBuilder = this.getDao().updateBuilder();
            updateBuilder.where().eq(Settings.KEY_KEY, key).and().eq(Settings.KEY_SUB_KEY, subKey);
            updateBuilder.updateColumnValue(Settings.KEY_VALUE, value);
            int count = updateBuilder.update();
            _logger.debug("update count:{}", count);
        } catch (SQLException ex) {
            _logger.error("unable to get item for key:{}, subKey:{}", key, subKey, ex);
        }
    }

    private void createIfNotAvailable(String key, String subKey) {
        if (get(null, key, subKey) == null) {
            create(Settings.builder().key(key).subKey(subKey).build());
        }
    }

    @Override
    public List<Settings> getAll(Integer userId, String key) {
        try {
            return this.getDao().queryBuilder().where().eq(Settings.KEY_USER_ID, userId).and()
                    .eq(Settings.KEY_KEY, key).query();
        } catch (SQLException ex) {
            _logger.error("unable to get item for userId:{}, Key:{}", userId, key, ex);
            return null;
        }
    }

    @Override
    public QueryResponse getAll(Query query, String isAlterdTotalCountKey) {
        try {
            query.setIdColumn(Settings.KEY_ID);
            query.setTotalCountAltColumn(isAlterdTotalCountKey);
            return this.getQueryResponse(query);
        } catch (SQLException ex) {
            _logger.error("unable to run query:[{}]", query, ex);
            return null;
        }
    }
}