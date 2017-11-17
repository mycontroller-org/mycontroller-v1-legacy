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
package org.mycontroller.standalone.rule.model;

import java.util.HashMap;

import org.mycontroller.standalone.db.tables.RuleDefinitionTable;
import org.mycontroller.standalone.model.ResourceModel;
import org.mycontroller.standalone.rule.RuleUtils.STRING_OPERATOR;

import com.fasterxml.jackson.annotation.JsonGetter;
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
public class RuleDefinitionString extends RuleDefinition {
    public static final String KEY_OPERATOR = "operator";
    public static final String KEY_PATTERN = "pattern";
    public static final String KEY_IGNORE_CASE = "ignoreCase";

    private STRING_OPERATOR operator;
    private String pattern;
    private boolean ignoreCase;

    public RuleDefinitionString(RuleDefinitionTable ruleDefinitionTable) {
        updateRuleDefinition(ruleDefinitionTable);
    }

    @Override
    @JsonIgnore
    public RuleDefinitionTable getRuleDefinitionTable() {
        RuleDefinitionTable ruleDefinitionTable = super.getRuleDefinitionTable();
        HashMap<String, Object> conditionProperties = new HashMap<String, Object>();
        conditionProperties.put(KEY_OPERATOR, operator.getText());
        conditionProperties.put(KEY_PATTERN, pattern);
        conditionProperties.put(KEY_IGNORE_CASE, ignoreCase);
        ruleDefinitionTable.setConditionProperties(conditionProperties);
        return ruleDefinitionTable;
    }

    @Override
    @JsonIgnore
    public void updateRuleDefinition(RuleDefinitionTable ruleDefinitionTable) {
        super.updateRuleDefinition(ruleDefinitionTable);
        operator = STRING_OPERATOR.fromString((String) ruleDefinitionTable.getConditionProperties().get(KEY_OPERATOR));
        pattern = (String) ruleDefinitionTable.getConditionProperties().get(KEY_PATTERN);
        ignoreCase = (boolean) ruleDefinitionTable.getConditionProperties().get(KEY_IGNORE_CASE);
    }

    @Override
    public String getConditionString() {
        try {
            StringBuilder builder = new StringBuilder();
            builder.append(super.getConditionType().getText()).append(" [ ");
            builder.append("if {").append(
                    new ResourceModel(getResourceType(), getResourceId()).getResourceLessDetails());
            builder.append("} ").append(operator.getText()).append(" \"").append(pattern).append("\"");
            builder.append(" ]");
            return builder.toString();
        } catch (Exception ex) {
            _logger.error("Exception, ", ex);
            return GET_CONDITION_STRING_EXCEPTION + ex.getMessage();
        }
    }

    //JSON getters support
    @JsonGetter("operator")
    private String getOperatorString() {
        return operator.getText();
    }
}
