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
package org.mycontroller.standalone.db.migration;

import java.sql.Connection;
import java.util.List;

import org.mycontroller.standalone.auth.McCrypt;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.User;
import org.mycontroller.standalone.settings.SettingsUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Slf4j
public class V1_02_03__2016_Apr_14 extends MigrationBase {

    @Override
    public void migrate(Connection connection) throws Exception {
        _logger.debug("Migration triggered.");

        //Load dao's
        loadDao();

        /** Migration comments
         *  Description:
         *  1. Implemented encryption
         *  2. Remove 'publicAccess' column from 'operation' table
         **/

        /** Migration #1
         * Implemented encryption
         * Do migration for users, settings(email password, pushbullet auth, sms auth id)
         * steps
         * 1. read password from database table
         * 2. do encrypt and upload
         * */
        //Users password
        List<User> users = DaoUtils.getUserDao().getAll();
        for (User user : users) {
            user.setPassword(McCrypt.encrypt(user.getPassword()));
            DaoUtils.getUserDao().update(user);
        }
        //email password
        String emailPassword = SettingsUtils.getValue("email", "smtpPassword");
        if (emailPassword != null) {
            SettingsUtils.updateValue("email", "smtpPassword", McCrypt.encrypt(emailPassword));
        }
        //sms auth token
        String authToken = SettingsUtils.getValue("sms", "authToken");
        if (emailPassword != null) {
            SettingsUtils.updateValue("sms", "authToken", McCrypt.encrypt(authToken));
        }
        //pushbullet access token
        String accessToken = SettingsUtils.getValue("pushbullet", "accessToken");
        if (emailPassword != null) {
            SettingsUtils.updateValue("pushbullet", "accessToken", McCrypt.encrypt(accessToken));
        }

        /** Migration #2
         * Remove 'publicAccess' column from 'operation' table
         * */
        if (sqlClient().hasColumn("operation", "publicAccess")) {
            sqlClient().dropColumn("operation", "publicAccess");
        }

        _logger.info("Migration completed successfully.");
    }

}
