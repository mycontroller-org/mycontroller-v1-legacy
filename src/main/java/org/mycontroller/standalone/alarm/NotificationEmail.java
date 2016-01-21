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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.mail.EmailException;
import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.ObjectFactory;
import org.mycontroller.standalone.db.tables.AlarmDefinition;
import org.mycontroller.standalone.email.EmailUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class NotificationEmail implements INotification {
    private static final Logger _logger = LoggerFactory.getLogger(NotificationEmail.class);

    private String toEmailAddress;
    private AlarmDefinition alarmDefinition;

    public NotificationEmail(AlarmDefinition alarmDefinition) {
        this.alarmDefinition = alarmDefinition;
        this.toEmailAddress = alarmDefinition.getVariable1();
    }

    public String getToEmailAddress() {
        return toEmailAddress;
    }

    public String toString() {
        return this.toEmailAddress;
    }

    @Override
    public AlarmDefinition getAlarmDefinition() {
        return this.alarmDefinition;
    }

    @Override
    public void execute(String actualValue) {

        if (this.toEmailAddress == null) {
            throw new RuntimeException("Cannot execute send email without email address! AlarmDefination name: "
                    + this.alarmDefinition.getName());
        }

        StringBuilder builder = new StringBuilder();

        builder.append("AlarmDefinition: [").append(alarmDefinition.getName()).append("/")
                .append(alarmDefinition.getResourceType().getText())
                .append("] triggered!");

        String subject = builder.toString();

        builder.setLength(0);

        builder.append("<table border='0'>");

        builder.append("<tr>");
        builder.append("<td>").append("AlarmDefinition Name").append("</td>");
        builder.append("<td>").append(": ").append(alarmDefinition.getName()).append("</td>");
        builder.append("<tr>");

        builder.append("<tr>");
        builder.append("<td>").append("Condition").append("</td>");
        builder.append("<td>").append(": ").append(AlarmUtils.getConditionString(alarmDefinition))
                .append(AlarmUtils.getSensorUnit(alarmDefinition, true))
                .append("</td>");
        builder.append("<tr>");

        builder.append("<tr>");
        builder.append("<td>").append("Dampening").append("</td>");
        builder.append("<td>")
                .append(": ").append(alarmDefinition.getDampeningString()).append("</td>");
        builder.append("<tr>");

        builder.append("<tr>");
        builder.append("<td>").append(alarmDefinition.getResourceType().getText()).append("</td>");
        builder.append("<td>")
                .append(": ").append(AlarmUtils.getResourceString(alarmDefinition, false)).append("</td>");
        builder.append("<tr>");

        builder.append("<tr>");
        builder.append("<td>").append("Sensor Value").append("</td>");
        builder.append("<td>")
                .append(": ").append(actualValue).append(AlarmUtils.getSensorUnit(alarmDefinition, false))
                .append("</td>");
        builder.append("<tr>");

        builder.append("<tr>");
        builder.append("<td>").append("Triggered at").append("</td>");
        builder.append("<td>")
                .append(": ")
                .append(new SimpleDateFormat(ObjectFactory.getAppProperties().getDateFormat()).format(new Date()))
                .append("</td>");
        builder.append("<tr>");

        builder.append("</table>");

        String message = null;
        try {
            message = new String(Files.readAllBytes(Paths.get(AppProperties.EMAIL_TEMPLATE_ALARM)),
                    StandardCharsets.UTF_8);
        } catch (IOException ex) {
            _logger.error("Exception, ", ex);
            message = ex.getMessage();
        }

        try {
            EmailUtils.sendSimpleEmail(this.toEmailAddress, subject,
                    message.replaceAll(EmailUtils.ALARM_INFO, builder.toString()));
        } catch (EmailException ex) {
            _logger.error("Error on sending email, ", ex);
        }
    }
}
