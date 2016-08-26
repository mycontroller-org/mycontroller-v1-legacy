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
package org.mycontroller.standalone.rule;

import java.util.List;

import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.ResourceOperation;
import org.mycontroller.standalone.db.ResourcesLogsUtils;
import org.mycontroller.standalone.db.tables.OperationRuleDefinitionMap;
import org.mycontroller.standalone.db.tables.OperationTable;
import org.mycontroller.standalone.db.tables.RuleDefinitionTable;
import org.mycontroller.standalone.model.ResourceModel;
import org.mycontroller.standalone.operation.OperationUtils;
import org.mycontroller.standalone.rule.model.RuleDefinition;
import org.mycontroller.standalone.rule.model.RuleDefinitionCompare;
import org.mycontroller.standalone.rule.model.RuleDefinitionScript;
import org.mycontroller.standalone.rule.model.RuleDefinitionState;
import org.mycontroller.standalone.rule.model.RuleDefinitionString;
import org.mycontroller.standalone.rule.model.RuleDefinitionThreshold;
import org.mycontroller.standalone.rule.model.RuleDefinitionThresholdRange;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RuleUtils {

    public enum CONDITION_TYPE {
        THRESHOLD("Threshold"),
        THRESHOLD_RANGE("Threshold range"),
        COMPARE("Compare"),
        STATE("State"),
        STRING("String"),
        SCRIPT("Script");

        public static CONDITION_TYPE get(int id) {
            for (CONDITION_TYPE type : values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

        private String value;

        private CONDITION_TYPE(String value) {
            this.value = value;
        }

        public String getText() {
            return this.value;
        }

        public static CONDITION_TYPE fromString(String text) {
            if (text != null) {
                for (CONDITION_TYPE type : CONDITION_TYPE.values()) {
                    if (text.equalsIgnoreCase(type.getText())) {
                        return type;
                    }
                }
            }
            return null;
        }
    }

    public enum DATA_TYPE {
        VALUE("Value"),
        SENSOR_VARIABLE("Sensor variable");
        public static DATA_TYPE get(int id) {
            for (DATA_TYPE type : values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

        private String value;

        private DATA_TYPE(String value) {
            this.value = value;
        }

        public String getText() {
            return this.value;
        }

        public static DATA_TYPE fromString(String text) {
            if (text != null) {
                for (DATA_TYPE type : DATA_TYPE.values()) {
                    if (text.equalsIgnoreCase(type.getText())) {
                        return type;
                    }
                }
            }
            return null;
        }
    }

    public enum OPERATOR {
        GT(">"),
        GTE(">="),
        LT("<"),
        LTE("<="),
        EQ("=="),
        NEQ("!=");

        public static OPERATOR get(int id) {
            for (OPERATOR type : values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

        private String value;

        private OPERATOR(String value) {
            this.value = value;
        }

        public String getText() {
            return this.value;
        }

        public static OPERATOR fromString(String text) {
            if (text != null) {
                for (OPERATOR type : OPERATOR.values()) {
                    if (text.equalsIgnoreCase(type.getText())) {
                        return type;
                    }
                }
            }
            return null;
        }
    }

    public enum STRING_OPERATOR {
        CONTAINS("Contains"),
        STARTS_WITH("Starts with"),
        ENDS_WITH("Ends with"),
        EQUAL("Equals"),
        NOT_EQUAL("Not equals"),
        MATCH("Match");

        public static STRING_OPERATOR get(int id) {
            for (STRING_OPERATOR type : values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

        private String value;

        private STRING_OPERATOR(String value) {
            this.value = value;
        }

        public String getText() {
            return this.value;
        }

        public static STRING_OPERATOR fromString(String text) {
            if (text != null) {
                for (STRING_OPERATOR type : STRING_OPERATOR.values()) {
                    if (text.equalsIgnoreCase(type.getText())) {
                        return type;
                    }
                }
            }
            return null;
        }
    }

    public enum DAMPENING_TYPE {
        NONE("None"),
        CONSECUTIVE("Consecutive"),
        LAST_N_EVALUATIONS("Last N evaluations"),
        ACTIVE_TIME("Active time");
        public static DAMPENING_TYPE get(int id) {
            for (DAMPENING_TYPE type : values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

        private String value;

        private DAMPENING_TYPE(String value) {
            this.value = value;
        }

        public String getText() {
            return this.value;
        }

        public static DAMPENING_TYPE fromString(String text) {
            if (text != null) {
                for (DAMPENING_TYPE type : DAMPENING_TYPE.values()) {
                    if (text.equalsIgnoreCase(type.getText())) {
                        return type;
                    }
                }
            }
            return null;
        }
    }

    //----------------review-----------------

    public static RuleDefinition getRuleDefinition(RuleDefinitionTable ruleDefinitionTable) {
        if (ruleDefinitionTable == null) {
            return null;
        }
        switch (ruleDefinitionTable.getConditionType()) {
            case THRESHOLD:
                return new RuleDefinitionThreshold(ruleDefinitionTable);
            case THRESHOLD_RANGE:
                return new RuleDefinitionThresholdRange(ruleDefinitionTable);
            case COMPARE:
                return new RuleDefinitionCompare(ruleDefinitionTable);
            case STATE:
                return new RuleDefinitionState(ruleDefinitionTable);
            case STRING:
                return new RuleDefinitionString(ruleDefinitionTable);
            case SCRIPT:
                return new RuleDefinitionScript(ruleDefinitionTable);
            default:
                return null;
        }
    }

    public static void enableRuleDefinition(RuleDefinition ruleDefinition) {
        if (ruleDefinition.isEnabled()) {
            _logger.debug("This rule definition already in enabled state. Nothing to do.[{}]", ruleDefinition);
        }
        ruleDefinition.setEnabled(true);
        ruleDefinition.reset();
        DaoUtils.getRuleDefinitionDao().update(ruleDefinition.getRuleDefinitionTable());
    }

    public static void disableRuleDefinition(RuleDefinition ruleDefinition) {
        //unload notification timer jobs
        OperationUtils.unloadOperationTimerJobs(ruleDefinition);
        //Disable
        ruleDefinition.setEnabled(false);
        DaoUtils.getRuleDefinitionDao().update(ruleDefinition.getRuleDefinitionTable());
    }

    public static void enableRuleDefinitions(List<Integer> ids) {
        for (org.mycontroller.standalone.db.tables.RuleDefinitionTable ruleDefinitionTable : DaoUtils
                .getRuleDefinitionDao()
                .getAll(ids)) {
            enableRuleDefinition(getRuleDefinition(ruleDefinitionTable));
        }
    }

    public static void disableRuleDefinitions(List<Integer> ids) {
        for (org.mycontroller.standalone.db.tables.RuleDefinitionTable ruleDefinitionTable : DaoUtils
                .getRuleDefinitionDao()
                .getAll(ids)) {
            disableRuleDefinition(getRuleDefinition(ruleDefinitionTable));
        }
    }

    public static void addRuleDefinition(RuleDefinition ruleDefinition) {
        ruleDefinition.setTimestamp(System.currentTimeMillis()); //Set current time
        DaoUtils.getRuleDefinitionDao().create(ruleDefinition.getRuleDefinitionTable());
        if (ruleDefinition.isEnabled()) {
            enableRuleDefinition(ruleDefinition);
        }
        //Keep operations id
        List<Integer> operationIds = ruleDefinition.getOperationIds();
        ruleDefinition = getRuleDefinition(DaoUtils.getRuleDefinitionDao().getByName(ruleDefinition.getName()));
        ruleDefinition.setOperationIds(operationIds);
        //update operations map
        updateOperationRuleDefinitionMap(ruleDefinition);
    }

    public static void updateRuleDefinition(RuleDefinition ruleDefinition) {
        //Remove delay timer if any
        disableRuleDefinition(getRuleDefinition(DaoUtils.getRuleDefinitionDao().getById(ruleDefinition.getId())));
        ruleDefinition.setTimestamp(System.currentTimeMillis()); //Set current time
        ruleDefinition.reset();
        DaoUtils.getRuleDefinitionDao().update(ruleDefinition.getRuleDefinitionTable());
        if (ruleDefinition.isEnabled()) {
            enableRuleDefinition(ruleDefinition);
        }
        //update operations map
        updateOperationRuleDefinitionMap(ruleDefinition);
    }

    private static void updateOperationRuleDefinitionMap(RuleDefinition ruleDefinition) {
        //clear all old mapping
        DaoUtils.getOperationRuleDefinitionMapDao().deleteByRuleDefinitionId(ruleDefinition.getId());
        //Update operations
        if (ruleDefinition.getOperationIds() != null && !ruleDefinition.getOperationIds().isEmpty()) {
            OperationTable operationTable = OperationTable.builder().build();
            OperationRuleDefinitionMap operationRuleDefinitionMap = OperationRuleDefinitionMap.builder()
                    .ruleDefinitionTable(ruleDefinition.getRuleDefinitionTable()).build();
            for (Integer operationId : ruleDefinition.getOperationIds()) {
                operationTable.setId(operationId);
                operationRuleDefinitionMap.setOperationTable(operationTable);
                DaoUtils.getOperationRuleDefinitionMapDao().create(operationRuleDefinitionMap);
            }
        }
    }

    public static void deleteRuleDefinition(RuleDefinition ruleDefinition) {
        //unload notification timer jobs
        OperationUtils.unloadOperationTimerJobs(ruleDefinition);

        //Delete from resources log
        ResourcesLogsUtils.deleteResourcesLog(RESOURCE_TYPE.RULE_DEFINITION, ruleDefinition.getId());
        //Delete rule
        DaoUtils.getRuleDefinitionDao().deleteById(ruleDefinition.getId());
        //Remove from operations map
        DaoUtils.getOperationRuleDefinitionMapDao().deleteByRuleDefinitionId(ruleDefinition.getId());
        _logger.debug("Item removed:{}", ruleDefinition);
    }

    public static void deleteRuleDefinitionIds(List<Integer> ids) {
        for (RuleDefinitionTable ruleDefinitionTable : DaoUtils.getRuleDefinitionDao().getAll(ids)) {
            deleteRuleDefinition(getRuleDefinition(ruleDefinitionTable));
        }
    }

    public static void executeRuleDefinitionOperation(ResourceModel resourceModel, ResourceOperation operation) {
        switch (operation.getOperationType()) {
            case ENABLE:
                enableRuleDefinition((RuleDefinition) resourceModel.getResource());
                break;
            case DISABLE:
                disableRuleDefinition((RuleDefinition) resourceModel.getResource());
                break;
            default:
                _logger.warn("RuleDefinitionTable not support for this operation!:[{}]",
                        operation.getOperationType().getText());
                break;
        }
    }
}
