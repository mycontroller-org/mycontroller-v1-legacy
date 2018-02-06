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

import org.mycontroller.standalone.db.tables.RuleDefinitionTable;
import org.mycontroller.standalone.rule.RuleUtils.DAMPENING_TYPE;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DampeningLastNEvaluations extends DampeningAbstract {
    public static final String KEY_OCCURRENCES_MAX = "occurrencesMax";
    public static final String KEY_EVALUATIONS_MAX = "evaluationsMax";
    public static final String KEY_OCCURRENCES_COUNT = "occurrencesCount";
    public static final String KEY_EVALUATIONS_COUNT = "evaluationsCount";

    private Integer occurrencesMax;
    private Integer evaluationsMax;
    private Integer occurrencesCount;
    private Integer evaluationsCount;

    @Override
    public void updateRuleDefinitionTable(RuleDefinitionTable ruleDefinitionTable) {
        ruleDefinitionTable.setDampeningType(super.getType());
        HashMap<String, Object> dampeningProperties = new HashMap<String, Object>();
        dampeningProperties.put(KEY_OCCURRENCES_MAX, occurrencesMax);
        dampeningProperties.put(KEY_EVALUATIONS_MAX, evaluationsMax);
        dampeningProperties.put(KEY_OCCURRENCES_COUNT, occurrencesCount);
        dampeningProperties.put(KEY_EVALUATIONS_COUNT, evaluationsCount);
        ruleDefinitionTable.setDampeningProperties(dampeningProperties);
    }

    @Override
    public void updateDampening(RuleDefinitionTable ruleDefinitionTable) {
        super.setType(ruleDefinitionTable.getDampeningType());
        occurrencesMax = (Integer) ruleDefinitionTable.getDampeningProperties().get(KEY_OCCURRENCES_MAX);
        evaluationsMax = (Integer) ruleDefinitionTable.getDampeningProperties().get(KEY_EVALUATIONS_MAX);

        occurrencesCount = ruleDefinitionTable.getDampeningProperties().get(KEY_OCCURRENCES_COUNT) == null ?
                0 : (Integer) ruleDefinitionTable.getDampeningProperties().get(KEY_OCCURRENCES_COUNT);
        evaluationsCount = ruleDefinitionTable.getDampeningProperties().get(KEY_EVALUATIONS_COUNT) == null ?
                0 : (Integer) ruleDefinitionTable.getDampeningProperties().get(KEY_EVALUATIONS_COUNT);
    }

    public void incrementOccurrencesCount() {
        occurrencesCount++;
    }

    public void incrementEvaluationsCount() {
        evaluationsCount++;
    }

    @Override
    public void reset() {
        occurrencesCount = 0;
        evaluationsCount = 0;
    }

    @Override
    public boolean evaluate() {
        if (isExecutable()) {
            if (occurrencesCount >= occurrencesMax) {
                return true;
            }
        }
        return false;
    }

    public boolean isExecutable() {
        if (evaluationsCount >= evaluationsMax) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(DAMPENING_TYPE.CONSECUTIVE.getText());
        builder.append("=").append("occurrencesCount:").append(this.occurrencesCount);
        builder.append(", evaluationsCount:").append(this.evaluationsCount);
        builder.append(", occurrencesMax:").append(this.occurrencesMax);
        builder.append(", evaluationsMax:").append(this.evaluationsMax);
        return builder.toString();
    }

    @Override
    public String getDampeningString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getType().getText());
        builder.append(" [ ").append(occurrencesMax);
        builder.append(" out of ").append(evaluationsMax).append(" ] ");
        return builder.toString();

    }
}
