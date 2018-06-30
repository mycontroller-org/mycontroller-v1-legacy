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

import org.mycontroller.standalone.api.BackupApi;
import org.mycontroller.standalone.api.ForwardPayloadApi;
import org.mycontroller.standalone.api.GatewayApi;
import org.mycontroller.standalone.api.HttpApi;
import org.mycontroller.standalone.api.MessageQueueApi;
import org.mycontroller.standalone.api.MetricApi;
import org.mycontroller.standalone.api.NodeApi;
import org.mycontroller.standalone.api.OSCommandExecuterApi;
import org.mycontroller.standalone.api.OperationApi;
import org.mycontroller.standalone.api.RuleApi;
import org.mycontroller.standalone.api.SensorApi;
import org.mycontroller.standalone.api.SystemApi;
import org.mycontroller.standalone.api.TimerApi;
import org.mycontroller.standalone.api.UidTagApi;
import org.mycontroller.standalone.api.VariableApi;
import org.mycontroller.standalone.api.XmlApi;
import org.mycontroller.standalone.metrics.export.CsvExportEngine;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
public class McScriptApi {
    private GatewayApi gatewayApi = new GatewayApi();
    private NodeApi nodeApi = new NodeApi();
    private SensorApi sensorApi = new SensorApi();
    private TimerApi timerApi = new TimerApi();
    private MetricApi metricApi = new MetricApi();
    private BackupApi backupApi = new BackupApi();
    private OperationApi operationApi = new OperationApi();
    private RuleApi ruleApi = new RuleApi();
    private UidTagApi uidTagApi = new UidTagApi();
    private VariableApi variableApi = new VariableApi();
    private LoggerApi logger = new LoggerApi();
    private UtilsApi utilsApi = new UtilsApi();
    private SystemApi systemApi = new SystemApi();
    private HttpApi httpApi = new HttpApi();
    private CsvExportEngine csvExportEngine = new CsvExportEngine();
    private MessageQueueApi messageQueueApi = new MessageQueueApi();
    private ForwardPayloadApi forwardPayloadApi = new ForwardPayloadApi();
    private XmlApi xmlApi = new XmlApi();
    private OSCommandExecuterApi osCommandExecuterApi = new OSCommandExecuterApi();

    public SystemApi system() {
        return systemApi;
    }

    public SensorApi sensor() {
        return sensorApi;
    }

    public GatewayApi gateway() {
        return gatewayApi;
    }

    public NodeApi node() {
        return nodeApi;
    }

    public TimerApi timer() {
        return timerApi;
    }

    public MetricApi metric() {
        return metricApi;
    }

    public BackupApi backup() {
        return backupApi;
    }

    public OperationApi operation() {
        return operationApi;
    }

    public RuleApi rule() {
        return ruleApi;
    }

    public UidTagApi uidTag() {
        return uidTagApi;
    }

    public VariableApi variable() {
        return variableApi;
    }

    public LoggerApi logger() {
        return logger;
    }

    public UtilsApi utils() {
        return utilsApi;
    }

    public HttpApi http() {
        return httpApi;
    }

    public CsvExportEngine csvExportEngine() {
        return csvExportEngine;
    }

    public MessageQueueApi messageQueue() {
        return messageQueueApi;
    }

    public ForwardPayloadApi forwardPayload() {
        return forwardPayloadApi;
    }

    public XmlApi xml() {
        return xmlApi;
    }

    public OSCommandExecuterApi osCommandExecuter() {
        return osCommandExecuterApi;
    }
}
