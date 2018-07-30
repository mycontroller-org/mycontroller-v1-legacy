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
import org.mycontroller.standalone.db.tables.UidTag;
import org.mycontroller.standalone.settings.MyControllerSettings;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Slf4j
public class V1_02_02__2016_Apr_11 extends MigrationBase {

    @Override
    public void migrate(Connection connection) throws Exception {
        _logger.debug("Migration triggered.");

        //Load dao's
        loadDao();

        /** Migration comments
         *  Description:
         *  1. "tableRowsLimit" global settings added
         *  2. Changed UID settings table
         **/

        /** Migration #1
         * "tableRowsLimit" global settings added
         * steps
         * 1. read old settings update if entry is null
         * */
        if (MyControllerSettings.get().getTableRowsLimit() == null) {
            MyControllerSettings.builder().tableRowsLimit(10).build().save();
            AppProperties.getInstance().loadPropertiesFromDb();//reload properties
        }

        /** Migration #2
         * "Changed UID settings table
         * Steps
         * 1. Remove old table
         * 2. Create new table
         */
        sqlClient().dropTable("uid_tag");//Drop table
        sqlClient().createTable(UidTag.class);//create table

        _logger.info("Migration completed successfully.");
    }

}
