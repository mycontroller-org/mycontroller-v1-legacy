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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

import org.apache.commons.mail.EmailException;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.OperationTable;
import org.mycontroller.standalone.db.tables.Timer;
import org.mycontroller.standalone.email.EmailUtils;
import org.mycontroller.standalone.operation.OperationNotification;
import org.mycontroller.standalone.rule.model.RuleDefinition;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;
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
public class OperationSendEmail extends Operation {
    public static final String EMAIL_TEMPLATE_RULE = "../conf/templates/emailTemplateAlarm.html";

    public static final String KEY_TO_EMAIL_ADDRESSES = "toEmailAddress";
    public static final String KEY_EMAIL_SUBJECT = "emailSubject";

    private String toEmailAddresses;
    private String emailSubject;

    public OperationSendEmail() {

    }

    public OperationSendEmail(OperationTable operationTable) {
        this.updateOperation(operationTable);
    }

    @Override
    public void updateOperation(OperationTable operationTable) {
        super.updateOperation(operationTable);
        emailSubject = (String) operationTable.getProperties().get(KEY_EMAIL_SUBJECT);
        toEmailAddresses = (String) operationTable.getProperties().get(KEY_TO_EMAIL_ADDRESSES);
    }

    @Override
    @JsonIgnore
    public OperationTable getOperationTable() {
        OperationTable operationTable = super.getOperationTable();
        HashMap<String, Object> properties = new HashMap<String, Object>();
        properties.put(KEY_TO_EMAIL_ADDRESSES, toEmailAddresses);
        properties.put(KEY_EMAIL_SUBJECT, emailSubject);
        operationTable.setProperties(properties);
        return operationTable;
    }

    @Override
    public String getOperationString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(super.getType().getText()).append(" [ ");
        stringBuilder.append(toEmailAddresses).append(" ]");
        return stringBuilder.toString();
    }

    @Override
    public void execute(RuleDefinition ruleDefinition) {
        if (!getEnabled()) {
            //This operation disabled, nothing to do.
            return;
        }
        if (toEmailAddresses == null) {
            throw new RuntimeException("Cannot execute send email without email address! RuleDefinitionTable name: "
                    + ruleDefinition.getName());
        }

        OperationNotification operationNotification = new OperationNotification(ruleDefinition);
        String emailBody = null;
        try {
            emailBody = new String(Files.readAllBytes(Paths.get(EMAIL_TEMPLATE_RULE)),
                    StandardCharsets.UTF_8);
        } catch (IOException ex) {
            _logger.error("Exception, ", ex);
            emailBody = ex.getMessage();
        }

        try {
            EmailUtils.sendSimpleEmail(
                    toEmailAddresses,
                    operationNotification.updateReferances(emailSubject),
                    operationNotification.updateReferances(emailBody));
        } catch (EmailException ex) {
            _logger.error("Error on sending email, ", ex);
        }
        //Update last execution
        setLastExecution(System.currentTimeMillis());
        DaoUtils.getOperationDao().update(this.getOperationTable());
    }

    @Override
    public void execute(Timer timer) {
        _logger.error("Timer will not support for this operation");
    }

}
