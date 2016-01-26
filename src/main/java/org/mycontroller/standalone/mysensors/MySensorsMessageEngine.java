/**
 * Copyright (C) 2015-2016 Jeeva Kandasamy (jkandasa@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mycontroller.standalone.mysensors;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.AppProperties.STATE;
import org.mycontroller.standalone.MYCMessages.MESSAGE_TYPE;
import org.mycontroller.standalone.MYCMessages.MESSAGE_TYPE_INTERNAL;
import org.mycontroller.standalone.MYCMessages.MESSAGE_TYPE_PRESENTATION;
import org.mycontroller.standalone.MYCMessages.MESSAGE_TYPE_SET_REQ;
import org.mycontroller.standalone.MYCMessages.MESSAGE_TYPE_STREAM;
import org.mycontroller.standalone.MYCMessages.PAYLOAD_TYPE;
import org.mycontroller.standalone.MYCMessages;
import org.mycontroller.standalone.NodeIdException;
import org.mycontroller.standalone.NumericUtils;
import org.mycontroller.standalone.ObjectFactory;
import org.mycontroller.standalone.alarm.AlarmEngine;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.ResourcesLogsUtils;
import org.mycontroller.standalone.db.ResourcesLogsUtils.LOG_LEVEL;
import org.mycontroller.standalone.db.tables.AlarmDefinition;
import org.mycontroller.standalone.db.tables.Firmware;
import org.mycontroller.standalone.db.tables.Gateway;
import org.mycontroller.standalone.db.tables.MetricsBatteryUsage;
import org.mycontroller.standalone.db.tables.MetricsDoubleTypeDevice;
import org.mycontroller.standalone.db.tables.MetricsBinaryTypeDevice;
import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.db.tables.ForwardPayload;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.fwpayload.ExecuteForwardPayload;
import org.mycontroller.standalone.gateway.GatewayException;
import org.mycontroller.standalone.message.IMessageProcessEngine;
import org.mycontroller.standalone.message.MessageUtils;
import org.mycontroller.standalone.message.RawMessage;
import org.mycontroller.standalone.message.RawMessageException;
import org.mycontroller.standalone.metrics.MetricsUtils.AGGREGATION_TYPE;
import org.mycontroller.standalone.metrics.MetricsUtils.METRIC_TYPE;
import org.mycontroller.standalone.mysensors.firmware.FirmwareUtils;
import org.mycontroller.standalone.mysensors.structs.FirmwareConfigRequest;
import org.mycontroller.standalone.mysensors.structs.FirmwareConfigResponse;
import org.mycontroller.standalone.mysensors.structs.FirmwareRequest;
import org.mycontroller.standalone.mysensors.structs.FirmwareResponse;
import org.mycontroller.standalone.uidtag.ExecuteUidTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class MySensorsMessageEngine implements IMessageProcessEngine {
    private static final Logger _logger = LoggerFactory.getLogger(MySensorsMessageEngine.class.getName());
    private static final int FIRMWARE_PRINT_LOG = 100;

    private Firmware firmware;

    @Override
    public void executeMessage(RawMessage rawMessage) throws GatewayException {
        MySensorsRawMessage mySensorsRawMessage = null;
        try {
            mySensorsRawMessage = new MySensorsRawMessage(rawMessage);
        } catch (RawMessageException ex) {
            _logger.error("Exception while parsing RawMessage:[{}]", rawMessage);
            return;
        }
        if (mySensorsRawMessage.getAck() == 1) {
            _logger.info("This is ack message[{}]", mySensorsRawMessage);
        }

        if (mySensorsRawMessage.isTxMessage()) {
            MessageUtils.sendMessgaeToGateway(rawMessage);
            _logger.debug("This is Tx Message[{}] sent", mySensorsRawMessage);
        }

        switch (MESSAGE_TYPE.get(mySensorsRawMessage.getMessageType())) {
            case C_PRESENTATION:
                if (mySensorsRawMessage.isTxMessage()) {
                    //ResourcesLogs message data
                    if (ResourcesLogsUtils.isLevel(LOG_LEVEL.NOTICE)) {
                        this.setSensorOtherData(LOG_LEVEL.NOTICE,
                                mySensorsRawMessage,
                                MESSAGE_TYPE.get(mySensorsRawMessage.getMessageType()).getText(),
                                null);
                    }

                } else {
                    _logger.debug("Received a 'Presentation' message");
                    this.presentationSubMessageTypeSelector(mySensorsRawMessage);
                }
                break;
            case C_SET:
                _logger.debug("Received a 'Set' message");
                this.recordSetTypeData(mySensorsRawMessage);
                break;
            case C_REQ:
                _logger.debug("Received a 'Req' message");
                this.responseReqTypeData(mySensorsRawMessage);
                break;
            case C_INTERNAL:
                _logger.debug("Received a 'Internal' message");
                this.internalSubMessageTypeSelector(mySensorsRawMessage);
                break;
            case C_STREAM:
                _logger.debug("Received a 'Stream' message");
                streamSubMessageTypeSelector(mySensorsRawMessage);
                break;
            default:
                _logger.warn("Received unknown message type, "
                        + "not able to process further. Message[{}] dropped", mySensorsRawMessage);
                break;
        }
    }

    private void presentationSubMessageTypeSelector(MySensorsRawMessage mySensorsRawMessage) {
        if (mySensorsRawMessage.getChildSensorId() == MySensorsUtils.SENSOR_ID_BROADCAST) {
            Node node = getNode(mySensorsRawMessage);
            node.setLibVersion(mySensorsRawMessage.getPayload());
            node.setType(MESSAGE_TYPE_PRESENTATION.get(mySensorsRawMessage.getSubType()));
            updateNode(node);
        } else {
            Node node = getNode(mySensorsRawMessage);
            Sensor sensor = DaoUtils.getSensorDao().get(node.getId(), mySensorsRawMessage.getChildSensorId());
            if (sensor == null) {
                sensor = Sensor.builder()
                        .sensorId(mySensorsRawMessage.getChildSensorId())
                        .type(MESSAGE_TYPE_PRESENTATION.get(mySensorsRawMessage.getSubType()))
                        .name(mySensorsRawMessage.getPayload())
                        .build();
                sensor.setNode(node);
                DaoUtils.getSensorDao().create(sensor);
            } else {
                sensor.setType(MESSAGE_TYPE_PRESENTATION.get(mySensorsRawMessage.getSubType()));
                sensor.setName(mySensorsRawMessage.getPayload());
                DaoUtils.getSensorDao().update(sensor);
            }
        }
        _logger.debug("Presentation Message[type:{},payload:{}]",
                MESSAGE_TYPE_PRESENTATION.get(mySensorsRawMessage.getSubType()),
                mySensorsRawMessage.getPayload());
        //ResourcesLogs message data
        if (ResourcesLogsUtils.isLevel(LOG_LEVEL.NOTICE)) {
            this.setSensorOtherData(LOG_LEVEL.NOTICE,
                    mySensorsRawMessage,
                    MESSAGE_TYPE_PRESENTATION.get(mySensorsRawMessage.getSubType()).getText(),
                    null);
        }

    }

    private void internalSubMessageTypeSelector(MySensorsRawMessage mySensorsRawMessage) {
        //Get node, if node not available create
        Node node = getNode(mySensorsRawMessage);
        //ResourcesLogs message data
        if (ResourcesLogsUtils.isLevel(LOG_LEVEL.NOTICE)) {
            this.setSensorOtherData(LOG_LEVEL.NOTICE,
                    mySensorsRawMessage,
                    MESSAGE_TYPE_INTERNAL.get(mySensorsRawMessage.getSubType()).getText(),
                    null);
        }

        _logger.debug("Message Type:{}", MESSAGE_TYPE_INTERNAL.get(mySensorsRawMessage.getSubType()).toString());
        switch (MESSAGE_TYPE_INTERNAL.get(mySensorsRawMessage.getSubType())) {
            case I_BATTERY_LEVEL:
                if (mySensorsRawMessage.isTxMessage()) {
                    return;
                }
                _logger.debug("Battery Level:[nodeId:{},Level:{}%]",
                        mySensorsRawMessage.getNodeEui(),
                        mySensorsRawMessage.getPayload());
                node.setBatteryLevel(mySensorsRawMessage.getPayload());
                updateNode(node);
                //Update battery level in to metrics table
                MetricsBatteryUsage batteryUsage = MetricsBatteryUsage.builder()
                        .node(node)
                        .timestamp(System.currentTimeMillis())
                        .value(mySensorsRawMessage.getPayloadDouble())
                        .build();

                DaoUtils.getMetricsBatteryUsageDao().create(batteryUsage);

                break;
            case I_TIME:
                if (mySensorsRawMessage.isTxMessage()) {
                    return;
                }
                TimeZone timeZone = TimeZone.getDefault();
                long utcTime = System.currentTimeMillis();
                long timeOffset = timeZone.getOffset(utcTime);
                long localTime = utcTime + timeOffset;
                mySensorsRawMessage.setPayload(String.valueOf(localTime / 1000));
                mySensorsRawMessage.setTxMessage(true);
                _logger.debug("Time Message:[{}]", mySensorsRawMessage);
                ObjectFactory.getRawMessageQueue().putMessage(mySensorsRawMessage.getRawMessage());
                _logger.debug("Time request resolved.");
                break;
            case I_VERSION:
                _logger.debug("Gateway version requested by {}! Message:{}",
                        AppProperties.APPLICATION_NAME,
                        mySensorsRawMessage);
                break;
            case I_ID_REQUEST:
                try {
                    int nodeId = MySensorsUtils.getNextNodeId(mySensorsRawMessage.getGatewayId());
                    mySensorsRawMessage.setPayload(nodeId);
                    mySensorsRawMessage.setSubType(MESSAGE_TYPE_INTERNAL.I_ID_RESPONSE.ordinal());
                    mySensorsRawMessage.setTxMessage(true);
                    ObjectFactory.getRawMessageQueue().putMessage(mySensorsRawMessage.getRawMessage());
                    _logger.debug("New Id[{}] sent to node", nodeId);
                } catch (NodeIdException ex) {
                    _logger.error("Unable to generate new node Id,", ex);
                    //ResourcesLogs message data
                    if (ResourcesLogsUtils.isLevel(LOG_LEVEL.ERROR)) {
                        this.setSensorOtherData(LOG_LEVEL.ERROR,
                                mySensorsRawMessage,
                                MESSAGE_TYPE_INTERNAL.get(mySensorsRawMessage.getSubType()).getText(),
                                ex.getMessage());
                    }

                }
                break;
            case I_INCLUSION_MODE:
                _logger.warn("Inclusion mode not supported by this controller! Message:{}",
                        mySensorsRawMessage);
                break;
            case I_CONFIG:
                if (mySensorsRawMessage.isTxMessage()) {
                    return;
                }
                mySensorsRawMessage.setPayload(MySensorsUtils.getMetricType());
                mySensorsRawMessage.setTxMessage(true);
                ObjectFactory.getRawMessageQueue().putMessage(mySensorsRawMessage.getRawMessage());
                _logger.debug("Configuration sent as follow[M/I]?:{}", mySensorsRawMessage.getPayload());
                break;
            case I_LOG_MESSAGE:
                if (mySensorsRawMessage.isTxMessage()) {
                    return;
                }
                _logger.debug("Node trace-log message[nodeId:{},sensorId:{},message:{}]",
                        mySensorsRawMessage.getNodeEui(),
                        mySensorsRawMessage.getChildSensorId(),
                        mySensorsRawMessage.getPayload());
                break;
            case I_SKETCH_NAME:
                if (mySensorsRawMessage.isTxMessage()) {
                    return;
                }
                _logger.debug("Internal Message[type:{},name:{}]",
                        MESSAGE_TYPE_INTERNAL.get(mySensorsRawMessage.getSubType()),
                        mySensorsRawMessage.getPayload());
                node = getNode(mySensorsRawMessage);
                node.setName(mySensorsRawMessage.getPayload());
                updateNode(node);
                break;
            case I_SKETCH_VERSION:
                if (mySensorsRawMessage.isTxMessage()) {
                    return;
                }
                _logger.debug("Internal Message[type:{},version:{}]",
                        MESSAGE_TYPE_INTERNAL.get(mySensorsRawMessage.getSubType()),
                        mySensorsRawMessage.getPayload());
                node = getNode(mySensorsRawMessage);
                node.setVersion(mySensorsRawMessage.getPayload());
                updateNode(node);
                break;
            case I_REBOOT:
                break;
            case I_GATEWAY_READY:
                if (mySensorsRawMessage.isTxMessage()) {
                    return;
                }
                _logger.debug("Gateway Ready[nodeId:{},message:{}]",
                        mySensorsRawMessage.getNodeEui(),
                        mySensorsRawMessage.getPayload());
                break;

            case I_ID_RESPONSE:
                _logger.debug("Internal Message, Type:I_ID_RESPONSE[{}]", mySensorsRawMessage);
                return;
            case I_HEARTBEAT:
                if (mySensorsRawMessage.isTxMessage()) {
                    return;
                }
                node = getNode(mySensorsRawMessage);
                node.setState(STATE.UP);
                updateNode(node);
                break;
            default:
                _logger.warn(
                        "Internal Message[type:{},payload:{}], This type may not be supported (or) not implemented yet",
                        MESSAGE_TYPE_INTERNAL.get(mySensorsRawMessage.getSubType()),
                        mySensorsRawMessage.getPayload());
                break;
        }
    }

    //We are not logging firmware request/response in to db, as it is huge!
    private void streamSubMessageTypeSelector(MySensorsRawMessage mySensorsRawMessage) {
        switch (MESSAGE_TYPE_STREAM.get(mySensorsRawMessage.getSubType())) {
            case ST_FIRMWARE_CONFIG_REQUEST:
                //ResourcesLogs message data
                if (ResourcesLogsUtils.isLevel(LOG_LEVEL.NOTICE)) {
                    this.setSensorOtherData(LOG_LEVEL.NOTICE,
                            mySensorsRawMessage,
                            MESSAGE_TYPE_STREAM.get(mySensorsRawMessage.getSubType()).getText(), null);
                }
                this.processFirmwareConfigRequest(mySensorsRawMessage);
                break;
            case ST_FIRMWARE_REQUEST:
                //ResourcesLogs message data
                if (ResourcesLogsUtils.isLevel(LOG_LEVEL.TRACE)) {
                    this.setSensorOtherData(LOG_LEVEL.TRACE,
                            mySensorsRawMessage,
                            MESSAGE_TYPE_STREAM.get(mySensorsRawMessage.getSubType()).getText(), null);
                }
                this.procressFirmwareRequest(mySensorsRawMessage);
                break;
            case ST_FIRMWARE_CONFIG_RESPONSE:
                //ResourcesLogs message data
                if (ResourcesLogsUtils.isLevel(LOG_LEVEL.NOTICE)) {
                    this.setSensorOtherData(LOG_LEVEL.NOTICE,
                            mySensorsRawMessage,
                            MESSAGE_TYPE_STREAM.get(mySensorsRawMessage.getSubType()).getText(), null);
                }

                break;
            case ST_FIRMWARE_RESPONSE:
            case ST_IMAGE:
            case ST_SOUND:
                //ResourcesLogs message data
                if (ResourcesLogsUtils.isLevel(LOG_LEVEL.TRACE)) {
                    this.setSensorOtherData(LOG_LEVEL.TRACE,
                            mySensorsRawMessage,
                            MESSAGE_TYPE_STREAM.get(mySensorsRawMessage.getSubType()).getText(), null);
                }
                break;
            default:
                //ResourcesLogs message data
                if (ResourcesLogsUtils.isLevel(LOG_LEVEL.WARNING)) {
                    this.setSensorOtherData(LOG_LEVEL.WARNING,
                            mySensorsRawMessage,
                            MESSAGE_TYPE_STREAM.get(mySensorsRawMessage.getSubType()).getText(), null);
                }
                _logger.debug("Stream Message[type:{},payload:{}], This type not be implemented yet",
                        MESSAGE_TYPE_STREAM.get(mySensorsRawMessage.getSubType()),
                        mySensorsRawMessage.getPayload());
                break;
        }
    }

    private void procressFirmwareRequest(MySensorsRawMessage mySensorsRawMessage) {
        FirmwareRequest firmwareRequest = new FirmwareRequest();
        try {
            firmwareRequest.setByteBuffer(
                    ByteBuffer.wrap(Hex.decodeHex(mySensorsRawMessage.getPayload().toCharArray())).order(
                            ByteOrder.LITTLE_ENDIAN), 0);
            _logger.debug("Firmware Request:[Type:{},Version:{},Block:{}]", firmwareRequest.getType(),
                    firmwareRequest.getVersion(),
                    firmwareRequest.getBlock());

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
                            mySensorsRawMessage,
                            MESSAGE_TYPE_STREAM.ST_FIRMWARE_REQUEST.getText(),
                            "Block No: " + firmwareRequest.getBlock());
                }
            }

            mySensorsRawMessage.setTxMessage(true);
            mySensorsRawMessage.setSubType(MESSAGE_TYPE_STREAM.ST_FIRMWARE_RESPONSE.ordinal());
            mySensorsRawMessage.setPayload(Hex.encodeHexString(firmwareResponse.getByteBuffer().array())
                    + builder.toString());
            ObjectFactory.getRawMessageQueue().putMessage(mySensorsRawMessage.getRawMessage());
            _logger.debug("FirmwareRespone:[Type:{},Version:{},Block:{}]",
                    firmwareResponse.getType(), firmwareResponse.getVersion(), firmwareResponse.getBlock());

            // Print firmware status in sensor logs
            if (firmwareRequest.getBlock() % FIRMWARE_PRINT_LOG == 0
                    || firmwareRequest.getBlock() == (firmware.getBlocks() - 1)) {
                //ResourcesLogs message data
                if (ResourcesLogsUtils.isLevel(LOG_LEVEL.INFO)) {
                    this.setSensorOtherData(LOG_LEVEL.INFO,
                            mySensorsRawMessage,
                            MESSAGE_TYPE_STREAM.ST_FIRMWARE_RESPONSE.getText(),
                            "Block No:" + firmwareRequest.getBlock());
                }

            }

        } catch (DecoderException ex) {
            _logger.error("Exception, ", ex);
        }
    }

    private void processFirmwareConfigRequest(MySensorsRawMessage mySensorsRawMessage) {
        FirmwareConfigRequest firmwareConfigRequest = new FirmwareConfigRequest();
        try {
            firmwareConfigRequest.setByteBuffer(
                    ByteBuffer.wrap(Hex.decodeHex(mySensorsRawMessage.getPayload().toCharArray())).order(
                            ByteOrder.LITTLE_ENDIAN), 0);
            boolean bootLoaderCommand = false;
            Firmware firmware = null;

            //Check firmware is configured for this particular node
            Node node = DaoUtils.getNodeDao()
                    .get(mySensorsRawMessage.getGatewayId(), mySensorsRawMessage.getNodeEui());
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
                    _logger.debug("If requested firmware is not available, redirect to default firmware is set, Checking the default firmware");
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

            mySensorsRawMessage.setTxMessage(true);
            mySensorsRawMessage.setSubType(MESSAGE_TYPE_STREAM.ST_FIRMWARE_CONFIG_RESPONSE.ordinal());
            mySensorsRawMessage
                    .setPayload(Hex.encodeHexString(firmwareConfigResponse.getByteBuffer().array()).toUpperCase());
            ObjectFactory.getRawMessageQueue().putMessage(mySensorsRawMessage.getRawMessage());
            _logger.debug("FirmwareConfigRequest:[{}]", firmwareConfigRequest);
            _logger.debug("FirmwareConfigResponse:[{}]", firmwareConfigResponse);
        } catch (DecoderException ex) {
            _logger.error("Exception, ", ex);
        }
    }

    private void responseReqTypeData(MySensorsRawMessage mySensorsRawMessage) {
        Sensor sensor = this.getSensor(mySensorsRawMessage);
        SensorVariable sensorVariable = DaoUtils.getSensorVariableDao().get(sensor.getId(),
                MESSAGE_TYPE_SET_REQ.get(mySensorsRawMessage.getSubType()));
        if (sensorVariable != null && sensorVariable.getValue() != null) {
            //ResourcesLogs message data
            if (ResourcesLogsUtils.isLevel(LOG_LEVEL.INFO)) {
                this.setSensorVariableData(LOG_LEVEL.INFO, MESSAGE_TYPE.C_REQ, sensorVariable, mySensorsRawMessage,
                        null);
            }
            mySensorsRawMessage.setTxMessage(true);
            mySensorsRawMessage.setMessageType(MESSAGE_TYPE.C_SET.ordinal());
            mySensorsRawMessage.setAck(0);
            mySensorsRawMessage.setPayload(sensorVariable.getValue());
            ObjectFactory.getRawMessageQueue().putMessage(mySensorsRawMessage.getRawMessage());
            _logger.debug("Request processed! Message Sent: {}", mySensorsRawMessage);
        } else {
            //If sensorVariable not available create new one.
            if (sensorVariable == null) {
                sensorVariable = this.updateSensorVariable(mySensorsRawMessage, this.getSensor(mySensorsRawMessage),
                        MYCMessages.getPayLoadType(MESSAGE_TYPE_SET_REQ.get(mySensorsRawMessage.getSubType())));
            }
            //ResourcesLogs message data
            if (ResourcesLogsUtils.isLevel(LOG_LEVEL.WARNING)) {
                this.setSensorVariableData(LOG_LEVEL.WARNING, MESSAGE_TYPE.C_REQ, sensorVariable, mySensorsRawMessage,
                        "Data not available in " + AppProperties.APPLICATION_NAME);
            }
            _logger.warn("Data not available! but there is request from sensor[{}], "
                    + "Ignored this request!", mySensorsRawMessage);
        }
    }

    private SensorVariable updateSensorVariable(MySensorsRawMessage mySensorsRawMessage, Sensor sensor,
            PAYLOAD_TYPE payloadType) {
        SensorVariable sensorVariable = DaoUtils.getSensorVariableDao().get(sensor.getId(),
                MESSAGE_TYPE_SET_REQ.get(mySensorsRawMessage.getSubType()));
        METRIC_TYPE metricType = MYCMessages.getMetricType(payloadType);
        if (sensorVariable == null) {
            sensorVariable = SensorVariable.builder()
                    .sensor(sensor)
                    .variableType(MESSAGE_TYPE_SET_REQ.get(mySensorsRawMessage.getSubType()))
                    .value(mySensorsRawMessage.getPayload())
                    .timestamp(System.currentTimeMillis())
                    .metricType(metricType).build().updateUnitAndMetricType();
            _logger.debug("This SensorVariable:[{}] for Sensor:{}] is not available in our DB, Adding...",
                    sensorVariable, sensor);
            DaoUtils.getSensorVariableDao().create(sensorVariable);
            sensorVariable = DaoUtils.getSensorVariableDao().get(sensorVariable);
        } else {
            sensorVariable.setValue(mySensorsRawMessage.getPayload());
            sensorVariable.setTimestamp(System.currentTimeMillis());
            DaoUtils.getSensorVariableDao().update(sensorVariable);
        }

        //TODO: Add unit
        /* if (rawMessage.getSubType() == MESSAGE_TYPE_SET_REQ.V_UNIT_PREFIX.ordinal()) {
             sensor.setUnit(rawMessage.getPayLoad());
         }*/
        return sensorVariable;
    }

    private Sensor getSensor(MySensorsRawMessage mySensorsRawMessage) {
        Sensor sensor = DaoUtils.getSensorDao().get(
                mySensorsRawMessage.getGatewayId(),
                mySensorsRawMessage.getNodeEui(),
                mySensorsRawMessage.getChildSensorId());
        if (sensor == null) {
            getNode(mySensorsRawMessage);
            _logger.debug("This sensor[{} from Node:{}] not available in our DB, Adding...",
                    mySensorsRawMessage.getChildSensorId(), mySensorsRawMessage.getNodeEui());
            sensor = Sensor.builder().sensorId(mySensorsRawMessage.getChildSensorId()).build();
            sensor.setNode(this.getNode(mySensorsRawMessage));
            DaoUtils.getSensorDao().create(sensor);
            sensor = DaoUtils.getSensorDao().get(
                    mySensorsRawMessage.getGatewayId(),
                    mySensorsRawMessage.getNodeEui(),
                    mySensorsRawMessage.getChildSensorId());
        }
        return sensor;
    }

    private Node getNode(MySensorsRawMessage mySensorsRawMessage) {
        Node node = DaoUtils.getNodeDao().get(mySensorsRawMessage.getGatewayId(), mySensorsRawMessage.getNodeEui());
        if (node == null) {
            _logger.debug("This Node[{}] not available in our DB, Adding...", mySensorsRawMessage.getNodeEui());
            node = Node.builder().gateway(Gateway.builder().id(mySensorsRawMessage.getGatewayId()).build())
                    .eui(mySensorsRawMessage.getNodeEui()).state(STATE.UP).build();
            node.setLastSeen(System.currentTimeMillis());
            DaoUtils.getNodeDao().create(node);
            node = DaoUtils.getNodeDao().get(mySensorsRawMessage.getGatewayId(), mySensorsRawMessage.getNodeEui());
        }
        _logger.debug("Node:[{}], message:[{}]", node, mySensorsRawMessage);
        return node;
    }

    private void updateNode(Node node) {
        node.setLastSeen(System.currentTimeMillis());
        DaoUtils.getNodeDao().update(node);
    }

    private void recordSetTypeData(MySensorsRawMessage mySensorsRawMessage) {
        PAYLOAD_TYPE payloadType = MYCMessages.getPayLoadType(MESSAGE_TYPE_SET_REQ.get(mySensorsRawMessage
                .getSubType()));
        Sensor sensor = this.getSensor(mySensorsRawMessage);
        SensorVariable sensorVariable = this.updateSensorVariable(mySensorsRawMessage, sensor, payloadType);

        _logger.debug("Sensor:{}[NodeId:{},SesnorId:{},SubType({}):{}], PayLoad Type: {}",
                sensor.getName(),
                sensor.getNode().getEui(), sensor.getSensorId(),
                mySensorsRawMessage.getSubType(),
                MESSAGE_TYPE_SET_REQ.get(mySensorsRawMessage.getSubType()),
                payloadType.toString());

        if (mySensorsRawMessage.getSubType() == MESSAGE_TYPE_SET_REQ.V_UNIT_PREFIX.ordinal()) {
            //TODO: Add fix
            /*
              //Set Unit
              sensor.setUnit(rawMessage.getPayLoad());*/
            DaoUtils.getSensorDao().update(sensor);
            //ResourcesLogs message data
            if (ResourcesLogsUtils.isLevel(LOG_LEVEL.NOTICE)) {
                this.setSensorOtherData(LOG_LEVEL.NOTICE, mySensorsRawMessage,
                        MESSAGE_TYPE_SET_REQ.V_UNIT_PREFIX.getText(), null);
            }
            return;
        }

        /*if (sensor.getMessageType() == null) {
            sensor.setMessageType(rawMessage.getSubType());
        } else if (sensor.getMessageType() != rawMessage.getSubType()) {
            sensor.setMessageType(rawMessage.getSubType());
        }
        */
        sensor.setLastSeen(System.currentTimeMillis());
        DaoUtils.getSensorDao().update(sensor);

        switch (sensorVariable.getMetricType()) {
            case DOUBLE:
                DaoUtils.getMetricsDoubleTypeDeviceDao()
                        .create(MetricsDoubleTypeDevice.builder()
                                .sensorVariable(sensorVariable)
                                .aggregationType(AGGREGATION_TYPE.RAW)
                                .timestamp(System.currentTimeMillis())
                                .avg(NumericUtils.getDouble(mySensorsRawMessage.getPayload()))
                                .samples(1).build());

                break;
            case BINARY:
                DaoUtils.getMetricsBinaryTypeDeviceDao()
                        .create(MetricsBinaryTypeDevice.builder()
                                .sensorVariable(sensorVariable)
                                .timestamp(System.currentTimeMillis())
                                .state(mySensorsRawMessage.getPayloadBoolean()).build());
                break;
            default:
                _logger.debug(
                        "This type not be implemented yet, PayloadType:{}, MessageType:{}, MySensorsRawMessage:{}",
                        payloadType, MESSAGE_TYPE_SET_REQ.get(mySensorsRawMessage.getSubType())
                                .toString(),
                        mySensorsRawMessage.getPayload());
                break;
        }

        //ResourcesLogs message data
        if (ResourcesLogsUtils.isLevel(LOG_LEVEL.INFO)) {
            this.setSensorVariableData(LOG_LEVEL.INFO, MESSAGE_TYPE.C_SET, sensorVariable, mySensorsRawMessage, null);
        }

        if (!mySensorsRawMessage.isTxMessage()) {
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

        //Trigger AlarmDefinition for this sensor
        List<AlarmDefinition> alarmDefinitions = DaoUtils.getAlarmDefinitionDao().getAllEnabled(
                RESOURCE_TYPE.SENSOR_VARIABLE,
                sensorVariable.getId());
        if (alarmDefinitions.size() > 0 && alarmDefinitions != null) {
            AlarmEngine alarmEngine = new AlarmEngine(alarmDefinitions, sensorVariable);
            new Thread(alarmEngine).run();
        }
    }

    private void setSensorVariableData(LOG_LEVEL logLevel, MESSAGE_TYPE type, SensorVariable sensorVariable,
            MySensorsRawMessage mySensorsRawMessage, String extraMessage) {
        this.setSensorOtherData(RESOURCE_TYPE.SENSOR_VARIABLE, sensorVariable.getId(),
                logLevel, type, null,
                mySensorsRawMessage.isTxMessage(),
                mySensorsRawMessage.getPayload(), extraMessage);
    }

    private void setSensorOtherData(LOG_LEVEL logLevel, MySensorsRawMessage mySensorsRawMessage,
            String messageSubType, String extraMessage) {
        if (mySensorsRawMessage.getChildSensorId() == MySensorsUtils.SENSOR_ID_BROADCAST) {
            Node node = DaoUtils.getNodeDao().get(
                    mySensorsRawMessage.getGatewayId(), mySensorsRawMessage.getNodeEui());
            this.setSensorOtherData(
                    RESOURCE_TYPE.NODE, node.getId(),
                    logLevel, MESSAGE_TYPE.get(mySensorsRawMessage.getMessageType()),
                    messageSubType, mySensorsRawMessage.isTxMessage(),
                    mySensorsRawMessage.getPayload(), extraMessage);
        } else {
            Sensor sensor = DaoUtils.getSensorDao().get(mySensorsRawMessage.getGatewayId(),
                    mySensorsRawMessage.getNodeEui(), mySensorsRawMessage.getChildSensorId());
            //TODO: For now creating sensor, if it's not available, we should remove this once this issue resolved
            //http://forum.mysensors.org/topic/2669/gateway-ready-message-with-sensor-id-0-v-1-6-beta
            //-----------------------------------------
            if (sensor == null) {
                sensor = this.getSensor(mySensorsRawMessage);
            }
            //-----------------------------------------
            this.setSensorOtherData(RESOURCE_TYPE.SENSOR, sensor.getId(),
                    logLevel, MESSAGE_TYPE.get(mySensorsRawMessage.getMessageType()),
                    messageSubType, mySensorsRawMessage.isTxMessage(),
                    mySensorsRawMessage.getPayload(), extraMessage);
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
}
