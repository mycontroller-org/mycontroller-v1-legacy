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

import org.mycontroller.standalone.AppProperties.NETWORK_TYPE;
import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.AppProperties.STATE;
import org.mycontroller.standalone.db.DB_TABLES;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.GatewayTable;
import org.mycontroller.standalone.db.tables.OperationRuleDefinitionMap;
import org.mycontroller.standalone.db.tables.OperationTable;
import org.mycontroller.standalone.db.tables.OperationTimerMap;
import org.mycontroller.standalone.db.tables.RuleDefinitionTable;
import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.db.tables.Timer;
import org.mycontroller.standalone.db.tables.User;
import org.mycontroller.standalone.gateway.GatewayUtils.GATEWAY_TYPE;
import org.mycontroller.standalone.gateway.config.GatewayConfigEthernet;
import org.mycontroller.standalone.gateway.config.GatewayConfigMQTT;
import org.mycontroller.standalone.gateway.config.GatewayConfigSerial;
import org.mycontroller.standalone.metrics.MetricsUtils.METRIC_TYPE;
import org.mycontroller.standalone.operation.OperationUtils.OPERATION_TYPE;
import org.mycontroller.standalone.operation.model.OperationSendEmail;
import org.mycontroller.standalone.operation.model.OperationSendPayload;
import org.mycontroller.standalone.operation.model.OperationSendPushbulletNote;
import org.mycontroller.standalone.operation.model.OperationSendSMS;
import org.mycontroller.standalone.rule.RuleUtils.CONDITION_TYPE;
import org.mycontroller.standalone.rule.RuleUtils.DAMPENING_TYPE;
import org.mycontroller.standalone.rule.RuleUtils.DATA_TYPE;
import org.mycontroller.standalone.rule.RuleUtils.OPERATOR;
import org.mycontroller.standalone.rule.model.DampeningActiveTime;
import org.mycontroller.standalone.rule.model.DampeningConsecutive;
import org.mycontroller.standalone.rule.model.DampeningLastNEvaluations;
import org.mycontroller.standalone.rule.model.RuleDefinitionState;
import org.mycontroller.standalone.rule.model.RuleDefinitionString;
import org.mycontroller.standalone.rule.model.RuleDefinitionThreshold;
import org.mycontroller.standalone.timer.TimerUtils.FREQUENCY_TYPE;
import org.mycontroller.standalone.timer.TimerUtils.TIMER_TYPE;
import org.mycontroller.standalone.utils.McUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Slf4j
public class V1_02_01__2016_Mar_24 extends MigrationBase {

