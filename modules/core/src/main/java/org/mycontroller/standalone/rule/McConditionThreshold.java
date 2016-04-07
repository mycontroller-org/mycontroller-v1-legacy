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

import org.mycontroller.standalone.McUtils;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.RuleDefinitionTable;
import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.rule.model.RuleDefinitionThreshold;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Slf4j
public class McConditionThreshold extends McRuleBase {
    public RuleDefinitionThreshold rdThreshold; //data to operate on

    public McConditionThreshold(RuleDefinitionTable ruleDefinitionTable) {
        rdThreshold = new RuleDefinitionThreshold();
        rdThreshold.updateRuleDefinition(ruleDefinitionTable);
        setRuleDefinitionBase(rdThreshold);
    }

    public McConditionThreshold(RuleDefinitionThreshold rdThreshold) {
        this.rdThreshold = rdThreshold;
        setRuleDefinitionBase(this.rdThreshold);
    }

    @Override
    public boolean evaluate() {
        boolean triggerOperation = false;
        //Update current value
        try {
            actualValue = super.getResourceValue(rdThreshold.getResourceType(), rdThreshold.getResourceId());
        } catch (IllegalAccessException ex) {
            _logger.error("Failed to get actual value", ex);
            return false;
        }
        if (_logger.isDebugEnabled()) {
            _logger.debug("Actual value:{}", actualValue);
        }
        String thresholdValue = null;

        //Get threshold value
        switch (rdThreshold.getDataType()) {
            case VALUE:
                thresholdValue = rdThreshold.getData();
                break;
            case SENSOR_VARIABLE:
                SensorVariable thresholdSensorValue = DaoUtils.getSensorVariableDao().get(
                        McUtils.getInteger(rdThreshold.getData()));
                if (thresholdSensorValue != null) {
                    thresholdValue = thresholdSensorValue.getValue();
                }
                break;
            default:
                _logger.error("Unable to execute this rule! unsupported 'Data type': {}", rdThreshold);
                return false;
        }

        switch (rdThreshold.getOperator()) {
            case EQ:
                if (actualValue.equals(thresholdValue)) {
                    triggerOperation = true;
                }
                break;
            case GT:
                if (McUtils.getDouble(actualValue) > McUtils.getDouble(thresholdValue)) {
                    triggerOperation = true;
                }
                break;
            case GTE:
                if (McUtils.getDouble(actualValue) >= McUtils.getDouble(thresholdValue)) {
                    triggerOperation = true;
                }
                break;
            case LT:
                if (McUtils.getDouble(actualValue) < McUtils.getDouble(thresholdValue)) {
                    triggerOperation = true;
                }
                break;
            case LTE:
                if (McUtils.getDouble(actualValue) <= McUtils.getDouble(thresholdValue)) {
                    triggerOperation = true;
                }
                break;
            case NEQ:
                if (!actualValue.equals(thresholdValue)) {
                    triggerOperation = true;
                }
                break;
            default:
                _logger.warn("Operater[{}] not supported!", rdThreshold.getOperator());
                return false;
        }
        if (_logger.isDebugEnabled()) {
            _logger.debug("Rule evaluate result:{}", triggerOperation);
        }
        return executeDampening(triggerOperation);
    }
}
