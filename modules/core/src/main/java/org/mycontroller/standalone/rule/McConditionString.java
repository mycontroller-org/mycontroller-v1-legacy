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

import org.mycontroller.standalone.db.tables.RuleDefinitionTable;
import org.mycontroller.standalone.rule.model.RuleDefinitionString;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Slf4j
public class McConditionString extends McRuleBase {
    public RuleDefinitionString rdString; //data to operate on

    public McConditionString(RuleDefinitionTable ruleDefinitionTable) {
        rdString = new RuleDefinitionString();
        rdString.updateRuleDefinition(ruleDefinitionTable);
        setRuleDefinitionBase(rdString);
    }

    public McConditionString(RuleDefinitionString rdString) {
        this.rdString = rdString;
        setRuleDefinitionBase(this.rdString);
    }

    @Override
    public boolean evaluate() {
        boolean triggerOperation = false;
        //Update current value
        try {
            setActualValue(super.getResourceValue(rdString.getResourceType(), rdString.getResourceId()));
        } catch (IllegalAccessException ex) {
            _logger.error("Failed to get actual value", ex);
            return false;
        }
        _logger.debug("Actual value:{}", getActualValue());

        switch (rdString.getOperator()) {
            case CONTAINS:
                if (rdString.isIgnoreCase()) {
                    if (getActualValue().toLowerCase().contains(rdString.getPattern().toLowerCase())) {
                        triggerOperation = true;
                    }
                } else {
                    if (getActualValue().contains(rdString.getPattern())) {
                        triggerOperation = true;
                    }
                }
                break;
            case ENDS_WITH:
                if (rdString.isIgnoreCase()) {
                    if (getActualValue().toLowerCase().endsWith(rdString.getPattern().toLowerCase())) {
                        triggerOperation = true;
                    }
                } else {
                    if (getActualValue().endsWith(rdString.getPattern())) {
                        triggerOperation = true;
                    }
                }
                break;
            case EQUAL:
                if (rdString.isIgnoreCase()) {
                    if (getActualValue().equalsIgnoreCase(rdString.getPattern())) {
                        triggerOperation = true;
                    }
                } else {
                    if (getActualValue().equals(rdString.getPattern())) {
                        triggerOperation = true;
                    }
                }
                break;
            case MATCH:
                if (rdString.isIgnoreCase()) {
                    if (getActualValue().toLowerCase().matches(rdString.getPattern().toLowerCase())) {
                        triggerOperation = true;
                    }
                } else {
                    if (getActualValue().matches(rdString.getPattern())) {
                        triggerOperation = true;
                    }
                }
                break;
            case NOT_EQUAL:
                if (rdString.isIgnoreCase()) {
                    if (!getActualValue().equalsIgnoreCase(rdString.getPattern())) {
                        triggerOperation = true;
                    }
                } else {
                    if (!getActualValue().equals(rdString.getPattern())) {
                        triggerOperation = true;
                    }
                }
                break;
            case STARTS_WITH:
                if (rdString.isIgnoreCase()) {
                    if (getActualValue().toLowerCase().startsWith(rdString.getPattern().toLowerCase())) {
                        triggerOperation = true;
                    }
                } else {
                    if (getActualValue().startsWith(rdString.getPattern())) {
                        triggerOperation = true;
                    }
                }
                break;
            default:
                _logger.warn("Operater[{}] not supported!", rdString.getOperator());
                return false;
        }

        _logger.debug("Rule evaluate result:{}", triggerOperation);
        return executeDampening(triggerOperation);
    }
}
