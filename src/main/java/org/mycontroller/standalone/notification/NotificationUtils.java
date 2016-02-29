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

import java.util.ArrayList;
import java.util.List;

import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.AlarmDefinition;
import org.mycontroller.standalone.db.tables.Notification;
import org.mycontroller.standalone.db.tables.Timer;
import org.mycontroller.standalone.scheduler.SchedulerUtils;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
public class NotificationUtils {
    public static final String NOTIFICATION_SEND_PAYLOAD_TIMER_JOB = "notification_send_payload_";
    public static final String ALARM_NOTIFICATION = "alarm_noti_";
    public static final String TIMER_NOTIFICATION = "timer_noti_";

    // private static final Logger _logger = LoggerFactory.getLogger(NotificationUtils.class);

    private NotificationUtils() {
    }

    public enum NOTIFICATION_TYPE {
        SEND_PAYLOAD("Send payload"),
        SEND_SMS("Send SMS"),
        SEND_EMAIL("Send email"),
        PUSHBULLET_NOTE("Pushbullet note");
        public static NOTIFICATION_TYPE get(int id) {
            for (NOTIFICATION_TYPE notification_type : values()) {
                if (notification_type.ordinal() == id) {
                    return notification_type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

        private String value;

        private NOTIFICATION_TYPE(String value) {
            this.value = value;
        }

        public String getText() {
            return this.value;
        }

        public static NOTIFICATION_TYPE fromString(String text) {
            if (text != null) {
                for (NOTIFICATION_TYPE type : NOTIFICATION_TYPE.values()) {
                    if (text.equalsIgnoreCase(type.getText())) {
                        return type;
                    }
                }
            }
            return null;
        }
    }

    public static String getSendPayloadTimerJobName(AlarmDefinition alarmDefinition, Notification notification) {
        return getSendPayloadTimerJobName(notification) + ALARM_NOTIFICATION + alarmDefinition.getId();
    }

    public static String getSendPayloadTimerJobName(Notification notification) {
        return NOTIFICATION_SEND_PAYLOAD_TIMER_JOB + notification.getId() + "_";
    }

    public static List<String> getNotifications(AlarmDefinition alarmDefinition) {
        List<String> notificationList = new ArrayList<String>();
        List<Notification> notifications = DaoUtils.getNotificationDao().getByAlarmDefinitionId(
                alarmDefinition.getId());
        for (Notification notification : notifications) {
            notificationList.add(notification.getName());
        }
        return notificationList;
    }

    public static String getNotificationString(Notification notification) {
        StringBuilder builder = new StringBuilder();
        builder.append(notification.getType().getText()).append(": ");
        switch (notification.getType()) {
            case SEND_PAYLOAD:
                NotificationSendPayLoad sendPayLoad = NotificationSendPayLoad.builder().notification(notification)
                        .build().update();
                builder.append(sendPayLoad.getString());
                break;
            case SEND_EMAIL:
                NotificationEmail notificationEmail = NotificationEmail.builder().notification(notification)
                        .build().update();
                builder.append(notificationEmail.getString());
                break;
            case SEND_SMS:
                NotificationSMS notificationSMS = NotificationSMS.builder().notification(notification)
                        .build().update();
                builder.append(notificationSMS.getString());
                break;
            case PUSHBULLET_NOTE:
                NotificationPushbulletNote pushbulletNote = NotificationPushbulletNote.builder()
                        .notification(notification)
                        .build().update();
                builder.append(pushbulletNote.getString());
                break;
            default:
                builder.append("-");
        }
        return builder.toString();
    }

    public static void unloadNotificationTimerJobs(List<Integer> notificationIds) {
        for (Notification notification : DaoUtils.getNotificationDao().getAll(notificationIds)) {
            unloadNotificationTimerJobs(notification);
        }
    }

    public static void unloadNotificationTimerJobs(Notification notification) {
        //Unload timer job
        Timer timer = new Timer();
        List<String> allJobs = SchedulerUtils.getAllJobNames();
        for (String jobName : allJobs) {
            if (jobName.startsWith(getSendPayloadTimerJobName(notification))) {
                timer.setName(jobName);
                SchedulerUtils.unloadTimerJob(timer);
            }
        }
    }

    public static void unloadNotificationTimerJobs(AlarmDefinition alarmDefinition) {
        //Unload timer job
        Timer timer = new Timer();
        List<Notification> notifications = DaoUtils.getNotificationDao().getByAlarmDefinitionId(
                alarmDefinition.getId());
        for (Notification notification : notifications) {
            if (notification.getType() == NOTIFICATION_TYPE.SEND_PAYLOAD) {
                timer.setName(getSendPayloadTimerJobName(alarmDefinition, notification));
                SchedulerUtils.unloadTimerJob(timer);
            }
        }
    }

    public static void disableAlarmDefinition(AlarmDefinition alarmDefinition) {
        //unload notification timer jobs
        unloadNotificationTimerJobs(alarmDefinition);
        //Disable
        alarmDefinition.setEnabled(false);
        DaoUtils.getAlarmDefinitionDao().update(alarmDefinition);
    }

}
