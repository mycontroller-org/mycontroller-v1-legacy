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

import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.NodeUtils.NODE_REGISTRATION_STATE;
import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.db.tables.Settings;
import org.mycontroller.standalone.jobs.ExecuteDiscoverJob;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Slf4j
public class V1_02_09__2016_Jul_16 extends MigrationBase {

    @Override
    public void migrate(Connection connection) throws Exception {
        _logger.debug("Migration triggered.");

        //Load dao's
        loadDao();

        /** Migration comments
         *  Description:
         *  1. Node table changes
         *  2. new settings added in controller settings
         **/

        /** Migration #1
         * Node table changed
         * steps
         * 1. Added new columns: parentId, registrationState, properties
         * 2. We cannot put 'registrationState' as NULL for existing values,
         *  create NULL allowed column, update existing values. Change to NULL not allowed
         * 3. removed column: otherData
         * */
        //Execute only if changes not available in database
        if (!sqlClient().hasColumn("node", "registrationState")) {
            sqlClient().addColumn("node", "registrationState", "VARCHAR(100)");
            sqlClient().addColumn("node", "parentNodeEui", "VARCHAR(255)");
            sqlClient().addColumn("node", "properties", "BLOB");
            sqlClient().dropColumn("node", "otherData");
            reloadDao();
            List<Node> nodes = DaoUtils.getNodeDao().getAll();
            for (Node node : nodes) {
                node.setRegistrationState(NODE_REGISTRATION_STATE.REGISTERED);
                DaoUtils.getNodeDao().update(node);
            }
            sqlClient().alterColumn("node", "registrationState", "VARCHAR(100) NOT NULL");
        }

        /** Migration #2
         *  New settings added in controller settings
         * steps
         * 1. Added new settings: autoNodeRegistration, executeDiscoverInterval
         * */
        //Create autoNodeRegistration
        Settings settings = Settings.builder()
                .key("myController")
                .subKey("autoNodeRegistration")
                .value("true")
                .build();
        DaoUtils.getSettingsDao().create(settings);
        //Create executeDiscoverInterval
        settings = Settings.builder()
                .key("myController")
                .subKey("executeDiscoverInterval")
                .value(String.valueOf(ExecuteDiscoverJob.DEFAULT_EXECUTE_DISCOVER_INTERVAL))
                .build();
        DaoUtils.getSettingsDao().create(settings);

        _logger.info("Migration completed successfully.");
    }

}
