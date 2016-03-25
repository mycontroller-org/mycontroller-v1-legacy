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
package org.mycontroller.standalone.message;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.AppProperties.NETWORK_TYPE;
import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.AppProperties.STATE;
import org.mycontroller.standalone.McUtils;
import org.mycontroller.standalone.ObjectFactory;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.ResourcesLogsUtils;
import org.mycontroller.standalone.db.ResourcesLogsUtils.LOG_LEVEL;
import org.mycontroller.standalone.db.tables.Firmware;
import org.mycontroller.standalone.db.tables.ForwardPayload;
import org.mycontroller.standalone.db.tables.GatewayTable;
import org.mycontroller.standalone.db.tables.MetricsBatteryUsage;
import org.mycontroller.standalone.db.tables.MetricsBinaryTypeDevice;
import org.mycontroller.standalone.db.tables.MetricsDoubleTypeDevice;
import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.exceptions.NodeIdException;
import org.mycontroller.standalone.fwpayload.ExecuteForwardPayload;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_INTERNAL;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_PRESENTATION;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_SET_REQ;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_STREAM;
import org.mycontroller.standalone.message.McMessageUtils.PAYLOAD_TYPE;
import org.mycontroller.standalone.metrics.MetricsUtils.AGGREGATION_TYPE;
import org.mycontroller.standalone.metrics.MetricsUtils.METRIC_TYPE;
import org.mycontroller.standalone.provider.mysensors.MySensorsUtils;
import org.mycontroller.standalone.provider.mysensors.firmware.FirmwareUtils;
import org.mycontroller.standalone.provider.mysensors.structs.FirmwareConfigRequest;
import org.mycontroller.standalone.provider.mysensors.structs.FirmwareConfigResponse;
import org.mycontroller.standalone.provider.mysensors.structs.FirmwareRequest;
import org.mycontroller.standalone.provider.mysensors.structs.FirmwareResponse;
import org.mycontroller.standalone.rule.McRuleEngine;
import org.mycontroller.standalone.uidtag.ExecuteUidTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
public class McMessageEngine implements Runnable {
    private static final Logger _logger = LoggerFactory.getLogger(McMessageEngine.class.getName());
    private static final int FIRMWARE_PRINT_LOG = 100;

    private Firmware firmware;
    private McMessage mcMessage;

    public McMessageEngine(McMessage mcMessage) {
        this.mcMessage = mcMessage;
    }

    public void execute() {
        mcMessage.setScreeningDone(true);
        if (_logger.isDebugEnabled()) {
            _logger.debug("McMessage:{}", mcMessage);
        }
        switch (mcMessage.getType()) {
            case C_PRESENTATION:
                if (mcMessage.isTxMessage()) {
                    //ResourcesLogs message data
                    if (ResourcesLogsUtils.isLevel(LOG_LEVEL.NOTICE)) {
                        this.setSensorOtherData(LOG_LEVEL.NOTICE,
                                mcMessage,
                                mcMessage.getSubType(),
                                null);
                    }

                } else {
                    this.presentationSubMessageTypeSelector(mcMessage);
                }
                break;
            case C_SET:
                this.recordSetTypeData(mcMessage);
                break;
            case C_REQ:
                this.responseReqTypeData(mcMessage);
                break;
            case C_INTERNAL:
                this.internalSubMessageTypeSelector(mcMessage);
                break;
            case C_STREAM:
                streamSubMessageTypeSelector(mcMessage);
                break;
            default:
                _logger.warn("Unknown message type, "
                        + "unable to process further. Message[{}] dropped", mcMessage);
                break;
        }
        //update node last seen and status as UP
        if (!mcMessage.isTxMessage()) {
            if (!mcMessage.getNodeEui().equalsIgnoreCase(McMessage.NODE_BROADCAST_ID)) {
                Node node = getNode(mcMessage);
                node.setState(STATE.UP);
                updateNode(node);
            }
        }
    }

