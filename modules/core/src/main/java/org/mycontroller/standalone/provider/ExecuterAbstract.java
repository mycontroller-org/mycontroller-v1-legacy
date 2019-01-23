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
package org.mycontroller.standalone.provider;

import java.util.ArrayList;
import java.util.TimeZone;

import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.AppProperties.STATE;
import org.mycontroller.standalone.McThreadPoolFactory;
import org.mycontroller.standalone.api.GoogleAnalyticsApi;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.NodeUtils.NODE_REGISTRATION_STATE;
import org.mycontroller.standalone.db.tables.GatewayTable;
import org.mycontroller.standalone.db.tables.MetricsGPSTypeDevice;
import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.exceptions.McBadRequestException;
import org.mycontroller.standalone.message.IMessage;
import org.mycontroller.standalone.message.McMessageUtils;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_INTERNAL;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_PRESENTATION;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_SET_REQ;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_STREAM;
import org.mycontroller.standalone.message.McMessageUtils.PAYLOAD_TYPE;
import org.mycontroller.standalone.metrics.DATA_TYPE;
import org.mycontroller.standalone.metrics.MetricsUtils;
import org.mycontroller.standalone.metrics.MetricsUtils.METRIC_TYPE;
import org.mycontroller.standalone.metrics.model.DataPointer;
import org.mycontroller.standalone.model.ResourceModel;
import org.mycontroller.standalone.offheap.MessageQueueImpl;
import org.mycontroller.standalone.offheap.MessageQueueSleepImpl;
import org.mycontroller.standalone.utils.McUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.2.0
 */
@Slf4j
public abstract class ExecuterAbstract implements IExecutor {
    protected IMessage _message;
    private long startTime = 0;

    protected MessageQueueImpl _queue;
    protected MessageQueueSleepImpl _queueSleep;

    public ExecuterAbstract(MessageQueueImpl _queue, MessageQueueSleepImpl _queueSleep) {
        this._queue = _queue;
        this._queueSleep = _queueSleep;
    }

    public void execute(IMessage _message) {
        // mark start time
        startTime = System.currentTimeMillis();
        if (_logger.isDebugEnabled()) {
            startTime = System.currentTimeMillis();
        }

        // Take a clone to log it on database
        IMessage _message_for_resources_log = _message.clone();

        // clone the message and do process
        this._message = _message.clone();

        try {
            _logger.debug("Processing {}", _message);
            switch (MESSAGE_TYPE.fromString(_message.getType())) {
                case C_INTERNAL:
                    if (!isNodeRegistered()) {
                        return;
                    }
                    executeInternal();
                    break;
                case C_PRESENTATION:
                    executePresentation();
                    break;
                case C_REQ:
                    if (!isNodeRegistered()) {
                        return;
                    }
                    executeRequest();
                    break;
                case C_SET:
                    if (!isNodeRegistered()) {
                        return;
                    }
                    executeSet();
                    break;
                case C_STREAM:
                    if (!isNodeRegistered()) {
                        return;
                    }
                    executeStream();
                    break;
                default:
                    _logger.warn("This type not implemented! {}", _message.getType());
                    break;
            }
            //update node last seen and status as UP
            if (!_message.isTxMessage()) {
                if (!_message.getNodeEui().equalsIgnoreCase(IMessage.NODE_BROADCAST_ID)) {
                    Node node = getNode();
                    node.setState(STATE.UP);
                    updateNode(node);
                }
            }
        } finally {
            if (_logger.isDebugEnabled()) {
                _logger.debug("Time taken to execute this _message: {} ms, {}",
                        System.currentTimeMillis() - startTime, _message);
            }
            _message = null;
        }
        // do log on database
        McThreadPoolFactory.execute(new ResourcesLogger(_message_for_resources_log));
    }

