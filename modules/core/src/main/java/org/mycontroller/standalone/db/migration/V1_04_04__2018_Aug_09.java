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
import java.util.UUID;

import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.Settings;
import org.mycontroller.standalone.db.tables.User;
import org.mycontroller.standalone.settings.Dashboard;
import org.mycontroller.standalone.settings.DashboardSettings;
import org.mycontroller.standalone.settings.SettingsUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.4.0
 */
@Slf4j
public class V1_04_04__2018_Aug_09 extends MigrationBase {

    @Override
    public void migrate(Connection connection) throws Exception {
        _logger.debug("Migration triggered.");

        //Load dao's
        loadDao();

        /** Migration comments
         *  Description:
         *  1. Migrate dashboard row configuration from database to disk
         **/

        // execute only if running on existing db
        int schemaVersion = sqlClient().getDatabaseSchemaVersionInt();
        _logger.debug("Schema version:{}", schemaVersion);
        if (schemaVersion != 0 && schemaVersion < 10404) {
            // read all the users from database
            List<User> users = DaoUtils.getUserDao().getAll();

            for (User user : users) {
                // read dashboard from settings table
                List<Settings> settingsList = SettingsUtils.getSettingsList(user.getId(), Dashboard.KEY_DASHBOARD);

                // update settings
                for (Settings settings : settingsList) {
                    // add uuid for disk reference
                    settings.setValue4(UUID.randomUUID().toString());
                    // store row data into disk
                    DashboardSettings.writeToDisk(settings.getValue4(), settings.getValue2());
                    // remove row data and save settings
                    settings.setValue2(null);
                    SettingsUtils.updateSettings(settings);
                }
            }
        }
        reloadDao();
        _logger.info("Migration completed successfully.");
    }
}
