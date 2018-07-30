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

import org.mycontroller.standalone.db.tables.OperationRuleDefinitionMap;

import com.j256.ormlite.support.ConnectionSource;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Slf4j
public class OperationRuleDefinitionMapDaoImpl extends BaseAbstractDaoImpl<OperationRuleDefinitionMap, Object>
        implements OperationRuleDefinitionMapDao {

    public OperationRuleDefinitionMapDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, OperationRuleDefinitionMap.class);
    }

    @Override
    public OperationRuleDefinitionMap get(OperationRuleDefinitionMap tdao) {
        // not supported
        return null;
    }

    @Override
    public List<OperationRuleDefinitionMap> getAll(List<Object> ids) {
        // not supported
        return null;
    }

    @Override
    public List<OperationRuleDefinitionMap> getByRuleDefinitionId(Integer ruleDefinitionId) {
        return super.getAll(OperationRuleDefinitionMap.KEY_RULE_DEFINITION_ID, ruleDefinitionId);
    }

    @Override
    public List<OperationRuleDefinitionMap> getByOperationId(Integer operationId) {
        return super.getAll(OperationRuleDefinitionMap.KEY_OPERATION_ID, operationId);
    }

    @Override
    public void deleteByOperationId(Integer operationId) {
        super.delete(OperationRuleDefinitionMap.KEY_OPERATION_ID, operationId);
    }

    @Override
    public void deleteByRuleDefinitionId(Integer ruleDefinitionId) {
        super.delete(OperationRuleDefinitionMap.KEY_RULE_DEFINITION_ID, ruleDefinitionId);

    }

    @Override
    public List<Integer> getOperationIdsByRuleDefinitionId(Integer ruleDefinitionId) {
        List<Integer> roleIds = new ArrayList<Integer>();
        try {
            if (ruleDefinitionId != null) {
                List<OperationRuleDefinitionMap> ruleDefinitionOperationMaps = this.getDao().queryBuilder()
                        .where()
                        .eq(OperationRuleDefinitionMap.KEY_RULE_DEFINITION_ID, ruleDefinitionId).query();
                for (OperationRuleDefinitionMap ruleDefinitionOperationMap : ruleDefinitionOperationMaps) {
                    roleIds.add(ruleDefinitionOperationMap.getOperationTable().getId());
                }
            }
        } catch (SQLException ex) {
            _logger.error("Exception, ", ex);
        }
        return roleIds;
    }

}
