/**
 * Copyright (C) 2015 Jeeva Kandasamy (jkandasa@gmail.com)
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
package org.mycontroller.standalone.email;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.alarm.SendEmail;
import org.mycontroller.standalone.db.tables.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class EmailUtils {
    private static final Logger _logger = LoggerFactory.getLogger(EmailUtils.class);

    private static Email email = new SimpleEmail();

    private EmailUtils() {

    }

    public static void sendSimpleEmail(SendEmail sendEmail, String subject, String message) throws EmailException {
        initializeEmail();
        email.setSubject(subject);
        email.setMsg(message);
        email.addTo(sendEmail.getToEmailAddress().split(","));
        email.send();
        _logger.debug("Email successfully sent to [{}], Message:[{}]", sendEmail.getToEmailAddress(), message);
    }

    private static String getString(String key) {
        Settings settings = DaoUtils.getSettingsDao().get(key);
        if (settings.getValue() != null && settings.getValue().trim().length() > 0) {
            return settings.getValue().trim();
        }
        throw new IllegalArgumentException("Email configuration [" + key + "] should not be null or empty");
    }

    public static void initializeEmail() throws EmailException {
        email = new SimpleEmail();
        email.setHostName(getString(Settings.EMAIL_SMTP_HOST));
        email.setSmtpPort(Integer.valueOf(getString(Settings.EMAIL_SMTP_PORT)));
        if (DaoUtils.getSettingsDao().get(Settings.EMAIL_SMTP_USERNAME).getValue() != null
                && DaoUtils.getSettingsDao().get(Settings.EMAIL_SMTP_USERNAME).getValue().length() > 0) {
            email.setAuthenticator(new DefaultAuthenticator(getString(Settings.EMAIL_SMTP_USERNAME),
                    getString(Settings.EMAIL_SMTP_PASSWORD)));
        }
        email.setSSLOnConnect(getString(Settings.EMAIL_ENABLE_SSL).equalsIgnoreCase("true") ? true : false);
        email.setFrom(getString(Settings.EMAIL_FROM));
    }
}
