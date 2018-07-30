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

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Slf4j
public class V1_03_06__2017_Mar_03 extends MigrationBase {

    @Override
    public void migrate(Connection connection) throws Exception {
        _logger.debug("Migration triggered.");

        //Load dao's
        loadDao();

        /** Migration comments
         *  Description:
         *  1. Remove NULL gateway ID in Node table
         **/

        //Execute only if running on existing db
        int schemaVersion = sqlClient().getDatabaseSchemaVersionInt();
        _logger.debug("Schema version:{}", schemaVersion);
        if (schemaVersion != 0 && schemaVersion < 10306) {
            //Update node table
            switch (AppProperties.getInstance().getDbType()) {
                case H2DB:
                case H2DB_EMBEDDED:
                    sqlClient().alterColumn("node", "gatewayId", "INTEGER NOT NULL");
                    break;
                case MARIADB:
                case MYSQL:
                    sqlClient().alterColumn("node", "gatewayId", "int(11) NOT NULL");
                    break;
                case POSTGRESQL:
                    sqlClient().alterColumn("node", "gatewayId", "integer NOT NULL");
                    break;
                default:
                    _logger.error("Not implemented for this type of db:[{}]", AppProperties.getInstance().getDbType());
            }
        }
        reloadDao();
        _logger.info("Migration completed successfully.");
    }
}
