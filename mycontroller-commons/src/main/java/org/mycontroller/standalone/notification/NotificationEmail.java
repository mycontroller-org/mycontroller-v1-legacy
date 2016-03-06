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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.mail.EmailException;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.AlarmDefinition;
import org.mycontroller.standalone.db.tables.Notification;
import org.mycontroller.standalone.email.EmailUtils;
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
public class NotificationEmail implements INotificationEngine {
    private static final Logger _logger = LoggerFactory.getLogger(NotificationEmail.class);
    public static final String EMAIL_TEMPLATE_ALARM = "../conf/templates/emailTemplateAlarm.html";

    private String toEmailAddress;
    private String emailSubject;

    private Notification notification;
    private AlarmDefinition alarmDefinition;
    private String actualValue;

    public NotificationEmail update() {
        emailSubject = notification.getVariable1();
        toEmailAddress = notification.getVariable2();
        return this;
    }

    public String getString() {
        return this.toEmailAddress;
    }

    @Override
    public void execute() {
        if (toEmailAddress == null) {
            throw new RuntimeException("Cannot execute send email without email address! AlarmDefinition name: "
                    + this.alarmDefinition.getName());
        }

        AlarmNotification alarmNotification = new AlarmNotification(alarmDefinition, actualValue);
        String emailBody = null;
        try {
            emailBody = new String(Files.readAllBytes(Paths.get(EMAIL_TEMPLATE_ALARM)),
                    StandardCharsets.UTF_8);
        } catch (IOException ex) {
            _logger.error("Exception, ", ex);
            emailBody = ex.getMessage();
        }

        try {
            EmailUtils.sendSimpleEmail(
                    toEmailAddress,
                    alarmNotification.updateReferances(emailSubject),
                    alarmNotification.updateReferances(emailBody));
        } catch (EmailException ex) {
            _logger.error("Error on sending email, ", ex);
        }

        //Update last execution
        notification.setLastExecution(System.currentTimeMillis());
        DaoUtils.getNotificationDao().update(notification);
    }
}
