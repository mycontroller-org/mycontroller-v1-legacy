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
package org.mycontroller.standalone.db.tables;

import java.security.Principal;
import java.util.List;

import org.mycontroller.standalone.auth.AuthUtils.PERMISSION_TYPE;
import org.mycontroller.standalone.db.DB_TABLES;
import org.mycontroller.standalone.db.DaoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@DatabaseTable(tableName = DB_TABLES.USER)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(includeFieldNames = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class User implements Principal {
    public static final Logger _logger = LoggerFactory.getLogger(User.class);

    public static final String KEY_ID = "id";
    public static final String KEY_ENABLED = "enabled";
    public static final String KEY_USER_NAME = "username";
    public static final String KEY_FULL_NAME = "fullName";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_VALIDITY = "validity";

    @DatabaseField(generatedId = true, columnName = KEY_ID)
    private Integer id;
    @DatabaseField(canBeNull = false, columnName = KEY_ENABLED)
    private Boolean enabled;
    @DatabaseField(unique = true, index = true, canBeNull = false, columnName = KEY_USER_NAME)
    private String username;
    @DatabaseField(canBeNull = false, columnName = KEY_FULL_NAME)
    private String fullName;
    @DatabaseField(canBeNull = false, columnName = KEY_EMAIL)
    private String email;
    @DatabaseField(canBeNull = false, columnName = KEY_PASSWORD)
    private String password;
    @DatabaseField(canBeNull = true, columnName = KEY_VALIDITY)
    private Long validity;

    private List<String> permissions;

    private List<Integer> gatewayIds;

    private List<Integer> nodeIds;

    private List<Integer> sensorIds;

    public List<String> getPermissions() {
        if (permissions == null) {
            permissions = DaoUtils.getRoleDao().getPermissionsByUserId(id);
        }
        return permissions;
    }

    public String getPermission() {
        if (getPermissions().contains(PERMISSION_TYPE.SUPER_ADMIN.getText())) {
            return PERMISSION_TYPE.SUPER_ADMIN.getText();
        } else if (getPermissions().contains(PERMISSION_TYPE.USER.getText())) {
            return PERMISSION_TYPE.USER.getText();
        } else if (getPermissions().contains(PERMISSION_TYPE.MQTT_USER.getText())) {
            return PERMISSION_TYPE.MQTT_USER.getText();
        } else {
            return null;
        }
    }

    public List<Integer> getGatewayIds() {
        if (gatewayIds == null || gatewayIds.size() == 0) {
            gatewayIds = DaoUtils.getRoleDao().getGatewayIds(id);
            if (gatewayIds.isEmpty()) {
                gatewayIds.add(-1);
            }
        }
        return gatewayIds;
    }

    public List<Integer> getNodeIds() {
        if (nodeIds == null || nodeIds.size() == 0) {
            nodeIds = DaoUtils.getRoleDao().getNodeIds(id);
            if (nodeIds.isEmpty()) {
                nodeIds.add(-1);
            }
        }
        return nodeIds;
    }

    public List<Integer> getSensorIds() {
        if (sensorIds == null || sensorIds.size() == 0) {
            sensorIds = DaoUtils.getRoleDao().getSensorIds(id);
            if (sensorIds.isEmpty()) {
                sensorIds.add(-1);
            }
        }
        return sensorIds;
    }

    @Override
    public String getName() {
        return this.username;
    }

}