    public void executeInternal() {
        //Get node, if node not available create
        Node node = null;
        if (!_message.getNodeEui().equalsIgnoreCase(IMessage.NODE_BROADCAST_ID)) {
            node = getNode();
        }
        switch (MESSAGE_TYPE_INTERNAL.fromString(_message.getSubType())) {
            case I_BATTERY_LEVEL:
                if (_message.isTxMessage()) {
                    return;
                }
                node.setBatteryLevel(_message.getPayload());
                updateNode(node);
                //Update battery level in to metrics table
                MetricsUtils.engine().post(DataPointer.builder()
                        .payload(_message.getPayload())
                        .timestamp(System.currentTimeMillis())
                        .resourceModel(new ResourceModel(RESOURCE_TYPE.NODE, node))
                        .dataType(DATA_TYPE.NODE_BATTERY_USAGE)
                        .build());
                break;
            case I_TIME:
                if (_message.isTxMessage()) {
                    return;
                }
                TimeZone timeZone = TimeZone.getDefault();
                long utcTime = System.currentTimeMillis();
                long timeOffset = timeZone.getOffset(utcTime);
                long localTime = utcTime + timeOffset;
                _message.setPayload(String.valueOf(localTime / 1000));
                _message.setTxMessage(true);
                _message.setTimestamp(System.currentTimeMillis());
                addInQueue(_message);
                break;
            case I_VERSION:
                _logger.debug("GatewayTable version requested by {}! Message:{}",
                        AppProperties.APPLICATION_NAME,
                        _message);
                break;
            case I_ID_REQUEST:
                nodeEuiRequest();
                break;
            case I_INCLUSION_MODE:
                _logger.warn("Inclusion mode not supported by this controller! Message:{}",
                        _message);
                break;
            case I_CONFIG:
                if (_message.isTxMessage()) {
                    return;
                }
                _message.setPayload(metricType());
                _message.setTxMessage(true);
                _message.setTimestamp(System.currentTimeMillis());
                addInQueue(_message);
                break;
            case I_LOG_MESSAGE:
                if (_message.isTxMessage()) {
                    return;
                }
                break;
            case I_SKETCH_NAME:
                if (_message.isTxMessage()) {
                    return;
                }
                node = getNode();
                if (!node.isNameLocked()) {
                    //Update node name only when it is null or name length is greater than 0
                    if (node.getName() == null) {
                        node.setName(_message.getPayload());
                    } else if (_message.getPayload() != null && _message.getPayload().trim().length() > 0) {
                        node.setName(_message.getPayload());
                    }
                    updateNode(node);
                }
                break;
            case I_SKETCH_VERSION:
                if (_message.isTxMessage()) {
                    return;
                }
                node = getNode();
                node.setVersion(_message.getPayload());
                updateNode(node);
                break;
            case I_REBOOT:
                break;
            case I_GATEWAY_READY:
                if (_message.isTxMessage()) {
                    return;
                }
                break;

            case I_ID_RESPONSE:
                _logger.debug("Internal Message, Type:I_ID_RESPONSE[{}]", _message);
                return;
            case I_POST_SLEEP_NOTIFICATION:
                if (_message.isTxMessage()) {
                    return;
                }
                // update sleep duration
                updateSleepNode(Node.KEY_SMART_SLEEP_DURATION);
                break;
            case I_PRE_SLEEP_NOTIFICATION:
                if (_message.isTxMessage()) {
                    return;
                }
                // update sleep wait duration
                updateSleepNode(Node.KEY_SMART_SLEEP_WAIT_DURATION);
                moveSleepQueueToNormalQueue();
                break;
            case I_HEARTBEAT:
            case I_HEARTBEAT_RESPONSE:
                if (_message.isTxMessage()) {
                    return;
                }
                node = getNode();
                node.setState(STATE.UP);
                updateNode(node);
                moveSleepQueueToNormalQueue();
                break;
            case I_DISCOVER:
                if (_message.isTxMessage()) {
                    return;
                }
            case I_DISCOVER_RESPONSE:
                if (_message.isTxMessage()) {
                    return;
                }
                node = getNode();
                node.setParentNodeEui(_message.getPayload());
                updateNode(node);
                break;
            case I_DEBUG:
                break;
            case I_REGISTRATION_REQUEST:
                if (_message.isTxMessage()) {
                    return;
                }
                if (AppProperties.getInstance().getControllerSettings().getAutoNodeRegistration()) {
                    _message.setAck(IMessage.NO_ACK);
                    _message.setSubType(MESSAGE_TYPE_INTERNAL.I_REGISTRATION_RESPONSE.getText());
                    _message.setTxMessage(true);
                    _message.setTimestamp(System.currentTimeMillis());
                    addInQueue(_message);
                    _logger.debug("Registration response sent to gateway:{}, node:{}", _message.getGatewayId(),
                            _message.getNodeEui());
                }
                break;
            case I_REGISTRATION_RESPONSE:
                if (_message.isTxMessage()) {
                    return;
                }
                //TODO: do some action, if controller want to react for this type of _message
            case I_PRESENTATION:
                if (_message.isTxMessage()) {
                    return;
                }
            case I_RSSI:
                if (_message.isTxMessage()) {
                    return;
                }
                node = getNode();
                node.setRssi(_message.getPayload());
                updateNode(node);
                return;
            case I_PROPERTIES:
                if (_message.isTxMessage()) {
                    return;
                }
                updateProperties(_message);
                return;
            case I_FACTORY_RESET:
                if (_message.isTxMessage()) {
                    return;
                }
            default:
                _logger.warn("This type not implemented yet! {}", _message);
                break;
        }
    }

