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
import org.mycontroller.standalone.utils.McUtils;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DampeningActiveTime extends Dampening {
    public static final String KEY_ACTIVE_TIME = "activeTime";
    public static final String KEY_ACTIVE_FROM = "activeFrom";

    public static final long ACTIVE_FROM_RESET_VALUE = 0L;
    private Long activeTime;
    private Long activeFrom;

    @Override
    public void updateRuleDefinitionTable(RuleDefinitionTable ruleDefinitionTable) {
        ruleDefinitionTable.setDampeningType(super.getType());
        HashMap<String, Object> dampeningProperties = new HashMap<String, Object>();
        dampeningProperties.put(KEY_ACTIVE_TIME, activeTime);
        dampeningProperties.put(KEY_ACTIVE_FROM, activeFrom);
        ruleDefinitionTable.setDampeningProperties(dampeningProperties);
    }

    @Override
    public void updateDampening(RuleDefinitionTable ruleDefinitionTable) {
        super.setType(ruleDefinitionTable.getDampeningType());
        this.activeTime = (Long) ruleDefinitionTable.getDampeningProperties().get(KEY_ACTIVE_TIME);
        this.activeFrom = ruleDefinitionTable.getDampeningProperties().get(KEY_ACTIVE_FROM) == null ?
                ACTIVE_FROM_RESET_VALUE : (long) ruleDefinitionTable.getDampeningProperties().get(KEY_ACTIVE_FROM);
    }

    @Override
    public boolean evaluate() {
        if (activeTime == 0L) {//If user sets active time as 0 it never run
            return false;
        }
        if (activeFrom != ACTIVE_FROM_RESET_VALUE) {
            if ((System.currentTimeMillis() - activeFrom) >= activeTime) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void reset() {
        this.activeFrom = ACTIVE_FROM_RESET_VALUE;
    }

    @Override
    public String getDampeningString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getType().getText());
        builder.append(" [ ").append(McUtils.getFriendlyTime(activeTime, true));
        builder.append(" ]");
        return builder.toString();
    }

}
