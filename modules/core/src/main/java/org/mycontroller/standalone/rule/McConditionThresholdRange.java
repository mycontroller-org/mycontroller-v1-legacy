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
import org.mycontroller.standalone.rule.model.RuleDefinitionThresholdRange;
import org.mycontroller.standalone.utils.McUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Slf4j
public class McConditionThresholdRange extends McRuleBase {
    public RuleDefinitionThresholdRange rdThRange; //data to operate on

    public McConditionThresholdRange(RuleDefinitionTable ruleDefinitionTable) {
        rdThRange = new RuleDefinitionThresholdRange();
        rdThRange.updateRuleDefinition(ruleDefinitionTable);
        setRuleDefinitionBase(rdThRange);
    }

    public McConditionThresholdRange(RuleDefinitionThresholdRange rdThRange) {
        this.rdThRange = rdThRange;
        setRuleDefinitionBase(this.rdThRange);
    }

    @Override
    public boolean evaluate() {
        boolean triggerOperation = false;
        //Update current value
        try {
            setActualValue(super.getResourceValue(rdThRange.getResourceType(), rdThRange.getResourceId()));
            setActualUnit(super.getResourceUnit(rdThRange.getResourceType(), rdThRange.getResourceId()));
        } catch (IllegalAccessException ex) {
            _logger.error("Failed to get actual value", ex);
            return false;
        }

        Double avDouble = McUtils.getDouble(getActualValue());

        _logger.debug("Actual value:{}", getActualValue());

        if (rdThRange.isIncludeOperatorLow() && rdThRange.isIncludeOperatorHigh()) {
            if (avDouble >= rdThRange.getThresholdLow() && avDouble <= rdThRange.getThresholdHigh()) {
                if (rdThRange.isInRange()) {
                    triggerOperation = true;
                }
            } else if (!rdThRange.isInRange()) {
                triggerOperation = true;
            }
        } else if (rdThRange.isIncludeOperatorLow() && !rdThRange.isIncludeOperatorHigh()) {
            if (avDouble >= rdThRange.getThresholdLow() && avDouble < rdThRange.getThresholdHigh()) {
                if (rdThRange.isInRange()) {
                    triggerOperation = true;
                }
            } else if (!rdThRange.isInRange()) {
                triggerOperation = true;
            }
        } else if (!rdThRange.isIncludeOperatorLow() && rdThRange.isIncludeOperatorHigh()) {
            if (avDouble > rdThRange.getThresholdLow() && avDouble <= rdThRange.getThresholdHigh()) {
                if (rdThRange.isInRange()) {
                    triggerOperation = true;
                }
            } else if (!rdThRange.isInRange()) {
                triggerOperation = true;
            }
        } else if (!rdThRange.isIncludeOperatorLow() && !rdThRange.isIncludeOperatorHigh()) {
            if (avDouble > rdThRange.getThresholdLow() && avDouble < rdThRange.getThresholdHigh()) {
                if (rdThRange.isInRange()) {
                    triggerOperation = true;
                }
            } else if (!rdThRange.isInRange()) {
                triggerOperation = true;
            }
        } else {
            _logger.warn("Operaters[OPL:{}, OPH:{}] not supported!",
                    rdThRange.isIncludeOperatorLow(), rdThRange.isIncludeOperatorHigh());
            return false;
        }

        _logger.debug("Rule evaluate result:{}", triggerOperation);
        return executeDampening(triggerOperation);
    }
}
