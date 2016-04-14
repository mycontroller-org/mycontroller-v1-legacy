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
package org.mycontroller.standalone.api.jaxrs.json;

import java.util.ArrayList;
import java.util.List;

import org.mycontroller.standalone.auth.McCrypt;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.Role;
import org.mycontroller.standalone.db.tables.RoleUserMap;
import org.mycontroller.standalone.db.tables.User;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
@NoArgsConstructor
@ToString(includeFieldNames = true)
@Data
public class UserJson {

    private User user;
    private List<Integer> roles;
    private String currentPassword;

    @JsonIgnore
    public void mapResources(User user) {
        this.user = user;
        if (user.getId() != null) {
            List<RoleUserMap> roleUserMaps = DaoUtils.getRoleUserMapDao().getByUserId(user.getId());
            roles = new ArrayList<Integer>();
            for (RoleUserMap userMap : roleUserMaps) {
                roles.add(userMap.getRole().getId());
            }
        }
    }

    @JsonIgnore
    public void createOrUpdateUser() {
        if (user.getId() != null) {
            //clear all old mapping
            if (user.getPassword() == null) {
                User userOld = DaoUtils.getUserDao().get(user);
                user.setPassword(userOld.getPassword());
            } else {
                user.setPassword(McCrypt.encrypt(user.getPassword()));
            }
            removeMapping(user.getId());
            //Update user
            DaoUtils.getUserDao().update(user);
        } else {
            DaoUtils.getUserDao().create(user);
        }

        //Update roles
        if (roles != null) {
            Role role = Role.builder().build();
            RoleUserMap roleUserMap = RoleUserMap.builder().user(user).build();
            for (Integer roleId : roles) {
                role.setId(roleId);
                roleUserMap.setRole(role);
                DaoUtils.getRoleUserMapDao().create(roleUserMap);
            }
        }
    }

    @JsonIgnore
    public void updateProfile() {
        if (user.getId() != null) {
            User userOld = DaoUtils.getUserDao().get(user);
            user.setUsername(userOld.getUsername());//Self should not change user name
            if (user.getPassword() == null || currentPassword == null
                    || user.getPassword().length() == 0 || currentPassword.length() == 0) {
                user.setPassword(userOld.getPassword());
            } else if (user.getPassword() != null && currentPassword != null) {
                if (!McCrypt.decrypt(userOld.getPassword()).equals(currentPassword)) {
                    throw new IllegalAccessError("Incorrect current password!");
                }else{
                    user.setPassword(McCrypt.encrypt(user.getPassword()));
                }
            }
            //Update user
            DaoUtils.getUserDao().update(user);
        } else {
            throw new IllegalAccessError("user id missing!");
        }
    }

    @JsonIgnore
    public void deleteUsers(List<Integer> userIds) {
        for (Integer id : userIds) {
            removeMapping(id);
        }
        DaoUtils.getUserDao().deleteByIds(userIds);
    }

    @JsonIgnore
    private void removeMapping(Integer id) {
        //clear all old mapping
        //TODO: Logged in user should not be removed
        DaoUtils.getRoleUserMapDao().deleteByUserId(id);
    }
}
