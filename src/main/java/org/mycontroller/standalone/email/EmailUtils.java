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
package org.mycontroller.standalone.email;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.mycontroller.standalone.ObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class EmailUtils {
    private static final Logger _logger = LoggerFactory.getLogger(EmailUtils.class);

    public static final String ALARM_INFO = "\\$alarmInfo";

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
        email.setHostName(ObjectFactory.getAppProperties().getEmailSettings().getSmtpHost());
        email.setSmtpPort(ObjectFactory.getAppProperties().getEmailSettings().getSmtpPort());
        if (ObjectFactory.getAppProperties().getEmailSettings().getSmtpUsername() != null
                && ObjectFactory.getAppProperties().getEmailSettings().getSmtpUsername().length() > 0) {
            email.setAuthenticator(new DefaultAuthenticator(ObjectFactory.getAppProperties().getEmailSettings()
                    .getSmtpUsername(),
                    ObjectFactory.getAppProperties().getEmailSettings().getSmtpPassword()));
        }
        email.setSSLOnConnect(ObjectFactory.getAppProperties().getEmailSettings().getEnableSsl());
        email.setFrom(ObjectFactory.getAppProperties().getEmailSettings().getFromAddress());
    }
}
