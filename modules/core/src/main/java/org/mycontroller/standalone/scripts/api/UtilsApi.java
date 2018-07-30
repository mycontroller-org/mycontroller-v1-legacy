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
package org.mycontroller.standalone.scripts.api;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.apache.commons.mail.EmailException;
import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.api.jaxrs.model.McHeatMap;
import org.mycontroller.standalone.api.jaxrs.model.Query;
import org.mycontroller.standalone.db.tables.GatewayTable;
import org.mycontroller.standalone.db.tables.OperationTable;
import org.mycontroller.standalone.db.tables.RuleDefinitionTable;
import org.mycontroller.standalone.email.EmailUtils;
import org.mycontroller.standalone.gateway.GatewayUtils;
import org.mycontroller.standalone.gateway.config.GatewayConfig;
import org.mycontroller.standalone.operation.OperationUtils;
import org.mycontroller.standalone.operation.model.Operation;
import org.mycontroller.standalone.rule.RuleUtils;
import org.mycontroller.standalone.rule.model.RuleDefinitionAbstract;
import org.mycontroller.standalone.settings.LocationSettings;
import org.mycontroller.standalone.utils.McUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Slf4j
public class UtilsApi {

    public Query getQuery() {
        return Query.builder()
                .order(Query.ORDER_ASC)
                .orderBy("id")
                .filters(new HashMap<String, Object>())
                .pageLimit(Query.MAX_ITEMS_PER_PAGE)
                .page(1L)
                .build();
    }

    public List<Integer> asList(Integer... ids) {
        return Arrays.asList(ids);
    }

    public GatewayConfig getGateway(GatewayTable gatewayTable) {
        return GatewayUtils.getGateway(gatewayTable);
    }

    public Operation getOperation(OperationTable operationTable) {
        return OperationUtils.getOperation(operationTable);
    }

    public RuleDefinitionAbstract getRuleDefinition(RuleDefinitionTable ruleDefinitionTable) {
        return RuleUtils.getRuleDefinition(ruleDefinitionTable);
    }

    public McHeatMap getHeatMap() {
        return McHeatMap.builder().build();
    }

    public String getUUID() {
        return UUID.randomUUID().toString();
    }

    public String friendlyTime(Long timestamp) {
        if (timestamp == null) {
            return "Never";
        }
        return McUtils.getFriendlyTime(System.currentTimeMillis() - timestamp, true);
    }

    public String formatTime(String pattern) {
        return formatTime(pattern, null);
    }

    public String formatTime(String pattern, Long timestamp) {
        Date date = null;
        if (timestamp != null) {
            date = new Date(timestamp);
        } else {
            date = new Date();
        }
        return new SimpleDateFormat(pattern).format(date);
    }

    public LocationSettings getServerLocationSettings() {
        return AppProperties.getInstance().getLocationSettings();
    }

    public void sendEmail(String toAddresses, String subject, String message) {
        try {
            EmailUtils.sendSimpleEmail(toAddresses, subject, message);
        } catch (EmailException ex) {
            _logger.error("Unable to send an email:[toAddress:{}, subject:{}, message:{}], Exception:{}", toAddresses,
                    subject, message, ex.toString(), ex);
        }
    }

}
