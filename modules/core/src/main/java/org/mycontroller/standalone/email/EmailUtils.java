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
package org.mycontroller.standalone.email;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.mycontroller.standalone.AppProperties;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Slf4j
public class EmailUtils {

    private static HtmlEmail email = null;

    private EmailUtils() {

    }

    public static void sendSimpleEmail(String emails, String subject, String message) throws EmailException {
        initializeEmail();
        email.setSubject(subject);
        email.setHtmlMsg(message);
        email.addTo(emails.split(","));
        String sendReturn = email.send();
        _logger.debug("Send Status:[{}]", sendReturn);
        _logger.debug("EmailSettings successfully sent to [{}], Message:[{}]", emails, message);
    }

    public static void initializeEmail() throws EmailException {
        email = new HtmlEmail();
        email.setHostName(AppProperties.getInstance().getEmailSettings().getSmtpHost());
        email.setSmtpPort(AppProperties.getInstance().getEmailSettings().getSmtpPort());
        if (AppProperties.getInstance().getEmailSettings().getSmtpUsername() != null
                && AppProperties.getInstance().getEmailSettings().getSmtpUsername().length() > 0) {
            email.setAuthenticator(new DefaultAuthenticator(AppProperties.getInstance().getEmailSettings()
                    .getSmtpUsername(),
                    AppProperties.getInstance().getEmailSettings().getSmtpPassword()));
        }
        email.setSSLOnConnect(AppProperties.getInstance().getEmailSettings().getEnableSsl());
        email.setFrom(AppProperties.getInstance().getEmailSettings().getFromAddress());
    }
}
