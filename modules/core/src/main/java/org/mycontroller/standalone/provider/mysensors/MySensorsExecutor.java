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
package org.mycontroller.standalone.provider.mysensors;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.AppProperties.UNIT_CONFIG;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.Firmware;
import org.mycontroller.standalone.db.tables.FirmwareData;
import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.firmware.FirmwareUtils;
import org.mycontroller.standalone.gateway.IGateway;
import org.mycontroller.standalone.message.IMessage;
import org.mycontroller.standalone.message.McMessageUtils;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_INTERNAL;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_STREAM;
import org.mycontroller.standalone.offheap.MessageQueueImpl;
import org.mycontroller.standalone.offheap.MessageQueueSleepImpl;
import org.mycontroller.standalone.provider.ExecuterAbstract;
import org.mycontroller.standalone.provider.mysensors.structs.FirmwareConfigRequest;
import org.mycontroller.standalone.provider.mysensors.structs.FirmwareConfigResponse;
import org.mycontroller.standalone.provider.mysensors.structs.FirmwareRequest;
import org.mycontroller.standalone.provider.mysensors.structs.FirmwareResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.2.0
 */
@Slf4j
public class MySensorsExecutor extends ExecuterAbstract {
    private IGateway _gateway;

    public MySensorsExecutor(MessageQueueImpl qq, MessageQueueSleepImpl _queueSleep, IGateway _gateway) {
        super(qq, _queueSleep);
        this._gateway = _gateway;
    }

    @Override
    public void executeFirmwareRequest() {
        FirmwareRequest firmwareRequest = new FirmwareRequest();
        try {
            firmwareRequest
                    .setByteBuffer(
                            ByteBuffer.wrap(Hex.decodeHex(_message.getPayload().toCharArray())).order(
                                    ByteOrder.LITTLE_ENDIAN),
                            0);
            _logger.debug("Firmware Request:[Type:{},Version:{},Block:{}]", firmwareRequest.getType(),
                    firmwareRequest.getVersion(), firmwareRequest.getBlock());
            FirmwareData firmwareData = FirmwareUtils.getFirmwareDataFromOfflineMap(firmwareRequest.getType(),
                    firmwareRequest.getVersion());
            if (firmwareData == null) {
                _logger.warn("Requested firmware is not available in MyController server. "
                        + "FirmwareRequest[typeId:{}, versionId:{}, block:{}]",
                        firmwareRequest.getType(), firmwareRequest.getVersion(), firmwareRequest.getBlock());
                return;
            }

            FirmwareResponse firmwareResponse = new FirmwareResponse();
            firmwareResponse.setByteBufferPosition(0);
            firmwareResponse.setBlock(firmwareRequest.getBlock());
            firmwareResponse.setVersion(firmwareRequest.getVersion());
            firmwareResponse.setType(firmwareRequest.getType());
            StringBuilder builder = new StringBuilder();
            Integer blockSize = (Integer) firmwareData.getFirmware().getProperties().get(Firmware.KEY_PROP_BLOCK_SIZE);
            Integer blocks = (Integer) firmwareData.getFirmware().getProperties().get(Firmware.KEY_PROP_BLOCKS);
            int fromIndex = firmwareRequest.getBlock() * blockSize;
            for (int index = fromIndex; index < fromIndex + blockSize; index++) {
                builder.append(String.format("%02X", firmwareData.getData().get(index)));
            }

            _message.setTxMessage(true);
            _message.setSubType(MESSAGE_TYPE_STREAM.ST_FIRMWARE_RESPONSE.getText());
            _message.setPayload(Hex.encodeHexString(firmwareResponse.getByteBuffer().array())
                    + builder.toString());
            _message.setTimestamp(System.currentTimeMillis());
            addInQueue(_message);
            _logger.debug("FirmwareRespone:[Type:{},Version:{},Block:{}]",
                    firmwareResponse.getType(), firmwareResponse.getVersion(), firmwareResponse.getBlock());

            // in Dualoptiboot fetching blocks in reverse order
            int blocksDone = blocks - firmwareResponse.getBlock();
            // firmware starts
            if (blocksDone == 0 || blocksDone == 1) {
                firmwareUpdateStart(blocks);
            } else if (blocksDone % 25 == 0) {
                updateFirmwareStatus(blocksDone);
            } else if (blocksDone == blocks) {
                updateFirmwareStatus(blocksDone);
                firmwareUpdateFinished();
            }
        } catch (DecoderException ex) {
            _logger.error("Exception, ", ex);
        }

    }

