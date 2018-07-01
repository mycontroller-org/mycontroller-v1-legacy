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

import java.sql.SQLException;

import org.mycontroller.standalone.db.DaoUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Slf4j
public class ClientMysql extends ClientBase implements IMigrationClient {

    public void renameColumn(String tableName, String oldColumnName, String newColumnName) throws SQLException {
        if (hasColumn(tableName, oldColumnName)) {
            int dropCount = DaoUtils.getUserDao().getDao().executeRaw(
                    "ALTER TABLE " + getTableName(tableName) + " CHANGE COLUMN "
                            + getColumnName(oldColumnName) + " " + getColumnName(newColumnName));
            _logger.debug("Renamed OldColumn:{}, NewColumn:{}, Table:{}, Drop count:{}", oldColumnName, newColumnName,
                    tableName, dropCount);
        } else {
            _logger.warn("Selected column[{}] not found! Table:{}", oldColumnName, tableName);
        }
    }

    public void alterColumn(String tableName, String columnName, String columnDefinition) throws SQLException {
        int alterCount = DaoUtils.getUserDao().getDao().executeRaw("ALTER TABLE "
                + getTableName(tableName) + " MODIFY COLUMN " + getColumnName(columnName) + " " + columnDefinition);
        _logger.debug("Altered column:{}, columnDefinition:{}, table:{}, add count:{}",
                columnName, columnDefinition, tableName, alterCount);
    }

    public void renameTable(String tableName, String newTableName) throws SQLException {
        if (hasTable(tableName)) {
            int changeCount = DaoUtils.getUserDao().getDao().executeRaw(
                    "RENAME TABLE " + getTableName(tableName) + " TO " + getColumnName(newTableName));
            _logger.debug("Renamed table:{}, NewTable:{}, Change count:{}", tableName, newTableName, changeCount);
        } else {
            _logger.warn("Selected table[{}] not found!", tableName);
        }
    }

    @Override
    public void createIndex(String indexSuffix, String tableName, String columnName) throws SQLException {
        if (hasColumn(tableName, columnName)) {
            DaoUtils.getUserDao().getDao().executeRaw(
                    "CREATE INDEX " + getIndexName(indexSuffix, tableName, columnName) + " ON "
                            + getTableName(tableName) + "(" + getColumnName(columnName) + ")");
        }
    }

    @Override
    public void dropSequence(String sequenceName) throws SQLException {
        int dropCount = DaoUtils.getUserDao().getDao()
                .executeRaw("DROP SEQUENCE IF EXISTS " + getSequenceName(sequenceName));
        _logger.info("Dropped sequence:{}, drop count:{}", sequenceName, dropCount);
    }
}
