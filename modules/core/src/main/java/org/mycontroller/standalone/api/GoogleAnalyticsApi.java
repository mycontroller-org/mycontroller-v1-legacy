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
import java.util.List;
import java.util.UUID;

import org.mycontroller.restclient.core.RestHeader;
import org.mycontroller.restclient.core.RestHttpClient;
import org.mycontroller.restclient.core.TRUST_HOST_TYPE;
import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.McThreadPoolFactory;
import org.mycontroller.standalone.api.jaxrs.ScriptsHandler;
import org.mycontroller.standalone.api.jaxrs.TemplatesHandler;
import org.mycontroller.standalone.api.jaxrs.model.Query;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.GatewayTable;
import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.db.tables.OperationTable;
import org.mycontroller.standalone.db.tables.RuleDefinitionTable;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.db.tables.Settings;
import org.mycontroller.standalone.db.tables.Timer;
import org.mycontroller.standalone.gateway.GatewayUtils.GATEWAY_TYPE;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_PRESENTATION;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_SET_REQ;
import org.mycontroller.standalone.operation.OperationUtils.OPERATION_TYPE;
import org.mycontroller.standalone.rule.RuleUtils.CONDITION_TYPE;
import org.mycontroller.standalone.scripts.McScriptEngineUtils.SCRIPT_TYPE;
import org.mycontroller.standalone.settings.SettingsUtils;
import org.mycontroller.standalone.timer.TimerUtils.TIMER_TYPE;
import org.mycontroller.standalone.utils.McScriptFileUtils;
import org.mycontroller.standalone.utils.McTemplateUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.4.0
 */

@Slf4j
public class GoogleAnalyticsApi extends RestHttpClient {

    private static final String REST_URL_COLLECT = "https://www.google-analytics.com/collect";
    private static final String REST_URL_BATCH = "https://www.google-analytics.com/batch";

    private static final String KEY_GA = "GOOGLE ANALYTICS";
    private static final String KEY_ANONYMOUS_ID = "ANONYMOUS_ID";

    private static final int OK_RESPONSE_CODE = 200;

    private static GoogleAnalyticsApi _INSTANCE = null;

    private String gaAnonymousId = null;
    private String applicationVersion = null;
    private String javaVersion = null;

    public GoogleAnalyticsApi() {
        this(TRUST_HOST_TYPE.DEFAULT);
    }

    public GoogleAnalyticsApi(TRUST_HOST_TYPE trustHostType) {
        super(trustHostType == null ? TRUST_HOST_TYPE.DEFAULT : trustHostType);

        // load anonymous id
        Settings _settings = SettingsUtils.getSettings(KEY_GA, KEY_ANONYMOUS_ID);
        if (_settings == null) {
            gaAnonymousId = UUID.randomUUID().toString();
            SettingsUtils.updateValue(KEY_GA, KEY_ANONYMOUS_ID, gaAnonymousId);
        } else {
            gaAnonymousId = _settings.getValue();
        }
        // load application version
        SystemApi sApi = new SystemApi();
        applicationVersion = sApi.getAbout().getApplicationVersion();
        javaVersion = sApi.getAbout().getJavaRuntimeVersion();
        _logger.info("Google analytics details[enabled:{}, ga_anonymous_id:{}]", isEnabled(), gaAnonymousId);
    }

    public static GoogleAnalyticsApi instance() {
        if (_INSTANCE == null) {
            _INSTANCE = new GoogleAnalyticsApi();
        }
        return _INSTANCE;
    }

    public static GoogleAnalyticsApi newInstance() {
        return new GoogleAnalyticsApi();
    }

    private String getGADetails() {
        return String.format("v=%s&tid=%s&cid=%s&an=%s&av=%s&",
                "1",
                AppProperties.GOOGLE_ANALYTICS_TID,
                gaAnonymousId,
                applicationVersion,
                javaVersion);
    }

    public void trackStartTime(Long milliseconds) {
        trackEvent("core", "start_time", javaVersion, milliseconds.intValue());
    }

    public void trackGatewayCreation(String type) {
        trackEvent("resource_added", "gateway", type, null);
    }

    public void trackNodeCreation(String type) {
        trackEvent("resource_added", "node", type, null);
    }

    public void trackSensorCreation(String type) {
        trackEvent("resource_added", "sensor", type, null);
    }

    public void trackSensorVariableCreation(String type) {
        trackEvent("resource_added", "sensor_variable", type, null);
    }

    public void trackRuleCreation(String type) {
        trackEvent("resource_added", "rule", type, null);
    }

    public void trackTimerCreation(String type) {
        trackEvent("resource_added", "timer", type, null);
    }

    public void trackOperationCreation(String type) {
        trackEvent("resource_added", "operation", type, null);
    }

