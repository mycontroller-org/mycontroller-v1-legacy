/*
 * Copyright 2015-2017 Jeeva Kandasamy (jkandasa@gmail.com)
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
package org.mycontroller.standalone.jobs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.knowm.sundial.Job;
import org.knowm.sundial.exceptions.JobInterruptException;
import org.mycontroller.standalone.api.RuleApi;
import org.mycontroller.standalone.api.jaxrs.model.Query;
import org.mycontroller.standalone.api.jaxrs.model.QueryResponse;
import org.mycontroller.standalone.db.tables.RuleDefinitionTable;
import org.mycontroller.standalone.rule.model.RuleDefinition;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Slf4j
public class RuleDefinitionsReEnableJob extends Job {
    private static final RuleApi RULE_API = new RuleApi();

    private void updateRuleDefinitions() {
        HashMap<String, Object> filters = new HashMap<String, Object>();
        filters.put(RuleDefinitionTable.KEY_ENABLED, false);
        filters.put(RuleDefinitionTable.KEY_RE_ENABLE, true);
        filters.put(Query.PAGE_LIMIT, -1L);
        QueryResponse response = RULE_API.getAll(filters);
        _logger.debug("Response:{}", response);
        if (response.getData() != null) {
            @SuppressWarnings("unchecked")
            List<RuleDefinition> rules = (List<RuleDefinition>) response.getData();
            Long currentTime = System.currentTimeMillis();
            List<Integer> enableRuleIds = new ArrayList<Integer>();
            for (RuleDefinition rule : rules) {
                if (rule.getLastTrigger() != null && (currentTime - rule.getLastTrigger()) >= rule.getReEnableDelay()) {
                    enableRuleIds.add(rule.getId());
                    _logger.debug("Enable {}", rule);
                }
            }
            if (!enableRuleIds.isEmpty()) {
                RULE_API.enableIds(enableRuleIds);
                _logger.debug("Rule ids enabled:{}", enableRuleIds);
            }
        }
    }

    @Override
    public void doRun() throws JobInterruptException {
        try {
            updateRuleDefinitions();
        } catch (Exception ex) {
            _logger.error("Exception, ", ex);
        }
    }

}
