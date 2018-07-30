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
package org.mycontroller.standalone.api.jaxrs.utils;

import java.util.HashMap;

import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.User;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
//As we are using rest basic authentication, this map used to increase retrieve speed
public class UserMapper {
    private static HashMap<String, User> usersList = new HashMap<String, User>();

    public static User getUser(String userName) {
        if (usersList.get(userName) == null) {
            usersList.put(userName, DaoUtils.getUserDao().getByUsername(userName));
        }
        return usersList.get(userName);
    }

    public static void removeUser(String userName) {
        usersList.remove(userName);
    }
}