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
package org.mycontroller.standalone.settings;

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
public class SmsSettings {
    public static final String KEY_SMS = "sms";
    public static final String SKEY_VENDOR = "vendor";
    public static final String SKEY_AUTH_SID = "sid";
    public static final String SKEY_AUTH_TOKEN = "authToken";
    public static final String SKEY_FROM_NUMBER = "fromAddress";

    private String vendor;
    private String authSid;
    private String authToken;
    private String fromNumber;

    public static SmsSettings get() {
        return SmsSettings.builder()
                .vendor(getValue(SKEY_VENDOR))
                .authSid(getValue(SKEY_AUTH_SID))
                .authToken(getValue(SKEY_AUTH_TOKEN))
                .fromNumber(getValue(SKEY_FROM_NUMBER)).build();
    }

    public void save() {
        updateValue(SKEY_VENDOR, vendor);
        updateValue(SKEY_AUTH_SID, authSid);
        updateValue(SKEY_AUTH_TOKEN, authToken);
        updateValue(SKEY_FROM_NUMBER, fromNumber);
    }

    private static String getValue(String subKey) {
        return SettingsUtils.getValue(KEY_SMS, subKey);
    }

    private static void updateValue(String subKey, Object value) {
        SettingsUtils.updateValue(KEY_SMS, subKey, value);
    }
}
