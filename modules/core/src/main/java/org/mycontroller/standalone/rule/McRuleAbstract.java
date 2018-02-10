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

import org.easyrules.core.BasicRule;
import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.operation.model.Operation;
import org.mycontroller.standalone.rule.model.DampeningActiveTime;
import org.mycontroller.standalone.rule.model.DampeningConsecutive;
import org.mycontroller.standalone.rule.model.DampeningLastNEvaluations;
import org.mycontroller.standalone.rule.model.RuleDefinitionAbstract;
import org.mycontroller.standalone.units.UnitUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Slf4j
public abstract class McRuleAbstract extends BasicRule {
    private RuleDefinitionAbstract ruleDefinition;

    protected String getActualValue() {
        return ruleDefinition.getActualValue();
    }

    protected void setActualValue(String actualValue) {
        ruleDefinition.setActualValue(actualValue);
    }

    protected String getActualUnit() {
        return ruleDefinition.getActualUnit();
    }

    protected void setActualUnit(String actualUnit) {
        ruleDefinition.setActualUnit(actualUnit);
    }

    public RuleDefinitionAbstract getRuleDefinitionBase() {
        return ruleDefinition;
    }

    public void setRuleDefinitionBase(RuleDefinitionAbstract ruleDefinition) {
        this.ruleDefinition = ruleDefinition;
    }

    @Override
    public String getName() {
        return ruleDefinition.getId() + "_" + ruleDefinition.getName();
    }

    protected String getResourceValue(RESOURCE_TYPE resourceType, Integer resourceId) throws IllegalAccessException {
        String value;
        //Get value to compare
        switch (resourceType) {
            case SENSOR_VARIABLE:
                value = DaoUtils.getSensorVariableDao().get(resourceId).getValue();
                break;
            case NODE:
                value = DaoUtils.getNodeDao().getById(resourceId)
                        .getState().getText();
                break;
            case GATEWAY:
                value = DaoUtils.getGatewayDao().getById(resourceId)
                        .getState().getText();
                break;
            case RESOURCES_GROUP:
                value = DaoUtils.getResourcesGroupDao().get(resourceId)
                        .getState().getText();
                break;
            default:
                throw new IllegalAccessException("Unable to execute this rule! unsupported 'Resource type': {}"
                        + resourceType);
        }
        return value;
    }

    protected String getResourceUnit(RESOURCE_TYPE resourceType, Integer resourceId) throws IllegalAccessException {
        if (resourceType == RESOURCE_TYPE.SENSOR_VARIABLE) {
            return UnitUtils.getUnit(DaoUtils.getSensorVariableDao().get(resourceId).getUnitType()).getUnit();
        } else {
            return "";
        }
    }

    protected boolean executeDampening(boolean triggerOperations) {
        //Ignore dampening check if already triggered and if ignore duplicate enabled
        if (!ruleDefinition.isTriggered() || !ruleDefinition.isIgnoreDuplicate()) {
            switch (ruleDefinition.getDampeningType()) {
                case NONE:
                    break;
                case CONSECUTIVE:
                    DampeningConsecutive dampeningConsecutive = (DampeningConsecutive) ruleDefinition.getDampening();
                    if (triggerOperations) {
                        dampeningConsecutive.incrementConsecutiveCount();
                    } else {
                        dampeningConsecutive.setConsecutiveCount(0);
                    }
                    if (dampeningConsecutive.evaluate()) {
                        dampeningConsecutive.reset();
                    } else {
                        triggerOperations = false;
                    }
                    break;
                case LAST_N_EVALUATIONS:
                    DampeningLastNEvaluations lastNEvaluations = (DampeningLastNEvaluations) ruleDefinition
                            .getDampening();
                    lastNEvaluations.incrementEvaluationsCount();
                    if (triggerOperations) {
                        lastNEvaluations.incrementOccurrencesCount();
                    }
                    if (lastNEvaluations.isExecutable()) {
                        if (lastNEvaluations.evaluate()) {
                            triggerOperations = true;
                        } else {
                            triggerOperations = false;
                        }
                        lastNEvaluations.reset();
                    } else {
                        triggerOperations = false;
                    }
                    break;
                case ACTIVE_TIME:
                    DampeningActiveTime dampeningActiveTime = (DampeningActiveTime) ruleDefinition.getDampening();
                    if (triggerOperations) {
                        //Set active as current time
                        if (dampeningActiveTime.getActiveFrom() == DampeningActiveTime.ACTIVE_FROM_RESET_VALUE) {
                            dampeningActiveTime.setActiveFrom(System.currentTimeMillis());
                            triggerOperations = false;
                        } else {
                            triggerOperations = dampeningActiveTime.evaluate();
                        }
                    } else {
                        dampeningActiveTime.reset();
                    }
                    break;
                default:
                    break;
            }
            //Update triggered
            if (triggerOperations) {
                ruleDefinition.setTriggered(true);
                //Update Last Trigger Time
                ruleDefinition.setLastTrigger(System.currentTimeMillis());
                //Update if asked to disable on trigger
                if (ruleDefinition.isDisableWhenTrigger()) {
                    ruleDefinition.setEnabled(false);
                    ruleDefinition.setDisabledByUser(false);
                }
            }
        } else if (!triggerOperations) {//set trigger to false, when we receive failed condition
            ruleDefinition.setTriggered(false);
        } else {
            triggerOperations = false;
        }

        /*
        //Update triggered
        if (triggerOperations) {
            if (!ruleDefinition.getTriggered() || !ruleDefinition.getIgnoreDuplicate()) {
                ruleDefinition.setTriggered(true);
                //Update Last Trigger Time
                ruleDefinition.setLastTrigger(System.currentTimeMillis());
                //Update if asked to disable on trigger
                if (ruleDefinition.getDisableWhenTrigger()) {
                    ruleDefinition.setEnabled(false);
                }
            } else {
                triggerOperations = false;
            }
        } else {
            ruleDefinition.setTriggered(false);
        }
         */
        if (_logger.isDebugEnabled()) {
            _logger.debug("Dampening evaluate result:{}", triggerOperations);
            _logger.debug("Rule definition details:{}", ruleDefinition);
        }
        //Update changes in to database
        DaoUtils.getRuleDefinitionDao().update(ruleDefinition.getRuleDefinitionTable());
        return triggerOperations;
    }

    //Execute operations
    @Override
    public void execute() throws Exception {
        _logger.debug("Operations going to execute:{}", ruleDefinition.getOperations());
        for (Operation operation : ruleDefinition.getOperations()) {
            try {
                operation.execute(ruleDefinition);
                operation.setLastExecution(System.currentTimeMillis());
                DaoUtils.getOperationDao().update(operation.getOperationTable());
            } catch (Exception ex) {
                _logger.error("Exception,", ex);
            }

        }
    }
}
