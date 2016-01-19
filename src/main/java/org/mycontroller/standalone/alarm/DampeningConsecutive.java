/**
 * Copyright (C) 2015-2016 Jeeva Kandasamy (jkandasa@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mycontroller.standalone.alarm;

import org.mycontroller.standalone.alarm.AlarmUtils.DAMPENING_TYPE;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.AlarmDefinition;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class DampeningConsecutive implements IDampening {
    private int consecutiveMax;
    private int consecutiveCount;
    private AlarmDefinition alarmDefinition;

    public DampeningConsecutive(AlarmDefinition alarmDefinition) {
        this.alarmDefinition = alarmDefinition;
        this.consecutiveMax = Integer.valueOf(alarmDefinition.getDampeningVar1());
        this.consecutiveCount = alarmDefinition.getDampeningInternal1() == null ?
                0 : Integer.valueOf(alarmDefinition.getDampeningInternal1());
    }

    public int getConsecutiveMax() {
        return consecutiveMax;
    }

    public int getConsecutiveCount() {
        return consecutiveCount;
    }

    public void setConsecutiveCount(int consecutiveCount) {
        this.consecutiveCount = consecutiveCount;
    }

    public void incrementConsecutiveCount() {
        this.consecutiveCount++;
    }

    @Override
    public boolean evaluate() {
        if (this.consecutiveCount >= this.consecutiveMax) {
            return true;
        }
        return false;
    }

    @Override
    public void reset() {
        this.consecutiveCount = 0;
        this.update();
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(DAMPENING_TYPE.CONSECUTIVE.getText());
        builder.append("=").append("consecutiveCount:").append(this.consecutiveCount);
        builder.append(", consecutiveMax:").append(this.consecutiveMax);
        return builder.toString();
    }

    @Override
    public AlarmDefinition getAlarmDefinition() {
        return this.alarmDefinition;
    }

    @Override
    public void update() {
        this.alarmDefinition.setDampeningInternal1(String.valueOf(this.consecutiveCount));
        DaoUtils.getAlarmDefinitionDao().update(alarmDefinition);
    }
}
