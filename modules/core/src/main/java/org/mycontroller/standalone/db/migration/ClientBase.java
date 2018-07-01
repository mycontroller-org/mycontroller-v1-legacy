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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.mycontroller.standalone.AppProperties;
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
public class ClientBase {

    public void addColumn(String tableName, String columnName, String columnDefinition) throws SQLException {
        if (!hasColumn(tableName, columnName)) {
            int addCount = DaoUtils
                    .getUserDao().getDao().executeRaw(
                            "ALTER TABLE " + getTableName(tableName) + " ADD COLUMN " + getColumnName(columnName)
                                    + " " + columnDefinition);
            _logger.debug("Added column:{}, columnDefinition:{}, table:{}, add count:{}",
                    columnName, columnDefinition, tableName, addCount);
        }
    }

    public boolean hasColumn(String tableName, String columnName) throws SQLException {
        if (!hasTable(tableName)) {
            return false;
        }
        try {
            // test if the column already exists
            DaoUtils.getUserDao().getDao()
                    .queryRaw("SELECT count(" + getColumnName(columnName) + ") FROM " + getTableName(tableName));
            return true;
        } catch (SQLException ex) {
            return false;
        }
    }

    public void dropColumn(String tableName, String columnName) throws SQLException {
        if (hasColumn(tableName, columnName)) {
            int dropCount = DaoUtils.getUserDao().getDao().executeRaw(
                    "ALTER TABLE " + getTableName(tableName) + " DROP COLUMN " + getColumnName(columnName));
            _logger.debug("Droupped column:{}, Table:{}, Drop count:{}", columnName, tableName, dropCount);
        }
    }

    public boolean hasTable(String tableName) {
        try {
            // test if the table already exists
            DaoUtils.getUserDao().getDao().queryRaw("SELECT count(*) FROM " + getTableName(tableName));
            return true;
        } catch (SQLException ex) {
            return false;
        }
    }

    public void dropTable(String tableName) throws SQLException {
        if (hasTable(tableName)) {
            int dropCount = DaoUtils.getUserDao().getDao().executeRaw("DROP TABLE " + getTableName(tableName));
            _logger.debug("Dropped table:{}, drop count:{}", tableName, dropCount);
        } else {
            _logger.warn("Selected table[{}] not found!", tableName);
        }
    }

    public void dropTable(Class<?> entity) throws SQLException {
        TableUtils.dropTable(DaoUtils.getUserDao().getDao().getConnectionSource(), entity, true);
    }

    public void createTable(Class<?> entity) throws SQLException {
        TableUtils.createTableIfNotExists(DaoUtils.getUserDao().getDao().getConnectionSource(), entity);
    }

    public List<HashMap<String, String>> getRows(String tableName) {
        return getRowsByQuery("SELECT * FROM " + getTableName(tableName));
    }

    @SuppressWarnings("unchecked")
    public List<HashMap<String, String>> getRowsByQuery(String query) {
        List<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();
        try {
            GenericRawResults<String[]> queryResult = DaoUtils.getUserDao().getDao().queryRaw(query);
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

    public HashMap<String, String> getRow(String tableName, String column, String value) {
        return getRow(getRows(tableName), column, value);
    }

    public HashMap<String, String> getRow(List<HashMap<String, String>> rows, String column, String value) {
        for (HashMap<String, String> row : rows) {
            if (row.get(column).equals(value)) {
                return row;
            }
        }
        return null;
    }

    public String getColumnName(String columnName) {
        switch (AppProperties.getInstance().getDbType()) {
            case H2DB:
            case H2DB_EMBEDDED:
                return columnName.toUpperCase();
            case POSTGRESQL:
                return "\"" + columnName + "\"";
            default:
                return columnName;
        }
    }

    public String getTableName(String tableName) {
        switch (AppProperties.getInstance().getDbType()) {
            case H2DB:
            case H2DB_EMBEDDED:
                return tableName.toUpperCase();
            case POSTGRESQL:
                return "\"" + tableName + "\"";
            default:
                return tableName;
        }
    }

    public String getSequenceName(String sequence) {
        switch (AppProperties.getInstance().getDbType()) {
            case H2DB:
            case H2DB_EMBEDDED:
                return sequence.toUpperCase();
            case POSTGRESQL:
                return "\"" + sequence + "\"";
            default:
                return sequence;
        }
    }

    public User getAdminUser() {
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

    public void executeRaw(String rawQuery) throws SQLException {
        int count = DaoUtils.getUserDao().getDao().executeRaw(rawQuery);
        _logger.debug("count:{}", count);
    }

    protected String getIndexName(String indexSuffix, String tableName, String columnName) {
        return getTableName(tableName) + "_" + getColumnName(columnName) + "_" + getColumnName(indexSuffix);
    }

    public void createIndex(String indexSuffix, String tableName, String columnName) throws SQLException {
        if (hasColumn(tableName, columnName)) {
            DaoUtils.getUserDao().getDao().executeRaw(
                    "CREATE INDEX " + getIndexName(indexSuffix, tableName, columnName) + " ON "
                            + getTableName(tableName) + "(" + getColumnName(columnName) + ")");
        }
    }

    public String getDatabaseSchemaVersion() {
        return AppProperties.getInstance().getControllerSettings().getDbVersion();
    }

    public int getDatabaseSchemaVersionInt() {
        String schemaVersion = getDatabaseSchemaVersion(); //Example: 1.03.05 - 2016 Nov 18
        int version = 0;
        if (schemaVersion != null) {
            version = Integer.valueOf(schemaVersion.split("-")[0].replaceAll("\\.", "").trim());
        }
        return version;
    }

    public void renameSequence(String sequenceName, String newSequenceName) throws SQLException {
        _logger.debug("Rename sequence not supported in {}", AppProperties.getInstance().getDbType().getText());
    }

}