    @Override
    public void executeFirmwareConfigRequest() {

        FirmwareConfigRequest firmwareConfigRequest = new FirmwareConfigRequest();
        try {
            firmwareConfigRequest.setByteBuffer(
                    ByteBuffer.wrap(Hex.decodeHex(_message.getPayload().toCharArray())).order(
                            ByteOrder.LITTLE_ENDIAN), 0);
            boolean bootLoaderCommand = false;
            Firmware firmware = null;

            //Check firmware is configured for this particular node
            Node node = DaoUtils.getNodeDao()
                    .get(_message.getGatewayId(), _message.getNodeEui());
            if (node != null && node.getEraseConfig() != null && node.getEraseConfig()) {
                bootLoaderCommand = true;
                _logger.debug("Erase EEPROM has been set...");
            } else if (node != null && node.getFirmware() != null) {
                firmware = DaoUtils.getFirmwareDao().getById(node.getFirmware().getId());
                _logger.debug("Firmware selected based on node configuration...");
            } else if (firmwareConfigRequest.getType() == 65535 && firmwareConfigRequest.getVersion() == 65535) {
                if (AppProperties.getInstance().getMySensorsSettings().getDefaultFirmware() != null) {
                    firmware = DaoUtils.getFirmwareDao().getById(
                            AppProperties.getInstance().getMySensorsSettings().getDefaultFirmware());
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
                if (AppProperties.getInstance().getMySensorsSettings().getEnbaledDefaultOnNoFirmware()) {
                    _logger.debug("If requested firmware is not available, "
                            + "redirect to default firmware is set, Checking the default firmware");
                    if (AppProperties.getInstance().getMySensorsSettings().getDefaultFirmware() != null) {
                        firmware = DaoUtils.getFirmwareDao().getById(
                                AppProperties.getInstance().getMySensorsSettings().getDefaultFirmware());
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
                firmwareConfigResponse.setBlocks((Integer) firmware.getProperties().get(Firmware.KEY_PROP_BLOCKS));
                firmwareConfigResponse.setCrc((Integer) firmware.getProperties().get(Firmware.KEY_PROP_CRC));
            }

            _message.setTxMessage(true);
            _message.setSubType(MESSAGE_TYPE_STREAM.ST_FIRMWARE_CONFIG_RESPONSE.getText());
            _message
                    .setPayload(Hex.encodeHexString(firmwareConfigResponse.getByteBuffer().array()).toUpperCase());
            _message.setTimestamp(System.currentTimeMillis());
            addInQueue(_message);
            _logger.debug("FirmwareConfigRequest:[{}]", firmwareConfigRequest);
            _logger.debug("FirmwareConfigResponse:[{}]", firmwareConfigResponse);
        } catch (DecoderException ex) {
            _logger.error("Exception, ", ex);
        }
    }

    @Override
    public void nodeEuiRequest() {
        int nodeId = MySensors.NODE_ID_MIN;
        boolean isIdAvailable = false;
        for (; nodeId <= MySensors.NODE_ID_MAX; nodeId++) {
            if (DaoUtils.getNodeDao().get(_gateway.config().getId(), String.valueOf(nodeId)) == null) {
                isIdAvailable = true;
                break;
            }
        }
        if (isIdAvailable) {
            _message.setAck(IMessage.NO_ACK);
            _message.setSubType(MESSAGE_TYPE_INTERNAL.I_ID_RESPONSE.getText());
            _message.setPayload(String.valueOf(nodeId));
            _message.setTxMessage(true);
            addInQueue(_message);
        } else {
            // TODO: notify request failed
            _logger.error("There is no free node id! All 254 id's are already reserved!");
        }
    }

    @Override
    public String metricType() {
        return UNIT_CONFIG.fromString(McMessageUtils.getMetricType()) == UNIT_CONFIG.METRIC ? "M" : "I";
    }

}
