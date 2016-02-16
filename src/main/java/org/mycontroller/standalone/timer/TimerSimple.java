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
package org.mycontroller.standalone.timer;

import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.Timer;
import org.mycontroller.standalone.timer.TimerUtils.TIMER_TYPE;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
public class TimerSimple {
    public static final String VALUE_SPLITTER = ",";
    private Timer timer;
    private Integer repeatCount;
    private Long repeatInterval;
    private Integer executedCount;

    public TimerSimple(Timer timer) {
        this.timer = timer;
        String[] repeat = timer.getFrequencyData().split(VALUE_SPLITTER);
        if (repeat.length != 2) {
            throw new RuntimeException("Invalid format:" + timer.getFrequencyData());
        }
        this.repeatInterval = Long.valueOf(repeat[0]);
        this.repeatCount = Integer.valueOf(repeat[1]);
        this.executedCount = timer.getInternalVariable1() == null ? 0 : Integer.valueOf(timer.getInternalVariable1());
    }

    public TimerSimple(String name, boolean enabled, String targetClass, Long repeatInterval, Integer repeatCount) {
        this(name, null, null, null, repeatInterval, repeatCount);
        timer.setTargetClass(targetClass);
        timer.setEnabled(enabled);
    }

    public TimerSimple(String name, RESOURCE_TYPE resourceType, Integer resourceId, String payload,
            Long repeatInterval, Integer repeatCount) {
        this.timer = new Timer();
        this.timer.setTimerType(TIMER_TYPE.SIMPLE);
        this.timer.setEnabled(true);
        this.timer.setName(name);
        this.timer.setResourceType(resourceType);
        this.timer.setResourceId(resourceId);
        this.timer.setPayload(payload);
        this.repeatCount = repeatCount;
        this.repeatInterval = repeatInterval;
        this.timer.setFrequencyData(this.repeatInterval + VALUE_SPLITTER + this.repeatCount);
    }

    public Timer getTimer() {
        return timer;
    }

    public Integer getRepeatCount() {
        return repeatCount;
    }

    public Long getRepeatInterval() {
        return repeatInterval;
    }

    public Integer getExecutedCount() {
        return executedCount;
    }

    public void setExecutedCount(Integer executedCount) {
        this.executedCount = executedCount;
    }

    public void incrementExecutedCount() {
        this.executedCount++;
    }

    public boolean isValid() {
        if (this.repeatCount == -1) { //always valid
            return true;
        } else if (this.repeatCount > this.executedCount) {
            return true;
        }
        return false;
    }

    public void update() {
        this.timer.setInternalVariable1(String.valueOf(this.executedCount));
        this.timer.setEnabled(isValid());
        if (this.timer.getEnabled()) {
            DaoUtils.getTimerDao().update(timer);
        } else {
            TimerUtils.updateTimer(timer);
        }
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Repeat[Interval:").append(this.repeatInterval).append(", Count:").append(this.repeatCount)
                .append("], Executed count:").append(this.executedCount);
        return builder.toString();
    }
}