    private void presentationSubMessageTypeSelector(McMessage mcMessage) {
        if (mcMessage.getSensorId() == McMessage.SENSOR_BROADCAST_ID) {
            Node node = getNode(mcMessage);
            node.setLibVersion(mcMessage.getPayload());
            node.setType(MESSAGE_TYPE_PRESENTATION.fromString(mcMessage.getSubType()));
            updateNode(node);
        } else {
            Node node = getNode(mcMessage);
            Sensor sensor = DaoUtils.getSensorDao().get(node.getId(), mcMessage.getSensorId());
            if (sensor == null) {
                sensor = Sensor.builder()
                        .sensorId(String.valueOf(mcMessage.getSensorId()))
                        .type(MESSAGE_TYPE_PRESENTATION.fromString(mcMessage.getSubType()))
                        .name(mcMessage.getPayload())
                        .build();
                sensor.setNode(node);
                DaoUtils.getSensorDao().create(sensor);
            } else {
                sensor.setType(MESSAGE_TYPE_PRESENTATION.fromString(mcMessage.getSubType()));
                sensor.setName(mcMessage.getPayload());
                DaoUtils.getSensorDao().update(sensor);
            }
        }
        if (_logger.isDebugEnabled()) {
            _logger.debug("Presentation Message[type:{},payload:{}]",
                    MESSAGE_TYPE_PRESENTATION.fromString(mcMessage.getSubType()),
                    mcMessage.getPayload());
        }
        //ResourcesLogs message data
        if (ResourcesLogsUtils.isLevel(LOG_LEVEL.NOTICE)) {
            this.setSensorOtherData(LOG_LEVEL.NOTICE,
                    mcMessage,
                    MESSAGE_TYPE_PRESENTATION.fromString(mcMessage.getSubType()).getText(),
                    null);
        }

    }

    private void internalSubMessageTypeSelector(McMessage mcMessage) {
        //Get node, if node not available create
        Node node = getNode(mcMessage);
        //ResourcesLogs message data
        if (ResourcesLogsUtils.isLevel(LOG_LEVEL.NOTICE)) {
            this.setSensorOtherData(LOG_LEVEL.NOTICE,
                    mcMessage,
                    MESSAGE_TYPE_INTERNAL.fromString(mcMessage.getSubType()).getText(),
                    null);
        }
        if (_logger.isDebugEnabled()) {
            _logger.debug("Message Type:{}", MESSAGE_TYPE_INTERNAL.fromString(mcMessage.getSubType()).toString());
        }
        switch (MESSAGE_TYPE_INTERNAL.fromString(mcMessage.getSubType())) {
            case I_BATTERY_LEVEL:
                if (mcMessage.isTxMessage()) {
                    return;
                }
                if (_logger.isDebugEnabled()) {
                    _logger.debug("Battery Level:[nodeId:{},Level:{}%]",
                            mcMessage.getNodeEui(),
                            mcMessage.getPayload());
                }
                node.setBatteryLevel(mcMessage.getPayload());
                updateNode(node);
                //Update battery level in to metrics table
                MetricsBatteryUsage batteryUsage = MetricsBatteryUsage.builder()
                        .node(node)
                        .timestamp(System.currentTimeMillis())
                        .aggregationType(AGGREGATION_TYPE.RAW)
                        .avg(McUtils.getDouble(mcMessage.getPayload()))
                        .samples(1)
                        .build();

                DaoUtils.getMetricsBatteryUsageDao().create(batteryUsage);

                break;
            case I_TIME:
                if (mcMessage.isTxMessage()) {
                    return;
                }
                TimeZone timeZone = TimeZone.getDefault();
                long utcTime = System.currentTimeMillis();
                long timeOffset = timeZone.getOffset(utcTime);
                long localTime = utcTime + timeOffset;
                mcMessage.setPayload(String.valueOf(localTime / 1000));
                mcMessage.setTxMessage(true);
                if (_logger.isDebugEnabled()) {
                    _logger.debug("Time Message:[{}]", mcMessage);
                }
                McMessageUtils.sendToProviderBridge(mcMessage);
                _logger.debug("Time request resolved.");
                break;
            case I_VERSION:
                if (_logger.isDebugEnabled()) {
                    _logger.debug("GatewayTable version requested by {}! Message:{}",
                            AppProperties.APPLICATION_NAME,
                            mcMessage);
                }
                break;
            case I_ID_REQUEST:
                try {
                    if (mcMessage.getNetworkType() == NETWORK_TYPE.MY_SENSORS) {
                        int nodeId = MySensorsUtils.getNextNodeId(mcMessage.getGatewayId());
                        mcMessage.setPayload(String.valueOf(nodeId));
                        mcMessage.setSubType(MESSAGE_TYPE_INTERNAL.I_ID_RESPONSE.getText());
                        mcMessage.setTxMessage(true);
                        McMessageUtils.sendToProviderBridge(mcMessage);
                        if (_logger.isDebugEnabled()) {
                            _logger.debug("New Id[{}] sent to node", nodeId);
                        }
                    }
                } catch (NodeIdException ex) {
                    _logger.error("Unable to generate new node Id,", ex);
                    //ResourcesLogs message data
                    if (ResourcesLogsUtils.isLevel(LOG_LEVEL.ERROR)) {
                        this.setSensorOtherData(LOG_LEVEL.ERROR,
                                mcMessage,
                                MESSAGE_TYPE_INTERNAL.fromString(mcMessage.getSubType()).getText(),
                                ex.getMessage());
                    }

                }
                break;
            case I_INCLUSION_MODE:
                _logger.warn("Inclusion mode not supported by this controller! Message:{}",
                        mcMessage);
                break;
            case I_CONFIG:
                if (mcMessage.isTxMessage()) {
                    return;
                }
                mcMessage.setPayload(McMessageUtils.getMetricType());
                mcMessage.setTxMessage(true);
                McMessageUtils.sendToProviderBridge(mcMessage);
                if (_logger.isDebugEnabled()) {
                    _logger.debug("Configuration sent as follow[M/I]?:{}", mcMessage.getPayload());
                }
                break;
            case I_LOG_MESSAGE:
                if (mcMessage.isTxMessage()) {
                    return;
                }
                if (_logger.isDebugEnabled()) {
                    _logger.debug("Node trace-log message[nodeId:{},sensorId:{},message:{}]",
                            mcMessage.getNodeEui(),
                            mcMessage.getSensorId(),
                            mcMessage.getPayload());
                }
                break;
            case I_SKETCH_NAME:
                if (mcMessage.isTxMessage()) {
                    return;
                }
                if (_logger.isDebugEnabled()) {
                    _logger.debug("Internal Message[type:{},name:{}]",
                            MESSAGE_TYPE_INTERNAL.fromString(mcMessage.getSubType()),
                            mcMessage.getPayload());
                }
                node = getNode(mcMessage);
                node.setName(mcMessage.getPayload());
                updateNode(node);
                break;
            case I_SKETCH_VERSION:
                if (mcMessage.isTxMessage()) {
                    return;
                }
                if (_logger.isDebugEnabled()) {
                    _logger.debug("Internal Message[type:{},version:{}]",
                            MESSAGE_TYPE_INTERNAL.fromString(mcMessage.getSubType()),
                            mcMessage.getPayload());
                }
                node = getNode(mcMessage);
                node.setVersion(mcMessage.getPayload());
                updateNode(node);
                break;
            case I_REBOOT:
                break;
            case I_GATEWAY_READY:
                if (mcMessage.isTxMessage()) {
                    return;
                }
                if (_logger.isDebugEnabled()) {
                    _logger.debug("GatewayTable Ready[nodeId:{},message:{}]",
                            mcMessage.getNodeEui(),
                            mcMessage.getPayload());
                }
                break;

            case I_ID_RESPONSE:
                if (_logger.isDebugEnabled()) {
                    _logger.debug("Internal Message, Type:I_ID_RESPONSE[{}]", mcMessage);
                }
                return;
            case I_HEARTBEAT:
            case I_HEARTBEAT_RESPONSE:
                if (mcMessage.isTxMessage()) {
                    return;
                }
                node = getNode(mcMessage);
                node.setState(STATE.UP);
                updateNode(node);
                break;
            default:
                _logger.warn(
                        "Internal Message[type:{},payload:{}], "
                        + "This type may not be supported (or) not implemented yet",
                        MESSAGE_TYPE_INTERNAL.fromString(mcMessage.getSubType()),
                        mcMessage.getPayload());
                break;
        }
    }

