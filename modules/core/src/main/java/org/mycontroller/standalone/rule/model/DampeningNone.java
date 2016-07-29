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

import org.mycontroller.standalone.db.tables.RuleDefinitionTable;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
public class DampeningNone extends Dampening {

    @Override
    public String getDampeningString() {
        return super.getType().getText();
    }

    @Override
    public void reset() {
        //Nothing to do
    }

    @Override
    public boolean evaluate() {
        //nothing to do
        return true;
    }

    @Override
    public void updateRuleDefinitionTable(RuleDefinitionTable ruleDefinitionTable) {
        ruleDefinitionTable.setDampeningType(super.getType());
        ruleDefinitionTable.setDampeningProperties(null);
    }

    @Override
    public void updateDampening(RuleDefinitionTable ruleDefinitionTable) {
        super.setType(ruleDefinitionTable.getDampeningType());
    }
}
