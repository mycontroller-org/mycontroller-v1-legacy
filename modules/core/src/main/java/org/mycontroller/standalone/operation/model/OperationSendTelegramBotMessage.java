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

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.OperationTable;
import org.mycontroller.standalone.db.tables.Timer;
import org.mycontroller.standalone.operation.Notification;
import org.mycontroller.standalone.operation.TelegramBotUtils;
import org.mycontroller.standalone.rule.model.RuleDefinitionAbstract;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.3.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString
@Slf4j
@NoArgsConstructor
public class OperationSendTelegramBotMessage extends Operation {

    public static final String KEY_CHANNEL_USERNAME = "channelusername";
    public static final String KEY_PARSE_MODE = "parseMode";
    public static final String KEY_CUSTOM_MESSAGE = "customMessage";

    private String chatId;
    private String parseMode;
    private String customMessage;

    public OperationSendTelegramBotMessage(OperationTable operationTable) {
        this.updateOperation(operationTable);
    }

    @Override
    public void updateOperation(OperationTable operationTable) {
        super.updateOperation(operationTable);
        chatId = (String) operationTable.getProperties().get(KEY_CHANNEL_USERNAME);
        parseMode = (String) operationTable.getProperties().get(KEY_PARSE_MODE);
        customMessage = (String) operationTable.getProperties().get(KEY_CUSTOM_MESSAGE);

    }

    @Override
    @JsonIgnore
    public OperationTable getOperationTable() {
        OperationTable operationTable = super.getOperationTable();
        HashMap<String, Object> properties = new HashMap<String, Object>();
        properties.put(KEY_CHANNEL_USERNAME, chatId);
        properties.put(KEY_PARSE_MODE, parseMode);
        properties.put(KEY_CUSTOM_MESSAGE, customMessage);
        operationTable.setProperties(properties);
        return operationTable;
    }

    @Override
    public String getOperationString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(super.getType().getText()).append(" [ ");
        stringBuilder.append(chatId).append(" ]");
        return stringBuilder.toString();
    }

    private String parseModeText() {
        if (parseMode != null && parseMode.equalsIgnoreCase("text")) {
            return null;
        }
        return parseMode;
    }

    @Override
    public void execute(RuleDefinitionAbstract ruleDefinition) {
        if (!getEnabled()) {
            //This operation disabled, nothing to do.
            return;
        }
        if (chatId == null) {
            throw new RuntimeException(
                    "Cannot execute send telegram bot message without channelusername! RuleDefination name: "
                            + ruleDefinition.getName());
        }
        try {
            Notification notification = new Notification(ruleDefinition, this);
            HashMap<String, Object> bindings = new HashMap<String, Object>();
            bindings.put("notification", notification);
            String textRaw = null;
            if (customMessage != null && customMessage.trim().length() > 0) {
                textRaw = updateTemplate(customMessage, bindings);
            } else {
                textRaw = notification.toString();
            }
            String text = new String(textRaw.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
            TelegramBotUtils.sendMessage(chatId, parseModeText(), text);
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
        String textRaw = null;
        if (customMessage != null && customMessage.trim().length() > 0) {
            textRaw = customMessage;
        } else {
            textRaw = "ERROR: No msg specified!";
        }
        String text = new String(textRaw.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        TelegramBotUtils.sendMessage(chatId, parseModeText(), text);
    }

}
