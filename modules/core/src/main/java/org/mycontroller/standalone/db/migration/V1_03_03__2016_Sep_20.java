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

import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.Firmware;
import org.mycontroller.standalone.firmware.FirmwareUtils;
import org.mycontroller.standalone.offheap.OffHeapFactory;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Slf4j
public class V1_03_03__2016_Sep_20 extends MigrationBase {

    @Override
    public void migrate(Connection connection) throws Exception {
        _logger.debug("Migration triggered.");

        //Load dao's
        loadDao();

        /** Migration comments
         *  Description:
         *  1. Firmware table changes, Added new table Firmware_data
         *  2. Clear McPersistent location
         **/

        /** Migration #1
         * Firmware table changed
         * steps
         * 1. Added new columns: properties
         * 2. Update blocks and crc from old column
         * 3. Copy data to firmware_data table
         * 4. drop blocks, data and crc columns
         * */
        //Execute only if changes not available in database
        if (sqlClient().hasColumn("firmware", "blocks")) {
            //1. Add columns
            sqlClient().addColumn("firmware", "properties", "BLOB");
            reloadDao();
            //2. Update properties
            List<Firmware> firmwares = DaoUtils.getFirmwareDao().getAll();
            List<HashMap<String, String>> rows = sqlClient().getRows("FIRMWARE");
            if (firmwares != null && !firmwares.isEmpty()) {
                for (Firmware firmware : firmwares) {
                    HashMap<String, String> row = sqlClient().getRow(rows, "ID", String.valueOf(firmware.getId()));
                    if (row != null) {
                        firmware.getProperties().put(Firmware.KEY_PROP_CRC, Integer.valueOf(row.get("CRC")));
                        firmware.getProperties().put(Firmware.KEY_PROP_BLOCKS, Integer.valueOf(row.get("BLOCKS")));
                        firmware.getProperties().put(Firmware.KEY_PROP_TYPE, FirmwareUtils.FILE_TYPE.HEX.getText());
                        firmware.getProperties().put(Firmware.KEY_PROP_BLOCK_SIZE,
                                FirmwareUtils.FIRMWARE_BLOCK_SIZE_HEX);
                        DaoUtils.getFirmwareDao().update(firmware);
                    }
                }
            }
            //3. Copy firmware data to FirmwareData table
            sqlClient().executeRaw("INSERT INTO firmware_data (firmwareId, data) SELECT id, data FROM firmware");
            //4. Drop columns
            sqlClient().dropColumn("firmware", "blocks");
            sqlClient().dropColumn("firmware", "crc");
            sqlClient().dropColumn("firmware", "data");

            //Reload Dao
            reloadDao();
        }

        /** Migration #2
         * Clear McPersistent location
         * */
        OffHeapFactory.reset();

        _logger.info("Migration completed successfully.");
    }
}
