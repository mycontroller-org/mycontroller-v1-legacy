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
package org.mycontroller.standalone.rule.model;

import java.util.HashMap;

import org.mycontroller.standalone.db.tables.RuleDefinitionTable;
import org.mycontroller.standalone.model.ResourceModel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Slf4j
@NoArgsConstructor
public class RuleDefinitionThresholdRange extends RuleDefinition {
    public static final String KEY_IN_RANGE = "inRange";
    public static final String KEY_INCLUDE_OPERATOR_LOW = "includeOperatorLow";
    public static final String KEY_INCLUDE_OPERATOR_HIGH = "includeOperatorHigh";
    public static final String KEY_THRESHOLD_LOW = "thresholdLow";
    public static final String KEY_THRESHOLD_HIGH = "thresholdHigh";

    private boolean inRange;
    private boolean includeOperatorLow;
    private boolean includeOperatorHigh;
    private Double thresholdLow;
    private Double thresholdHigh;

    public RuleDefinitionThresholdRange(RuleDefinitionTable ruleDefinitionTable) {
        updateRuleDefinition(ruleDefinitionTable);
    }

    @Override
    @JsonIgnore
    public RuleDefinitionTable getRuleDefinitionTable() {
        RuleDefinitionTable ruleDefinitionTable = super.getRuleDefinitionTable();
        HashMap<String, Object> conditionProperties = new HashMap<String, Object>();
        conditionProperties.put(KEY_IN_RANGE, inRange);
        conditionProperties.put(KEY_INCLUDE_OPERATOR_LOW, includeOperatorLow);
        conditionProperties.put(KEY_INCLUDE_OPERATOR_HIGH, includeOperatorHigh);
        conditionProperties.put(KEY_THRESHOLD_LOW, thresholdLow);
        conditionProperties.put(KEY_THRESHOLD_HIGH, thresholdHigh);
        ruleDefinitionTable.setConditionProperties(conditionProperties);
        return ruleDefinitionTable;
    }

    @Override
    @JsonIgnore
    public void updateRuleDefinition(RuleDefinitionTable ruleDefinitionTable) {
        super.updateRuleDefinition(ruleDefinitionTable);
        inRange = (boolean) ruleDefinitionTable.getConditionProperties().get(KEY_IN_RANGE);
        includeOperatorLow = (boolean) ruleDefinitionTable.getConditionProperties().get(KEY_INCLUDE_OPERATOR_LOW);
        includeOperatorHigh = (boolean) ruleDefinitionTable.getConditionProperties().get(KEY_INCLUDE_OPERATOR_HIGH);
        thresholdLow = (Double) ruleDefinitionTable.getConditionProperties().get(KEY_THRESHOLD_LOW);
        thresholdHigh = (Double) ruleDefinitionTable.getConditionProperties().get(KEY_THRESHOLD_HIGH);

    }

    @Override
    public String getConditionString() {
        try {
            StringBuilder builder = new StringBuilder();
            builder.append(super.getConditionType().getText()).append(" [ ");
            builder.append("if {").append(
                    new ResourceModel(getResourceType(), getResourceId()).getResourceLessDetails());
            builder.append("} ");
            if (inRange) {
                builder.append(" inside (");
            } else {
                builder.append(" outside (");
            }
            builder.append(thresholdLow).append(", ").append(thresholdHigh).append(")");
            builder.append(" ]");
            return builder.toString();
        } catch (Exception ex) {
            _logger.error("Exception, ", ex);
            return GET_CONDITION_STRING_EXCEPTION + ex.getMessage();
        }

    }
}
