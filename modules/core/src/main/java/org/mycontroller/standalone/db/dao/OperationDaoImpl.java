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
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.OperationTable;

import com.j256.ormlite.support.ConnectionSource;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Slf4j
public class OperationDaoImpl extends BaseAbstractDaoImpl<OperationTable, Integer> implements OperationDao {

    public OperationDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, OperationTable.class);
    }

    @Override
    public List<OperationTable> getAll(List<Integer> ids) {
        return super.getAll(OperationTable.KEY_ID, ids);
    }

    @Override
    public OperationTable get(OperationTable operationTable) {
        return super.getById(operationTable.getId());
    }

    @Override
    public OperationTable getByName(String notificationName) {
        List<OperationTable> operationTables = super.getAll(OperationTable.KEY_NAME, notificationName);
        if (operationTables != null && !operationTables.isEmpty()) {
            return operationTables.get(0);
        }
        return null;
    }

    @Override
    public QueryResponse getAll(Query query) {
        try {
            query.setIdColumn(OperationTable.KEY_ID);
            return super.getQueryResponse(query);
        } catch (SQLException ex) {
            _logger.error("unable to run query:[{}]", query, ex);
            return null;
        }
    }

    @Override
    public List<OperationTable> getByRuleDefinitionId(Integer ruleDefinitionId) {
        List<Integer> ids = DaoUtils.getOperationRuleDefinitionMapDao().getOperationIdsByRuleDefinitionId(
                ruleDefinitionId);
        return super.getAll(OperationTable.KEY_ID, ids);
    }

    @Override
    public List<OperationTable> getByRuleDefinitionIdEnabled(Integer ruleDefinitionId) {
        List<Integer> ids = DaoUtils.getOperationRuleDefinitionMapDao().getOperationIdsByRuleDefinitionId(
                ruleDefinitionId);
        try {
            return this.getDao().queryBuilder().where().in(OperationTable.KEY_ID, ids).and()
                    .eq(OperationTable.KEY_ENABLED, true).query();
        } catch (SQLException ex) {
            _logger.error("Exception,", ex);
        }
        return null;
    }

    @Override
    public List<OperationTable> getByTimerId(Integer timerId) {
        List<Integer> ids = DaoUtils.getOperationTimerMapDao().getOperationIdsByTimerId(timerId);
        return super.getAll(OperationTable.KEY_ID, ids);
    }

    @Override
    public List<OperationTable> getByTimerIdEnabled(Integer timerId) {
        List<Integer> ids = DaoUtils.getOperationTimerMapDao().getOperationIdsByTimerId(timerId);
        try {
            return this.getDao().queryBuilder().where().in(OperationTable.KEY_ID, ids).and()
                    .eq(OperationTable.KEY_ENABLED, true).query();
        } catch (SQLException ex) {
            _logger.error("Exception,", ex);
        }
        return null;
    }

}