    public void trackResourceGroupCreation() {
        trackEvent("resource_added", "resource_group", "default", null);
    }

    public void trackRoomCreation() {
        trackEvent("resource_added", "room", "default", null);
    }

    public void trackScriptCreation(String type) {
        trackEvent("resource_added", "script", type, null);
    }

    public void trackTemplateCreation() {
        trackEvent("resource_added", "template", "default", null);
    }

    private String getEventString(String category, String action, String label, int value) {
        return String.format("t=event&ec=%s&ea=%s&el=%s&ev=%s", category, action, label, value);
    }

    public void trackServerAliveStatus() {
        trackEvent("server_alive", applicationVersion, javaVersion, null);
    }

    private void updateGatewaysCount(List<String> events) {
        for (GATEWAY_TYPE type : GATEWAY_TYPE.values()) {
            Long count = DaoUtils.getGatewayDao().countOf(GatewayTable.KEY_TYPE, type);
            if (count > 0) {
                events.add(getEventString("resource_count", "gateways", type.getText(), count.intValue()));
            }
        }
    }

    private void updateNodesCount(List<String> events) {
        List<MESSAGE_TYPE_PRESENTATION> types = new ArrayList<MESSAGE_TYPE_PRESENTATION>();
        types.add(MESSAGE_TYPE_PRESENTATION.S_ARDUINO_NODE);
        types.add(MESSAGE_TYPE_PRESENTATION.S_ARDUINO_REPEATER_NODE);

        for (MESSAGE_TYPE_PRESENTATION type : types) {
            Long count = DaoUtils.getNodeDao().countOf(Node.KEY_TYPE, type);
            if (count > 0) {
                events.add(getEventString("resource_count", "nodes", type.getText(), count.intValue()));
            }
        }
        Long count = DaoUtils.getNodeDao().countOf(Node.KEY_SMART_SLEEP_ENABLED, true);
        if (count > 0) {
            events.add(getEventString("resource_count", "nodes", "smartsleep_enabled", count.intValue()));
        }
    }

    private void updateSensorsCount(List<String> events) {
        for (MESSAGE_TYPE_PRESENTATION type : MESSAGE_TYPE_PRESENTATION.values()) {
            Long count = DaoUtils.getSensorDao().countOf(Sensor.KEY_TYPE, type);
            if (count > 0) {
                events.add(getEventString("resource_count", "sensors", type.getText(), count.intValue()));
            }
        }
    }

    private void updateSensorVariablesCount(List<String> events) {
        for (MESSAGE_TYPE_SET_REQ type : MESSAGE_TYPE_SET_REQ.values()) {
            Long count = DaoUtils.getSensorVariableDao().countOf(SensorVariable.KEY_VARIABLE_TYPE, type);
            if (count > 0) {
                events.add(getEventString("resource_count", "sensor_variables", type.getText(), count.intValue()));
            }
        }
    }

    private void updateRulesCount(List<String> events) {
        for (CONDITION_TYPE type : CONDITION_TYPE.values()) {
            Long count = DaoUtils.getRuleDefinitionDao().countOf(RuleDefinitionTable.KEY_CONDITION_TYPE, type);
            if (count > 0) {
                events.add(getEventString("resource_count", "rules", type.getText(), count.intValue()));
            }
        }
    }

    private void updateTimersCount(List<String> events) {
        for (TIMER_TYPE type : TIMER_TYPE.values()) {
            Long count = DaoUtils.getTimerDao().countOf(Timer.KEY_TIMER_TYPE, type);
            if (count > 0) {
                events.add(getEventString("resource_count", "rules", type.getText(), count.intValue()));
            }
        }
    }

    private void updateOperationsCount(List<String> events) {
        for (OPERATION_TYPE type : OPERATION_TYPE.values()) {
            Long count = DaoUtils.getOperationDao().countOf(OperationTable.KEY_TYPE, type);
            if (count > 0) {
                events.add(getEventString("resource_count", "operations", type.getText(), count.intValue()));
            }
        }
    }

    private void updateForwardPayloadCount(List<String> events) {
        Long count = DaoUtils.getForwardPayloadDao().countOf();
        if (count > 0) {
            events.add(getEventString("resource_count", "forward_payload", "default", count.intValue()));
        }
    }

    private void updateResourceGroupsCount(List<String> events) {
        Long count = DaoUtils.getResourcesGroupDao().countOf();
        if (count > 0) {
            events.add(getEventString("resource_count", "resource_groups", "default", count.intValue()));
        }
    }

    private void updateRoomsCount(List<String> events) {
        Long count = DaoUtils.getRoomDao().countOf();
        if (count > 0) {
            events.add(getEventString("resource_count", "rooms", "default", count.intValue()));
        }
    }

