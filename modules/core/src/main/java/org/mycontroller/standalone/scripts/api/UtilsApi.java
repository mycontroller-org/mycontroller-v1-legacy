/*
 * Copyright 2015-2016 Jeeva Kandasamy (jkandasa@gmail.com)
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
package org.mycontroller.standalone.scripts.api;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.mycontroller.standalone.api.jaxrs.json.McHeatMap;
import org.mycontroller.standalone.api.jaxrs.json.Query;
import org.mycontroller.standalone.db.tables.GatewayTable;
import org.mycontroller.standalone.db.tables.OperationTable;
import org.mycontroller.standalone.db.tables.RuleDefinitionTable;
import org.mycontroller.standalone.gateway.GatewayUtils;
import org.mycontroller.standalone.gateway.model.Gateway;
import org.mycontroller.standalone.operation.OperationUtils;
import org.mycontroller.standalone.operation.model.Operation;
import org.mycontroller.standalone.rule.RuleUtils;
import org.mycontroller.standalone.rule.model.RuleDefinition;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */

public class UtilsApi {

    public Query getQuery() {
        return Query.builder()
                .order(Query.ORDER_ASC)
                .orderBy("id")
                .filters(new HashMap<String, Object>())
                .pageLimit(Query.MAX_ITEMS_PER_PAGE)
                .page(1L)
                .build();
    }

    public List<Integer> asList(Integer... ids) {
        return Arrays.asList(ids);
    }

    public Gateway getGateway(GatewayTable gatewayTable) {
        return GatewayUtils.getGateway(gatewayTable);
    }

    public Operation getOperation(OperationTable operationTable) {
        return OperationUtils.getOperation(operationTable);
    }

    public RuleDefinition getRuleDefinition(RuleDefinitionTable ruleDefinitionTable) {
        return RuleUtils.getRuleDefinition(ruleDefinitionTable);
    }

    public McHeatMap getHeatMap() {
        return McHeatMap.builder().build();
    }

}
