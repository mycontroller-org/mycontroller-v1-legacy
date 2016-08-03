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
package org.mycontroller.standalone.rule.model;

import java.util.ArrayList;
import java.util.List;

import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.OperationTable;
import org.mycontroller.standalone.db.tables.RuleDefinitionTable;
import org.mycontroller.standalone.operation.OperationUtils;
import org.mycontroller.standalone.operation.model.Operation;
import org.mycontroller.standalone.rule.RuleUtils.CONDITION_TYPE;
import org.mycontroller.standalone.rule.RuleUtils.DAMPENING_TYPE;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.ToString;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Data
@ToString
public abstract class RuleDefinition {
    protected static final String GET_CONDITION_STRING_EXCEPTION =
            "oops! exception occurred! possible cases: resource might not be available! Exception: ";
    private Integer id;
    private boolean enabled;
    private boolean disableWhenTrigger;
    private boolean reEnable;
    private Long reEnableDelay;
    private String name;
    private RESOURCE_TYPE resourceType;
    private Integer resourceId;
    private CONDITION_TYPE conditionType;
    private Long timestamp;
    private Long lastTrigger;
    private boolean ignoreDuplicate;
    private boolean triggered;
    private DAMPENING_TYPE dampeningType;
    private IDampening dampening;
    private List<Integer> operationIds;

    @JsonIgnore
    private List<Operation> operations;

    @JsonIgnore
    private String actualValue;

    public abstract String getConditionString();

    public List<Integer> getOperationIds() {
        if (operationIds == null) {
            if (id != null) {
                operationIds = DaoUtils.getOperationRuleDefinitionMapDao().getOperationIdsByRuleDefinitionId(getId());
            } else {
                operationIds = new ArrayList<Integer>();
            }
        }
        return operationIds;
    }

    public void reset() {
        setTriggered(false);
        setTimestamp(null);
        if (dampening != null) {
            dampening.reset();
        }
    }

    @JsonIgnore
    public List<Operation> getOperations() {
        if (operations == null) {
            operations = new ArrayList<Operation>();
            List<OperationTable> OperationsInDb = DaoUtils.getOperationDao().getByRuleDefinitionId(getId());
            for (OperationTable operationTable : OperationsInDb) {
                Operation operationModel = OperationUtils.getOperation(operationTable);
                if (operationModel != null) {
                    operations.add(operationModel);
                }
            }
        }
        return operations;
    }

    @JsonIgnore
    public RuleDefinitionTable getRuleDefinitionTable() {
        RuleDefinitionTable ruleDefinitionTable = org.mycontroller.standalone.db.tables.RuleDefinitionTable.builder()
                .id(id)
                .enabled(enabled)
                .disableWhenTrigger(disableWhenTrigger)
                .name(name)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .conditionType(conditionType)
                .timestamp(timestamp)
                .lastTrigger(lastTrigger)
                .ignoreDuplicate(ignoreDuplicate)
                .triggered(triggered)
                .dampeningType(dampeningType)
                .reEnable(reEnable)
                .reEnableDelay(reEnableDelay)
                .build();
        if (dampening != null) {
            dampening.updateRuleDefinitionTable(ruleDefinitionTable);
        } else {
            ruleDefinitionTable.setDampeningProperties(null);
        }
        return ruleDefinitionTable;
    }

    @JsonIgnore
    public void updateRuleDefinition(RuleDefinitionTable ruleDefinitionTable) {
        id = ruleDefinitionTable.getId();
        enabled = ruleDefinitionTable.getEnabled();
        disableWhenTrigger = ruleDefinitionTable.getDisableWhenTrigger();
        name = ruleDefinitionTable.getName();
        resourceType = ruleDefinitionTable.getResourceType();
        resourceId = ruleDefinitionTable.getResourceId();
        conditionType = ruleDefinitionTable.getConditionType();
        timestamp = ruleDefinitionTable.getTimestamp();
        lastTrigger = ruleDefinitionTable.getLastTrigger();
        ignoreDuplicate = ruleDefinitionTable.getIgnoreDuplicate();
        triggered = ruleDefinitionTable.getTriggered();
        dampeningType = ruleDefinitionTable.getDampeningType();
        reEnable = ruleDefinitionTable.getReEnable();
        reEnableDelay = ruleDefinitionTable.getReEnableDelay();
        switch (dampeningType) {
            case CONSECUTIVE:
                dampening = new DampeningConsecutive();
                break;
            case ACTIVE_TIME:
                dampening = new DampeningActiveTime();
                break;
            case LAST_N_EVALUATIONS:
                dampening = new DampeningLastNEvaluations();
                break;
            case NONE:
                dampening = new DampeningNone();
                break;
            default:
                break;
        }
        if (dampening != null) {
            dampening.updateDampening(ruleDefinitionTable);
        }
        //TODO: update operationIds
        //operationIds

    }

    //These methods are used for JSON
    @JsonGetter("resourceType")
    private String getResourceTypeString() {
        return resourceType.getText();
    }

    @JsonGetter("conditionType")
    private String getConditionTypeString() {
        return conditionType.getText();
    }

    @JsonGetter("dampeningType")
    private String getDampeningTypeString() {
        return dampeningType.getText();
    }

    @JsonGetter("operations")
    private List<String> getOperationsString() {
        List<String> operations = new ArrayList<String>();
        for (Operation operation : getOperations()) {
            operations.add(operation.getName());
        }
        return operations;
    }
}