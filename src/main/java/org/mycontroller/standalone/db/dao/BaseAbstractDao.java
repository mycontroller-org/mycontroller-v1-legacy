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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public abstract class BaseAbstractDao<Tdao, Tid> {
    public static final Logger _logger = LoggerFactory.getLogger(BaseAbstractDao.class);

    private Dao<Tdao, Tid> dao;

    @SuppressWarnings("unchecked")
    public BaseAbstractDao(ConnectionSource connectionSource, Class<Tdao> entity) throws SQLException {
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
}
