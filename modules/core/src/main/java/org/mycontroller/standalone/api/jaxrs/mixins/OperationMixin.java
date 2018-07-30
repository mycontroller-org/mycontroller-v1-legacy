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
package org.mycontroller.standalone.api.jaxrs.mixins;

import java.io.IOException;
import java.util.HashMap;

import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.api.jaxrs.utils.RestUtils;
import org.mycontroller.standalone.db.tables.OperationTable;
import org.mycontroller.standalone.operation.OperationUtils;
import org.mycontroller.standalone.operation.OperationUtils.OPERATION_TYPE;
import org.mycontroller.standalone.operation.model.Operation;
import org.mycontroller.standalone.operation.model.OperationExecuteScript;
import org.mycontroller.standalone.operation.model.OperationRequestPayload;
import org.mycontroller.standalone.operation.model.OperationSendEmail;
import org.mycontroller.standalone.operation.model.OperationSendPayload;
import org.mycontroller.standalone.operation.model.OperationSendPushbulletNote;
import org.mycontroller.standalone.operation.model.OperationSendSMS;
import org.mycontroller.standalone.operation.model.OperationSendTelegramBotMessage;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@JsonSerialize(using = OperationTableSerializer.class)
@JsonDeserialize(using = OperationDeserializer.class)
abstract class OperationMixin {

}

class OperationTableSerializer extends JsonSerializer<OperationTable> {
    @Override
    public void serialize(OperationTable operationTable, JsonGenerator jgen, SerializerProvider provider)
            throws IOException,
            JsonProcessingException {
        if (operationTable != null) {
            RestUtils.getObjectMapper().writeValue(jgen, OperationUtils.getOperation(operationTable));
        } else {
            jgen.writeNull();
        }

    }
}

class OperationDeserializer extends JsonDeserializer<Operation> {

    @Override
    public Operation deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException,
            JsonProcessingException {
        ObjectCodec objectCodec = jp.getCodec();
        JsonNode node = objectCodec.readTree(jp);

        OPERATION_TYPE operationType = OPERATION_TYPE.fromString(node.get("type").asText());

        Operation operation = null;
        switch (operationType) {
            case EXECUTE_SCRIPT:
                OperationExecuteScript operationExecuteScript = new OperationExecuteScript();
                operationExecuteScript.setScriptFile(node.get("scriptFile").asText());
                if (node.get("scriptBindings") != null) {
                    operationExecuteScript.setScriptBindings(RestUtils.getObjectMapper().convertValue(
                            node.get("scriptBindings"), new TypeReference<HashMap<String, Object>>() {
                            }));
                }
                operation = operationExecuteScript;
                break;
            case SEND_EMAIL:
                OperationSendEmail operationSendEmail = new OperationSendEmail();
                operationSendEmail.setEmailSubject(node.get("emailSubject").asText());
                operationSendEmail.setToEmailAddresses(node.get("toEmailAddresses").asText());
                operationSendEmail.setTemplate(node.get("template").asText());
                if (node.get("templateBindings") != null) {
                    operationSendEmail.setTemplateBindings(RestUtils.getObjectMapper().convertValue(
                            node.get("templateBindings"), new TypeReference<HashMap<String, Object>>() {
                            }));
                }
                operation = operationSendEmail;
                break;
            case SEND_PAYLOAD:
                OperationSendPayload operationSendPayload = new OperationSendPayload();
                operationSendPayload.setResourceType(RESOURCE_TYPE.fromString(node.get("resourceType").asText()));
                operationSendPayload.setResourceId(node.get("resourceId").asInt());
                operationSendPayload.setPayload(node.get("payload").asText());
                if (node.get("delayTime") != null) {
                    operationSendPayload.setDelayTime(node.get("delayTime").asLong());
                } else {
                    operationSendPayload.setDelayTime(0L);
                }
                operation = operationSendPayload;
                break;
            case REQUEST_PAYLOAD:
                OperationRequestPayload operationRequestPayload = new OperationRequestPayload();
                operationRequestPayload.setResourceType(RESOURCE_TYPE.fromString(node.get("resourceType").asText()));
                operationRequestPayload.setResourceId(node.get("resourceId").asInt());
                operation = operationRequestPayload;
                break;
            case SEND_PUSHBULLET_NOTE:
                OperationSendPushbulletNote operationSendPushbulletNote = new OperationSendPushbulletNote();
                operationSendPushbulletNote.setTitle(node.get("title").asText());
                if (node.get("idens") != null) {
                    operationSendPushbulletNote.setIdens(node.get("idens").asText());
                }
                if (node.get("emails") != null) {
                    operationSendPushbulletNote.setEmails(node.get("emails").asText());
                }
                if (node.get("channelTags") != null) {
                    operationSendPushbulletNote.setChannelTags(node.get("channelTags").asText());
                }
                if (node.get("body") != null) {
                    operationSendPushbulletNote.setBody(node.get("body").asText());
                }
                operation = operationSendPushbulletNote;
                break;
            case SEND_TELEGRAM_BOT_MESSAGE:
                OperationSendTelegramBotMessage operationSendTelegramBotMessage = new OperationSendTelegramBotMessage();
                operationSendTelegramBotMessage.setChatId(node.get("chatId").asText());
                operationSendTelegramBotMessage.setParseMode(node.get("parseMode").asText());
                if (node.get("customMessage") != null) {
                    operationSendTelegramBotMessage.setCustomMessage(node.get("customMessage").asText());
                }
                operation = operationSendTelegramBotMessage;
                break;
            case SEND_SMS:
                OperationSendSMS operationSendSMS = new OperationSendSMS();
                operationSendSMS.setToPhoneNumbers(node.get("toPhoneNumbers").asText());
                if (node.get("customMessage") != null) {
                    operationSendSMS.setCustomMessage(node.get("customMessage").asText());
                }
                operation = operationSendSMS;
                break;
            default:
                break;
        }
        //Update RuleDefinition details
        if (node.get("id") != null) {
            operation.setId(node.get("id").asInt());
        }
        operation.setEnabled(node.get("enabled").asBoolean());
        operation.setName(node.get("name").asText());
        operation.setType(operationType);
        //operation.setUser(User.builder().id(node.get("user").get("id").intValue()).build());
        return operation;
    }
}