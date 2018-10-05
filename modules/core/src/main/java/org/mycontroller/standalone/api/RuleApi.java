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
package org.mycontroller.standalone.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.mycontroller.standalone.api.jaxrs.model.Query;
import org.mycontroller.standalone.api.jaxrs.model.QueryResponse;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.RuleDefinitionTable;
import org.mycontroller.standalone.rule.RuleUtils;
import org.mycontroller.standalone.rule.model.RuleDefinitionAbstract;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */

public class RuleApi {

    public RuleDefinitionTable getRaw(int id) {
        return DaoUtils.getRuleDefinitionDao().getById(id);
    }

    public QueryResponse getAllRaw(HashMap<String, Object> filters) {
        return DaoUtils.getRuleDefinitionDao().getAll(Query.get(filters));
    }

    public RuleDefinitionAbstract get(int id) {
        return RuleUtils.getRuleDefinition(getRaw(id));
    }

    public QueryResponse getAll(HashMap<String, Object> filters) {
        QueryResponse queryResponse = getAllRaw(filters);
        ArrayList<RuleDefinitionAbstract> gateways = new ArrayList<RuleDefinitionAbstract>();
        @SuppressWarnings("unchecked")
        List<RuleDefinitionTable> rows = (List<RuleDefinitionTable>) queryResponse.getData();
        for (RuleDefinitionTable row : rows) {
            gateways.add(RuleUtils.getRuleDefinition(row));
        }
        queryResponse.setData(gateways);
        return queryResponse;
    }

    public RuleDefinitionAbstract get(HashMap<String, Object> filters) {
        QueryResponse response = getAll(filters);
        @SuppressWarnings("unchecked")
        List<RuleDefinitionTable> items = (List<RuleDefinitionTable>) response.getData();
        if (items != null && !items.isEmpty()) {
            return RuleUtils.getRuleDefinition(items.get(0));
        }
        return null;
    }

    public void add(RuleDefinitionAbstract ruleDefinition) {
        ruleDefinition.reset();
        RuleUtils.addRuleDefinition(ruleDefinition);
        GoogleAnalyticsApi.instance().trackRuleCreation(ruleDefinition.getConditionType().getText());
    }

    public void update(RuleDefinitionAbstract ruleDefinition) {
        RuleUtils.updateRuleDefinition(ruleDefinition);
    }

    public void deleteIds(List<Integer> ids) {
        RuleUtils.deleteRuleDefinitionIds(ids);
    }

    public void enableIds(List<Integer> ids) {
        RuleUtils.enableRuleDefinitions(ids);
    }

    public void disableIds(List<Integer> ids) {
        RuleUtils.disableRuleDefinitions(ids);
    }

}