    //We are not logging firmware request/response in to db, as it is huge!
    private void streamSubMessageTypeSelector(McMessage mcMessage) {
        switch (MESSAGE_TYPE_STREAM.fromString(mcMessage.getSubType())) {
            case ST_FIRMWARE_CONFIG_REQUEST:
                //ResourcesLogs message data
                if (ResourcesLogsUtils.isLevel(LOG_LEVEL.NOTICE)) {
                    this.setSensorOtherData(LOG_LEVEL.NOTICE,
                            mcMessage,
                            MESSAGE_TYPE_STREAM.fromString(mcMessage.getSubType()).getText(), null);
                }
                this.processFirmwareConfigRequest(mcMessage);
                break;
            case ST_FIRMWARE_REQUEST:
                //ResourcesLogs message data
                if (ResourcesLogsUtils.isLevel(LOG_LEVEL.TRACE)) {
                    this.setSensorOtherData(LOG_LEVEL.TRACE,
                            mcMessage,
                            MESSAGE_TYPE_STREAM.fromString(mcMessage.getSubType()).getText(), null);
                }
                this.procressFirmwareRequest(mcMessage);
                break;
            case ST_FIRMWARE_CONFIG_RESPONSE:
                //ResourcesLogs message data
                if (ResourcesLogsUtils.isLevel(LOG_LEVEL.NOTICE)) {
                    this.setSensorOtherData(LOG_LEVEL.NOTICE,
                            mcMessage,
                            MESSAGE_TYPE_STREAM.fromString(mcMessage.getSubType()).getText(), null);
                }

                break;
            case ST_FIRMWARE_RESPONSE:
            case ST_IMAGE:
            case ST_SOUND:
                //ResourcesLogs message data
                if (ResourcesLogsUtils.isLevel(LOG_LEVEL.TRACE)) {
                    this.setSensorOtherData(LOG_LEVEL.TRACE,
                            mcMessage,
                            MESSAGE_TYPE_STREAM.fromString(mcMessage.getSubType()).getText(), null);
                }
                break;
            default:
                //ResourcesLogs message data
                if (ResourcesLogsUtils.isLevel(LOG_LEVEL.WARNING)) {
                    this.setSensorOtherData(LOG_LEVEL.WARNING,
                            mcMessage,
                            MESSAGE_TYPE_STREAM.fromString(mcMessage.getSubType()).getText(), null);
                }
                if (_logger.isDebugEnabled()) {
                    _logger.debug("Stream Message[type:{},payload:{}], This type not be implemented yet",
                            MESSAGE_TYPE_STREAM.fromString(mcMessage.getSubType()),
                            mcMessage.getPayload());
                }
                break;
        }
    }

