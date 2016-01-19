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

import org.mycontroller.standalone.db.tables.AlarmDefinition;
import org.mycontroller.standalone.sms.SMSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class NotificationSMS implements INotification {
    private static final Logger _logger = LoggerFactory.getLogger(NotificationSMS.class);

    private String toPhoneNumber;
    private String customMessage;
    private AlarmDefinition alarmDefinition;

    public NotificationSMS(AlarmDefinition alarmDefinition) {
        this.alarmDefinition = alarmDefinition;
        this.toPhoneNumber = alarmDefinition.getVariable1();
        this.customMessage = alarmDefinition.getVariable2();
    }

    public String getToPhoneNumber() {
        return toPhoneNumber;
    }

    public String toString() {
        return this.toPhoneNumber;
    }

    @Override
    public AlarmDefinition getAlarmDefinition() {
        return this.alarmDefinition;
    }

    public String getCustomMessage() {
        return customMessage;
    }

    @Override
    public void execute(String actualValue) {
        if (this.toPhoneNumber == null) {
            throw new RuntimeException("Cannot execute send SMS without phone number! AlarmDefination name: "
                    + this.alarmDefinition.getName());
        }
        try {
            if (this.customMessage != null) {
                SMSUtils.sendSMS(this.toPhoneNumber, this.customMessage);
            } else {
                StringBuilder builder = new StringBuilder();
                builder.append("AlarmDefinition: [")
                        .append(alarmDefinition.getName())
                        .append("], Cond: ").append(AlarmUtils.getConditionString(alarmDefinition))
                        .append(AlarmUtils.getSensorUnit(alarmDefinition, true))
                        .append(", Present Value:").append(actualValue)
                        .append(AlarmUtils.getSensorUnit(alarmDefinition, false))
                        .append(", ").append(AlarmUtils.getResourceString(alarmDefinition, false))
                        .append("\nwww.mycontroller.org");
                SMSUtils.sendSMS(this.toPhoneNumber, builder.toString());
            }
        } catch (Exception ex) {
            _logger.error("Exception,", ex);
        }
    }

}