    public void executePresentation() {
        if (_message.getSensorId().equalsIgnoreCase(IMessage.SENSOR_BROADCAST_ID)) {
            Node node = getNode();
            node.setLibVersion(_message.getPayload());
            node.setType(MESSAGE_TYPE_PRESENTATION.fromString(_message.getSubType()));
            updateNode(node);
        } else {
            Node node = getNode();
            Sensor sensor = DaoUtils.getSensorDao().get(node.getId(), _message.getSensorId());
            if (sensor == null) {
                sensor = Sensor.builder()
                        .sensorId(String.valueOf(_message.getSensorId()))
                        .type(MESSAGE_TYPE_PRESENTATION.fromString(_message.getSubType()))
                        .name(_message.getPayload())
                        .build();
                sensor.setNode(node);
                DaoUtils.getSensorDao().create(sensor);
            } else {
                sensor.setType(MESSAGE_TYPE_PRESENTATION.fromString(_message.getSubType()));
                if (_message.getPayload() != null && _message.getPayload().trim().length() > 0) {
                    sensor.setName(_message.getPayload());
                }
                DaoUtils.getSensorDao().update(sensor);
            }
        }
        _logger.debug("Presentation Message[type:{},payload:{}]",
                MESSAGE_TYPE_PRESENTATION.fromString(_message.getSubType()),
                _message.getPayload());
    }

    public void executeSet() {
        PAYLOAD_TYPE payloadType = McMessageUtils.getPayLoadType(MESSAGE_TYPE_SET_REQ.fromString(_message
                .getSubType()));
        Sensor sensor = getSensor();
        // before updating value into table convert payload types
        // change RGB and RGBW values
        if (MESSAGE_TYPE_SET_REQ.V_RGB == MESSAGE_TYPE_SET_REQ.fromString(_message.getSubType())
                || MESSAGE_TYPE_SET_REQ.V_RGBW == MESSAGE_TYPE_SET_REQ.fromString(_message.getSubType())) {
            if (!_message.getPayload().startsWith("#")) {
                _message.setPayload("#" + _message.getPayload());
            }
        }
        SensorVariable _sv = this.updateSensorVariable(_message, sensor, payloadType);
        _logger.debug(
                "GatewayName:{}, SensorName:{}, NodeId:{}, SesnorId:{}, SubType:{}, PayloadType:{}, Payload:{}",
                sensor.getName(),
                sensor.getNode().getGatewayTable().getName(),
                sensor.getNode().getEui(), sensor.getSensorId(),
                MESSAGE_TYPE_SET_REQ.fromString(_message.getSubType()).getText(),
                payloadType.toString(),
                _message.getPayload());
        if (_message.getSubType().equals(MESSAGE_TYPE_SET_REQ.V_UNIT_PREFIX.getText())) {
            // TODO: No idea, how to support unit
            DaoUtils.getSensorDao().update(sensor);
            return;
        }
        sensor.setLastSeen(System.currentTimeMillis());
        DaoUtils.getSensorDao().update(sensor);

        // execute other tasks like, log in metric table, rule engine, forward payload,etc...
        executeDependentTask(_sv);
    }

