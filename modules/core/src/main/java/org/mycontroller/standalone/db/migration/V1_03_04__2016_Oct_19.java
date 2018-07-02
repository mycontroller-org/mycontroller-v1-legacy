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

import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.AppProperties.DB_TYPE;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Slf4j
public class V1_03_04__2016_Oct_19 extends MigrationBase {

    @Override
    public void migrate(Connection connection) throws Exception {
        _logger.debug("Migration triggered.");

        //Load dao's
        loadDao();

        /** Migration comments
         *  Description:
         *  1. Add new column on Node table for smartSleepEnabled
         **/

        /** Migration #1
         * Add new column on Node table for smartSleepEnabled
         * steps
         * 1. Add new column: smartSleepEmabled
         * */
        //Execute only if changes not available in database
        if (!sqlClient().hasColumn("node", "smartSleepEnabled")) {
            if (AppProperties.getInstance().getDbType() == DB_TYPE.POSTGRESQL) {
                sqlClient().addColumn("node", "smartSleepEnabled", "boolean");
            } else {
                sqlClient().addColumn("node", "smartSleepEnabled", "TINYINT");
            }

            //Reload Dao
            reloadDao();
        }
        _logger.info("Migration completed successfully.");
    }
}
