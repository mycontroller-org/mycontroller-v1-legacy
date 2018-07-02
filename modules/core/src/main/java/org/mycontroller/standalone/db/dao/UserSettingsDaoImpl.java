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

import org.mycontroller.standalone.db.tables.User;
import org.mycontroller.standalone.db.tables.UserSettings;

import com.j256.ormlite.dao.Dao.CreateOrUpdateStatus;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Slf4j
public class UserSettingsDaoImpl extends BaseAbstractDaoImpl<UserSettings, Integer> implements UserSettingsDao {

    public UserSettingsDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, UserSettings.class);
    }

    @Override
    public void create(UserSettings userSettings) {
        try {
            int count = this.getDao().create(userSettings);
            _logger.debug("Created UserSettings:[{}], Create count:{}", userSettings, count);

        } catch (SQLException ex) {
            _logger.error("unable to add UserSettings:[{}]", userSettings, ex);
        }
    }

    @Override
    public void createOrUpdate(UserSettings userSettings) {
        try {
            CreateOrUpdateStatus status = this.getDao().createOrUpdate(userSettings);
            _logger.debug("CreateOrUpdate UserSettings:[{}],Create:{},Update:{},Lines Changed:{}",
                    userSettings, status.isCreated(), status.isUpdated(),
                    status.getNumLinesChanged());
        } catch (SQLException ex) {
            _logger.error("unable to CreateOrUpdate UserSettings:[{}]", userSettings, ex);
        }
    }

    @Override
    public void delete(UserSettings userSettings) {
        try {
            int count = this.getDao().delete(userSettings);
            _logger.debug("UserSettings:[{}] deleted, Delete count:{}", userSettings, count);

        } catch (SQLException ex) {
            _logger.error("unable to delete UserSettings:[{}]", userSettings, ex);
        }
    }

    @Override
    public void delete(User user, String key) {
        delete(new UserSettings(user, key));
    }

    @Override
    public void update(UserSettings userSettings) {
        try {
            this.getDao().update(userSettings);
        } catch (SQLException ex) {
            _logger.error("unable to update UserSettings:[{}]", userSettings, ex);
        }

    }

    @Override
    public List<UserSettings> getAll() {
        try {
            return this.getDao().queryBuilder().orderBy("key", true).orderBy("id", true).query();
        } catch (SQLException ex) {
            _logger.error("unable to get all list", ex);
            return null;
        }
    }

    @Override
    public List<UserSettings> get(User user, List<String> keys) {
        try {
            QueryBuilder<UserSettings, Integer> queryBuilder = getDao().queryBuilder();
            queryBuilder.where().in("key", keys);
            return queryBuilder.orderBy("id", true).query();
        } catch (SQLException ex) {
            _logger.error("unable to get list:[{}]", keys, ex);
            return null;
        }
    }

    @Override
    public List<UserSettings> getLike(User user, String key) {
        try {
            QueryBuilder<UserSettings, Integer> queryBuilder = getDao().queryBuilder();
            queryBuilder.where().like("key", key);
            return queryBuilder.orderBy("id", true).query();
        } catch (SQLException ex) {
            _logger.error("unable to get like:[{}]", key, ex);
            return null;
        }

    }

    @Override
    public UserSettings get(UserSettings userSettings) {
        return this.get(userSettings.getUser(), userSettings.getKey());
    }

    @Override
    public UserSettings get(User user, String key) {
        try {
            QueryBuilder<UserSettings, Integer> queryBuilder = getDao().queryBuilder();
            return queryBuilder.where().eq("key", key).queryForFirst();
        } catch (SQLException ex) {
            _logger.error("unable to get:[key:{}]", key, ex);
            return null;
        }
    }
}