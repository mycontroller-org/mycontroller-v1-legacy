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
package org.mycontroller.standalone.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.mail.EmailException;
import org.mycontroller.standalone.api.jaxrs.model.Query;
import org.mycontroller.standalone.api.jaxrs.model.QueryResponse;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.OperationTable;
import org.mycontroller.standalone.email.EmailUtils;
import org.mycontroller.standalone.operation.OperationUtils;
import org.mycontroller.standalone.operation.PushbulletUtils;
import org.mycontroller.standalone.operation.SMSUtils;
import org.mycontroller.standalone.operation.TelegramBotUtils;
import org.mycontroller.standalone.operation.model.Operation;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */

public class OperationApi {

    public OperationTable getRaw(int id) {
        return DaoUtils.getOperationDao().getById(id);
    }

    public QueryResponse getAllRaw(HashMap<String, Object> filters) {
        return DaoUtils.getOperationDao().getAll(Query.get(filters));
    }

    public Operation get(int id) {
        return OperationUtils.getOperation(getRaw(id));
    }

    public QueryResponse getAll(HashMap<String, Object> filters) {
        QueryResponse queryResponse = getAllRaw(filters);
        ArrayList<Operation> gateways = new ArrayList<Operation>();
        @SuppressWarnings("unchecked")
        List<OperationTable> rows = (List<OperationTable>) queryResponse.getData();
        for (OperationTable row : rows) {
            gateways.add(OperationUtils.getOperation(row));
        }
        queryResponse.setData(gateways);
        return queryResponse;
    }

    public Operation get(HashMap<String, Object> filters) {
        QueryResponse response = getAll(filters);
        @SuppressWarnings("unchecked")
        List<OperationTable> items = (List<OperationTable>) response.getData();
        if (items != null && !items.isEmpty()) {
            return OperationUtils.getOperation(items.get(0));
        }
        return null;
    }

    public void add(Operation operation) {
        DaoUtils.getOperationDao().create(operation.getOperationTable());
        GoogleAnalyticsApi.instance().trackOperationCreation(operation.getType().getText());
    }

    public void update(Operation operation) {
        if (!operation.getEnabled()) {
            OperationUtils.unloadOperationTimerJobs(operation.getOperationTable());
        }
        DaoUtils.getOperationDao().update(operation.getOperationTable());
    }

    public void deleteIds(List<Integer> ids) {
        OperationUtils.unloadOperationTimerJobs(ids);
        DaoUtils.getOperationDao().deleteByIds(ids);
    }

    public void enableIds(List<Integer> ids) {
        for (OperationTable operationTable : DaoUtils.getOperationDao().getAll(ids)) {
            operationTable.setEnabled(true);
            DaoUtils.getOperationDao().update(operationTable);
        }
    }

    public void disableIds(List<Integer> ids) {
        for (OperationTable operationTable : DaoUtils.getOperationDao().getAll(ids)) {
            OperationUtils.unloadOperationTimerJobs(operationTable);
            operationTable.setEnabled(false);
            DaoUtils.getOperationDao().update(operationTable);
        }
    }

    public void sendSMS(String toPhoneNumbers, String message) {
        SMSUtils.sendSMS(toPhoneNumbers, message);
    }

    public void sendEmail(String emails, String subject, String message) throws EmailException {
        EmailUtils.sendSimpleEmail(emails, subject, message);
    }

    public void sendPushbulletNote(String idens, String emails, String channelTags, String title, String body) {
        PushbulletUtils.sendNote(idens, emails, channelTags, title, body);
    }

    public void sendTelegramMessage(String chatId, String text) {
        sendTelegramMessage(chatId, "text", text);
    }

    public void sendTelegramMessage(String chatId, String parseMode, String text) {
        TelegramBotUtils.sendMessage(chatId, parseMode, text);
    }
}
