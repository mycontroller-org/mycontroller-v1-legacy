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
import org.mycontroller.standalone.db.tables.User;

import com.j256.ormlite.support.ConnectionSource;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Slf4j
public class UserDaoImpl extends BaseAbstractDaoImpl<User, Integer> implements UserDao {

    public UserDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, User.class);
    }

    @Override
    public User get(User user) {
        return super.getById(user.getId());
    }

    @Override
    public List<User> getAll(List<Integer> ids) {
        return super.getAll(User.KEY_ID, ids);
    }

    @Override
    public User getByUsername(String userName) {
        try {
            List<User> users = this.getDao().queryForEq(User.KEY_USER_NAME, userName);
            if (users != null && !users.isEmpty()) {
                return users.get(0);
            }
        } catch (SQLException ex) {
            _logger.error("Error,", ex);
        }
        return null;
    }

    @Override
    public QueryResponse getAll(Query query) {
        try {
            query.setIdColumn(User.KEY_ID);
            return super.getQueryResponse(query);
        } catch (SQLException ex) {
            _logger.error("unable to run query:[{}]", query, ex);
            return null;
        }
    }
}
