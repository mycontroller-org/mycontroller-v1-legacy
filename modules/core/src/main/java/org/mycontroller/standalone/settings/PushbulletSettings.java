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
package org.mycontroller.standalone.settings;

import org.mycontroller.standalone.auth.McCrypt;
import org.mycontroller.standalone.utils.McUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Builder
@ToString(includeFieldNames = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PushbulletSettings {
    public static final String KEY_PUSHBULLET = "pushbullet";

    public static final String SKEY_ACCESS_TOKEN = "accessToken";
    public static final String SKEY_IDEN = "iden";
    public static final String SKEY_ACTIVE = "active";
    public static final String SKEY_NAME = "name";
    public static final String SKEY_EMAIL = "email";
    public static final String SKEY_IMAGE_URL = "imageUrl";

    private String accessToken;

    private String iden;
    private Boolean active;
    private String name;
    private String email;
    private String imageUrl;

    public static PushbulletSettings get() {
        String accessToken = getValue(SKEY_ACCESS_TOKEN);
        if (accessToken != null) {
            accessToken = McCrypt.decrypt(accessToken);
        }
        return PushbulletSettings.builder()
                .accessToken(accessToken)
                .iden(getValue(SKEY_IDEN))
                .active(McUtils.getBoolean(getValue(SKEY_ACTIVE)))
                .name(getValue(SKEY_NAME))
                .email(getValue(SKEY_EMAIL))
                .imageUrl(getValue(SKEY_IMAGE_URL)).build();
    }

    public void save() {
        if (accessToken != null) {
            accessToken = McCrypt.encrypt(accessToken);
        }
        updateValue(SKEY_ACCESS_TOKEN, accessToken);
    }

    public void updateInternal() {
        updateValue(SKEY_IDEN, iden);
        updateValue(SKEY_ACTIVE, active);
        updateValue(SKEY_NAME, name);
        updateValue(SKEY_EMAIL, email);
        updateValue(SKEY_IMAGE_URL, imageUrl);
    }

    private static String getValue(String subKey) {
        return SettingsUtils.getValue(KEY_PUSHBULLET, subKey);
    }

    private static void updateValue(String subKey, Object value) {
        SettingsUtils.updateValue(KEY_PUSHBULLET, subKey, value);
    }
}