    private void procressFirmwareRequest(McMessage mcMessage) {
        FirmwareRequest firmwareRequest = new FirmwareRequest();
        try {
            firmwareRequest.setByteBuffer(
                    ByteBuffer.wrap(Hex.decodeHex(mcMessage.getPayload().toCharArray())).order(
                            ByteOrder.LITTLE_ENDIAN), 0);
            if (_logger.isDebugEnabled()) {
                _logger.debug("Firmware Request:[Type:{},Version:{},Block:{}]", firmwareRequest.getType(),
                        firmwareRequest.getVersion(),
                        firmwareRequest.getBlock());
            }
            boolean requestFirmwareReload = false;
            if (firmware == null) {
                requestFirmwareReload = true;
            } else if (firmware != null) {
                if (firmwareRequest.getBlock() == (firmware.getBlocks() - 1)) {
                    requestFirmwareReload = true;
                } else if (firmwareRequest.getType() == firmware.getType().getId()
                        && firmwareRequest.getVersion() == firmware.getVersion().getId()) {
                    //Nothing to do just continue
                } else {
                    requestFirmwareReload = true;
                }
            } else {
                requestFirmwareReload = true;
            }

            if (requestFirmwareReload) {
                firmware = DaoUtils.getFirmwareDao().get(firmwareRequest.getType(), firmwareRequest.getVersion());
                _logger.debug("Firmware reloaded...");
            }

            if (firmware == null) {
                _logger.debug("selected firmware type/version not available");
                return;
            }

            FirmwareResponse firmwareResponse = new FirmwareResponse();
            firmwareResponse.setByteBufferPosition(0);
            firmwareResponse.setBlock(firmwareRequest.getBlock());
            firmwareResponse.setVersion(firmwareRequest.getVersion());
            firmwareResponse.setType(firmwareRequest.getType());
            StringBuilder builder = new StringBuilder();
            int fromIndex = firmwareRequest.getBlock() * FirmwareUtils.FIRMWARE_BLOCK_SIZE;
            for (int index = fromIndex; index < fromIndex + FirmwareUtils.FIRMWARE_BLOCK_SIZE; index++) {
                builder.append(String.format("%02X", firmware.getData().get(index)));
            }
            if (firmwareRequest.getBlock() == 0) {
                firmware = null;
                _logger.debug("Firmware unloaded...");
            }

            // Print firmware status in sensor logs
            if (firmwareRequest.getBlock() % FIRMWARE_PRINT_LOG == 0
                    || firmwareRequest.getBlock() == (firmware.getBlocks() - 1)) {
                //ResourcesLogs message data
                if (ResourcesLogsUtils.isLevel(LOG_LEVEL.INFO)) {
                    this.setSensorOtherData(LOG_LEVEL.INFO,
                            mcMessage,
                            MESSAGE_TYPE_STREAM.ST_FIRMWARE_REQUEST.getText(),
                            "Block No: " + firmwareRequest.getBlock());
                }
            }

            mcMessage.setTxMessage(true);
            mcMessage.setSubType(MESSAGE_TYPE_STREAM.ST_FIRMWARE_RESPONSE.getText());
            mcMessage.setPayload(Hex.encodeHexString(firmwareResponse.getByteBuffer().array())
                    + builder.toString());
            McMessageUtils.sendToProviderBridge(mcMessage);
            if (_logger.isDebugEnabled()) {
                _logger.debug("FirmwareRespone:[Type:{},Version:{},Block:{}]",
                        firmwareResponse.getType(), firmwareResponse.getVersion(), firmwareResponse.getBlock());
            }
            // Print firmware status in sensor logs
            if (firmwareRequest.getBlock() % FIRMWARE_PRINT_LOG == 0
                    || firmwareRequest.getBlock() == (firmware.getBlocks() - 1)) {
                //ResourcesLogs message data
                if (ResourcesLogsUtils.isLevel(LOG_LEVEL.INFO)) {
                    this.setSensorOtherData(LOG_LEVEL.INFO,
                            mcMessage,
                            MESSAGE_TYPE_STREAM.ST_FIRMWARE_RESPONSE.getText(),
                            "Block No:" + firmwareRequest.getBlock());
                }

            }

        } catch (DecoderException ex) {
            _logger.error("Exception, ", ex);
        }
    }

