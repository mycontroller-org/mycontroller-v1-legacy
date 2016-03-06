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
package org.mycontroller.standalone.notification;

import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.AlarmDefinition;
import org.mycontroller.standalone.db.tables.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Builder
@Data
@AllArgsConstructor
@ToString(includeFieldNames = true)
public class NotificationSMS implements INotificationEngine {
    private static final Logger _logger = LoggerFactory.getLogger(NotificationSMS.class);

    private String toPhoneNumber;
    private String customMessage;
    private Notification notification;
    private AlarmDefinition alarmDefinition;
    private String actualValue;

    public NotificationSMS update() {
        this.toPhoneNumber = notification.getVariable1();
        this.customMessage = notification.getVariable2();
        return this;
    }

    public String getString() {
        return this.toPhoneNumber;
    }

    @Override
    public void execute() {
        if (this.toPhoneNumber == null) {
            throw new RuntimeException("Cannot execute send SMS without phone number! AlarmDefination name: "
                    + this.alarmDefinition.getName());
        }
        try {
            AlarmNotification alarmNotification = new AlarmNotification(alarmDefinition, actualValue);
            if (customMessage != null && customMessage.trim().length() > 0) {
                SMSUtils.sendSMS(toPhoneNumber, alarmNotification.updateReferances(customMessage));
            } else {
                SMSUtils.sendSMS(this.toPhoneNumber, alarmNotification.toString());
            }
        } catch (Exception ex) {
            _logger.error("Exception,", ex);
        }
        //Update last execution
        notification.setLastExecution(System.currentTimeMillis());
        DaoUtils.getNotificationDao().update(notification);
    }
}
