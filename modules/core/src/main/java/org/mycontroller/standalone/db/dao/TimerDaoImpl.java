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
import org.mycontroller.standalone.db.tables.RuleDefinitionTable;
import org.mycontroller.standalone.db.tables.Timer;

import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Slf4j
public class TimerDaoImpl extends BaseAbstractDaoImpl<Timer, Integer> implements TimerDao {
    public TimerDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, Timer.class);
    }

    @Override
    public List<Timer> getAllEnabled() {
        try {
            QueryBuilder<Timer, Integer> queryBuilder = this.getDao().queryBuilder();
            queryBuilder.where().eq(Timer.KEY_ENABLED, true);
            List<Timer> timers = this.getDao().query(queryBuilder.prepare());
            return timers;
        } catch (SQLException ex) {
            _logger.error("unable to get all enabled timers", ex);
            return null;
        }
    }

    @Override
    public QueryResponse getAll(Query query) {
        try {
            query.setIdColumn(Timer.KEY_ID);
            return this.getQueryResponse(query);
        } catch (SQLException ex) {
            _logger.error("unable to run query:[{}]", query, ex);
            return null;
        }
    }

    @Override
    public List<Timer> getAll(List<Integer> ids) {
        return getAll(Timer.KEY_ID, ids);
    }

    @Override
    public Timer get(Timer timer) {
        return super.getById(timer.getId());
    }

    @Override
    public Timer getByName(String name) {
        try {
            return this.getDao().queryBuilder().where().eq(RuleDefinitionTable.KEY_NAME, name).queryForFirst();
        } catch (SQLException ex) {
            _logger.error("unable to get timer:{},", name, ex);
        }
        return null;
    }

}