    private void processFirmwareConfigRequest(McMessage mcMessage) {
        FirmwareConfigRequest firmwareConfigRequest = new FirmwareConfigRequest();
        try {
            firmwareConfigRequest.setByteBuffer(
                    ByteBuffer.wrap(Hex.decodeHex(mcMessage.getPayload().toCharArray())).order(
                            ByteOrder.LITTLE_ENDIAN), 0);
            boolean bootLoaderCommand = false;
            Firmware firmware = null;

            //Check firmware is configured for this particular node
            Node node = DaoUtils.getNodeDao()
                    .get(mcMessage.getGatewayId(), mcMessage.getNodeEui());
            if (node != null && node.getEraseConfig() != null && node.getEraseConfig()) {
                bootLoaderCommand = true;
                _logger.debug("Erase EEPROM has been set...");
            } else if (node != null && node.getFirmware() != null) {
                firmware = DaoUtils.getFirmwareDao().getById(node.getFirmware().getId());
                _logger.debug("Firmware selected based on node configuration...");
            } else if (firmwareConfigRequest.getType() == 65535 && firmwareConfigRequest.getVersion() == 65535) {
                if (ObjectFactory.getAppProperties().getMySensorsSettings().getDefaultFirmware() != null) {
                    firmware = DaoUtils.getFirmwareDao().getById(
                            ObjectFactory.getAppProperties().getMySensorsSettings().getDefaultFirmware());
                } else {
                    _logger.warn("There is no default firmware set!");
                }
            } else {
                firmware = DaoUtils.getFirmwareDao().get(firmwareConfigRequest.getType(),
                        firmwareConfigRequest.getVersion());
            }

            FirmwareConfigResponse firmwareConfigResponse = new FirmwareConfigResponse();
            firmwareConfigResponse.setByteBufferPosition(0);

            if (bootLoaderCommand) {//If it is bootloader command
                if (node.getEraseConfig() != null && node.getEraseConfig()) {
                    firmwareConfigResponse.loadEraseEepromCommand();
                    node.setEraseConfig(false); //Remove erase EEPROM flag and update in to database
                    DaoUtils.getNodeDao().update(node);
                } else {
                    _logger.warn("Selected booloader command is not available, FirmwareConfigRequest:[{}]",
                            firmwareConfigRequest);
                    return;
                }
            } else if (firmware == null) {//Non bootloader command
                if (ObjectFactory.getAppProperties().getMySensorsSettings().getEnbaledDefaultOnNoFirmware()) {
                    _logger.debug("If requested firmware is not available, "
                            + "redirect to default firmware is set, Checking the default firmware");
                    if (ObjectFactory.getAppProperties().getMySensorsSettings().getDefaultFirmware() != null) {
                        firmware = DaoUtils.getFirmwareDao().getById(
                                ObjectFactory.getAppProperties().getMySensorsSettings().getDefaultFirmware());
                        _logger.debug("Default firmware:[{}]", firmware.getFirmwareName());
                    } else {
                        _logger.warn("There is no default firmware set!");
                    }
                }
                //Selected, default: No firmware available for this request
                if (firmware == null) {
                    _logger.warn("Selected Firmware is not available, FirmwareConfigRequest:[{}]",
                            firmwareConfigRequest);
                    return;
                }
            }

            if (firmware != null) {
                firmwareConfigResponse.setType(firmware.getType().getId());
                firmwareConfigResponse.setVersion(firmware.getVersion().getId());
                firmwareConfigResponse.setBlocks(firmware.getBlocks());
                firmwareConfigResponse.setCrc(firmware.getCrc());
            }

            mcMessage.setTxMessage(true);
            mcMessage.setSubType(MESSAGE_TYPE_STREAM.ST_FIRMWARE_CONFIG_RESPONSE.getText());
            mcMessage
                    .setPayload(Hex.encodeHexString(firmwareConfigResponse.getByteBuffer().array()).toUpperCase());
            McMessageUtils.sendToProviderBridge(mcMessage);
            if (_logger.isDebugEnabled()) {
                _logger.debug("FirmwareConfigRequest:[{}]", firmwareConfigRequest);
                _logger.debug("FirmwareConfigResponse:[{}]", firmwareConfigResponse);
            }
        } catch (DecoderException ex) {
            _logger.error("Exception, ", ex);
        }
    }

