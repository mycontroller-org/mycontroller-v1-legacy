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

import java.sql.SQLException;

import org.mycontroller.standalone.ObjectFactory;
import org.mycontroller.standalone.db.DaoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
public class MigrationBase {
    protected static final Logger _logger = LoggerFactory.getLogger(MigrationBase.class);

    protected void updateDao() {
        //Load Dao's if not loaded already
        if (!DaoUtils.isDaoInitialized()) {
            DaoUtils.loadAllDao();
        }

        //Load properties from database
        ObjectFactory.getAppProperties().loadPropertiesFromDb();
    }

    protected boolean hasColumn(String tableName, String columnName) throws SQLException {
        String[] queryResult = DaoUtils.getUserDao().getDao().queryRaw(
                "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE  TABLE_NAME = '"
                        + tableName.toUpperCase() + "'  AND COLUMN_NAME = '" + columnName.toUpperCase() + "'")
                .getFirstResult();
        if (queryResult != null && queryResult.length > 0 && queryResult[0] != null && queryResult[0].length() > 0) {
            return queryResult[0].equalsIgnoreCase(columnName);
        }
        return false;
    }

    protected void dropColumn(String tableName, String columnName) throws SQLException {
        int dropCount = DaoUtils.getUserDao().getDao().executeRaw(
                "ALTER TABLE " + tableName.toUpperCase() + " DROP COLUMN IF EXISTS " + columnName.toUpperCase());
        _logger.debug("Droupped column:{}, Table:{}, Drop count:{}", columnName, tableName, dropCount);
    }
}
