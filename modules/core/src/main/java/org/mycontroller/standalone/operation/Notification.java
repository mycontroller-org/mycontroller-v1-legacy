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

import java.text.SimpleDateFormat;
import java.util.Date;

import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.rule.model.RuleDefinition;

import lombok.Getter;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */

@Getter
public class Notification {
    private String ruleName;
    private String ruleCondition;
    private String actualValue;
    private String actualUnit;
    private String triggeredAt;

    private String operationName;

    public Notification(RuleDefinition ruleDefinition) {
        ruleName = ruleDefinition.getName();
        ruleCondition = ruleDefinition.getConditionString();
        actualValue = ruleDefinition.getActualValue();
        actualUnit = ruleDefinition.getActualUnit();
        triggeredAt = new SimpleDateFormat(AppProperties.getInstance().getDateFormatWithTimezone())
                .format(new Date(ruleDefinition.getLastTrigger()));
    }

    public String toString(String spaceVariable) {
        StringBuilder builder = new StringBuilder();
        builder.append("Rule definition: ").append(ruleName);
        builder.append(spaceVariable).append("Condition: ").append(ruleCondition);
        builder.append(spaceVariable).append("Present value: ").append(actualValue);
        if (actualUnit.length() > 0) {
            builder.append(" ").append(actualUnit);
        }
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

}
