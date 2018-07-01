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

import org.mycontroller.standalone.db.DB_TABLES;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.3.0
 */
@Slf4j
public class V1_04_03__2018_Jul_01 extends MigrationBase {

    @Override
    public void migrate(Connection connection) throws Exception {
        _logger.debug("Migration triggered.");

        //Load dao's
        loadDao();

        /** Migration comments
         *  Description:
         *  1. drop tables if exists [externalServer, externalServerResourceMap]
         *  2. rename database table [externalServer, externalServerResourceMap]
         *  3. drop sequence if exists [externalServer_id_seq]
         *  4. rename database sequence [externalServer_id_seq]
         **/

        //Execute only if running on existing db
        int schemaVersion = sqlClient().getDatabaseSchemaVersionInt();
        _logger.debug("Schema version:{}", schemaVersion);
        if (schemaVersion != 0 && schemaVersion < 10403) {
            // drop tables
            sqlClient().dropTable(DB_TABLES.EXTERNAL_SERVER);
            sqlClient().dropTable(DB_TABLES.EXTERNAL_SERVER_RESOURCE_MAP);

            // rename tables
            sqlClient().renameTable("externalServer", DB_TABLES.EXTERNAL_SERVER);
            sqlClient().renameTable("externalServerResourceMap", DB_TABLES.EXTERNAL_SERVER_RESOURCE_MAP);

            // drop sequence
            sqlClient().dropSequence("external_server_id_seq");

            // rename sequence
            sqlClient().renameSequence("externalServer_id_seq", "external_server_id_seq");
        }
        reloadDao();
        _logger.info("Migration completed successfully.");
    }
}