    @SuppressWarnings("unchecked")
    private void updateScriptsCount(List<String> events) {
        Query query = Query.builder().build();
        query.getFilters().put(ScriptsHandler.KEY_LESS_INFO, true);
        try {

            query.getFilters().put(ScriptsHandler.KEY_TYPE, SCRIPT_TYPE.CONDITION);
            List<String> files = (List<String>) McScriptFileUtils.getScriptFiles(query).getData();
            if (files != null && files.size() > 0) {
                events.add(getEventString("resource_count", "scripts", SCRIPT_TYPE.CONDITION.getText(), files.size()));
            }

            query.getFilters().put(ScriptsHandler.KEY_TYPE, SCRIPT_TYPE.OPERATION);
            files = (List<String>) McScriptFileUtils.getScriptFiles(query).getData();
            if (files != null && files.size() > 0) {
                events.add(getEventString("resource_count", "scripts", SCRIPT_TYPE.OPERATION.getText(), files.size()));
            }

            query.getFilters().remove(ScriptsHandler.KEY_TYPE);
            for (String extension : McScriptFileUtils.MC_SCRIPT_SUFFIX_FILTER) {
                query.getFilters().put(ScriptsHandler.KEY_EXTENSION, extension);
                files = (List<String>) McScriptFileUtils.getScriptFiles(query).getData();
                if (files != null && files.size() > 0) {
                    events.add(getEventString("resource_count", "scripts", extension, files.size()));
                }
            }

        } catch (Exception ex) {
            _logger.error("Exception", ex);
        }
    }

    private void updateTemplatesCount(List<String> events) {
        Query query = Query.builder().build();
        query.getFilters().put(TemplatesHandler.KEY_LESS_INFO, true);
        try {
            @SuppressWarnings("unchecked")
            List<String> files = (List<String>) McTemplateUtils.get(query).getData();
            if (files != null && files.size() > 0) {
                events.add(getEventString("resource_count", "templates", "default", files.size()));
            }
        } catch (Exception ex) {
            _logger.error("Exception", ex);
        }
    }

    public void sendOverallStatus() {
        if (!isEnabled()) {
            return;
        }
        List<String> events = new ArrayList<String>();

        updateGatewaysCount(events);
        updateNodesCount(events);
        updateSensorsCount(events);
        updateSensorVariablesCount(events);
        updateRulesCount(events);
        updateTimersCount(events);
        updateOperationsCount(events);
        updateForwardPayloadCount(events);
        updateResourceGroupsCount(events);
        updateRoomsCount(events);
        updateScriptsCount(events);
        updateTemplatesCount(events);

        // MQTT status
        events.add(getEventString("core", "mqtt_status", "enabled",
                AppProperties.getInstance().getMqttBrokerSettings().getEnabled() ? 1 : 0));

        // MQTT anonymous
        events.add(getEventString("core", "mqtt_status", "anonymous",
                AppProperties.getInstance().getMqttBrokerSettings().getAllowAnonymous() ? 1 : 0));

        doPost(events);
    }

    public void trackEvent(String category, String action, String label, Integer value) {
        if (!isEnabled()) {
            return;
        }
        if (value != null) {
            doPost(String.format("t=event&ec=%s&ea=%s&el=%s&ev=%s", category, action, label, value));
        } else {
            doPost(String.format("t=event&ec=%s&ea=%s&el=%s", category, action, label));
        }
    }

    public void trackException(Exception ex) {
        if (!isEnabled()) {
            return;
        }
        String payload = String.format("t=exception&exd=%s&exf=%s", ex.getMessage(), "0");
        doPost(payload);
    }

    private boolean isEnabled() {
        return AppProperties.getInstance().isGoogleAnalyticsEnabled();
    }

    private void doPost(String payload) {
        if (!isEnabled()) {
            return;
        }

        McThreadPoolFactory.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    doPost(REST_URL_COLLECT, RestHeader.getDefault(), getGADetails() + payload, OK_RESPONSE_CODE);
                } catch (Exception ex) {
                    _logger.error("Exception,", ex);
                }
            }
        });
    }

    private void doPost(List<String> dataList) {
        if (!isEnabled()) {
            return;
        }

        McThreadPoolFactory.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    StringBuilder payload = new StringBuilder();
                    int count = 0;
                    for (String data : dataList) {
                        payload.append(getGADetails()).append(data).append("\n");
                        count++;
                        if (count == 20) {
                            doPost(REST_URL_BATCH, RestHeader.getDefault(), payload.toString(), OK_RESPONSE_CODE);
                            count = 0;
                            payload.setLength(0);
                        }
                    }
                    if (count != 0) {
                        doPost(REST_URL_BATCH, RestHeader.getDefault(), payload.toString(), OK_RESPONSE_CODE);
                    }
                } catch (Exception ex) {
                    _logger.error("Exception,", ex);
                }
            }
        });
    }
}
