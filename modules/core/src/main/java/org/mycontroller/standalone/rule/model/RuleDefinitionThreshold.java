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

import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.McUtils;
import org.mycontroller.standalone.db.tables.RuleDefinitionTable;
import org.mycontroller.standalone.model.ResourceModel;
import org.mycontroller.standalone.rule.RuleUtils.DATA_TYPE;
import org.mycontroller.standalone.rule.RuleUtils.OPERATOR;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class RuleDefinitionThreshold extends RuleDefinition {
    private static final Logger _logger = LoggerFactory.getLogger(RuleDefinitionThreshold.class);
    public static final String KEY_OPERATOR = "operator";
    public static final String KEY_DATA_TYPE = "dataType";
    public static final String KEY_DATA = "data";

    private OPERATOR operator;
    private DATA_TYPE dataType;
    private String data;

    public RuleDefinitionThreshold() {
        //For now supports only for sensor variables
        setResourceType(RESOURCE_TYPE.SENSOR_VARIABLE);
    }

    public RuleDefinitionThreshold(RuleDefinitionTable ruleDefinitionTable) {
        //For now supports only for sensor variables
        setResourceType(RESOURCE_TYPE.SENSOR_VARIABLE);
        updateRuleDefinition(ruleDefinitionTable);
    }

    @Override
    @JsonIgnore
    public RuleDefinitionTable getRuleDefinitionTable() {
        RuleDefinitionTable ruleDefinitionTable = super.getRuleDefinitionTable();
        HashMap<String, Object> conditionProperties = new HashMap<String, Object>();
        conditionProperties.put(KEY_OPERATOR, operator.getText());
        conditionProperties.put(KEY_DATA_TYPE, dataType.getText());
        conditionProperties.put(KEY_DATA, data);
        ruleDefinitionTable.setConditionProperties(conditionProperties);
        return ruleDefinitionTable;
    }

    @Override
    @JsonIgnore
    public void updateRuleDefinition(RuleDefinitionTable ruleDefinitionTable) {
        super.updateRuleDefinition(ruleDefinitionTable);
        operator = OPERATOR.fromString((String) ruleDefinitionTable.getConditionProperties().get(KEY_OPERATOR));
        dataType = DATA_TYPE.fromString((String) ruleDefinitionTable.getConditionProperties().get(KEY_DATA_TYPE));
        data = (String) ruleDefinitionTable.getConditionProperties().get(KEY_DATA);
    }

    @Override
    public String getConditionString() {
        try {
            StringBuilder builder = new StringBuilder();
            builder.append(super.getConditionType().getText()).append(" [ ");
            builder.append("if {").append(
                    new ResourceModel(getResourceType(), getResourceId()).getResourceLessDetails());
            builder.append("} ").append(getOperator().getText()).append(" {");
            if (getDataType() == DATA_TYPE.VALUE) {
                builder.append(getData());
            } else if (getDataType() == DATA_TYPE.SENSOR_VARIABLE) {
                builder.append(new ResourceModel(RESOURCE_TYPE.SENSOR_VARIABLE, McUtils.getInteger(getData()))
                        .getResourceLessDetails());
            }
            builder.append("} ]");
            return builder.toString();
        } catch (Exception ex) {
            _logger.error("Exception, ", ex);
            return GET_CONDITION_STRING_EXCEPTION + ex.getMessage();
        }

    }

    //For json
    @JsonGetter("operator")
    private String getOperatorString() {
        return operator.getText();
    }

    @JsonGetter("dataType")
    private String getDataTypeString() {
        return dataType.getText();
    }

}