    private void responseReqTypeData(McMessage mcMessage) {
        Sensor sensor = this.getSensor(mcMessage);
        SensorVariable sensorVariable = DaoUtils.getSensorVariableDao().get(sensor.getId(),
                MESSAGE_TYPE_SET_REQ.fromString(mcMessage.getSubType()));
        if (sensorVariable != null && sensorVariable.getValue() != null) {
            //ResourcesLogs message data
            if (ResourcesLogsUtils.isLevel(LOG_LEVEL.INFO)) {
                this.setSensorVariableData(LOG_LEVEL.INFO, MESSAGE_TYPE.C_REQ, sensorVariable, mcMessage,
                        null);
            }
            mcMessage.setTxMessage(true);
            mcMessage.setType(MESSAGE_TYPE.C_SET);
            mcMessage.setAcknowledge(false);
            mcMessage.setPayload(sensorVariable.getValue());
            McMessageUtils.sendToProviderBridge(mcMessage);
            if (_logger.isDebugEnabled()) {
                _logger.debug("Request processed! Message Sent: {}", mcMessage);
            }
        } else {
            //If sensorVariable not available create new one.
            if (sensorVariable == null) {
                sensorVariable = this.updateSensorVariable(mcMessage, this.getSensor(mcMessage),
                        McMessageUtils.getPayLoadType(MESSAGE_TYPE_SET_REQ.fromString(mcMessage.getSubType())));
            }
            //ResourcesLogs message data
            if (ResourcesLogsUtils.isLevel(LOG_LEVEL.WARNING)) {
                this.setSensorVariableData(LOG_LEVEL.WARNING, MESSAGE_TYPE.C_REQ, sensorVariable, mcMessage,
                        "Data not available in " + AppProperties.APPLICATION_NAME);
            }
            if (_logger.isWarnEnabled()) {
                _logger.warn("Data not available! but there is request from sensor[{}], "
                        + "Ignored this request!", mcMessage);
            }

        }
    }

    private SensorVariable updateSensorVariable(McMessage mcMessage, Sensor sensor,
            PAYLOAD_TYPE payloadType) {
        SensorVariable sensorVariable = DaoUtils.getSensorVariableDao().get(sensor.getId(),
                MESSAGE_TYPE_SET_REQ.fromString(mcMessage.getSubType()));
        METRIC_TYPE metricType = McMessageUtils.getMetricType(payloadType);
        if (sensorVariable == null) {
            sensorVariable = SensorVariable.builder()
                    .sensor(sensor)
                    .variableType(MESSAGE_TYPE_SET_REQ.fromString(mcMessage.getSubType()))
                    .value(mcMessage.getPayload())
                    .timestamp(System.currentTimeMillis())
                    .metricType(metricType).build().updateUnitAndMetricType();
            if (_logger.isDebugEnabled()) {
                _logger.debug("This SensorVariable:[{}] for Sensor:{}] is not available in our DB, Adding...",
                        sensorVariable, sensor);
            }

            DaoUtils.getSensorVariableDao().create(sensorVariable);
            sensorVariable = DaoUtils.getSensorVariableDao().get(sensorVariable);
        } else {
            sensorVariable.setValue(mcMessage.getPayload());
            sensorVariable.setTimestamp(System.currentTimeMillis());
            DaoUtils.getSensorVariableDao().update(sensorVariable);
        }

        //TODO: Add unit
        /* if (rawMessage.getSubType() == MYS_MESSAGE_TYPE_SET_REQ.V_UNIT_PREFIX.ordinal()) {
             sensor.setUnit(rawMessage.getPayLoad());
         }*/
        return sensorVariable;
    }

