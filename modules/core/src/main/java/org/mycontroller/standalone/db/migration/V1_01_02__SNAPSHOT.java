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

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Slf4j
public class V1_01_02__SNAPSHOT extends MigrationBase {

    private static final String TABLE_ALARM_DEFINITION = "alarm_definition";
    private static final String TABLE_NOTIFICATION = "notification";
    private static final String TABLE__ALARM_NOTIFICATION_MAP = "alarm_notification_map";

    @Override
    public void migrate(Connection connection) throws Exception {
        _logger.debug("Migration triggered.");

        //Load dao's
        loadDao();

        /** Migration comments
         *  Description: alarm now supports for multiple notifications.
         *  To support this we have removed fields from alarmDefinition table and added new table OperationTable
         *  1. Remove columns (TBD) from alarm definition table, copy notifications to notification table if possible
         *  2. OperationTable table will be created automatically by ormlite
         *  3. Timer table 'LastFired' renamed to 'lastFire'
         *  4. Add gateway and node monitor job in to system job
         **/

        StringBuilder sqlQuery = new StringBuilder();

        //Migration #1
        //uniquecombo to unique 'name'
        String[] oldTableColumns = { "notificationType", "variable1", "variable2", "variable3", "variable4",
                "variable5", "variable6", "variable7" };
        if (sqlClient().hasColumn(TABLE_ALARM_DEFINITION, oldTableColumns[0])) {
            //Insert notification values from alarmDefinition table to OperationTable table.
            sqlQuery.setLength(0);
            sqlQuery.append("INSERT INTO ").append(TABLE_NOTIFICATION).append(" (")
                    .append("enabled").append(", ")
                    .append("publicAccess").append(", ")
                    .append("name").append(", ")
                    .append("type").append(", ")
                    .append("userId").append(", ")
                    .append("variable1").append(", ")
                    .append("variable2").append(", ")
                    .append("variable3").append(", ")
                    .append("variable4").append(", ")
                    .append("lastExecution").append(" )")
                    .append(" SELECT ")
                    .append(true).append(" AS ").append("enabled").append(", ")
                    .append(true).append(" AS ").append("publicAccess").append(", ")
                    .append("name").append(", ")
                    .append(oldTableColumns[0]).append(" AS ").append("type").append(", ")
                    .append(sqlClient().getAdminUser().getId()).append(" AS ").append("userId").append(", ")
                    //Change userId
                    .append(oldTableColumns[1]).append(", ")
                    .append(oldTableColumns[2]).append(", ")
                    .append(oldTableColumns[3]).append(", ")
                    .append(oldTableColumns[4]).append(", ")
                    .append("lastTrigger").append(" AS ").append("lastExecution")
                    .append(" FROM ").append(TABLE_ALARM_DEFINITION);
            _logger.debug("INSERT sql query:[{}]", sqlQuery.toString());
            int insertCount = DaoUtils.getRuleDefinitionDao().getDao().executeRaw(sqlQuery.toString());
            _logger.debug("Insert count:{}", insertCount);

            //Map notification and alarmDefinition table
            List<HashMap<String, String>> alarmRows = sqlClient().getRows(TABLE_ALARM_DEFINITION);
            List<HashMap<String, String>> notificationRows = sqlClient().getRows(TABLE_NOTIFICATION);
            for (HashMap<String, String> alarm : alarmRows) {
                sqlQuery.setLength(0);
                sqlQuery.append("INSERT INTO ").append(TABLE__ALARM_NOTIFICATION_MAP)
                        .append(" (notificationId,alarmDefinitionId) VALUES(")
                        .append(sqlClient().getRow(notificationRows, "name", alarm.get("name")))
                        .append(alarm.get("id"))
                        .append(")");
                _logger.debug("INSERT sql query:[{}]", sqlQuery.toString());
                insertCount = DaoUtils.getRuleDefinitionDao().getDao().executeRaw(sqlQuery.toString());
                _logger.debug("Insert count:{}", insertCount);
            }

            //Drop columns from RuleDefinitionTable table
            for (String columnName : oldTableColumns) {
                sqlClient().dropColumn(TABLE_ALARM_DEFINITION, columnName);
            }
        }

        //Migration #2
        //Nothing to do

        //Migration #3
        String oldColumnName = "lastFired";
        if (sqlClient().hasColumn("timer", oldColumnName)) {
            sqlClient().renameColumn("timer", oldColumnName, "lastFire");
        }

        //Migration #4
        // Add a job to monitor Alarm definitions with active time dampening
        // run this job every 30 seconds once
        //DataBaseUtils.createSystemJob("Alarm definition gateway monitor", "20,50 * * * * ? *", true,
        //      RuleDefinitionMonitorGatewayAndNode.class);

        _logger.info("Migration completed successfully.");
    }
}
