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
public class DampeningActiveTime implements IDampening {
    public static final long ACTIVE_FROM_RESET_VALUE = 0l;
    private Long activeTime;
    private Long activeFrom;
    private AlarmDefinition alarmDefinition;

    public DampeningActiveTime(AlarmDefinition alarmDefinition) {
        this.alarmDefinition = alarmDefinition;
        this.activeTime = Long.valueOf(alarmDefinition.getDampeningVar1());
        this.activeFrom = alarmDefinition.getDampeningInternal1() == null ?
                ACTIVE_FROM_RESET_VALUE : Long.valueOf(alarmDefinition.getDampeningInternal1());
    }

    @Override
    public boolean evaluate() {
        if (activeTime == 0l) {//If user sets active time as 0 it never run
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
        this.setActiveFrom(ACTIVE_FROM_RESET_VALUE);
        this.update();
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(DAMPENING_TYPE.CONSECUTIVE.getText());
        builder.append("=").append("activeFrom:").append(this.activeFrom);
        builder.append(", activeTime:").append(this.activeTime);
        return builder.toString();
    }

    @Override
    public AlarmDefinition getAlarmDefinition() {
        return this.alarmDefinition;
    }

    @Override
    public void update() {
        this.alarmDefinition.setDampeningInternal1(String.valueOf(this.activeFrom));
        DaoUtils.getAlarmDefinitionDao().update(alarmDefinition);
    }

    public Long getActiveTime() {
        return activeTime;
    }

    public Long getActiveFrom() {
        return activeFrom;
    }

    public void setActiveFrom(Long activeFrom) {
        this.activeFrom = activeFrom;
    }
}
