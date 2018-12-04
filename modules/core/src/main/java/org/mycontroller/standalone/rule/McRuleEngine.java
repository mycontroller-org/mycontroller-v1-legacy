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
package org.mycontroller.standalone.rule;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.easyrules.api.RulesEngine;
import org.easyrules.core.RulesEngineBuilder;
import org.knowm.sundial.Job;
import org.knowm.sundial.exceptions.JobInterruptException;
import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.RuleDefinitionTable;
import org.mycontroller.standalone.rule.RuleUtils.CONDITION_TYPE;
import org.mycontroller.standalone.rule.RuleUtils.DAMPENING_TYPE;
import org.mycontroller.standalone.rule.RuleUtils.DATA_TYPE;
import org.mycontroller.standalone.rule.model.RuleDefinitionCompare;
import org.mycontroller.standalone.rule.model.RuleDefinitionThreshold;
import org.mycontroller.standalone.utils.McUtils;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Slf4j
@NoArgsConstructor
public class McRuleEngine extends Job implements Runnable {
    public static final String MC_RULES_ENGINE_NAME = "mc_rules_engine";
    private static final AtomicBoolean IS_RUNNING = new AtomicBoolean(false);
    private static final long MAX_WAIT_TIME = 1000 * 4;// 4 seconds

    private RESOURCE_TYPE resourceType;
    private Integer resourceId;

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
                    McRuleAbstract mcRuleAbstract = null;
                    switch (ruleDefinitionDb.getConditionType()) {
                        case THRESHOLD:
                            mcRuleAbstract = new McConditionThreshold(ruleDefinitionDb);
                            break;
                        case THRESHOLD_RANGE:
                            mcRuleAbstract = new McConditionThresholdRange(ruleDefinitionDb);
                            break;
                        case COMPARE:
                            mcRuleAbstract = new McConditionCompare(ruleDefinitionDb);
                            break;
                        case STATE:
                            mcRuleAbstract = new McConditionState(ruleDefinitionDb);
                            break;
                        case STRING:
                            mcRuleAbstract = new McConditionString(ruleDefinitionDb);
                            break;
                        case SCRIPT:
                            mcRuleAbstract = new McConditionScript(ruleDefinitionDb);
                            break;
                        default:
                            break;
                    }
                    if (mcRuleAbstract != null) {
                        mcRulesEngine.registerRule(mcRuleAbstract);
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
        long startTime = System.currentTimeMillis();
        while (IS_RUNNING.get()) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException ex) {
                _logger.error("Exception,", ex);
            }
            if (System.currentTimeMillis() - startTime > MAX_WAIT_TIME) {
                _logger.warn("Scheduled Rule execution skipped. Engine not available for more than {} ms",
                        MAX_WAIT_TIME);
                return;
            }
        }
        IS_RUNNING.set(true);
        try {
            //Load rules
            List<RuleDefinitionTable> ruleDefinitionsDb = new ArrayList<RuleDefinitionTable>();
            //Create set to avoid duplicates
            Set<RuleDefinitionTable> ruleDefinitionsSet = new HashSet<RuleDefinitionTable>();

            //Get Gateway rules
            List<RuleDefinitionTable> gatewayRules = DaoUtils.getRuleDefinitionDao().getAll(
                    RuleDefinitionTable.KEY_RESOURCE_TYPE, RESOURCE_TYPE.GATEWAY);
            //Get Node rules
            List<RuleDefinitionTable> nodeRules = DaoUtils.getRuleDefinitionDao().getAll(
                    RuleDefinitionTable.KEY_RESOURCE_TYPE, RESOURCE_TYPE.NODE);
            //Get Script rules
            List<RuleDefinitionTable> scriptRules = DaoUtils.getRuleDefinitionDao().getAll(
                    RuleDefinitionTable.KEY_RESOURCE_TYPE, RESOURCE_TYPE.SCRIPT);
            //Get all active Dampening type rules
            List<RuleDefinitionTable> dampeningRules = DaoUtils.getRuleDefinitionDao().getAll(
                    DAMPENING_TYPE.ACTIVE_TIME);
            if (gatewayRules != null) {
                ruleDefinitionsSet.addAll(gatewayRules);
            }
            if (nodeRules != null) {
                ruleDefinitionsSet.addAll(nodeRules);
            }
            if (scriptRules != null) {
                ruleDefinitionsSet.addAll(scriptRules);
            }
            if (dampeningRules != null) {
                ruleDefinitionsSet.addAll(dampeningRules);
            }

            //Add all Set objects to arrayList
            ruleDefinitionsDb.clear();
            ruleDefinitionsDb.addAll(ruleDefinitionsSet);
            //Execute collected rules
            execute(ruleDefinitionsDb, MC_RULES_ENGINE_NAME);
        } catch (Exception ex) {
            _logger.error("Exception on scheduled job, ", ex);
        } finally {
            IS_RUNNING.set(false);
        }

    }

    @Override
    public void run() {
        if (resourceType == null || resourceId == null) {
            _logger.warn("ResourceType[{}] and resourceId[{}] should not be NULL", resourceType, resourceId);
            return;
        }
        long startTime = System.currentTimeMillis();
        while (IS_RUNNING.get()) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException ex) {
                _logger.error("Exception,", ex);
            }
            if (System.currentTimeMillis() - startTime > MAX_WAIT_TIME) {
                _logger.warn("Scheduled Rule exuection skipped. Engine not available for more than {} ms",
                        MAX_WAIT_TIME);
                return;
            }
        }
        IS_RUNNING.set(true);
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
            _logger.error("Exception on ondemand thread job, ", ex);
        } finally {
            IS_RUNNING.set(false);
        }
    }
}
