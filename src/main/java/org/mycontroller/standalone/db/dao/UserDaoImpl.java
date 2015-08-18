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

import org.mycontroller.standalone.db.tables.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.j256.ormlite.dao.Dao.CreateOrUpdateStatus;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class UserDaoImpl extends BaseAbstractDao<User, Integer> implements UserDao {
    private static final Logger _logger = LoggerFactory.getLogger(UserDaoImpl.class);

    public UserDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, User.class);
    }

    @Override
    public void create(User user) {
        try {
            int count = this.getDao().create(user);
            _logger.debug("Created User:[{}], Create count:{}", user, count);
        } catch (SQLException ex) {
            _logger.error("unable to add User:[{}]", user, ex);
        }
    }

    @Override
    public void createOrUpdate(User user) {
        try {
            CreateOrUpdateStatus status = this.getDao().createOrUpdate(user);
            _logger.debug("CreateOrUpdate User:[{}],Create:{},Update:{},Lines Changed:{}",
                    user, status.isCreated(), status.isUpdated(),
                    status.getNumLinesChanged());
        } catch (SQLException ex) {
            _logger.error("unable to CreateOrUpdate User:[{}]", user, ex);
        }
    }

    @Override
    public void delete(User user) {
        try {
            int count = this.getDao().delete(user);
            _logger.debug("User:[{}] deleted, Delete count:{}", user, count);
        } catch (SQLException ex) {
            _logger.error("unable to delete user:[{}]", user, ex);
        }
    }

    @Override
    public void delete(int userId) {
        User user = new User(userId);
        this.delete(user);
    }

    @Override
    public void update(User user) {
        try {
            int count = this.getDao().update(user);
            _logger.debug("Updated User:[{}], Update count:{}", user, count);
        } catch (SQLException ex) {
            _logger.error("unable to update user:[{}]", user, ex);
        }

    }

    @Override
    public List<User> getAll() {
        try {
            return this.getDao().queryForAll();
        } catch (SQLException ex) {
            _logger.error("unable to get all Nodes", ex);
            return null;
        }
    }

    @Override
    public User get(User user) {
        if (user.getId() != null) {
            return this.get(user.getId());
        } else {
            try {
                QueryBuilder<User, Integer> queryBuilder = this.getDao().queryBuilder();
                if (user.getName() != null) {
                    queryBuilder.where().eq(User.NAME, user.getName());
                } else if (user.getEmail() != null) {
                    queryBuilder.where().eq(User.EMAIL, user.getEmail());
                }
                List<User> users = this.getDao().query(queryBuilder.prepare());
                if (users.size() > 0) {
                    return users.get(0);
                } else {
                    return null;
                }
            } catch (SQLException ex) {
                _logger.error("unable to get, user:{}", user, ex);
            }
            return null;
        }
    }

    @Override
    public User get(int userId) {
        try {
            return this.getDao().queryForId(userId);
        } catch (SQLException ex) {
            _logger.error("unable to get User", ex);
            return null;
        }
    }
    
    @Override
    public User get(String userName) {
        return this.get(new User(userName));
    }

}
