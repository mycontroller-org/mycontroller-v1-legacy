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
package org.mycontroller.standalone.db.tables;

import java.security.Principal;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.mycontroller.standalone.db.USER_ROLE;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@DatabaseTable(tableName = "user")
@JsonIgnoreProperties(ignoreUnknown = true)
public class User implements Principal {
    public static final String NAME = "name";
    public static final String EMAIL = "email";

    @DatabaseField(generatedId = true, unique = true)
    private Integer id;
    @DatabaseField(canBeNull = false)
    private String fullName;
    @DatabaseField(unique = true, index = true, columnName = NAME, canBeNull = false)
    private String name;
    @DatabaseField(unique = true, columnName = EMAIL, canBeNull = false)
    private String email;
    @DatabaseField(canBeNull = false)
    private String password;
    @DatabaseField(canBeNull = false)
    private Integer roleId;

    public User() {

    }

    public User(int id) {
        this.id = id;
    }

    public User(String userName) {
        this.name = userName;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String name) {
        this.fullName = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String username) {
        this.name = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer role) {
        this.roleId = role;
    }

    public String getRole() {
        if (roleId != null) {
            return USER_ROLE.get(roleId).toString();
        } else {
            return null;
        }
    }

    public void setRole(String roleName) {
        if (roleName != null) {
            this.roleId = USER_ROLE.valueOf(roleName).ordinal();
        }
    }
}
