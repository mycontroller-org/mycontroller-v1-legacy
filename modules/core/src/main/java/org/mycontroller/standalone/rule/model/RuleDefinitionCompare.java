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
package org.mycontroller.standalone.rule.model;

import java.util.HashMap;

import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.db.tables.RuleDefinitionTable;
import org.mycontroller.standalone.model.ResourceModel;
import org.mycontroller.standalone.rule.RuleUtils.CONDITION_TYPE;
import org.mycontroller.standalone.rule.RuleUtils.OPERATOR;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;
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
public class RuleDefinitionCompare extends RuleDefinitionAbstract {
    public static final String KEY_OPERATOR = "operator";
    public static final String KEY_DATA2_RESOURCE_TYPE = "data2ResourceType";
    public static final String KEY_DATA2_RESOURCE_ID = "data2ResourceId";
    public static final String KEY_DATA2_MULTIPLIER = "data2Multiplier";

    private OPERATOR operator;
    private RESOURCE_TYPE data2ResourceType;
    private Integer data2ResourceId;
    private Double data2Multiplier;

    public RuleDefinitionCompare() {
        //For now supports only for sensor variable
        data2ResourceType = RESOURCE_TYPE.SENSOR_VARIABLE;
    }

    public RuleDefinitionCompare(RuleDefinitionTable ruleDefinitionTable) {
        //For now supports only for sensor variable
        data2ResourceType = RESOURCE_TYPE.SENSOR_VARIABLE;
        updateRuleDefinition(ruleDefinitionTable);
    }

    @Override
    @JsonIgnore
    public RuleDefinitionTable getRuleDefinitionTable() {
        RuleDefinitionTable ruleDefinitionTable = super.getRuleDefinitionTable();
        HashMap<String, Object> conditionProperties = new HashMap<String, Object>();
        conditionProperties.put(KEY_OPERATOR, operator.getText());
        conditionProperties.put(KEY_DATA2_RESOURCE_TYPE, data2ResourceType.getText());
        conditionProperties.put(KEY_DATA2_RESOURCE_ID, data2ResourceId);
        conditionProperties.put(KEY_DATA2_MULTIPLIER, data2Multiplier);
        ruleDefinitionTable.setConditionProperties(conditionProperties);
        return ruleDefinitionTable;
    }

    @Override
    @JsonIgnore
    public void updateRuleDefinition(RuleDefinitionTable ruleDefinitionTable) {
        super.updateRuleDefinition(ruleDefinitionTable);
        if (ruleDefinitionTable.getConditionType() == CONDITION_TYPE.COMPARE) {
            operator = OPERATOR.fromString((String) ruleDefinitionTable.getConditionProperties().get(KEY_OPERATOR));
            data2ResourceType = RESOURCE_TYPE.SENSOR_VARIABLE;
            data2ResourceId = (Integer) ruleDefinitionTable.getConditionProperties().get(KEY_DATA2_RESOURCE_ID);
            data2Multiplier = (Double) ruleDefinitionTable.getConditionProperties().get(KEY_DATA2_MULTIPLIER);
        }
    }

    @Override
    public String getConditionString() {
        try {
            StringBuilder builder = new StringBuilder();
            builder.append(super.getConditionType().getText()).append(" [ ");
            builder.append("if {").append(
                    new ResourceModel(getResourceType(), getResourceId()).getResourceLessDetails());
            builder.append("} ").append(operator.getText()).append(" ").append(data2Multiplier).append("%");
            builder.append(" of {").append(
                    new ResourceModel(data2ResourceType, data2ResourceId).getResourceLessDetails());
            builder.append("} ]");
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

    @JsonGetter("data2ResourceType")
    private String getResourceTypeString() {
        return data2ResourceType.getText();
    }

}