    @Override
    public void migrate(Connection connection) throws Exception {
        _logger.debug("Migration triggered.");

        //Load dao's
        loadDao();

        /**
         * Prerequisites: Need to done before this. V1_02_04__2016_Apr_25
         */
        V1_02_04__2016_Apr_25 prerequest = new V1_02_04__2016_Apr_25();
        prerequest.migrate(connection);

        /** Migration comments
         *  Description:
         *  1. "gateway" table changed
         *  2. "notification" table renamed to "operation" with modifications
         *  3. "alarm_definition" table renamed to "rule_definition" with modifications
         *  4. "alarm_notification_map" table renamed to "rule_operation_map" with modifications
         *  5. changes in "timer" table, added map for "operations"
         *  6. rename rule engine job
         **/

        /** Migration #1
         *"gateway" table changed
         * steps
         * 1. rename gateway to name to gateway_old
         * 2. create new table gateway
         * 3. migrate date from old table to new table
         * 4. drop old table
         * */
        if (sqlClient().hasColumn(DB_TABLES.GATEWAY, "variable1")) {
            String oldTableName = "gateway_old";
            sqlClient().renameTable(DB_TABLES.GATEWAY, oldTableName);
            //create table
            sqlClient().createTable(GatewayTable.class);
            List<HashMap<String, String>> rows = sqlClient().getRows(oldTableName);
            _logger.debug("Old table: {}", rows);
            for (HashMap<String, String> row : rows) {
                DaoUtils.getGatewayDao().create(
                        GatewayTable
                                .builder()
                                .id(McUtils.getInteger(row.get(sqlClient().getColumnName(GatewayTable.KEY_ID))))
                                .name(row.get(sqlClient().getColumnName(GatewayTable.KEY_NAME)))
                                .networkType(
                                        NETWORK_TYPE.valueOf(row.get(sqlClient().getColumnName(
                                                GatewayTable.KEY_NETWORK_TYPE))))
                                .type(GATEWAY_TYPE.valueOf(row.get(sqlClient().getColumnName(GatewayTable.KEY_TYPE))))
                                .state(STATE.valueOf(row.get(sqlClient().getColumnName(GatewayTable.KEY_STATE))))
                                .statusMessage(row.get(sqlClient().getColumnName(GatewayTable.KEY_STATUS_MESSAGE)))
                                .statusSince(
                                        McUtils.getLong(row.get(sqlClient().getColumnName(
                                                GatewayTable.KEY_STATUS_SINCE))))
                                .enabled(
                                        McUtils.getBoolean(row
                                                .get(sqlClient().getColumnName(GatewayTable.KEY_ENABLED))))
                                .properties(getGatewayProperties(row))
                                .build());
            }
            sqlClient().dropTable(oldTableName);
        }

        /** Migration #2
         * "notification" table renamed to "operation" with modifications
         * Steps
         * 1. migrate date from old table to new table
         * 2. drop old table
         */
        String notificationTable = "notification";
        if (sqlClient().hasTable(notificationTable)) {
            List<HashMap<String, String>> rows = sqlClient().getRows(notificationTable);
            _logger.debug("{} table data: {}", notificationTable, rows);
            for (HashMap<String, String> row : rows) {
                if (row.get(sqlClient().getColumnName(OperationTable.KEY_TYPE)).equalsIgnoreCase("PUSHBULLET_NOTE")) {
                    row.put(sqlClient().getColumnName(OperationTable.KEY_TYPE),
                            OPERATION_TYPE.SEND_PUSHBULLET_NOTE.name());
                }
                DaoUtils.getOperationDao().create(
                        OperationTable
                                .builder()
                                .id(McUtils.getInteger(row.get(sqlClient().getColumnName(OperationTable.KEY_ID))))
                                .name(row.get(sqlClient().getColumnName(OperationTable.KEY_NAME)))
                                .type(OPERATION_TYPE.valueOf(row.get(sqlClient()
                                        .getColumnName(OperationTable.KEY_TYPE))))
                                .user(User
                                        .builder()
                                        .id(McUtils.getInteger(row.get(sqlClient().getColumnName(
                                                OperationTable.KEY_USER_ID))))
                                        .build())
                                .enabled(
                                        McUtils.getBoolean(row.get(sqlClient().getColumnName(
                                                OperationTable.KEY_ENABLED))))
                                .lastExecution(
                                        McUtils.getLong(row.get(sqlClient().getColumnName(
                                                OperationTable.KEY_LAST_EXECUTION))))
                                .properties(getOperationProperties(row))
                                .build());
            }
            sqlClient().dropTable(notificationTable);
        }

        /** Migration #3
         * "alarm_definition" table renamed to "rule_definition" with modifications
         * Steps
         * 1. migrate date from old table to new table
         * 2. drop old table
         */
        String alarmTable = "alarm_definition";
        if (sqlClient().hasTable(alarmTable)) {
            List<HashMap<String, String>> rows = sqlClient().getRows(alarmTable);
            _logger.debug("{} table data: {}", alarmTable, rows);
            for (HashMap<String, String> row : rows) {
                DaoUtils.getRuleDefinitionDao()
                        .create(RuleDefinitionTable
                                .builder()
                                .id(McUtils.getInteger(row.get(sqlClient().getColumnName(RuleDefinitionTable.KEY_ID))))
                                .enabled(
                                        McUtils.getBoolean(row.get(sqlClient().getColumnName(
                                                RuleDefinitionTable.KEY_ENABLED))))
                                .disableWhenTrigger(
                                        McUtils.getBoolean(row
                                                .get(sqlClient().getColumnName(
                                                        RuleDefinitionTable.KEY_DISABLE_WHEN_TRIGGER))))
                                .name(row.get(sqlClient().getColumnName(RuleDefinitionTable.KEY_NAME)))
                                .resourceType(
                                        RESOURCE_TYPE.valueOf(row
                                                .get(sqlClient().getColumnName(RuleDefinitionTable.KEY_RESOURCE_TYPE))))
                                .resourceId(
                                        McUtils.getInteger(row.get(sqlClient().getColumnName(
                                                RuleDefinitionTable.KEY_RESOURCE_ID))))
                                .timestamp(
                                        McUtils.getLong(row.get(sqlClient().getColumnName(
                                                RuleDefinitionTable.KEY_TIMESTAMP))))
                                .lastTrigger(
                                        McUtils.getLong(row.get(sqlClient().getColumnName(
                                                RuleDefinitionTable.KEY_LAST_TRIGGER))))
                                .ignoreDuplicate(
                                        McUtils.getBoolean(row
                                                .get(sqlClient().getColumnName(
                                                        RuleDefinitionTable.KEY_IGNORE_DUPLICATE))))
                                .triggered(
                                        McUtils.getBoolean(row.get(sqlClient().getColumnName(
                                                RuleDefinitionTable.KEY_TRIGGERED))))
                                .dampeningType(
                                        DAMPENING_TYPE.valueOf(row.get(sqlClient()
                                                .getColumnName(RuleDefinitionTable.KEY_DAMPENING_TYPE))))
                                .conditionType(getConditionType(row))
                                .dampeningProperties(getDampeningProperties(row))
                                .conditionProperties(getConditionProperties(row))
                                .build());
            }
            sqlClient().dropTable(alarmTable);
        }

        /** Migration #4
         * "alarm_notification_map" table renamed to "rule_operation_map" with modifications
         * Steps
         * 1. migrate date from old table to new table
         * 2. drop old table
         */
        String alarmNotificationMapTable = "alarm_notification_map";
        if (sqlClient().hasTable(alarmNotificationMapTable)) {
            List<HashMap<String, String>> rows = sqlClient().getRows(alarmNotificationMapTable);
            _logger.debug("{} table data: {}", alarmNotificationMapTable, rows);
            for (HashMap<String, String> row : rows) {
                DaoUtils.getOperationRuleDefinitionMapDao().create(
                        OperationRuleDefinitionMap
                                .builder()
                                .operationTable(OperationTable.builder()
                                        .id(McUtils.getInteger(row.get("NOTIFICATIONID"))).build())
                                .ruleDefinitionTable(RuleDefinitionTable.builder()
                                        .id(McUtils.getInteger(row.get("ALARMDEFINITIONID"))).build())
                                .build());
            }
            sqlClient().dropTable(alarmNotificationMapTable);
        }

        /** Migration #5
         *"gateway" table changed
         * steps
         * 1. rename timer to name to timer_old
         * 2. create new table timer
         * 3. migrate date from old table to new table
         * 4. drop old table
         * */
        if (sqlClient().hasColumn(DB_TABLES.TIMER, "resourceType")) {
            String oldTableName = "timer_old";
            sqlClient().renameTable(DB_TABLES.TIMER, oldTableName);
            //create table
            sqlClient().createTable(Timer.class);
            List<HashMap<String, String>> rows = sqlClient().getRows(oldTableName);
            _logger.debug("Timer old table: {}", rows);
            for (HashMap<String, String> row : rows) {
                //Create operation
                String operationName = row.get(sqlClient().getColumnName(GatewayTable.KEY_NAME)) + " - DB Migration";
                String timerName = row.get(sqlClient().getColumnName(GatewayTable.KEY_NAME));
                HashMap<String, Object> properties = new HashMap<String, Object>();
                properties.put(OperationSendPayload.KEY_RESOURCE_TYPE,
                        RESOURCE_TYPE.get(McUtils.getInteger(row.get("RESOURCETYPE"))).getText());
                properties.put(OperationSendPayload.KEY_RESOURCE_ID, McUtils.getInteger(row.get("RESOURCEID")));
                properties.put(OperationSendPayload.KEY_PAYLOAD, row.get("PAYLOAD"));
                properties.put(OperationSendPayload.KEY_DELAY_TIME, null);

                DaoUtils.getOperationDao().create(
                        OperationTable.builder()
                                .name(operationName)
                                .enabled(true)
                                .type(OPERATION_TYPE.SEND_PAYLOAD)
                                .user(sqlClient().getAdminUser())
                                .properties(properties)
                                .build());

                //Create timer entry
                DaoUtils.getTimerDao()
                        .create(
                                Timer.builder()
                                        .id(McUtils.getInteger(row.get(sqlClient().getColumnName(Timer.KEY_ID))))
                                        .enabled(McUtils.getBoolean(row.get(sqlClient()
                                                .getColumnName(Timer.KEY_ENABLED))))
                                        .name(timerName)
                                        .timerType(TIMER_TYPE.valueOf(row.get(sqlClient().getColumnName(
                                                Timer.KEY_TIMER_TYPE))))
                                        .frequencyType(row.get(sqlClient().getColumnName(Timer.KEY_FREQUENCY)) == null
                                                ? null : FREQUENCY_TYPE.valueOf(row.get(sqlClient()
                                                        .getColumnName(Timer.KEY_FREQUENCY))))
                                        .frequencyData(row.get(sqlClient().getColumnName(Timer.KEY_FREQUENCY_DATA)))
                                        .triggerTime(McUtils.getLong(row.get(sqlClient().getColumnName(
                                                Timer.KEY_TRIGGER_TIME))))
                                        .validityFrom(McUtils.getLong(row.get(sqlClient().getColumnName(
                                                Timer.KEY_VALIDITY_FROM))))
                                        .validityTo(McUtils.getLong(row.get(sqlClient().getColumnName(
                                                Timer.KEY_VALIDITY_TO))))
                                        .lastFire(McUtils.getLong(row
                                                .get(sqlClient().getColumnName(Timer.KEY_LAST_FIRE))))
                                        .internalVariable1(
                                                row.get(sqlClient().getColumnName(Timer.KEY_INTERNAL_VARIABLE1)))
                                        .build());
                //map timer with operations table
                OperationTable operationTable = DaoUtils.getOperationDao().getByName(operationName);
                Timer timer = DaoUtils.getTimerDao().getByName(timerName);
                DaoUtils.getOperationTimerMapDao().create(
                        OperationTimerMap.builder()
                                .operationTable(operationTable)
                                .timer(timer)
                                .build());

            }
            sqlClient().dropTable(oldTableName);
        }

        _logger.info("Migration completed successfully.");
    }

