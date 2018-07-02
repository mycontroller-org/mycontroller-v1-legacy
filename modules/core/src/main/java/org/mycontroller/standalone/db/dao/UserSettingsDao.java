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
package org.mycontroller.standalone.db.dao;

import java.util.List;

import org.mycontroller.standalone.db.tables.User;
import org.mycontroller.standalone.db.tables.UserSettings;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public interface UserSettingsDao {
    void create(UserSettings userSettings);

    void createOrUpdate(UserSettings userSettings);

    void delete(UserSettings userSettings);

    void delete(User user, String key);

    void update(UserSettings userSettings);

    List<UserSettings> getAll();

    UserSettings get(User user, String key);

    UserSettings get(UserSettings userSettings);

    List<UserSettings> get(User user, List<String> keys);

    List<UserSettings> getLike(User user, String key);
}
