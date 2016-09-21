/*
 * Copyright 2015-2016 Jeeva Kandasamy (jkandasa@gmail.com)
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.api.SystemApi;
import org.mycontroller.standalone.auth.AuthUtils;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.User;

import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.table.TableUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Slf4j
public abstract class MigrationBase implements JdbcMigration {

    protected void loadDao() {
        //Load Dao's if not loaded already
        if (!DaoUtils.isDaoInitialized()) {
            DaoUtils.loadAllDao();
        }

        //Load properties from database
        AppProperties.getInstance().loadPropertiesFromDb();
    }

    protected void reloadDao() {
        DaoUtils.setIsDaoInitialized(false);
        loadDao();
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

    protected void renameColumn(String tableName, String oldColumnName, String newColumnName) throws SQLException {
        if (hasColumn(tableName, oldColumnName)) {
            int dropCount = DaoUtils.getUserDao().getDao().executeRaw(
                    "ALTER TABLE " + tableName.toUpperCase() + " ALTER COLUMN " + oldColumnName.toUpperCase()
                            + " RENAME TO " + newColumnName.toUpperCase());
            _logger.debug("Renamed OldColumn:{}, NewColumn:{}, Table:{}, Drop count:{}", oldColumnName, newColumnName,
                    tableName, dropCount);
        } else {
            _logger.warn("Selected column[{}] not found! Table:{}", oldColumnName, tableName);
        }
    }

    protected void alterColumn(String tableName, String columnName, String columnDefinition) throws SQLException {
        int alterCount = DaoUtils.getUserDao().getDao().executeRaw("ALTER TABLE "
                + tableName.toUpperCase() + " ALTER COLUMN " + columnName.toUpperCase() + " " + columnDefinition);
        _logger.debug("Altered column:{}, columnDefinition:{}, table:{}, add count:{}",
                columnName, columnDefinition, tableName, alterCount);
    }

    protected void addColumn(String tableName, String columnName, String columnDefinition) throws SQLException {
        int addCount = DaoUtils.getUserDao().getDao().executeRaw(
                "ALTER TABLE " + tableName.toUpperCase() + " ADD COLUMN IF NOT EXISTS "
                        + columnName.toUpperCase() + " " + columnDefinition);
        _logger.debug("Added column:{}, columnDefinition:{}, table:{}, add count:{}",
                columnName, columnDefinition, tableName, addCount);
    }

    protected boolean hasTable(String tableName) {
        try {
            String[] queryResult = DaoUtils.getUserDao().getDao().queryRaw(
                    "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = '"
                            + tableName.toUpperCase() + "'").getFirstResult();
            if (queryResult != null
                    && queryResult.length > 0
                    && queryResult[0] != null
                    && queryResult[0].length() > 0) {
                return queryResult[0].equalsIgnoreCase(tableName);
            }
        } catch (SQLException sEx) {
            _logger.error("Exception,", sEx);
        }
        return false;
    }

    protected void renameTable(String tableName, String newTableName) throws SQLException {
        if (hasTable(tableName)) {
            int changeCount = DaoUtils.getUserDao().getDao().executeRaw(
                    "ALTER TABLE " + tableName.toUpperCase() + " RENAME TO " + newTableName.toUpperCase());
            _logger.debug("Renamed table:{}, NewTable:{}, Change count:{}", tableName, newTableName, changeCount);
        } else {
            _logger.warn("Selected table[{}] not found!", tableName);
        }
    }

    protected void dropTable(String tableName) throws SQLException {
        if (hasTable(tableName)) {
            int dropCount = DaoUtils.getUserDao().getDao().executeRaw("DROP TABLE " + tableName.toUpperCase());
            _logger.debug("Dropped table:{}, drop count:{}", tableName, dropCount);
        } else {
            _logger.warn("Selected table[{}] not found!", tableName);
        }
    }

    protected void dropTable(Class<?> entity) throws SQLException {
        TableUtils.dropTable(DaoUtils.getUserDao().getDao().getConnectionSource(), entity, true);
    }

    protected void createTable(Class<?> entity) throws SQLException {
        TableUtils.createTableIfNotExists(DaoUtils.getUserDao().getDao().getConnectionSource(), entity);
    }

    @SuppressWarnings("unchecked")
    protected List<HashMap<String, String>> getRows(String tableName) {
        List<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();
        try {
            if (!hasTable(tableName)) {
                return null;
            }
            GenericRawResults<String[]> queryResult = DaoUtils.getUserDao().getDao()
                    .queryRaw("SELECT * FROM " + tableName.toUpperCase());
            String[] columnNames = queryResult.getColumnNames();
            HashMap<String, String> row = new HashMap<String, String>();
            for (String[] values : queryResult.getResults()) {
                row.clear();
                for (int columnNo = 0; columnNo < columnNames.length; columnNo++) {
                    row.put(columnNames[columnNo], values[columnNo]);
                }
                result.add((HashMap<String, String>) row.clone());
            }

        } catch (SQLException ex) {
            _logger.error("Exception, ", ex);
            return null;
        }

        return result;
    }

    protected HashMap<String, String> getRow(String tableName, String column, String value) {
        return getRow(getRows(tableName), column, value);
    }

    protected HashMap<String, String> getRow(List<HashMap<String, String>> rows, String column, String value) {
        for (HashMap<String, String> row : rows) {
            if (row.get(column).equals(value)) {
                return row;
            }
        }
        return null;
    }

    protected String getColumnName(String columnName) {
        return columnName.toUpperCase();
    }

    protected User getAdminUser() {
        //Get admin user
        List<User> users = DaoUtils.getUserDao().getAll();
        User user = null;
        for (User userTmp : users) {
            if (AuthUtils.isSuperAdmin(userTmp)) {
                user = userTmp;
                break;
            }
        }
        if (user == null) {
            throw new IllegalAccessError(
                    "There is no admin user in this database. For this migration a admin user required!");
        }
        return user;
    }

    protected void executeRaw(String rawQuery) throws SQLException {
        int count = DaoUtils.getUserDao().getDao().executeRaw(rawQuery);
        _logger.debug("count:{}", count);
    }

    protected String getApplicationDbVersion() {
        return new SystemApi().getAbout().getApplicationDbVersion();
    }
}
