/**
 * Copyright (C) 2015 Jeeva Kandasamy (jkandasa@gmail.com)
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
import org.mycontroller.standalone.NodeIdException;
import org.mycontroller.standalone.NumericUtils;
import org.mycontroller.standalone.ObjectFactory;
import org.mycontroller.standalone.db.AGGREGATION_TYPE;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.SensorLogUtils;
import org.mycontroller.standalone.db.SensorLogUtils.LOG_TYPE;
import org.mycontroller.standalone.db.alarm.ExecuteAlarm;
import org.mycontroller.standalone.db.fwpayload.ExecuteForwardPayload;
import org.mycontroller.standalone.db.tables.Alarm;
import org.mycontroller.standalone.db.tables.Firmware;
import org.mycontroller.standalone.db.tables.MetricsDoubleTypeDevice;
import org.mycontroller.standalone.db.tables.MetricsOnOffTypeDevice;
import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.db.tables.ForwardPayload;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.db.tables.Settings;
import org.mycontroller.standalone.db.uidtag.ExecuteUidTag;
import org.mycontroller.standalone.gateway.MySensorsGatewayException;
import org.mycontroller.standalone.mysensors.MyMessages.MESSAGE_TYPE;
import org.mycontroller.standalone.mysensors.MyMessages.MESSAGE_TYPE_INTERNAL;
import org.mycontroller.standalone.mysensors.MyMessages.MESSAGE_TYPE_PRESENTATION;
import org.mycontroller.standalone.mysensors.MyMessages.MESSAGE_TYPE_SET_REQ;
import org.mycontroller.standalone.mysensors.MyMessages.MESSAGE_TYPE_STREAM;
import org.mycontroller.standalone.mysensors.MyMessages.PAYLOAD_TYPE;
import org.mycontroller.standalone.mysensors.firmware.FirmwareUtils;
import org.mycontroller.standalone.mysensors.structs.FirmwareConfigRequest;
import org.mycontroller.standalone.mysensors.structs.FirmwareConfigResponse;
import org.mycontroller.standalone.mysensors.structs.FirmwareRequest;
import org.mycontroller.standalone.mysensors.structs.FirmwareResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class ProcessRawMessage {
    private static final Logger _logger = LoggerFactory.getLogger(ProcessRawMessage.class.getName());

    private Firmware firmware;

    public void messageTypeSelector(RawMessage rawMessage) throws MySensorsGatewayException {
        if (rawMessage.getAck() == 1) {
            _logger.info("This is ack message[{}]", rawMessage);
        }

        if (rawMessage.isTxMessage()) {
            ProcessRawMessageUtils.sendMessage(rawMessage);
            _logger.debug("This is Tx Message[{}] sent", rawMessage);
        }

        switch (MESSAGE_TYPE.get(rawMessage.getMessageType())) {
            case C_PRESENTATION:
                if (rawMessage.isTxMessage()) {
                    //Log message data
                    SensorLogUtils.setSensorInternalData(MESSAGE_TYPE_INTERNAL.get(rawMessage.getSubType()),
                            rawMessage, null);
                } else {
                    _logger.debug("Received a 'Presentation' message");
                    this.presentationSubMessageTypeSelector(rawMessage);
                }
                break;
            case C_SET:
                _logger.debug("Received a 'Set' message");
                this.recordSetTypeData(rawMessage);
                break;
            case C_REQ:
                _logger.debug("Received a 'Req' message");
                this.responseReqTypeData(rawMessage);
                break;
            case C_INTERNAL:
                _logger.debug("Received a 'Internal' message");
                this.internalSubMessageTypeSelector(rawMessage);
                break;
            case C_STREAM:
                _logger.debug("Received a 'Stream' message");
                streamSubMessageTypeSelector(rawMessage);
                break;
            default:
                _logger.warn("Received unknown message type, "
                        + "not able to process further. Message[{}] dropped", rawMessage);
                break;
        }
    }

    private void presentationSubMessageTypeSelector(RawMessage rawMessage) {
        if (rawMessage.getChildSensorId() == RawMessage.NODE_SENSOR_ID_BROADCAST) {
            Node node = DaoUtils.getNodeDao().get(rawMessage.getNodeId());
            if (node == null) {
                node = new Node(rawMessage.getNodeId());
                node.setUpdateTime(System.currentTimeMillis());
                node.setMySensorsVersion(rawMessage.getPayLoad());
                node.setType(rawMessage.getSubType());
                DaoUtils.getNodeDao().create(node);
            } else {
                node.setUpdateTime(System.currentTimeMillis());
                node.setMySensorsVersion(rawMessage.getPayLoad());
                node.setType(rawMessage.getSubType());
                DaoUtils.getNodeDao().update(node);
            }

        } else {
            Node node = DaoUtils.getNodeDao().get(rawMessage.getNodeId());
            if (node == null) {
                node = new Node(rawMessage.getNodeId());
                node.setUpdateTime(System.currentTimeMillis());
                DaoUtils.getNodeDao().create(node);
            }
            Sensor sensor = DaoUtils.getSensorDao().get(rawMessage.getNodeId(), rawMessage.getChildSensorId());
            if (sensor == null) {
                sensor = new Sensor(rawMessage.getChildSensorId(),
                        rawMessage.getSubType(), rawMessage.getPayLoad());
                sensor.setNode(new Node(rawMessage.getNodeId()));
                DaoUtils.getSensorDao().create(sensor);
            } else {
                sensor.setType(rawMessage.getSubType());
                sensor.setName(rawMessage.getPayLoad());
                DaoUtils.getSensorDao().update(sensor);
            }
        }
        _logger.debug("Presentation Message[type:{},payload:{}]",
                MESSAGE_TYPE_PRESENTATION.get(rawMessage.getSubType()),
                rawMessage.getPayLoad());
        SensorLogUtils.setSensorPresentationData(MESSAGE_TYPE_PRESENTATION.get(rawMessage.getSubType()), rawMessage,
                null);
    }

    private void internalSubMessageTypeSelector(RawMessage rawMessage) {
        //Log message data
        SensorLogUtils.setSensorInternalData(MESSAGE_TYPE_INTERNAL.get(rawMessage.getSubType()), rawMessage, null);
        _logger.debug("Message Type:{}", MESSAGE_TYPE_INTERNAL.get(rawMessage.getSubType()).toString());
        switch (MESSAGE_TYPE_INTERNAL.get(rawMessage.getSubType())) {
            case I_BATTERY_LEVEL:
                if (rawMessage.isTxMessage()) {
                    return;
                }
                _logger.debug("Battery Level:[nodeId:{},Level:{}%]",
                        rawMessage.getNodeId(),
                        rawMessage.getPayLoad());
                Node node = DaoUtils.getNodeDao().get(rawMessage.getNodeId());
                if (node == null) {
                    updateNode(rawMessage);
                }
                node.setBatteryLevel(rawMessage.getPayLoad());
                DaoUtils.getNodeDao().update(node);

                break;
            case I_TIME:
                if (rawMessage.isTxMessage()) {
                    return;
                }
                TimeZone timeZone = TimeZone.getDefault();
                long utcTime = System.currentTimeMillis();
                long timeOffset = timeZone.getOffset(utcTime);
                long localTime = utcTime + timeOffset;
                rawMessage.setPayLoad(String.valueOf(localTime / 1000));
                rawMessage.setTxMessage(true);
                _logger.debug("Time Message:[{}]", rawMessage);
                ObjectFactory.getRawMessageQueue().putMessage(rawMessage);
                _logger.debug("Time request resolved.");
                break;
            case I_VERSION:
                _logger.debug("Gateway version requested by {}! Message:{}",
                        AppProperties.APPLICATION_NAME,
                        rawMessage);
                break;
            case I_ID_REQUEST:
                try {
                    int nodeId = ObjectFactory.getAppProperties().getNextNodeId();
                    rawMessage.setPayLoad(nodeId);
                    rawMessage.setSubType(MESSAGE_TYPE_INTERNAL.I_ID_RESPONSE.ordinal());
                    rawMessage.setTxMessage(true);
                    ObjectFactory.getRawMessageQueue().putMessage(rawMessage);
                    _logger.debug("New Id[{}] sent to node", nodeId);
                } catch (NodeIdException ex) {
                    _logger.error("Unable to generate new node Id,", ex);
                    SensorLogUtils.setSensorInternalData(MESSAGE_TYPE_INTERNAL.get(rawMessage.getSubType()),
                            rawMessage, ex.getMessage());
                }
                break;
            case I_INCLUSION_MODE:
                _logger.warn("Inclusion mode not supported by this controller! Message:{}",
                        rawMessage);
                break;
            case I_CONFIG:
                if (rawMessage.isTxMessage()) {
                    return;
                }
                rawMessage.setPayLoad(ObjectFactory.getAppProperties().getMetricType());
                rawMessage.setTxMessage(true);
                ObjectFactory.getRawMessageQueue().putMessage(rawMessage);
                _logger.debug("Configuration sent as follow[M/I]?:{}", rawMessage.getPayLoad());
                break;
            case I_LOG_MESSAGE:
                if (rawMessage.isTxMessage()) {
                    return;
                }
                _logger.debug("Node trace-log message[nodeId:{},sensorId:{},message:{}]",
                        rawMessage.getNodeId(),
                        rawMessage.getChildSensorId(),
                        rawMessage.getPayLoad());
                break;
            case I_SKETCH_NAME:
                if (rawMessage.isTxMessage()) {
                    return;
                }
                _logger.debug("Internal Message[type:{},name:{}]",
                        MESSAGE_TYPE_INTERNAL.get(rawMessage.getSubType()),
                        rawMessage.getPayLoad());
                node = DaoUtils.getNodeDao().get(rawMessage.getNodeId());
                if (node == null) {
                    DaoUtils.getNodeDao().create(new Node(rawMessage.getNodeId(), rawMessage.getPayLoad()));
                } else {
                    node.setName(rawMessage.getPayLoad());
                    DaoUtils.getNodeDao().update(node);
                }

                break;
            case I_SKETCH_VERSION:
                if (rawMessage.isTxMessage()) {
                    return;
                }
                _logger.debug("Internal Message[type:{},version:{}]",
                        MESSAGE_TYPE_INTERNAL.get(rawMessage.getSubType()),
                        rawMessage.getPayLoad());
                node = DaoUtils.getNodeDao().get(rawMessage.getNodeId());
                node.setVersion(rawMessage.getPayLoad());
                DaoUtils.getNodeDao().createOrUpdate(node);
                break;

            case I_REBOOT:
                break;
            case I_GATEWAY_READY:
                if (rawMessage.isTxMessage()) {
                    return;
                }
                _logger.debug("Gateway Ready[nodeId:{},message:{}]",
                        rawMessage.getNodeId(),
                        rawMessage.getPayLoad());
                break;

            default:
                _logger.warn(
                        "Internal Message[type:{},payload:{}], This type may not be supported (or) not implemented yet",
                        MESSAGE_TYPE_INTERNAL.get(rawMessage.getSubType()),
                        rawMessage.getPayLoad());
                break;
        }
    }

    //We are not logging firmware request/response in to db, as it is huge!
    private void streamSubMessageTypeSelector(RawMessage rawMessage) {
        switch (MESSAGE_TYPE_STREAM.get(rawMessage.getSubType())) {
            case ST_FIRMWARE_CONFIG_REQUEST:
                SensorLogUtils.setSensorStreamData(MESSAGE_TYPE_STREAM.get(rawMessage.getSubType()), rawMessage, null);
                this.processFirmwareConfigRequest(rawMessage);
                break;
            case ST_FIRMWARE_REQUEST:
                this.procressFirmwareRequest(rawMessage);
                break;
            case ST_FIRMWARE_CONFIG_RESPONSE:
                SensorLogUtils.setSensorStreamData(MESSAGE_TYPE_STREAM.get(rawMessage.getSubType()), rawMessage, null);
                break;
            case ST_FIRMWARE_RESPONSE:
                break;
            case ST_IMAGE:
                break;
            case ST_SOUND:
                break;
            default:
                _logger.debug("Stream Message[type:{},payload:{}], This type not be implemented yet",
                        MESSAGE_TYPE_STREAM.get(rawMessage.getSubType()),
                        rawMessage.getPayLoad());
                break;
        }
    }

    private void procressFirmwareRequest(RawMessage rawMessage) {
        FirmwareRequest firmwareRequest = new FirmwareRequest();
        try {
            firmwareRequest.setByteBuffer(
                    ByteBuffer.wrap(Hex.decodeHex(rawMessage.getPayLoad().toCharArray())).order(
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
            rawMessage.setTxMessage(true);
            rawMessage.setSubType(MESSAGE_TYPE_STREAM.ST_FIRMWARE_RESPONSE.ordinal());
            rawMessage.setPayLoad(Hex.encodeHexString(firmwareResponse.getByteBuffer().array()) + builder.toString());
            ObjectFactory.getRawMessageQueue().putMessage(rawMessage);
            _logger.debug("FirmwareRespone:[Type:{},Version:{},Block:{}]",
                    firmwareResponse.getType(), firmwareResponse.getVersion(), firmwareResponse.getBlock());

        } catch (DecoderException ex) {
            _logger.error("Exception, ", ex);
        }
    }

    private void processFirmwareConfigRequest(RawMessage rawMessage) {
        FirmwareConfigRequest firmwareConfigRequest = new FirmwareConfigRequest();
        try {
            firmwareConfigRequest.setByteBuffer(
                    ByteBuffer.wrap(Hex.decodeHex(rawMessage.getPayLoad().toCharArray())).order(
                            ByteOrder.LITTLE_ENDIAN), 0);
            boolean bootLoaderCommand = false;
            Firmware firmware = null;

            //Check firmware is configured for this particular node
            Node node = DaoUtils.getNodeDao().get(rawMessage.getNodeId());
            if (node != null && node.getEraseEEPROM() != null && node.getEraseEEPROM()) {
                bootLoaderCommand = true;
                _logger.debug("Erase EEPROM has been set...");
            } else if (node != null && node.getFirmware() != null) {
                firmware = DaoUtils.getFirmwareDao().get(node.getFirmware().getId());
                _logger.debug("Firmware selected based on node configuration...");
            } else if (firmwareConfigRequest.getType() == 65535 && firmwareConfigRequest.getVersion() == 65535) {
                Settings defailtFirmware = DaoUtils.getSettingsDao().get(Settings.DEFAULT_FIRMWARE);
                if (defailtFirmware != null && defailtFirmware.getValue() != null) {
                    firmware = DaoUtils.getFirmwareDao().get(Integer.valueOf(defailtFirmware.getValue()));
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
                if (node.getEraseEEPROM() != null && node.getEraseEEPROM()) {
                    firmwareConfigResponse.loadEraseEepromCommand();
                    node.setEraseEEPROM(false); //Remove erase EEPROM flag and update in to database
                    DaoUtils.getNodeDao().update(node);
                } else {
                    _logger.warn("Selected booloader command is not available, FirmwareConfigRequest:[{}]",
                            firmwareConfigRequest);
                    return;
                }
            } else if (firmware == null) {//Non bootloader command
                if (DaoUtils.getSettingsDao().get(Settings.ENABLE_NOT_AVAILABLE_TO_DEFAULT_FIRMWARE).getValue()
                        .equalsIgnoreCase("true")) {
                    _logger.debug("If requested firmware is not available, redirect to default firmware is set, Checking the default firmware");
                    Settings defailtFirmware = DaoUtils.getSettingsDao().get(Settings.DEFAULT_FIRMWARE);
                    if (defailtFirmware != null && defailtFirmware.getValue() != null) {
                        firmware = DaoUtils.getFirmwareDao().get(Integer.valueOf(defailtFirmware.getValue()));
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

            rawMessage.setTxMessage(true);
            rawMessage.setSubType(MESSAGE_TYPE_STREAM.ST_FIRMWARE_CONFIG_RESPONSE.ordinal());
            rawMessage.setPayLoad(Hex.encodeHexString(firmwareConfigResponse.getByteBuffer().array()).toUpperCase());
            ObjectFactory.getRawMessageQueue().putMessage(rawMessage);
            _logger.debug("FirmwareConfigRequest:[{}]", firmwareConfigRequest);
            _logger.debug("FirmwareConfigResponse:[{}]", firmwareConfigResponse);
        } catch (DecoderException ex) {
            _logger.error("Exception, ", ex);
        }
    }

    private void responseReqTypeData(RawMessage rawMessage) {
        Sensor sensor = this.getSensor(rawMessage);
        if (sensor.getLastValue() != null) {
            rawMessage.setTxMessage(true);
            rawMessage.setMessageType(MESSAGE_TYPE.C_SET.ordinal());
            rawMessage.setAck(0);
            rawMessage.setPayLoad(sensor.getLastValue());
            ObjectFactory.getRawMessageQueue().putMessage(rawMessage);
            _logger.debug("Request processed! Message Sent: {}", rawMessage);
        } else {
            _logger.warn("Data not available! but there is request from sensor[{}], "
                    + "Ignored this request!", rawMessage);
        }
    }

    private Sensor getSensor(RawMessage rawMessage) {
        Sensor sensor = DaoUtils.getSensorDao().get(rawMessage.getNodeId(), rawMessage.getChildSensorId());
        if (sensor == null) {
            updateNode(rawMessage);
            _logger.debug("This sensor[{} from Node:{}] not available in our DB, Adding...",
                    rawMessage.getChildSensorId(), rawMessage.getNodeId());
            sensor = new Sensor(rawMessage.getChildSensorId());
            sensor.setNode(new Node(rawMessage.getNodeId()));
            if (rawMessage.getSubType() == MESSAGE_TYPE_SET_REQ.V_UNIT_PREFIX.ordinal()) {
                sensor.setUnit(rawMessage.getPayLoad());
            } else {
                sensor.setMessageType(rawMessage.getSubType());
            }
            DaoUtils.getSensorDao().create(sensor);
            sensor = DaoUtils.getSensorDao().get(rawMessage.getNodeId(), rawMessage.getChildSensorId());
        }
        return sensor;
    }

    private void updateNode(RawMessage rawMessage) {
        Node node = DaoUtils.getNodeDao().get(rawMessage.getNodeId());
        if (node == null) {
            _logger.debug("This Node[{}] not available in our DB, Adding...", rawMessage.getNodeId());
            node = new Node(rawMessage.getNodeId());
            DaoUtils.getNodeDao().create(node);
        }
    }

    private void recordSetTypeData(RawMessage rawMessage) {
        PAYLOAD_TYPE type = MyMessages.getPayLoadType(MESSAGE_TYPE_SET_REQ.get(rawMessage.getSubType()));
        Sensor sensor = this.getSensor(rawMessage);

        _logger.debug("Sensor:{}[NodeId:{},SesnorId:{},SubType({}):{}], PayLoad Type: {}",
                sensor.getNameWithNode(),
                sensor.getNode().getId(), sensor.getSensorId(),
                rawMessage.getSubType(),
                MESSAGE_TYPE_SET_REQ.get(rawMessage.getSubType()),
                type.toString());

        if (rawMessage.getSubType() == MESSAGE_TYPE_SET_REQ.V_UNIT_PREFIX.ordinal()) {
            //Set Unit
            sensor.setUnit(rawMessage.getPayLoad());
            DaoUtils.getSensorDao().update(sensor);
            //Log message data
            SensorLogUtils.setSensorOtherData(LOG_TYPE.SENSOR,
                    MyMessages.MESSAGE_TYPE_SET_REQ.V_UNIT_PREFIX.toString(), rawMessage, null);
            return;
        }

        if (sensor.getMessageType() == null) {
            sensor.setMessageType(rawMessage.getSubType());
        } else if (sensor.getMessageType() != rawMessage.getSubType()) {
            sensor.setMessageType(rawMessage.getSubType());
        }

        sensor.setUpdateTime(System.currentTimeMillis());
        sensor.setLastValue(rawMessage.getPayLoad());
        switch (type) {
            case PL_DOUBLE:
                sensor.setStatus(String.valueOf(NumericUtils.round(rawMessage.getPayLoadDouble(),
                        NumericUtils.DOUBLE_ROUND)));
                DaoUtils.getSensorDao().update(sensor);
                DaoUtils.getMetricsDoubleTypeDeviceDao()
                        .create(new MetricsDoubleTypeDevice(
                                sensor,
                                AGGREGATION_TYPE.RAW.ordinal(),
                                System.currentTimeMillis(),
                                NumericUtils.getDouble(rawMessage.getPayLoad()),
                                1));
                break;
            case PL_BOOLEAN:
                sensor.setStatus(rawMessage.getPayLoadBoolean() ? "ON" : "OFF");
                DaoUtils.getSensorDao().update(sensor);
                DaoUtils.getMetricsOnOffTypeDeviceDao()
                        .create(new MetricsOnOffTypeDevice(
                                sensor,
                                System.currentTimeMillis(),
                                rawMessage.getPayLoadBoolean()));
                break;
            default:
                sensor.setStatus(rawMessage.getPayLoad());
                DaoUtils.getSensorDao().update(sensor);
                _logger.debug("This type not be implemented yet, PayloadType:{}, MessageType:{}, RawMessage:{}",
                        type, MyMessages.MESSAGE_TYPE_SET_REQ.get(rawMessage.getSubType()).toString(),
                        rawMessage.getPayLoad());
                break;
        }

        //Log message data
        SensorLogUtils.setSensorData(sensor, rawMessage.isTxMessage(), null);

        if (!rawMessage.isTxMessage()) {
            //Execute UidTag
            if (sensor.getType() != null
                    && sensor.getType() != null
                    && sensor.getType() == MESSAGE_TYPE_PRESENTATION.S_CUSTOM.ordinal()
                    && sensor.getMessageType() == MESSAGE_TYPE_SET_REQ.V_VAR5.ordinal()) {
                ExecuteUidTag executeUidTag = new ExecuteUidTag(sensor);
                new Thread(executeUidTag).start();
            }
        }

        //Forward Payload to another node, if any and only on receive from gateway
        List<ForwardPayload> forwardPayloads = DaoUtils.getForwardPayloadDao().getAll(sensor.getId());
        if (forwardPayloads.size() > 0 && forwardPayloads != null) {
            ExecuteForwardPayload executeForwardPayload = new ExecuteForwardPayload(forwardPayloads, sensor);
            new Thread(executeForwardPayload).run();
        }

        //Trigger Alarm for this sensor
        List<Alarm> alarms = DaoUtils.getAlarmDao().getAll(sensor.getId(), true);
        if (alarms.size() > 0 && alarms != null) {
            ExecuteAlarm executeAlarm = new ExecuteAlarm(alarms);
            new Thread(executeAlarm).run();
        }
    }
}
