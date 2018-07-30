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
import java.util.HashMap;
import java.util.List;

import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.UidTag;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Slf4j
public class V1_03_05__2016_Nov_18 extends MigrationBase {

    @Override
    public void migrate(Connection connection) throws Exception {
        _logger.debug("Migration triggered.");

        //Load dao's
        loadDao();

        /** Migration comments
         *  Description:
         *  1. Add new column on UidTag table to support all type of resources
         *  2. remove old column sensorVariable
         **/

        /** Migration #1
         * Add new columns
         * steps
         * 1. Add new column: resourceType, resourceId
         * */
        //Execute only if 'sensorVariable' is available in database
        if (sqlClient().hasColumn("uid_tag", "sensorVariable")) {
            List<HashMap<String, String>> rows = sqlClient().getRows("uid_tag");

            //Drop table
            sqlClient().dropTable("uid_tag");
            //Create Table
            sqlClient().createTable(UidTag.class);
            //Reload Dao
            reloadDao();

            //Update old data
            for (HashMap<String, String> row : rows) {
                UidTag uidTag = UidTag.builder()
                        .uid(getValue(row, "uid"))
                        .resourceType(RESOURCE_TYPE.SENSOR_VARIABLE)
                        .resourceId(Integer.valueOf(getValue(row, "sensorVariable")))
                        .build();
                //_logger.info("UID: {}", uidTag);
                DaoUtils.getUidTagDao().create(uidTag);
            }

        }
        _logger.info("Migration completed successfully.");
    }
}
