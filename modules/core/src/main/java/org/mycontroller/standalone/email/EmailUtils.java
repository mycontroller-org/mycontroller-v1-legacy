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
package org.mycontroller.standalone.email;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.settings.EmailSettings;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EmailUtils {

    private static HtmlEmail email = null;

    public static void sendSimpleEmail(String emails, String subject, String message) throws EmailException {
        sendSimpleEmail(emails, subject, message, true);
    }

    public static void sendSimpleEmail(String emails, String subject, String message, boolean initializeEmail)
            throws EmailException {
        if (initializeEmail) {
            initializeEmail(AppProperties.getInstance().getEmailSettings());
        }
        email.setCharset("UTF-8");
        email.setSubject(subject);
        email.setHtmlMsg(message);
        email.addTo(emails.split(","));
        String sendReturn = email.send();
        _logger.debug("Send Status:[{}]", sendReturn);
        _logger.debug("EmailSettings successfully sent to [{}], Message:[{}]", emails, message);
    }

    private static void initializeEmail(EmailSettings emailSettings) throws EmailException {
        _logger.info("{}", emailSettings);
        email = new HtmlEmail();
        email.setHostName(emailSettings.getSmtpHost());
        email.setSmtpPort(emailSettings.getSmtpPort());
        if (emailSettings.getSmtpUsername() != null
                && emailSettings.getSmtpUsername().length() > 0) {
            email.setAuthenticator(new DefaultAuthenticator(
                    emailSettings.getSmtpUsername(),
                    emailSettings.getSmtpPassword()));
        }
        if (emailSettings.getEnableSsl()) {
            if (emailSettings.getUseStartTLS()) {
                email.setStartTLSEnabled(emailSettings.getEnableSsl());
            } else {
                email.setSSLOnConnect(emailSettings.getEnableSsl());
            }
        }
        email.setFrom(emailSettings.getFromAddress());
    }

    public static void sendTestEmail(EmailSettings emailSettings) throws EmailException {
        initializeEmail(emailSettings);
        sendSimpleEmail(emailSettings.getFromAddress(), "Test email from MyController.org",
                "Message: Test email from MyController.org", false);
    }
}