    private HashMap<String, Object> getGatewayProperties(HashMap<String, String> row) {
        HashMap<String, Object> properties = new HashMap<String, Object>();
        switch (GATEWAY_TYPE.valueOf(row.get(sqlClient().getColumnName(GatewayTable.KEY_TYPE)))) {
            case ETHERNET:
                properties.put(GatewayConfigEthernet.KEY_HOST, row.get("VARIABLE1"));
                properties.put(GatewayConfigEthernet.KEY_PORT, McUtils.getInteger(row.get("VARIABLE2")));
                properties.put(GatewayConfigEthernet.KEY_ALIVE_FREQUENCY, McUtils.getLong(row.get("VARIABLE3")));
                break;
            case MQTT:
                properties.put(GatewayConfigMQTT.KEY_BROKER_HOST, row.get("VARIABLE1"));
                properties.put(GatewayConfigMQTT.KEY_CLIENT_ID, row.get("VARIABLE2"));
                properties.put(GatewayConfigMQTT.KEY_TOPICS_PUBLISH, row.get("VARIABLE3"));
                properties.put(GatewayConfigMQTT.KEY_TOPICS_SUBSCRIBE, row.get("VARIABLE4"));
                properties.put(GatewayConfigMQTT.KEY_USERNAME, row.get("VARIABLE5"));
                properties.put(GatewayConfigMQTT.KEY_PASSWORD, row.get("VARIABLE6"));
                break;
            case SERIAL:
                properties.put(GatewayConfigSerial.KEY_DRIVER, row.get("VARIABLE1"));
                properties.put(GatewayConfigSerial.KEY_PORT_NAME, row.get("VARIABLE2"));
                properties.put(GatewayConfigSerial.KEY_BAUD_RATE, McUtils.getInteger(row.get("VARIABLE3")));
                properties.put(GatewayConfigSerial.KEY_RUNNING_DRIVER, row.get("VARIABLE5"));
                break;
            default:
                break;
        }
        return properties;
    }

