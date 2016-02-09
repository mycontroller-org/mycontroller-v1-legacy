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
public class DampeningLastNEvaluations implements IDampening {
    private int occurrencesMax;
    private int evaluationsMax;

    private int occurrencesCount;
    private int evaluationsCount;
    private AlarmDefinition alarmDefinition;

    public DampeningLastNEvaluations(AlarmDefinition alarmDefinition) {
        this.alarmDefinition = alarmDefinition;
        this.occurrencesMax = Integer.valueOf(alarmDefinition.getDampeningVar1());
        this.evaluationsMax = Integer.valueOf(alarmDefinition.getDampeningVar2());

        this.occurrencesCount = alarmDefinition.getDampeningInternal1() == null ?
                0 : Integer.valueOf(alarmDefinition.getDampeningInternal1());
        this.evaluationsCount = alarmDefinition.getDampeningInternal2() == null ?
                0 : Integer.valueOf(alarmDefinition.getDampeningInternal2());
    }

    public int getOccurrencesMax() {
        return occurrencesMax;
    }

    public int getEvaluationsMax() {
        return evaluationsMax;
    }

    public int getOccurrencesCount() {
        return occurrencesCount;
    }

    public void setOccurrencesCount(int occurrencesCount) {
        this.occurrencesCount = occurrencesCount;
        update();//update value to database
    }

    public int getEvaluationsCount() {
        return evaluationsCount;
    }

    public void setEvaluationsCount(int evaluationsCount) {
        this.evaluationsCount = evaluationsCount;
        update();//update value to database
    }

    public void incrementOccurrencesCount() {
        this.occurrencesCount++;
        update();//update value to database
    }

    public void incrementEvaluationsCount() {
        this.evaluationsCount++;
        update();//update value to database
    }

    @Override
    public void reset() {
        this.occurrencesCount = 0;
        this.evaluationsCount = 0;
        update();//update value to database
    }

    @Override
    public void update() {
        this.alarmDefinition.setDampeningInternal1(String.valueOf(this.occurrencesCount));
        this.alarmDefinition.setDampeningInternal2(String.valueOf(this.evaluationsCount));
        DaoUtils.getAlarmDefinitionDao().update(alarmDefinition);
    }

    @Override
    public boolean evaluate() {
        if (isExecutable()) {
            if (this.occurrencesCount >= this.occurrencesMax) {
                return true;
            }
        }
        return false;
    }

    public boolean isExecutable() {
        if (this.evaluationsCount >= this.evaluationsMax) {
            return true;
        }
        return false;
    }

    @Override
    public AlarmDefinition getAlarmDefinition() {
        return this.alarmDefinition;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(DAMPENING_TYPE.CONSECUTIVE.getText());
        builder.append("=").append("occurrencesCount:").append(this.occurrencesCount);
        builder.append(", evaluationsCount:").append(this.evaluationsCount);
        builder.append(", occurrencesMax:").append(this.occurrencesMax);
        builder.append(", evaluationsMax:").append(this.evaluationsMax);
        return builder.toString();
    }
}
