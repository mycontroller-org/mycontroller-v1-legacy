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
package org.mycontroller.standalone.timer;

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
        this(name, repeatInterval, repeatCount);
        timer.setTargetClass(targetClass);
        timer.setEnabled(enabled);
    }

    public TimerSimple(String name, Long repeatInterval, Integer repeatCount) {
        this.timer = new Timer();
        this.timer.setTimerType(TIMER_TYPE.SIMPLE);
        this.timer.setEnabled(true);
        this.timer.setName(name);
        this.repeatInterval = repeatInterval;
        this.repeatCount = repeatCount;
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
        } else if (repeatCount > executedCount) {
            return true;
        }
        return false;
    }

    public void update() {
        timer.setInternalVariable1(String.valueOf(executedCount));
        if (isValid()) {
            DaoUtils.getTimerDao().update(timer);
        } else {
            TimerUtils.disableTimer(timer);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Repeat[Interval:").append(repeatInterval).append(", Count:").append(repeatCount)
                .append("], Executed count:").append(executedCount);
        return builder.toString();
    }
}
