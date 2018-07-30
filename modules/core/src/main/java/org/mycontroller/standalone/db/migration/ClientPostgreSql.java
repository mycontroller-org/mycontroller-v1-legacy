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
import java.util.HashMap;
import java.util.List;

import org.mycontroller.standalone.db.DaoUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Slf4j
public class ClientPostgreSql extends ClientBase implements IMigrationClient {

    public void renameColumn(String tableName, String oldColumnName, String newColumnName) throws SQLException {
        if (hasColumn(tableName, oldColumnName)) {
            int dropCount = DaoUtils.getUserDao().getDao().executeRaw(
                    "ALTER TABLE " + getTableName(tableName) + " RENAME COLUMN "
                            + getColumnName(oldColumnName) + " TO " + getColumnName(newColumnName));
            _logger.debug("Renamed OldColumn:{}, NewColumn:{}, Table:{}, Drop count:{}", oldColumnName, newColumnName,
                    tableName, dropCount);
        } else {
            _logger.warn("Selected column[{}] not found! Table:{}", oldColumnName, tableName);
        }
    }

    public void alterColumn(String tableName, String columnName, String columnDefinition) throws SQLException {
        int alterCount = DaoUtils.getUserDao().getDao().executeRaw("ALTER TABLE "
                + tableName + " ALTER COLUMN " + getColumnName(columnName) + " TYPE " + columnDefinition);
        _logger.debug("Altered column:{}, columnDefinition:{}, table:{}, add count:{}",
                columnName, columnDefinition, tableName, alterCount);
    }

    public void renameTable(String tableName, String newTableName) throws SQLException {
        if (hasTable(tableName)) {
            int changeCount = DaoUtils.getUserDao().getDao().executeRaw(
                    "ALTER TABLE " + getTableName(tableName) + " RENAME TO " + getTableName(newTableName));
            _logger.debug("Renamed table:{}, NewTable:{}, Change count:{}", tableName, newTableName, changeCount);
        } else {
            _logger.warn("Selected table[{}] not found!", tableName);
        }
    }

    public void dropTable(String tableName) throws SQLException {
        if (hasTable(tableName)) {
            int dropCount = DaoUtils.getUserDao().getDao()
                    .executeRaw("DROP TABLE " + getTableName(tableName) + " CASCADE");
            _logger.debug("Dropped table:{}, drop count:{}", tableName, dropCount);
            List<HashMap<String, String>> rows = getRowsByQuery(
                    "SELECT c.relname as seq FROM pg_class c WHERE c.relkind = 'S' and c.relname like '"
                            + tableName + "_%_seq'");
            //Drop sequences
            for (HashMap<String, String> row : rows) {
                if (row.get("seq").matches(tableName + "_[a-z]*?_seq")) {
                    dropCount = DaoUtils.getUserDao().getDao()
                            .executeRaw("DROP SEQUENCE " + row.get("seq") + " CASCADE");
                    _logger.info("Dropped sequence:{}, drop count:{}", row.get("seq"), dropCount);
                } else {
                    _logger.info("Sequence does not matched:{}, table name:{}", row.get("seq"), tableName);
                }
            }
        } else {
            _logger.warn("Selected table[{}] not found!", tableName);
        }
    }

    @Override
    public void renameSequence(String sequenceName, String newSequenceName) throws SQLException {
        int changeCount = DaoUtils.getUserDao().getDao().executeRaw(
                "ALTER SEQUENCE IF EXISTS " + getSequenceName(sequenceName)
                        + " RENAME TO " + getSequenceName(newSequenceName));
        _logger.debug("Renamed sequence from '{}' to '{}', affected count:{}",
                sequenceName, newSequenceName, changeCount);

    }

    @Override
    public void dropSequence(String sequenceName) throws SQLException {
        int dropCount = DaoUtils.getUserDao().getDao()
                .executeRaw("DROP SEQUENCE IF EXISTS " + getSequenceName(sequenceName) + " CASCADE");
        _logger.info("Dropped sequence:{}, drop count:{}", sequenceName, dropCount);
    }

}