    private Sensor getSensor(McMessage mcMessage) {
        Sensor sensor = DaoUtils.getSensorDao().get(
                mcMessage.getGatewayId(),
                mcMessage.getNodeEui(),
                mcMessage.getSensorId());
        if (sensor == null) {
            getNode(mcMessage);
            if (_logger.isDebugEnabled()) {
                _logger.debug("This sensor[{} from Node:{}] not available in our DB, Adding...",
                        mcMessage.getSensorId(), mcMessage.getNodeEui());
            }
            sensor = Sensor.builder().sensorId(mcMessage.getSensorId()).build();
            sensor.setNode(this.getNode(mcMessage));
            DaoUtils.getSensorDao().create(sensor);
            sensor = DaoUtils.getSensorDao().get(
                    mcMessage.getGatewayId(),
                    mcMessage.getNodeEui(),
                    mcMessage.getSensorId());
        }
        return sensor;
    }

    private Node getNode(McMessage mcMessage) {
        Node node = DaoUtils.getNodeDao().get(mcMessage.getGatewayId(), mcMessage.getNodeEui());
        if (node == null) {
            if (_logger.isDebugEnabled()) {
                _logger.debug("This Node[{}] not available in our DB, Adding...", mcMessage.getNodeEui());
            }
            node = Node.builder().gatewayTable(GatewayTable.builder().id(mcMessage.getGatewayId()).build())
                    .eui(mcMessage.getNodeEui()).state(STATE.UP).build();
            node.setLastSeen(System.currentTimeMillis());
            DaoUtils.getNodeDao().create(node);
            node = DaoUtils.getNodeDao().get(mcMessage.getGatewayId(), mcMessage.getNodeEui());
        }
        if (_logger.isDebugEnabled()) {
            _logger.debug("Node:[{}], message:[{}]", node, mcMessage);
        }
        return node;
    }

    private void updateNode(Node node) {
        node.setLastSeen(System.currentTimeMillis());
        DaoUtils.getNodeDao().update(node);
    }

    private void recordSetTypeData(McMessage mcMessage) {
        PAYLOAD_TYPE payloadType = McMessageUtils.getPayLoadType(MESSAGE_TYPE_SET_REQ.fromString(mcMessage
                .getSubType()));
        Sensor sensor = this.getSensor(mcMessage);
        //Before updating value into table convert payload types
        //Change RGB and RGBW values
        if (MESSAGE_TYPE_SET_REQ.V_RGB == MESSAGE_TYPE_SET_REQ.fromString(mcMessage.getSubType())
                || MESSAGE_TYPE_SET_REQ.V_RGBW == MESSAGE_TYPE_SET_REQ.fromString(mcMessage.getSubType())) {
            if (!mcMessage.getPayload().startsWith("#")) {
                mcMessage.setPayload("#" + mcMessage.getPayload());
            }
        }

        SensorVariable sensorVariable = this.updateSensorVariable(mcMessage, sensor, payloadType);
        if (_logger.isDebugEnabled()) {
            _logger.debug(
                    "GatewayName:{}, SensorName:{}, NodeId:{}, SesnorId:{}, SubType:{}, PayloadType:{}, Payload:{}",
                    sensor.getName(),
                    sensor.getNode().getGatewayTable().getName(),
                    sensor.getNode().getEui(), sensor.getSensorId(),
                    MESSAGE_TYPE_SET_REQ.fromString(mcMessage.getSubType()).getText(),
                    payloadType.toString(),
                    mcMessage.getPayload());
        }
        if (mcMessage.getSubType().equals(MESSAGE_TYPE_SET_REQ.V_UNIT_PREFIX.getText())) {
            //TODO: Add fix
            /*
              //Set Unit
              sensor.setUnit(rawMessage.getPayLoad());*/
            DaoUtils.getSensorDao().update(sensor);
            //ResourcesLogs message data
            if (ResourcesLogsUtils.isLevel(LOG_LEVEL.NOTICE)) {
                this.setSensorOtherData(LOG_LEVEL.NOTICE, mcMessage,
                        MESSAGE_TYPE_SET_REQ.V_UNIT_PREFIX.getText(), null);
            }
            return;
        }

        sensor.setLastSeen(System.currentTimeMillis());
        DaoUtils.getSensorDao().update(sensor);

        switch (sensorVariable.getMetricType()) {
            case DOUBLE:
                DaoUtils.getMetricsDoubleTypeDeviceDao()
                        .create(MetricsDoubleTypeDevice.builder()
                                .sensorVariable(sensorVariable)
                                .aggregationType(AGGREGATION_TYPE.RAW)
                                .timestamp(System.currentTimeMillis())
                                .avg(McUtils.getDouble(mcMessage.getPayload()))
                                .samples(1).build());

                break;
            case BINARY:
                DaoUtils.getMetricsBinaryTypeDeviceDao()
                        .create(MetricsBinaryTypeDevice.builder()
                                .sensorVariable(sensorVariable)
                                .timestamp(System.currentTimeMillis())
                                .state(McUtils.getBoolean(mcMessage.getPayload())).build());
                break;
            default:
                if (_logger.isDebugEnabled()) {
                    _logger.debug(
                            "This type not be implemented yet, PayloadType:{}, MessageType:{}, McMessage:{}",
                            payloadType, MESSAGE_TYPE_SET_REQ.fromString(mcMessage.getSubType())
                                    .toString(),
                            mcMessage.getPayload());
                }
                break;
        }

        //ResourcesLogs message data
        if (ResourcesLogsUtils.isLevel(LOG_LEVEL.INFO)) {
            this.setSensorVariableData(LOG_LEVEL.INFO, MESSAGE_TYPE.C_SET, sensorVariable, mcMessage, null);
        }

        if (!mcMessage.isTxMessage()) {
            //Execute UidTag
            if (sensor.getType() != null
                    && sensor.getType() != null
                    && sensor.getType() == MESSAGE_TYPE_PRESENTATION.S_CUSTOM
                    && sensorVariable.getVariableType() == MESSAGE_TYPE_SET_REQ.V_VAR5) {
                ExecuteUidTag executeUidTag = new ExecuteUidTag(sensor, sensorVariable);
                new Thread(executeUidTag).start();
            }
        }

        //TODO: Forward Payload to another node, if any and only on receive from gateway
        List<ForwardPayload> forwardPayloads = DaoUtils.getForwardPayloadDao().getAllEnabled(sensorVariable.getId());
        if (forwardPayloads != null && !forwardPayloads.isEmpty()) {
            ExecuteForwardPayload executeForwardPayload =
                    new ExecuteForwardPayload(forwardPayloads, sensor, sensorVariable);
            new Thread(executeForwardPayload).run();
        }

        //Execute Rules for this sensor variable
        new Thread(new McRuleEngine(RESOURCE_TYPE.SENSOR_VARIABLE, sensorVariable.getId())).start();

    }

