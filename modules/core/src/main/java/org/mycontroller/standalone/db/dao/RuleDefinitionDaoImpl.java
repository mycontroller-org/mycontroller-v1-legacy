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
import java.util.List;

import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.api.jaxrs.model.Query;
import org.mycontroller.standalone.api.jaxrs.model.QueryResponse;
import org.mycontroller.standalone.db.tables.RuleDefinitionTable;
import org.mycontroller.standalone.rule.RuleUtils.DAMPENING_TYPE;

import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.support.ConnectionSource;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Slf4j
public class RuleDefinitionDaoImpl extends BaseAbstractDaoImpl<RuleDefinitionTable, Integer> implements
        RuleDefinitionDao {
    public RuleDefinitionDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, RuleDefinitionTable.class);
    }

    @Override
    public List<RuleDefinitionTable> getAllEnabled() {
        return super.getAll(RuleDefinitionTable.KEY_ENABLED, true);
    }

    @Override
    public void disableAllTriggered() {
        try {
            UpdateBuilder<RuleDefinitionTable, Integer> updateBuilder = getDao().updateBuilder();
            updateBuilder.updateColumnValue(RuleDefinitionTable.KEY_TRIGGERED, false).where()
                    .eq(RuleDefinitionTable.KEY_TRIGGERED, true);
            Integer count = updateBuilder.update();
            _logger.debug("Number of rows updated:[{}]", count);
        } catch (SQLException ex) {
            _logger.error("unable to update rule triggered status", ex);

        }
    }

    @Override
    public QueryResponse getAll(Query query) {
        try {
            query.setIdColumn(RuleDefinitionTable.KEY_ID);
            return this.getQueryResponse(query);
        } catch (SQLException ex) {
            _logger.error("unable to run query:[{}]", query, ex);
            return null;
        }
    }

    @Override
    public RuleDefinitionTable get(RuleDefinitionTable ruleDefinitionTable) {
        return this.getById(ruleDefinitionTable.getId());
    }

    @Override
    public List<RuleDefinitionTable> getAll(List<Integer> ids) {
        return getAll(RuleDefinitionTable.KEY_ID, ids);
    }

    @Override
    public List<RuleDefinitionTable> getAll(DAMPENING_TYPE dampeningType) {
        return getAll(RuleDefinitionTable.KEY_DAMPENING_TYPE, dampeningType);
    }

    @Override
    public RuleDefinitionTable getByName(String name) {
        try {
            return this.getDao().queryBuilder().where().eq(RuleDefinitionTable.KEY_NAME, name).queryForFirst();
        } catch (SQLException ex) {
            _logger.error("unable to get rule definition name:{},", name, ex);
        }
        return null;
    }

    @Override
    public List<RuleDefinitionTable> getAll(RESOURCE_TYPE resourceType, Integer resourceId) {
        List<RuleDefinitionTable> definitions = null;
        try {
            definitions = this.getDao().queryBuilder().where().eq(RuleDefinitionTable.KEY_RESOURCE_TYPE, resourceType)
                    .and()
                    .eq(RuleDefinitionTable.KEY_RESOURCE_ID, resourceId).query();
        } catch (SQLException ex) {
            _logger.error("unable to get rule definitions for ResourceTye:{}, ResourceId:{}", resourceType,
                    resourceId, ex);
        }
        if (definitions == null) {
            definitions = new ArrayList<RuleDefinitionTable>();
        }
        return definitions;

    }
}