    private HashMap<String, Object> getOperationProperties(HashMap<String, String> row) {
        HashMap<String, Object> properties = new HashMap<String, Object>();
        switch (OPERATION_TYPE.valueOf(row.get(sqlClient().getColumnName(OperationTable.KEY_TYPE)))) {
            case SEND_EMAIL:
                properties.put(OperationSendEmail.KEY_EMAIL_SUBJECT, row.get("VARIABLE1"));
                properties.put(OperationSendEmail.KEY_TO_EMAIL_ADDRESSES, row.get("VARIABLE2"));
                break;
            case SEND_PAYLOAD:
                if (row.get("VARIABLE1").equalsIgnoreCase("Alarm definition")) {
                    properties.put(OperationSendPayload.KEY_RESOURCE_TYPE, RESOURCE_TYPE.RULE_DEFINITION.getText());
                } else {
                    properties.put(OperationSendPayload.KEY_RESOURCE_TYPE, row.get("VARIABLE1"));
                }
                properties.put(OperationSendPayload.KEY_RESOURCE_ID, McUtils.getInteger(row.get("VARIABLE2")));
                properties.put(OperationSendPayload.KEY_PAYLOAD, row.get("VARIABLE3"));
                if (row.get("VARIABLE4") != null && row.get("VARIABLE4").length() == 0) {
                    properties.put(OperationSendPayload.KEY_DELAY_TIME, null);
                } else {
                    properties.put(OperationSendPayload.KEY_DELAY_TIME, McUtils.getLong(row.get("VARIABLE4")));
                }
                break;
            case SEND_PUSHBULLET_NOTE:
                properties.put(OperationSendPushbulletNote.KEY_IDENS, row.get("VARIABLE1"));
                properties.put(OperationSendPushbulletNote.KEY_BODY, row.get("VARIABLE2"));
                properties.put(OperationSendPushbulletNote.KEY_TITLE, row.get("VARIABLE3"));
                break;
            case SEND_SMS:
                properties.put(OperationSendSMS.KEY_TO_PHONE_NUMBERS, row.get("VARIABLE1"));
                properties.put(OperationSendSMS.KEY_CUSTOM_MESSAGE, row.get("VARIABLE2"));
                break;
            default:
                break;
        }
        return properties;
    }

