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
public class RuleDefinitionScript extends RuleDefinition {
    public static final String KEY_SCRIPT_FILE = "scriptFile";
    public static final String KEY_SCRIPT_BINDINGS = "scriptBindings";

    private String scriptFile;
    private HashMap<String, Object> scriptBindings;

    public RuleDefinitionScript(RuleDefinitionTable ruleDefinitionTable) {
        updateRuleDefinition(ruleDefinitionTable);
    }

    @Override
    @JsonIgnore
    public RuleDefinitionTable getRuleDefinitionTable() {
        RuleDefinitionTable ruleDefinitionTable = super.getRuleDefinitionTable();
        HashMap<String, Object> conditionProperties = new HashMap<String, Object>();
        conditionProperties.put(KEY_SCRIPT_FILE, scriptFile);
        conditionProperties.put(KEY_SCRIPT_BINDINGS, scriptBindings);
        ruleDefinitionTable.setConditionProperties(conditionProperties);
        return ruleDefinitionTable;
    }

    @SuppressWarnings("unchecked")
    @Override
    @JsonIgnore
    public void updateRuleDefinition(RuleDefinitionTable ruleDefinitionTable) {
        super.updateRuleDefinition(ruleDefinitionTable);
        scriptFile = (String) ruleDefinitionTable.getConditionProperties().get(KEY_SCRIPT_FILE);
        scriptBindings = (HashMap<String, Object>) ruleDefinitionTable.getConditionProperties().get(
                KEY_SCRIPT_BINDINGS);
    }

    public HashMap<String, Object> getScriptBindings() {
        if (scriptBindings == null) {
            return new HashMap<String, Object>();
        }
        return scriptBindings;
    }

    @Override
    public String getConditionString() {
        try {
            StringBuilder builder = new StringBuilder();
            builder.append(super.getConditionType().getText()).append(" [ ");
            builder.append(scriptFile).append(" <= ").append(scriptBindings).append(" ]");
            return builder.toString();
        } catch (Exception ex) {
            _logger.error("Exception, ", ex);
            return GET_CONDITION_STRING_EXCEPTION + ex.getMessage();
        }
    }

}
