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

import org.mycontroller.standalone.db.tables.User;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
public interface IMigrationClient {

    boolean hasColumn(String tableName, String columnName) throws SQLException;

    void dropColumn(String tableName, String columnName) throws SQLException;

    void renameColumn(String tableName, String oldColumnName, String newColumnName) throws SQLException;

    void alterColumn(String tableName, String columnName, String columnDefinition) throws SQLException;

    void addColumn(String tableName, String columnName, String columnDefinition) throws SQLException;

    boolean hasTable(String tableName) throws SQLException;

    void renameTable(String tableName, String newTableName) throws SQLException;

    void dropTable(String tableName) throws SQLException;

    void renameSequence(String sequenceName, String newSequenceName) throws SQLException;

    void dropSequence(String sequenceName) throws SQLException;

    void createIndex(String indexSuffix, String tableName, String columnName) throws SQLException;

    void dropTable(Class<?> entity) throws SQLException;

    void createTable(Class<?> entity) throws SQLException;

    List<HashMap<String, String>> getRows(String tableName);

    HashMap<String, String> getRow(String tableName, String column, String value);

    HashMap<String, String> getRow(List<HashMap<String, String>> rows, String column, String value)
            throws SQLException;

    String getColumnName(String columnName);

    String getTableName(String tableName);

    User getAdminUser();

    void executeRaw(String rawQuery) throws SQLException;

    String getDatabaseSchemaVersion();

    int getDatabaseSchemaVersionInt();

}