    public void executeRequest() {
        Sensor sensor = getSensor();
        SensorVariable sensorVariable = DaoUtils.getSensorVariableDao().get(sensor.getId(),
                MESSAGE_TYPE_SET_REQ.fromString(_message.getSubType()));
        if (_message.isTxMessage()) {
            if (sensorVariable == null) {
                //throw new McBadRequestException("Selected sensor variable is not available!");
            }
            return;
        }
        if (sensorVariable != null && sensorVariable.getValue() != null) {
            _message.setTxMessage(true);
            _message.setType(MESSAGE_TYPE.C_SET.getText());
            _message.setAck(IMessage.NO_ACK);
            _message.setPayload(sensorVariable.getValue());
            _message.setTimestamp(System.currentTimeMillis());
            // add it on the queue
            addInQueue(_message);
            _logger.debug("Request processed! Message Sent: {}", _message);
        } else {
            //If sensorVariable not available create new one.
            if (sensorVariable == null) {
                sensorVariable = updateSensorVariable(_message, getSensor(),
                        McMessageUtils.getPayLoadType(MESSAGE_TYPE_SET_REQ.fromString(_message.getSubType())));
            }
            _logger.warn("Data not available! but there is request from sensor[{}], Ignored this request!", _message);
        }
    }

    public void executeStream() {
        switch (MESSAGE_TYPE_STREAM.fromString(_message.getSubType())) {
            case ST_FIRMWARE_CONFIG_REQUEST:
                executeFirmwareConfigRequest();
                break;
            case ST_FIRMWARE_REQUEST:
                executeFirmwareRequest();
                break;
            case ST_FIRMWARE_CONFIG_RESPONSE:
            case ST_FIRMWARE_RESPONSE:
            case ST_IMAGE:
            case ST_SOUND:
                break;
            default:
                break;
        }
    }

    @Override
    public String metricType() {
        return McMessageUtils.getMetricType();
    }

    private void updateSleepNode(String propertyKey) {
        //Update sleep duration
        Long sleepDuration = McUtils.getLong(_message.getPayload());
        Node node = getNode();
        // if smart sleep not enabled do enable it.
        if (!node.getSmartSleepEnabled()) {
            node.setSmartSleepEnabled(true);
        }
        node.setState(STATE.UP);
        node.setProperty(propertyKey, sleepDuration);
        updateNode(node);
    }

    // move sleep queue messages to actual queue
    private void moveSleepQueueToNormalQueue() {
        Node _node = getNode();
        // if it is not a sleeping node, no action required.
        if (!_node.getSmartSleepEnabled()) {
            return;
        }
        _logger.debug("Moving sleep messages to normal queue...{}", _message);
        ArrayList<IMessage> _messages = _queueSleep.remove(_message.getNodeEui());
        for (int index = 0; index < _messages.size(); index++) {
            // update timestamp as now and move it
            _messages.get(index).setTimestamp(System.currentTimeMillis());
            addInQueue(_messages.get(index));
        }
    }

    private void updateProperties(IMessage _message) {
        Node node = getNode();
        if (_message.getPayload() != null && _message.getPayload().length() > 0) {
            String[] _properties = _message.getPayload().split(";");
            for (String property : _properties) {
                String[] _prop = property.split("=", 2);
                if (_prop.length == 2) {
                    node.getProperties().put(_prop[0].trim(), _prop[1]);
                }
            }
            _logger.debug("Updated properties for the {}", node);
            updateNode(node);
        }
    }