    private HashMap<String, Object> getConditionProperties(HashMap<String, String> row) {
        HashMap<String, Object> properties = new HashMap<String, Object>();
        String KEY_TRIGGER_TYPE = "TRIGGERTYPE";
        if (row.get(KEY_TRIGGER_TYPE).equalsIgnoreCase("GREATER_THAN")) {
            properties.put(RuleDefinitionThreshold.KEY_OPERATOR, OPERATOR.GT.getText());
        } else if (row.get(KEY_TRIGGER_TYPE).equalsIgnoreCase("GREATER_THAN_EQUAL")) {
            properties.put(RuleDefinitionThreshold.KEY_OPERATOR, OPERATOR.GTE.getText());
        } else if (row.get(KEY_TRIGGER_TYPE).equalsIgnoreCase("LESSER_THAN")) {
            properties.put(RuleDefinitionThreshold.KEY_OPERATOR, OPERATOR.LT.getText());
        } else if (row.get(KEY_TRIGGER_TYPE).equalsIgnoreCase("LESSER_THAN_EQUAL")) {
            properties.put(RuleDefinitionThreshold.KEY_OPERATOR, OPERATOR.LTE.getText());
        } else if (row.get(KEY_TRIGGER_TYPE).equalsIgnoreCase("EQUAL")) {
            properties.put(RuleDefinitionThreshold.KEY_OPERATOR, OPERATOR.EQ.getText());
        } else if (row.get(KEY_TRIGGER_TYPE).equalsIgnoreCase("NOT_EQUAL")) {
            properties.put(RuleDefinitionThreshold.KEY_OPERATOR, OPERATOR.NEQ.getText());
        }

        _logger.debug("Row data: {}", row);
        switch (getConditionType(row)) {
            case STATE:
                if (row.get(sqlClient().getColumnName(RuleDefinitionTable.KEY_RESOURCE_TYPE)).equalsIgnoreCase(
                        RESOURCE_TYPE.SENSOR_VARIABLE.name())) {
                    properties.put(RuleDefinitionState.KEY_STATE,
                            McUtils.getBoolean(row.get("THRESHOLDVALUE")) ? STATE.ON.getText() : STATE.OFF.getText());
                } else {
                    properties.put(RuleDefinitionState.KEY_STATE, STATE.fromString(row.get("THRESHOLDVALUE"))
                            .getText());
                }
                break;
            case STRING:
                properties.put(RuleDefinitionString.KEY_PATTERN, row.get("THRESHOLDVALUE"));
                break;
            case THRESHOLD:
                properties.put(RuleDefinitionThreshold.KEY_DATA_TYPE,
                        DATA_TYPE.valueOf(row.get("THRESHOLDTYPE")).getText());
                properties.put(RuleDefinitionThreshold.KEY_DATA, row.get("THRESHOLDVALUE"));
                break;
            default:
                break;

        }
        return properties;
    }

