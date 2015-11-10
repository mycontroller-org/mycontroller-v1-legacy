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

import org.mycontroller.standalone.db.tables.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.j256.ormlite.dao.Dao.CreateOrUpdateStatus;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class SettingsDaoImpl extends BaseAbstractDao<Settings, Integer> implements SettingsDao {
    private static final Logger _logger = LoggerFactory.getLogger(SettingsDaoImpl.class);

    public SettingsDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, Settings.class);
    }

    @Override
    public void create(Settings settings) {
        try {
            int count = this.getDao().create(settings);
            _logger.debug("Created Settings:[{}], Create count:{}", settings, count);

        } catch (SQLException ex) {
            _logger.error("unable to add Settings:[{}]", settings, ex);
        }
    }

    @Override
    public void createOrUpdate(Settings settings) {
        try {
            CreateOrUpdateStatus status = this.getDao().createOrUpdate(settings);
            _logger.debug("CreateOrUpdate Settings:[{}],Create:{},Update:{},Lines Changed:{}",
                    settings, status.isCreated(), status.isUpdated(),
                    status.getNumLinesChanged());
        } catch (SQLException ex) {
            _logger.error("unable to CreateOrUpdate Settings:[{}]", settings, ex);
        }
    }

    @Override
    public void delete(Settings settings) {
        try {
            int count = this.getDao().delete(settings);
            _logger.debug("Settings:[{}] deleted, Delete count:{}", settings, count);

        } catch (SQLException ex) {
            _logger.error("unable to delete Settings:[{}]", settings, ex);
        }
    }

    @Override
    public void delete(String key) {
        delete(new Settings(key));
    }

    @Override
    public void update(Settings settings) {
        try {
            this.getDao().update(settings);
        } catch (SQLException ex) {
            _logger.error("unable to update Settings:[{}]", settings, ex);
        }

    }

    @Override
    public List<Settings> getAll() {
        try {
            return this.getDao().queryBuilder().orderBy("key", true).orderBy("id", true).query();
        } catch (SQLException ex) {
            _logger.error("unable to get all list", ex);
            return null;
        }
    }

    @Override
    public List<Settings> get(List<String> keys) {
        try {
            QueryBuilder<Settings, Integer> queryBuilder = getDao().queryBuilder();
            queryBuilder.where().in("key", keys);
            return queryBuilder.orderBy("id", true).query();
        } catch (SQLException ex) {
            _logger.error("unable to get list:[{}]", keys, ex);
            return null;
        }
    }

    public List<Settings> getLike(String key) {
        try {
            QueryBuilder<Settings, Integer> queryBuilder = getDao().queryBuilder();
            queryBuilder.where().like("key", key);
            return queryBuilder.orderBy("id", true).query();
        } catch (SQLException ex) {
            _logger.error("unable to get like:[{}]", key, ex);
            return null;
        }

    }

    @Override
    public Settings get(Settings settings) {
        return this.get(settings.getKey());
    }

    @Override
    public Settings get(String key) {
        try {
            QueryBuilder<Settings, Integer> queryBuilder = getDao().queryBuilder();
            return queryBuilder.where().eq("key", key).queryForFirst();
        } catch (SQLException ex) {
            _logger.error("unable to get:[key:{}]", key, ex);
            return null;
        }
    }
}