    private Node getNode() {
        if (_message.getNodeEui().equalsIgnoreCase(IMessage.NODE_BROADCAST_ID)) {
            return null;
        }
        Node node = DaoUtils.getNodeDao().get(_message.getGatewayId(), _message.getNodeEui());
        if (node == null) {
            _logger.debug("This Node[{}] not available in our DB, Adding...", _message.getNodeEui());
            node = Node
                    .builder()
                    .gatewayTable(GatewayTable.builder().id(_message.getGatewayId()).build())
                    .eui(_message.getNodeEui())
                    .state(STATE.UP)
                    .registrationState(
                            AppProperties.getInstance().getControllerSettings().getAutoNodeRegistration()
                                    ? NODE_REGISTRATION_STATE.REGISTERED : NODE_REGISTRATION_STATE.NEW)
                    .build();
            node.setLastSeen(System.currentTimeMillis());
            DaoUtils.getNodeDao().create(node);
            GoogleAnalyticsApi.instance().trackNodeCreation("auto");
            node = DaoUtils.getNodeDao().get(_message.getGatewayId(), _message.getNodeEui());
        }
        _logger.debug("Node:[{}], _message:[{}]", node, _message);
        return node;
    }

    private void updateNode(Node node) {
        node.setLastSeen(System.currentTimeMillis());
        DaoUtils.getNodeDao().update(node);
    }

    private Sensor getSensor() {
        Sensor sensor = DaoUtils.getSensorDao().get(
                _message.getGatewayId(),
                _message.getNodeEui(),
                _message.getSensorId());
        if (sensor == null) {
            getNode();
            _logger.debug("This sensor[{} from Node:{}] not available in our DB, Adding...",
                    _message.getSensorId(), _message.getNodeEui());
            sensor = Sensor.builder().sensorId(_message.getSensorId()).build();
            sensor.setNode(getNode());
            DaoUtils.getSensorDao().create(sensor);
            GoogleAnalyticsApi.instance().trackSensorCreation("auto");
            sensor = DaoUtils.getSensorDao().get(
                    _message.getGatewayId(),
                    _message.getNodeEui(),
                    _message.getSensorId());
        }
        return sensor;
    }

