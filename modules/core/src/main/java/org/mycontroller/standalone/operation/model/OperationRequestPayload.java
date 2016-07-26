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
package org.mycontroller.standalone.operation.model;

import java.util.HashMap;

import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.McObjectManager;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.OperationTable;
import org.mycontroller.standalone.db.tables.Timer;
import org.mycontroller.standalone.model.ResourceModel;
import org.mycontroller.standalone.rule.model.RuleDefinition;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString
@NoArgsConstructor
public class OperationRequestPayload extends Operation {

    public static final String KEY_RESOURCE_TYPE = "resourceType";
    public static final String KEY_RESOURCE_ID = "resourceId";

    private RESOURCE_TYPE resourceType;
    private Integer resourceId;

    public OperationRequestPayload(OperationTable operationTable) {
        this.updateOperation(operationTable);
    }

    @Override
    public void updateOperation(OperationTable operationTable) {
        super.updateOperation(operationTable);
        resourceType = RESOURCE_TYPE.fromString((String) operationTable.getProperties().get(KEY_RESOURCE_TYPE));
        resourceId = (Integer) operationTable.getProperties().get(KEY_RESOURCE_ID);
    }

    @Override
    @JsonIgnore
    public OperationTable getOperationTable() {
        OperationTable operationTable = super.getOperationTable();
        HashMap<String, Object> properties = new HashMap<String, Object>();
        properties.put(KEY_RESOURCE_TYPE, resourceType.getText());
        properties.put(KEY_RESOURCE_ID, resourceId);
        operationTable.setProperties(properties);
        return operationTable;
    }

    @Override
    public String getOperationString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(super.getType().getText()).append(" [ ");
        stringBuilder.append(new ResourceModel(resourceType, resourceId).getResourceLessDetails());
        stringBuilder.append(" ]");
        return stringBuilder.toString();
    }

    //These methods are used for JSON
    @JsonGetter("resourceType")
    private String getResourceTypeString() {
        return resourceType.getText();
    }

    @Override
    public void execute(RuleDefinition ruleDefinition) {
        sendPayload();
    }

    @Override
    public void execute(Timer timer) {
        sendPayload();
    }

    private void sendPayload() {
        if (!getEnabled()) {
            //This operation disabled, nothing to do.
            return;
        }
        ResourceModel resourceModel = new ResourceModel(resourceType, resourceId);
        McObjectManager.getMcActionEngine().executeRequestPayload(resourceModel);

        //Update last execution
        setLastExecution(System.currentTimeMillis());
        DaoUtils.getOperationDao().update(this.getOperationTable());

    }
}
