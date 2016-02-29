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

import java.sql.Connection;
import java.util.List;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import org.mycontroller.standalone.alarm.jobs.AlarmDefinitionMonitorGatewayAndNode;
import org.mycontroller.standalone.auth.AuthUtils;
import org.mycontroller.standalone.db.DB_TABLES;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.DataBaseUtils;
import org.mycontroller.standalone.db.tables.AlarmDefinition;
import org.mycontroller.standalone.db.tables.Notification;
import org.mycontroller.standalone.db.tables.NotificationAlarmDefinitionMap;
import org.mycontroller.standalone.db.tables.Timer;
import org.mycontroller.standalone.db.tables.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
public class V1_01_02__SNAPSHOT extends MigrationBase implements JdbcMigration {
    private static final Logger _logger = LoggerFactory.getLogger(V1_01_02__SNAPSHOT.class.getName());

    @Override
    public void migrate(Connection connection) throws Exception {
        _logger.debug("Migration triggered.");

        //Load dao's
        updateDao();

        /** Migration comments
         *  Description: alarm now supports for multiple notifications. 
         *  To support this we have removed fields from alarmDefinition table and added new table Notification
         *  1. Remove columns (TBD) from alarm definition table, copy notifications to notification table if possible
         *  2. Notification table will be created automatically by ormlite
         *  3. Timer table 'LastFired' renamed to 'lastFire'
         *  4. Add gateway and node monitor job in to system job
         **/

        StringBuilder sqlQuery = new StringBuilder();

        //Migration #1
        //uniquecombo to unique 'name'
        String[] oldTableColumns = { "notificationType", "variable1", "variable2", "variable3", "variable4",
                "variable5", "variable6", "variable7" };
        if (hasColumn(DB_TABLES.ALARM_DEFINITION, oldTableColumns[0])) {
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
            //Insert notification values from alarmDefinition table to Notification table.
            sqlQuery.setLength(0);
            sqlQuery.append("INSERT INTO ").append(DB_TABLES.NOTIFICATION).append(" (")
                    .append(Notification.KEY_ENABLED).append(", ")
                    .append(Notification.KEY_PUBLIC_ACCESS).append(", ")
                    .append(Notification.KEY_NAME).append(", ")
                    .append(Notification.KEY_TYPE).append(", ")
                    .append(Notification.KEY_USER_ID).append(", ")
                    .append(Notification.KEY_VARIABLE1).append(", ")
                    .append(Notification.KEY_VARIABLE2).append(", ")
                    .append(Notification.KEY_VARIABLE3).append(", ")
                    .append(Notification.KEY_VARIABLE4).append(", ")
                    .append(Notification.KEY_LAST_EXECUTION).append(" )")
                    .append(" SELECT ")
                    .append(true).append(" AS ").append(Notification.KEY_ENABLED).append(", ")
                    .append(true).append(" AS ").append(Notification.KEY_PUBLIC_ACCESS).append(", ")
                    .append(AlarmDefinition.KEY_NAME).append(" AS ").append(Notification.KEY_NAME).append(", ")
                    .append(oldTableColumns[0]).append(" AS ").append(Notification.KEY_TYPE).append(", ")
                    .append(user.getId()).append(" AS ").append(Notification.KEY_USER_ID).append(", ")//Change userId
                    .append(oldTableColumns[1]).append(" AS ").append(Notification.KEY_VARIABLE1).append(", ")
                    .append(oldTableColumns[2]).append(" AS ").append(Notification.KEY_VARIABLE2).append(", ")
                    .append(oldTableColumns[3]).append(" AS ").append(Notification.KEY_VARIABLE3).append(", ")
                    .append(oldTableColumns[4]).append(" AS ").append(Notification.KEY_VARIABLE4).append(", ")
                    .append(AlarmDefinition.KEY_LAST_TRIGGER).append(" AS ").append(Notification.KEY_LAST_EXECUTION)
                    .append(" FROM ").append(DB_TABLES.ALARM_DEFINITION);
            _logger.debug("INSERT sql query:[{}]", sqlQuery.toString());
            int insertCount = DaoUtils.getAlarmDefinitionDao().getDao().executeRaw(sqlQuery.toString());
            _logger.debug("Insert count:{}", insertCount);

            //Map notification and alarmDefinition table
            List<AlarmDefinition> alarmDefinitions = DaoUtils.getAlarmDefinitionDao().getAll();
            for (AlarmDefinition alarmDefinition : alarmDefinitions) {
                Notification notification = DaoUtils.getNotificationDao().getByNotificationName(
                        alarmDefinition.getName());
                NotificationAlarmDefinitionMap notificationAlarmDefinitionMap = NotificationAlarmDefinitionMap
                        .builder().alarmDefinition(alarmDefinition).notification(notification).build();
                DaoUtils.getNotificationAlarmDefinitionMapDao().create(notificationAlarmDefinitionMap);
            }

            //Drop columns from AlarmDefinition table
            for (String columnName : oldTableColumns) {
                dropColumn(DB_TABLES.ALARM_DEFINITION, columnName);
            }
        }

        //Migration #2
        //Nothing to do

        //Migration #3
        String oldColumnName = "lastFired";
        if (hasColumn(DB_TABLES.TIMER, oldColumnName)) {
            renameColumn(DB_TABLES.TIMER, oldColumnName, Timer.KEY_LAST_FIRE);
        }

        //Migration #4
        // Add a job to monitor Alarm definitions with active time dampening
        // run this job every 30 seconds once
        DataBaseUtils.createSystemJob("Alarm definition gateway monitor", "20,50 * * * * ? *", true,
                AlarmDefinitionMonitorGatewayAndNode.class);

        _logger.info("Migration completed successfully.");
    }
}
