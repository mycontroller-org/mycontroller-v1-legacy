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
package org.mycontroller.standalone.operation.model;

import java.util.HashMap;

import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.OperationTable;
import org.mycontroller.standalone.db.tables.Timer;
import org.mycontroller.standalone.operation.Notification;
import org.mycontroller.standalone.operation.SMSUtils;
import org.mycontroller.standalone.rule.model.RuleDefinitionAbstract;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString
@Slf4j
@NoArgsConstructor
public class OperationSendSMS extends Operation {

    public static final String KEY_TO_PHONE_NUMBERS = "toPhoneNumbers";
    public static final String KEY_CUSTOM_MESSAGE = "customMessage";

    private String toPhoneNumbers;
    private String customMessage;

    public OperationSendSMS(OperationTable operationTable) {
        this.updateOperation(operationTable);
    }

    @Override
    public void updateOperation(OperationTable operationTable) {
        super.updateOperation(operationTable);
        toPhoneNumbers = (String) operationTable.getProperties().get(KEY_TO_PHONE_NUMBERS);
        customMessage = (String) operationTable.getProperties().get(KEY_CUSTOM_MESSAGE);

    }

    @Override
    @JsonIgnore
    public OperationTable getOperationTable() {
        OperationTable operationTable = super.getOperationTable();
        HashMap<String, Object> properties = new HashMap<String, Object>();
        properties.put(KEY_TO_PHONE_NUMBERS, toPhoneNumbers);
        properties.put(KEY_CUSTOM_MESSAGE, customMessage);
        operationTable.setProperties(properties);
        return operationTable;
    }

    @Override
    public String getOperationString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(super.getType().getText()).append(" [ ");
        stringBuilder.append(toPhoneNumbers).append(" ]");
        return stringBuilder.toString();
    }

    @Override
    public void execute(RuleDefinitionAbstract ruleDefinition) {
        if (!getEnabled()) {
            //This operation disabled, nothing to do.
            return;
        }
        if (toPhoneNumbers == null) {
            throw new RuntimeException("Cannot execute send SMS without phone number! AlarmDefination name: "
                    + ruleDefinition.getName());
        }
        try {
            Notification notification = new Notification(ruleDefinition, this);
            HashMap<String, Object> bindings = new HashMap<String, Object>();
            bindings.put("notification", notification);
            if (customMessage != null && customMessage.trim().length() > 0) {
                SMSUtils.sendSMS(toPhoneNumbers, updateTemplate(customMessage, bindings));
            } else {
                SMSUtils.sendSMS(toPhoneNumbers, notification.toString());
            }
        } catch (Exception ex) {
            _logger.error("Exception,", ex);
        }
        //Update last execution
        setLastExecution(System.currentTimeMillis());
        DaoUtils.getOperationDao().update(this.getOperationTable());
    }

    @Override
    public void execute(Timer timer) {
        if (!getEnabled()) {
            //This operation disabled, nothing to do.
            return;
        }
        if (customMessage != null && customMessage.trim().length() > 0) {
            SMSUtils.sendSMS(toPhoneNumbers, customMessage);
        } else {
            SMSUtils.sendSMS(toPhoneNumbers, "ERROR: No msg specified!");
        }

    }

}
