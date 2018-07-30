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
public class TelegramBotSettings {
    public static final String KEY_TELEGRAM_BOT = "telegramBot";

    public static final String SKEY_TOKEN = "token";
    public static final String SKEY_ID = "id";
    public static final String SKEY_IS_BOT = "isBot";
    public static final String SKEY_FIRST_NAME = "firstName";
    public static final String SKEY_USERNAME = "username";

    private String token;

    private Integer id;
    private Boolean isBot;
    private String firstName;
    private String username;

    public static TelegramBotSettings get() {
        String token = getValue(SKEY_TOKEN);
        if (token != null) {
            token = McCrypt.decrypt(token);
        }
        return TelegramBotSettings.builder()
                .token(token)
                .id(McUtils.getInteger(getValue(SKEY_ID)))
                .isBot(McUtils.getBoolean(getValue(SKEY_IS_BOT)))
                .firstName(getValue(SKEY_FIRST_NAME))
                .username(getValue(SKEY_USERNAME)).build();
    }

    public void save() {
        if (token != null) {
            token = McCrypt.encrypt(token);
        }
        updateValue(SKEY_TOKEN, token);
    }

    public void updateInternal() {
        updateValue(SKEY_ID, id);
        updateValue(SKEY_IS_BOT, isBot);
        updateValue(SKEY_FIRST_NAME, firstName);
        updateValue(SKEY_USERNAME, username);
    }

    private static String getValue(String subKey) {
        return SettingsUtils.getValue(KEY_TELEGRAM_BOT, subKey);
    }

    private static void updateValue(String subKey, Object value) {
        SettingsUtils.updateValue(KEY_TELEGRAM_BOT, subKey, value);
    }
}
