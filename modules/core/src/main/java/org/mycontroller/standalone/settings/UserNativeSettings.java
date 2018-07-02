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

import org.mycontroller.standalone.db.tables.Settings;
import org.mycontroller.standalone.db.tables.User;
import org.mycontroller.standalone.utils.McUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
@Builder
@ToString(includeFieldNames = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserNativeSettings {
    public static final String KEY_USER_NATIVE = "userNative";
    public static final String SKEY_IS_CONFIGURED = "isConfigured";
    public static final String SKEY_ACTION_BOARD_VIEW = "actionBoardView";
    public static final String SKEY_RESOURCES_LOGS_ITEMS_PER_PAGE = "rlogsItemsPerPage";

    private String actionBoardView;
    private String resourcesLogsItemsPerPage;
    private Boolean isCongifured;

    public static UserNativeSettings get(User user) {
        Boolean isCongifured = McUtils.getBoolean(getValue(user, SKEY_IS_CONFIGURED));
        if (isCongifured == null || !isCongifured) {
            UserNativeSettings.builder()
                    .actionBoardView("listView")
                    .resourcesLogsItemsPerPage("10")
                    .isCongifured(true)
                    .build().save(user);
        }

        return UserNativeSettings.builder()
                .actionBoardView(getValue(user, SKEY_ACTION_BOARD_VIEW))
                .resourcesLogsItemsPerPage(getValue(user, SKEY_RESOURCES_LOGS_ITEMS_PER_PAGE))
                .build();
    }

    public void save(User user) {
        if (actionBoardView != null) {
            updateSettings(user, SKEY_ACTION_BOARD_VIEW, actionBoardView);
        }
        if (resourcesLogsItemsPerPage != null) {
            updateSettings(user, SKEY_RESOURCES_LOGS_ITEMS_PER_PAGE, resourcesLogsItemsPerPage);
        }
        if (isCongifured != null) {
            updateSettings(user, SKEY_IS_CONFIGURED, String.valueOf(isCongifured));
        }
    }

    private static String getValue(User user, String subKey) {
        return SettingsUtils.getValue(user.getId(), KEY_USER_NATIVE, subKey);
    }

    private void updateSettings(User user, String subKey, String value) {
        SettingsUtils.updateSettings(Settings.builder()
                .userId(user.getId())
                .key(KEY_USER_NATIVE)
                .subKey(subKey)
                .value(value)
                .build());
    }
}