    private void setSensorVariableData(LOG_LEVEL logLevel, MESSAGE_TYPE type, SensorVariable sensorVariable,
            McMessage mcMessage, String extraMessage) {
        this.setSensorOtherData(RESOURCE_TYPE.SENSOR_VARIABLE, sensorVariable.getId(),
                logLevel, type, null,
                mcMessage.isTxMessage(),
                mcMessage.getPayload(), extraMessage);
    }

    private void setSensorOtherData(LOG_LEVEL logLevel, McMessage mcMessage,
            String messageSubType, String extraMessage) {
        if (mcMessage.getSensorId() == McMessage.SENSOR_BROADCAST_ID) {
            Node node = DaoUtils.getNodeDao().get(
                    mcMessage.getGatewayId(), mcMessage.getNodeEui());
            this.setSensorOtherData(
                    RESOURCE_TYPE.NODE, node.getId(),
                    logLevel, mcMessage.getType(),
                    messageSubType, mcMessage.isTxMessage(),
                    mcMessage.getPayload(), extraMessage);
        } else {
            Sensor sensor = DaoUtils.getSensorDao().get(mcMessage.getGatewayId(),
                    mcMessage.getNodeEui(), mcMessage.getSensorId());
            //TODO: For now creating sensor, if it's not available, we should remove this once this issue resolved
            //http://forum.mysensors.org/topic/2669/gateway-ready-message-with-sensor-id-0-v-1-6-beta
            //-----------------------------------------
            if (sensor == null) {
                sensor = this.getSensor(mcMessage);
            }
            //-----------------------------------------
            this.setSensorOtherData(RESOURCE_TYPE.SENSOR, sensor.getId(),
                    logLevel, mcMessage.getType(),
                    messageSubType, mcMessage.isTxMessage(),
                    mcMessage.getPayload(), extraMessage);
        }
    }

    private void setSensorOtherData(RESOURCE_TYPE resourceType, Integer resourceId, LOG_LEVEL logLevel,
            MESSAGE_TYPE messageType, String subType, boolean isTxMessage, String payload, String extraMessage) {
        StringBuilder builder = new StringBuilder();
        if (subType != null) {
            builder.append("[").append(subType).append("]");
        }
        if (payload != null) {
            builder.append(" ").append(payload);
        }
        if (extraMessage != null) {
            builder.append(" ").append(extraMessage);
        }
        ResourcesLogsUtils.recordSensorsResourcesLog(resourceType, resourceId, logLevel, messageType, isTxMessage,
                builder.toString());
    }

    @Override
    public void run() {
        this.execute();

    }
}
