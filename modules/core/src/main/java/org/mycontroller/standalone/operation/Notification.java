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
package org.mycontroller.standalone.operation;

import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import javax.script.ScriptException;

import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.rule.model.RuleDefinition;
import org.mycontroller.standalone.scripts.McScript;
import org.mycontroller.standalone.scripts.McScriptEngine;
import org.mycontroller.standalone.scripts.McScriptEngineUtils;
import org.mycontroller.standalone.scripts.McScriptEngineUtils.SCRIPT_TYPE;
import org.mycontroller.standalone.scripts.McScriptException;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */

@Getter
@Slf4j
public class Notification {
    private String ruleName;
    private String ruleCondition;
    private String actualValue;
    private String triggeredAt;

    private String operationName;

    public Notification(RuleDefinition ruleDefinition) {
        ruleName = ruleDefinition.getName();
        ruleCondition = ruleDefinition.getConditionString();
        actualValue = ruleDefinition.getActualValue();
        triggeredAt = new SimpleDateFormat(AppProperties.getInstance().getDateFormatWithTimezone())
                .format(new Date(ruleDefinition.getLastTrigger()));
    }

    public String toString(String spaceVariable) {
        StringBuilder builder = new StringBuilder();
        builder.append("Rule definition: ").append(ruleName);
        builder.append(spaceVariable).append("Condition: ").append(ruleCondition);
        builder.append(spaceVariable).append("Present value: ").append(actualValue);
        if (operationName != null) {
            builder.append(spaceVariable).append("OperationTable: ").append(operationName);
        }
        builder.append(spaceVariable).append("Triggered at: ").append(triggeredAt);
        builder.append(spaceVariable).append("--- www.mycontroller.org");
        return builder.toString();
    }

    @Override
    public String toString() {
        return toString("\n");
    }

    public static String updateTemplate(Notification notification, String source) {
        HashMap<String, Object> bindings = new HashMap<String, Object>();
        bindings.put("notification", notification);
        McScript mcTemplateScript = McScript.builder()
                .type(SCRIPT_TYPE.OPERATION)
                .engineName(McScriptEngineUtils.MC_TEMPLATE_ENGINE)
                .data(source)
                .bindings(bindings)
                .build();
        McScriptEngine templateEngine = new McScriptEngine(mcTemplateScript);
        try {
            return (String) templateEngine.executeScript();
        } catch (FileNotFoundException | McScriptException | ScriptException ex) {
            _logger.error("Exception: {}", mcTemplateScript, ex);
            return "<pre>Exception: " + ex.getMessage() + "</pre>";
        }
    }

}
