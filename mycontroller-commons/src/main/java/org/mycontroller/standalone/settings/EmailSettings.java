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
package org.mycontroller.standalone.settings;

import org.mycontroller.standalone.MycUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */

@Builder
@ToString(includeFieldNames = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EmailSettings {
    public static final String KEY_EMAIL = "email";
    public static final String SKEY_SMTP_HOST = "smtpHost";
    public static final String SKEY_SMTP_PORT = "smtpPort";
    public static final String SKEY_FROM_ADDRESS = "fromAddress";
    public static final String SKEY_ENABLE_SSL = "enableSsl";
    public static final String SKEY_SMTP_USERNAME = "smtpUsername";
    public static final String SKEY_SMTP_PASSWORD = "smtpPassword";

    private String smtpHost;
    private Integer smtpPort;
    private String fromAddress;
    private Boolean enableSsl;
    private String smtpUsername;
    private String smtpPassword;

    public static EmailSettings get() {
        return EmailSettings.builder().smtpHost(getValue(SKEY_SMTP_HOST))
                .smtpPort(MycUtils.getInteger(getValue(SKEY_SMTP_PORT)))
                .fromAddress(getValue(SKEY_FROM_ADDRESS))
                .enableSsl(MycUtils.getBoolean(getValue(SKEY_ENABLE_SSL)))
                .smtpUsername(getValue(SKEY_SMTP_USERNAME))
                .smtpPassword(getValue(SKEY_SMTP_PASSWORD)).build();
    }

    public void save() {
        updateValue(SKEY_SMTP_HOST, smtpHost);
        updateValue(SKEY_SMTP_PORT, smtpPort);
        updateValue(SKEY_FROM_ADDRESS, fromAddress);
        updateValue(SKEY_ENABLE_SSL, enableSsl);
        updateValue(SKEY_SMTP_USERNAME, smtpUsername);
        updateValue(SKEY_SMTP_PASSWORD, smtpPassword);
    }

    private static String getValue(String subKey) {
        return SettingsUtils.getValue(KEY_EMAIL, subKey);
    }

    private void updateValue(String subKey, Object value) {
        SettingsUtils.updateValue(KEY_EMAIL, subKey, value);
    }
}