    private CONDITION_TYPE getConditionType(HashMap<String, String> row) {
        if (row.get(sqlClient().getColumnName(RuleDefinitionTable.KEY_RESOURCE_TYPE)).equalsIgnoreCase(
                RESOURCE_TYPE.SENSOR_VARIABLE.name())) {
            SensorVariable sensorVariable = DaoUtils.getSensorVariableDao().getById(
                    Integer.valueOf(row.get(sqlClient().getColumnName(RuleDefinitionTable.KEY_RESOURCE_ID))));
            if (sensorVariable.getMetricType() == METRIC_TYPE.BINARY) {
                return CONDITION_TYPE.STATE;
            } else if (sensorVariable.getMetricType() == METRIC_TYPE.DOUBLE) {
                return CONDITION_TYPE.THRESHOLD;
            } else if (sensorVariable.getMetricType() == METRIC_TYPE.NONE) {
                return CONDITION_TYPE.STRING;
            } else {
                return CONDITION_TYPE.THRESHOLD;
            }
        } else {
            return CONDITION_TYPE.STATE;
        }
    }

    private HashMap<String, Object> getDampeningProperties(HashMap<String, String> row) {
        HashMap<String, Object> properties = new HashMap<String, Object>();
        switch (DAMPENING_TYPE.valueOf(row.get(sqlClient().getColumnName(RuleDefinitionTable.KEY_DAMPENING_TYPE)))) {
            case ACTIVE_TIME:
                properties.put(DampeningActiveTime.KEY_ACTIVE_TIME, McUtils.getLong(row.get("DAMPENINGVAR1")));
                properties.put(DampeningActiveTime.KEY_ACTIVE_FROM, McUtils.getLong(row.get("DAMPENINGINTERNAL1")));
                break;
            case CONSECUTIVE:
                properties.put(DampeningConsecutive.KEY_CONSECUTIVE_MAX, McUtils.getInteger(row.get("DAMPENINGVAR1")));
                properties.put(DampeningConsecutive.KEY_CONSECUTIVE_COUNT,
                        McUtils.getInteger(row.get("DAMPENINGINTERNAL1")));
                break;
            case LAST_N_EVALUATIONS:
                properties.put(DampeningLastNEvaluations.KEY_OCCURRENCES_MAX,
                        McUtils.getInteger(row.get("DAMPENINGVAR1")));
                properties.put(DampeningLastNEvaluations.KEY_EVALUATIONS_MAX,
                        McUtils.getInteger(row.get("DAMPENINGVAR2")));
                properties.put(DampeningLastNEvaluations.KEY_OCCURRENCES_COUNT,
                        McUtils.getInteger(row.get("DAMPENINGINTERNAL1")));
                properties.put(DampeningLastNEvaluations.KEY_EVALUATIONS_COUNT,
                        McUtils.getInteger(row.get("DAMPENINGINTERNAL2")));
                break;
            case NONE:
                break;
            default:
                break;
        }
        return properties;
    }

}
