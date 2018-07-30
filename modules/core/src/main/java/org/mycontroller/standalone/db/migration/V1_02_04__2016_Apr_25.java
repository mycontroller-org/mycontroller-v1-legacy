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

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Slf4j
public class V1_02_04__2016_Apr_25 extends MigrationBase {

    @Override
    public void migrate(Connection connection) throws Exception {
        _logger.debug("Migration triggered.");

        //Load dao's
        loadDao();

        /** Migration comments
         *  Description:
         *  1. Add 'parentId' and icon in 'Room' table
         **/

        /** Migration #1
         * Add 'parentId' and icon in 'Room' table
         * steps
         * 1. Add 'parentId' column in 'Room' table
         * 2. Add 'icon' column in 'Room' table
         * 3. Reload dao's
         * */
        //Add column 'parentId'
        sqlClient().addColumn("room", "parentId", "INTEGER");

        //Add column 'icon'
        sqlClient().addColumn("room", "icon", "VARCHAR(255)");

        //Reload dao
        reloadDao();

        _logger.info("Migration completed successfully.");
    }

}