    private SensorVariable updateSensorVariable(IMessage _message, Sensor sensor,
            PAYLOAD_TYPE payloadType) {
        SensorVariable sensorVariable = DaoUtils.getSensorVariableDao().get(sensor.getId(),
                MESSAGE_TYPE_SET_REQ.fromString(_message.getSubType()));
        METRIC_TYPE metricType = McMessageUtils.getMetricType(payloadType);
        if (sensorVariable == null) {
            String data = null;
            switch (metricType) {
                case BINARY:
                    data = _message.getPayload().equalsIgnoreCase("1") ? "1" : "0";
                    break;
                case COUNTER:
                    data = String.valueOf(McUtils.getLong(_message.getPayload()));
                    break;
                case DOUBLE:
                    data = String.valueOf(McUtils.getDoubleAsString(_message.getPayload()));
                    break;
                case GPS:
                    try {
                        data = MetricsGPSTypeDevice.get(_message.getPayload(), _message.getTimestamp()).getPosition();
                    } catch (McBadRequestException ex) {
                        _logger.error("Exception,", ex);
                    }
                    break;
                default:
                    data = _message.getPayload();
                    break;

            }
            sensorVariable = SensorVariable.builder()
                    .sensor(sensor)
                    .variableType(MESSAGE_TYPE_SET_REQ.fromString(_message.getSubType()))
                    .value(data)
                    .timestamp(_message.getTimestamp())
                    .metricType(metricType).build().updateUnitAndMetricType();
            _logger.debug("This SensorVariable:[{}] for Sensor:{}] is not available in our DB, Adding...",
                    sensorVariable, sensor);

            DaoUtils.getSensorVariableDao().create(sensorVariable);
            GoogleAnalyticsApi.instance().trackSensorVariableCreation(sensorVariable.getVariableType().getText());
            sensorVariable = DaoUtils.getSensorVariableDao().get(sensorVariable);
        } else {
            if (_message.getPayload() != null && _message.getPayload().length() > 0) {
                switch (sensorVariable.getMetricType()) {
                    case COUNTER:
                        long oldValue = sensorVariable.getValue() == null ? 0L : McUtils
                                .getLong(sensorVariable.getValue());
                        long newValue = McUtils.getLong(_message.getPayload());
                        sensorVariable.setValue(String.valueOf(oldValue + newValue));
                        break;
                    case DOUBLE:
                        //If it is received _message, update with offset
                        if (_message.isTxMessage()) {
                            sensorVariable
                                    .setValue(McUtils.getDoubleAsString(McUtils.getDouble(_message.getPayload())));
                        } else {
                            sensorVariable.setValue(McUtils.getDoubleAsString(
                                    McUtils.getDouble(_message.getPayload()) + sensorVariable.getOffset()));
                        }
                        break;
                    case BINARY:
                        sensorVariable.setValue(_message.getPayload().equalsIgnoreCase("0") ? "0" : "1");
                        break;
                    case GPS:
                        try {
                            sensorVariable.setValue(MetricsGPSTypeDevice.get(_message.getPayload(),
                                    _message.getTimestamp())
                                    .getPosition());
                        } catch (McBadRequestException ex) {
                            _logger.error("Exception,", ex);
                        }
                        break;
                    default:
                        sensorVariable.setValue(_message.getPayload());
                        break;
                }
            } else {
                sensorVariable.setValue(_message.getPayload());
            }
            sensorVariable.setTimestamp(_message.getTimestamp());
            DaoUtils.getSensorVariableDao().update(sensorVariable);
        }
        return sensorVariable;
    }

    protected void addInQueue(IMessage _message) {
        _queue.add(_message);
    }

    private boolean isNodeRegistered() {
        if (_message.getNodeEui().equalsIgnoreCase(IMessage.NODE_BROADCAST_ID)) {
            return true;
        }
        Node node = getNode();
        switch (node.getRegistrationState()) {
            case BLOCKED:
                return false;
            case NEW:
                if (AppProperties.getInstance().getControllerSettings().getAutoNodeRegistration()) {
                    node.setRegistrationState(NODE_REGISTRATION_STATE.REGISTERED);
                    updateNode(node);
                    return true;
                } else {
                    return false;
                }
            case REGISTERED:
                return true;
            default:
                return false;
        }
    }

    private void executeDependentTask(SensorVariable _sv) {
        McThreadPoolFactory.execute(new ExecuteMessageDependentTask(_sv));
    }

    @Override
    public void firmwareUpdateStart(int totalBlocks) {
        Node node = getNode();
        if (node != null) {
            node.firmwareUpdateStart(totalBlocks);
            updateNode(node);
            _logger.debug("Firmware update start, totalBlocks:{}, Node:[id:{}, name:{}, eui:{}, firmware:{}]",
                    totalBlocks, node.getId(), node.getName(), node.getEui(), node.getFirmware().getFirmwareName());
        }
    }

    @Override
    public void firmwareUpdateFinished() {
        Node node = getNode();
        if (node != null) {
            node.firmwareUpdateFinished();
            updateNode(node);
            _logger.debug("Firmware update finished, Node:[id:{}, name:{}, eui:{}, firmware:{}]",
                    node.getId(), node.getName(), node.getEui(), node.getFirmware().getFirmwareName());
        }
    }

    @Override
    public void updateFirmwareStatus(int blocksSent) {
        Node node = getNode();
        if (node != null) {
            node.updateFirmwareStatus(blocksSent);
            updateNode(node);
            _logger.debug("Firmware update status, blocksSent:{}, Node:[id:{}, name:{}, eui:{}, firmware:{}]",
                    blocksSent, node.getId(), node.getName(), node.getEui(), node.getFirmware().getFirmwareName());
        }
    }
}
