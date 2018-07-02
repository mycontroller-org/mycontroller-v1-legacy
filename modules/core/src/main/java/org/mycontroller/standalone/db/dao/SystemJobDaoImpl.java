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

import org.mycontroller.standalone.db.tables.SystemJob;

import com.j256.ormlite.support.ConnectionSource;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Slf4j
public class SystemJobDaoImpl extends BaseAbstractDaoImpl<SystemJob, Integer> implements SystemJobDao {

    public SystemJobDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, SystemJob.class);
    }

    @Override
    public List<SystemJob> getAllEnabled() {
        try {
            return this.getDao().queryBuilder().where()
                    .eq(SystemJob.KEY_ENABLED, true).query();
        } catch (SQLException ex) {
            _logger.error("Unable to get list of enabled systemjobs", ex);
            return null;
        }
    }

    @Override
    public List<SystemJob> getAll(List<Integer> ids) {
        return super.getAll(SystemJob.KEY_ID, ids);
    }

    @Override
    public SystemJob get(SystemJob tdao) {
        return super.getById(tdao.getId());
    }

}
