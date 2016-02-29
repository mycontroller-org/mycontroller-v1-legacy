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
 * @since 0.0.3
 */
@Builder
@Data
@AllArgsConstructor
@ToString(includeFieldNames = true)
public class NotificationPushbulletNote implements INotificationEngine {
    private static final Logger _logger = LoggerFactory.getLogger(NotificationPushbulletNote.class);

    private String idens;
    private String title;
    private String body;
    private Notification notification;
    private AlarmDefinition alarmDefinition;
    private String actualValue;

    public NotificationPushbulletNote update() {
        this.idens = notification.getVariable1();
        this.body = notification.getVariable2();
        this.title = notification.getVariable3();
        return this;
    }

    public String getString() {
        return title;
    }

    @Override
    public void execute() {
        try {
            AlarmNotification alarmNotification = new AlarmNotification(alarmDefinition, actualValue);
            if (body != null && body.trim().length() > 0) {
                PushbulletUtils.sendNote(
                        idens,
                        alarmNotification.updateReferances(title),
                        alarmNotification.updateReferances(body));
            } else {
                PushbulletUtils.sendNote(
                        idens,
                        alarmNotification.updateReferances(title),
                        alarmNotification.toString());
            }
        } catch (Exception ex) {
            _logger.error("Exception,", ex);
        }
        //Update last execution
        notification.setLastExecution(System.currentTimeMillis());
        DaoUtils.getNotificationDao().update(notification);
    }
}
