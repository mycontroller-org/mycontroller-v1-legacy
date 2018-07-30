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
import org.mycontroller.standalone.model.McTemplate;
import org.mycontroller.standalone.operation.Notification;
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
public class OperationSendEmail extends Operation {
    public static final String KEY_TO_EMAIL_ADDRESSES = "toEmailAddress";
    public static final String KEY_EMAIL_SUBJECT = "emailSubject";
    public static final String KEY_TEMPLATE = "template";
    public static final String KEY_TEMPLATE_BINDINGS = "templateBindings";

    private String toEmailAddresses;
    private String emailSubject;
    private String template;
    private HashMap<String, Object> templateBindings;

    public OperationSendEmail(OperationTable operationTable) {
        this.updateOperation(operationTable);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void updateOperation(OperationTable operationTable) {
        super.updateOperation(operationTable);
        emailSubject = (String) operationTable.getProperties().get(KEY_EMAIL_SUBJECT);
        toEmailAddresses = (String) operationTable.getProperties().get(KEY_TO_EMAIL_ADDRESSES);
        template = (String) operationTable.getProperties().get(KEY_TEMPLATE);
        templateBindings = (HashMap<String, Object>) operationTable.getProperties().get(KEY_TEMPLATE_BINDINGS);
    }

    @Override
    @JsonIgnore
    public OperationTable getOperationTable() {
        OperationTable operationTable = super.getOperationTable();
        HashMap<String, Object> properties = new HashMap<String, Object>();
        properties.put(KEY_TO_EMAIL_ADDRESSES, toEmailAddresses);
        properties.put(KEY_EMAIL_SUBJECT, emailSubject);
        properties.put(KEY_TEMPLATE, template);
        properties.put(KEY_TEMPLATE_BINDINGS, templateBindings);
        operationTable.setProperties(properties);
        return operationTable;
    }

    public HashMap<String, Object> getTemplateBindings() {
        if (templateBindings == null) {
            return new HashMap<String, Object>();
        }
        return templateBindings;
    }

    @Override
    public String getOperationString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(super.getType().getText()).append(" [ ");
        stringBuilder.append(toEmailAddresses).append(" ]");
        stringBuilder.append(", [").append(getTemplateBindings()).append("]");
        return stringBuilder.toString();
    }

    @Override
    public void execute(RuleDefinitionAbstract ruleDefinition) {
        if (!getEnabled()) {
            //This operation disabled, nothing to do.
            return;
        }
        if (toEmailAddresses == null) {
            throw new RuntimeException("Cannot execute send email without email address! RuleDefinitionTable name: "
                    + ruleDefinition.getName());
        }

        Notification notification = new Notification(ruleDefinition, this);
        String emailBody = null;
        try {
            McTemplate mcTemplate = McTemplate.get(template);
            emailBody = new String(Files.readAllBytes(Paths.get(mcTemplate.getCanonicalPath())),
                    StandardCharsets.UTF_8);
        } catch (IOException | IllegalAccessException ex) {
            _logger.error("Template exception, ", ex);
            //If failed to load template send email with this text option
            StringBuilder builder = new StringBuilder();
            //Add users details
            builder.append("Dear User,")
                    .append("\n\nThere is a rule triggered for you!")
                    .append("\n\nUnable to load your template, hence emailed as text message.");
            //Add error details
            builder.append("\nDefined template name: ").append(template);
            builder.append("\nError message: ").append(ex.getMessage());

            //Add message details in text format
            builder.append("\n\nRule definition name: ").append("${notification.ruleName}");
            builder.append("\nCondition: ").append("${notification.ruleCondition}");
            builder.append("\nActual value: ").append("${notification.actualValue}");
            builder.append("\nTriggered at: ").append("${notification.triggeredAt}");
            builder.append("\n\n\n-- Powered by").append(" www.MyController.org");
            emailBody = builder.toString();
        }

        @SuppressWarnings("unchecked")
        HashMap<String, Object> bindings = (HashMap<String, Object>) getTemplateBindings().clone();
        bindings.put("notification", notification);
        sendEmail(emailBody, bindings);
        //Update last execution
        setLastExecution(System.currentTimeMillis());
        DaoUtils.getOperationDao().update(this.getOperationTable());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(Timer timer) {
        if (!getEnabled()) {
            //This operation disabled, nothing to do.
            return;
        }
        String emailBody = null;
        try {
            McTemplate mcTemplate = McTemplate.get(template);
            emailBody = new String(Files.readAllBytes(Paths.get(mcTemplate.getCanonicalPath())),
                    StandardCharsets.UTF_8);
        } catch (IOException | IllegalAccessException ex) {
            _logger.error("Template exception, ", ex);
            //If failed to load template send email with this text option
            StringBuilder builder = new StringBuilder();
            //Add users details
            builder.append("Dear User,")
                    .append("\n\nThere is a timer triggered for you!")
                    .append("\n\nUnable to load your template, hence emailed as text message.");
            builder.append("\n\n\n-- Powered by").append(" www.MyController.org");
            emailBody = builder.toString();
        }
        sendEmail(emailBody, (HashMap<String, Object>) getTemplateBindings().clone());
    }

    private void sendEmail(String emailBody, HashMap<String, Object> bindings) {
        try {
            EmailUtils.sendSimpleEmail(
                    toEmailAddresses,
                    updateTemplate(emailSubject, bindings),
                    updateTemplate(emailBody, bindings));
        } catch (EmailException ex) {
            _logger.error("Error on sending email, ", ex);
        }
    }

}
