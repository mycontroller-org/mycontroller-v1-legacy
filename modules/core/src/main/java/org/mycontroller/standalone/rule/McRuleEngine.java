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
package org.mycontroller.standalone.rule;

import java.util.ArrayList;
import java.util.List;

import org.easyrules.api.RulesEngine;
import org.easyrules.core.RulesEngineBuilder;
import org.knowm.sundial.Job;
import org.knowm.sundial.exceptions.JobInterruptException;
import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.McUtils;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.RuleDefinitionTable;
import org.mycontroller.standalone.rule.RuleUtils.CONDITION_TYPE;
import org.mycontroller.standalone.rule.RuleUtils.DATA_TYPE;
import org.mycontroller.standalone.rule.model.RuleDefinitionCompare;
import org.mycontroller.standalone.rule.model.RuleDefinitionThreshold;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
public class McRuleEngine extends Job implements Runnable {
    private static final Logger _logger = LoggerFactory.getLogger(McRuleEngine.class);
    public static final String MC_RULES_ENGINE_NAME = "mc_rules_engine";

    private RESOURCE_TYPE resourceType;
    private Integer resourceId;

    public McRuleEngine() {

    }

    public McRuleEngine(RESOURCE_TYPE resourceType, Integer resourceId) {
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    private void execute(List<RuleDefinitionTable> ruleDefinitionsDb, String engineName) {
        try {
            //Create rule engine
            RulesEngine mcRulesEngine = RulesEngineBuilder.aNewRulesEngine()
                    .withRuleListener(new McRuleListener())
                    .named(engineName)
                    .withSilentMode(true)
                    .build();
            //Load rules
            for (RuleDefinitionTable ruleDefinitionDb : ruleDefinitionsDb) {
                if (ruleDefinitionDb.getEnabled()) {
                    McRuleBase mcRuleBase = null;
                    switch (ruleDefinitionDb.getConditionType()) {
                        case THRESHOLD:
                            mcRuleBase = new McConditionThreshold(ruleDefinitionDb);
                            break;
                        case THRESHOLD_RANGE:
                            mcRuleBase = new McConditionThresholdRange(ruleDefinitionDb);
                            break;
                        case COMPARE:
                            mcRuleBase = new McConditionCompare(ruleDefinitionDb);
                            break;
                        case STATE:
                            mcRuleBase = new McConditionState(ruleDefinitionDb);
                            break;
                        case STRING:
                            mcRuleBase = new McConditionString(ruleDefinitionDb);
                            break;
                        case SCRIPT:
                            mcRuleBase = new McConditionScript(ruleDefinitionDb);
                            break;
                        default:
                            break;
                    }
                    if (mcRuleBase != null) {
                        mcRulesEngine.registerRule(mcRuleBase);
                    }
                }
            }

            //Fire rules
            if (mcRulesEngine.getRules().size() > 0) {
                mcRulesEngine.fireRules();
            }
        } catch (Exception ex) {
            _logger.error("Exception, ", ex);
        }
    }

    @Override
    public void doRun() throws JobInterruptException {
        try {
            //Load rules
            List<RuleDefinitionTable> ruleDefinitionsDb = new ArrayList<RuleDefinitionTable>();
            //Get Gateway rules
            List<RuleDefinitionTable> gatewayRules = DaoUtils.getRuleDefinitionDao().getAll(
                    RuleDefinitionTable.KEY_RESOURCE_TYPE, RESOURCE_TYPE.GATEWAY);
            //Get Node rules
            List<RuleDefinitionTable> nodeRules = DaoUtils.getRuleDefinitionDao().getAll(
                    RuleDefinitionTable.KEY_RESOURCE_TYPE, RESOURCE_TYPE.NODE);
            //Get Script rules
            List<RuleDefinitionTable> scriptRules = DaoUtils.getRuleDefinitionDao().getAll(
                    RuleDefinitionTable.KEY_RESOURCE_TYPE, RESOURCE_TYPE.SCRIPT);
            if (gatewayRules != null) {
                ruleDefinitionsDb.addAll(gatewayRules);
            }
            if (nodeRules != null) {
                ruleDefinitionsDb.addAll(nodeRules);
            }
            if (scriptRules != null) {
                ruleDefinitionsDb.addAll(scriptRules);
            }
            //Execute collected rules
            execute(ruleDefinitionsDb, MC_RULES_ENGINE_NAME);
        } catch (Exception ex) {
            _logger.error("Exception, ", ex);
        }

    }

    @Override
    public void run() {
        if (resourceType == null || resourceId == null) {
            _logger.warn("ResourceType[{}] and resourceId[{}] should not be NULL", resourceType, resourceId);
            return;
        }
        try {
            //Load rules
            List<RuleDefinitionTable> ruleDefinitionsDb = new ArrayList<RuleDefinitionTable>();
            //Resource specific rules
            List<RuleDefinitionTable> resourceRules = DaoUtils.getRuleDefinitionDao().getAll(resourceType, resourceId);
            if (resourceRules != null) {
                ruleDefinitionsDb.addAll(resourceRules);
            }

            //Threshold Rules
            List<RuleDefinitionTable> otherRules = DaoUtils.getRuleDefinitionDao().getAll(
                    RuleDefinitionTable.KEY_CONDITION_TYPE, CONDITION_TYPE.THRESHOLD);
            if (otherRules != null) {
                for (RuleDefinitionTable ruleDefinitionTable : otherRules) {
                    RuleDefinitionThreshold thresholdRule = new RuleDefinitionThreshold(ruleDefinitionTable);
                    if (thresholdRule.getDataType() == DATA_TYPE.SENSOR_VARIABLE
                            && McUtils.getInteger(thresholdRule.getData()).equals(resourceId)) {
                        ruleDefinitionsDb.add(ruleDefinitionTable);
                    }
                }
            }

            //Compare Rules
            otherRules = DaoUtils.getRuleDefinitionDao().getAll(
                    RuleDefinitionTable.KEY_CONDITION_TYPE, CONDITION_TYPE.COMPARE);
            if (otherRules != null) {
                for (RuleDefinitionTable ruleDefinitionTable : otherRules) {
                    RuleDefinitionCompare compareRule = new RuleDefinitionCompare(ruleDefinitionTable);
                    if (compareRule.getData2ResourceType() == RESOURCE_TYPE.SENSOR_VARIABLE
                            && compareRule.getData2ResourceId().equals(resourceId)) {
                        ruleDefinitionsDb.add(ruleDefinitionTable);
                    }
                }
            }

            //Execute all the rules
            execute(ruleDefinitionsDb, MC_RULES_ENGINE_NAME + "_" + resourceId);
        } catch (Exception ex) {
            _logger.error("Exception, ", ex);
        }

    }
}
