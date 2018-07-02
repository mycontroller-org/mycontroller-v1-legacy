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
import org.mycontroller.standalone.db.tables.Room;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Slf4j
public class V1_03_02__2016_Aug_06 extends MigrationBase {

    @Override
    public void migrate(Connection connection) throws Exception {
        _logger.debug("Migration triggered.");

        //Load dao's
        loadDao();

        /** Migration comments
         *  Description:
         *  1. Changed room name unique to combination of name and parent id.
         **/

        /** Migration #1
         * Room table changed
         * steps
         * 1. Get all the data about room
         * 2. Drop room table
         * 3. re crate room table
         * 4. Update data to new table
         * */
        List<Room> rooms = DaoUtils.getRoomDao().getAll();
        sqlClient().dropTable(Room.class);
        sqlClient().createTable(Room.class);
        if (rooms != null && !rooms.isEmpty()) {
            for (Room room : rooms) {
                DaoUtils.getRoomDao().create(room);
            }
        }

        _logger.info("Migration completed successfully.");
    }

}
