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
package org.mycontroller.standalone.operation;

import java.util.List;

import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.OperationTable;
import org.mycontroller.standalone.db.tables.Timer;
import org.mycontroller.standalone.operation.model.Operation;
import org.mycontroller.standalone.operation.model.OperationExecuteScript;
import org.mycontroller.standalone.operation.model.OperationRequestPayload;
import org.mycontroller.standalone.operation.model.OperationSendEmail;
import org.mycontroller.standalone.operation.model.OperationSendPayload;
import org.mycontroller.standalone.operation.model.OperationSendPushbulletNote;
import org.mycontroller.standalone.operation.model.OperationSendSMS;
import org.mycontroller.standalone.operation.model.OperationSendTelegramBotMessage;
import org.mycontroller.standalone.rule.model.RuleDefinitionAbstract;
import org.mycontroller.standalone.scheduler.SchedulerUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OperationUtils {
    public static final String OPERATION_SEND_PAYLOAD_TIMER_JOB = "operation_send_payload_";
    public static final String RULE_OPERATION = "rule_oper_";
    public static final String TIMER_OPERATION = "timer_oper_";

    public enum OPERATION_TYPE {
        SEND_PAYLOAD("Send payload"),
        REQUEST_PAYLOAD("Request payload"),
        SEND_SMS("Send SMS"),
        SEND_EMAIL("Send email"),
        SEND_PUSHBULLET_NOTE("Send pushbullet note"),
        SEND_TELEGRAM_BOT_MESSAGE("Send telegram bot message"),
        EXECUTE_SCRIPT("Execute script");
        public static OPERATION_TYPE get(int id) {
            for (OPERATION_TYPE operation_type : values()) {
                if (operation_type.ordinal() == id) {
                    return operation_type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

        private String value;

        private OPERATION_TYPE(String value) {
            this.value = value;
        }

        public String getText() {
            return this.value;
        }

        public static OPERATION_TYPE fromString(String text) {
            if (text != null) {
                for (OPERATION_TYPE type : OPERATION_TYPE.values()) {
                    if (text.equalsIgnoreCase(type.getText())) {
                        return type;
                    }
                }
            }
            return null;
        }
    }

    public static Operation getOperation(OperationTable operationTable) {
        switch (operationTable.getType()) {
            case SEND_EMAIL:
                return new OperationSendEmail(operationTable);
            case EXECUTE_SCRIPT:
                return new OperationExecuteScript(operationTable);
            case SEND_PAYLOAD:
                return new OperationSendPayload(operationTable);
            case SEND_PUSHBULLET_NOTE:
                return new OperationSendPushbulletNote(operationTable);
            case SEND_TELEGRAM_BOT_MESSAGE:
                return new OperationSendTelegramBotMessage(operationTable);
            case SEND_SMS:
                return new OperationSendSMS(operationTable);
            case REQUEST_PAYLOAD:
                return new OperationRequestPayload(operationTable);
            default:
                return null;
        }
    }

    public static String getSendPayloadTimerJobName(
            RuleDefinitionAbstract ruleDefinition, OperationTable operationTable) {
        return getSendPayloadTimerJobName(operationTable) + RULE_OPERATION + ruleDefinition.getId();
    }

    public static String getSendPayloadTimerJobName(OperationTable operationTable) {
        return OPERATION_SEND_PAYLOAD_TIMER_JOB + operationTable.getId() + "_";
    }

    public static void unloadOperationTimerJobs(List<Integer> operationIds) {
        for (OperationTable operationTable : DaoUtils.getOperationDao().getAll(operationIds)) {
            unloadOperationTimerJobs(operationTable);
        }
    }

    public static void unloadOperationTimerJobs(OperationTable operationTable) {
        //Unload timer job
        Timer timer = new Timer();
        timer.setName(getSendPayloadTimerJobName(operationTable));
        SchedulerUtils.unloadTimerJobIfContains(timer);
    }

    public static void unloadOperationTimerJobs(RuleDefinitionAbstract ruleDefinition) {
        //Unload timer job
        Timer timer = new Timer();
        List<OperationTable> operationTables = DaoUtils.getOperationDao().getByRuleDefinitionId(
                ruleDefinition.getId());
        for (OperationTable operationTable : operationTables) {
            if (operationTable.getType() == OPERATION_TYPE.SEND_PAYLOAD) {
                timer.setName(getSendPayloadTimerJobName(ruleDefinition, operationTable));
                SchedulerUtils.unloadTimerJobIfContains(timer);
            }
        }
    }

    public static void disableRuleDefinition(RuleDefinitionAbstract ruleDefinition) {
        //unload notification timer jobs
        unloadOperationTimerJobs(ruleDefinition);
        //Disable
        ruleDefinition.setEnabled(false);
        DaoUtils.getRuleDefinitionDao().update(ruleDefinition.getRuleDefinitionTable());
    }

    public static void unloadOperationTimerJobs(Timer timer) {
        //Unload timer job which is scheduled by operations
        Timer timerTmp = new Timer();
        List<OperationTable> operationTables = DaoUtils.getOperationDao().getByTimerId(timer.getId());
        for (OperationTable operationTable : operationTables) {
            if (operationTable.getType() == OPERATION_TYPE.SEND_PAYLOAD) {
                timerTmp.setName(getSendPayloadTimerJobName(operationTable));
                SchedulerUtils.unloadTimerJobIfContains(timerTmp);
            }
        }
    }

}
