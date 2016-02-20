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
package org.mycontroller.standalone.db.migration;

import java.sql.Connection;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
public class V1_01_02__SNAPSHOT extends MigrationBase implements JdbcMigration {
    private static final Logger _logger = LoggerFactory.getLogger(V1_01_02__SNAPSHOT.class.getName());

    @Override
    public void migrate(Connection connection) throws Exception {
        _logger.debug("Migration triggered.");

        /*//Load Dao's if not loaded already
        if (!DaoUtils.isDaoInitialized()) {
            DaoUtils.loadAllDao();
        }
        //Load properties from database
        ObjectFactory.getAppProperties().loadPropertiesFromDb();*/

        updateDao();

        /** Migration comments
         *  Description: alarm now supports for multiple notifications. 
         *  To support this we have removed fields from alarmDefinition table and added new table Notification
         *  1. Remove columns (TBD) from alarm definition table, copy notifications to notification table if possible
         *  2. Notification table will be created automatically by ormlite
         **/

        //Migration #1

        //Migration #2

        _logger.info("Migration completed successfully.");
    }

}
