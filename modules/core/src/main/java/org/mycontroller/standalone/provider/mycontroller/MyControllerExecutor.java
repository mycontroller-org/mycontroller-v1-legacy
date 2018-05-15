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
package org.mycontroller.standalone.provider.mycontroller;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.Firmware;
import org.mycontroller.standalone.db.tables.FirmwareData;
import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.firmware.FirmwareUtils;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_STREAM;
import org.mycontroller.standalone.offheap.MessageQueueImpl;
import org.mycontroller.standalone.offheap.MessageQueueSleepImpl;
import org.mycontroller.standalone.provider.ExecuterAbstract;
import org.mycontroller.standalone.provider.mycontroller.structs.McFirmwareConfig;
import org.mycontroller.standalone.provider.mycontroller.structs.McFirmwareRequest;
import org.mycontroller.standalone.provider.mycontroller.structs.McFirmwareResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.2.0
 */
@Slf4j
public class MyControllerExecutor extends ExecuterAbstract {

    public MyControllerExecutor(MessageQueueImpl _queue, MessageQueueSleepImpl _queueSleep) {
        super(_queue, _queueSleep);
    }

    @Override
    public void executeFirmwareRequest() {
        McFirmwareRequest firmwareRequest = new McFirmwareRequest();
        try {
            firmwareRequest.setByteBuffer(
                    ByteBuffer.wrap(Hex.decodeHex(_message.getPayload().toCharArray())).order(
                            ByteOrder.LITTLE_ENDIAN), 0);
            _logger.debug("Firmware Request:[Type:{},Version:{},Block:{}]", firmwareRequest.getType(),
                    firmwareRequest.getVersion(), firmwareRequest.getBlock());
            FirmwareData firmwareData = FirmwareUtils.getFirmwareDataFromOfflineMap(firmwareRequest.getType(),
                    firmwareRequest.getVersion());
            if (firmwareData == null) {
                _logger.debug("selected firmware type/version not available");
                return;
            }

            McFirmwareResponse firmwareResponse = new McFirmwareResponse();
            firmwareResponse.setByteBufferPosition(0);
            firmwareResponse.setBlock(firmwareRequest.getBlock());
            firmwareResponse.setVersion(firmwareRequest.getVersion());
            firmwareResponse.setType(firmwareRequest.getType());

            StringBuilder builder = new StringBuilder();
            Integer blockSize = (Integer) firmwareData.getFirmware().getProperties().get(Firmware.KEY_PROP_BLOCK_SIZE);
            Integer blocks = (Integer) firmwareData.getFirmware().getProperties().get(Firmware.KEY_PROP_BLOCKS);
            int fromIndex = firmwareRequest.getBlock() * blockSize;
            if (firmwareRequest.getBlock() >= blocks || firmwareRequest.getBlock() < 0) {
                _logger.warn("Requested firmware out of range. Accepted range[0~{}] FirmwareRequest({}), {}",
                        blocks - 1, firmwareRequest, _message);
                return;
            }
            int toIndex = Math.min(fromIndex + blockSize, firmwareData.getData().size());
            firmwareResponse.setSize(toIndex - fromIndex);
            firmwareResponse.setData(firmwareData.getData().subList(fromIndex, toIndex));

            _message.setTxMessage(true);
            _message.setSubType(MESSAGE_TYPE_STREAM.ST_FIRMWARE_RESPONSE.getText());
            _message.setPayload(Hex.encodeHexString(firmwareResponse.getByteBuffer().array())
                    + builder.toString());
            _message.setTimestamp(System.currentTimeMillis());
            addInQueue(_message);
            _logger.debug("FirmwareRespone:[Type:{},Version:{},Block:{}]",
                    firmwareResponse.getType(), firmwareResponse.getVersion(), firmwareResponse.getBlock());

            // update firmware status
            int responseBlock = firmwareResponse.getBlock() + 1; // adding +1 as it starts from 0
            // firmware starts
            if (responseBlock == 1) {
                firmwareUpdateStart(blocks);
            } else if (responseBlock % 100 == 0) {
                updateFirmwareStatus(responseBlock);
            } else if (responseBlock == blocks) {
                updateFirmwareStatus(responseBlock);
                firmwareUpdateFinished();
            }
        } catch (DecoderException ex) {
            _logger.error("Exception, ", ex);
        }
    }

    @Override
    public void executeFirmwareConfigRequest() {
        McFirmwareConfig firmwareConfigRequest = new McFirmwareConfig();
        try {
            firmwareConfigRequest.setByteBuffer(
                    ByteBuffer.wrap(Hex.decodeHex(_message.getPayload().toCharArray())).order(
                            ByteOrder.LITTLE_ENDIAN), 0);
            Firmware firmware = null;

            //Check firmware is configured for this particular node
            Node node = DaoUtils.getNodeDao().get(_message.getGatewayId(), _message.getNodeEui());
            if (node != null && node.getFirmware() != null) {
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

            McFirmwareConfig firmwareConfigResponse = new McFirmwareConfig();
            firmwareConfigResponse.setByteBufferPosition(0);

            if (firmware == null) {//Non bootloader command
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
                firmwareConfigResponse.setMd5Sum((String) firmware.getProperties().get(Firmware.KEY_PROP_MD5_HEX));
            }

            _message.setTxMessage(true);
            _message.setSubType(MESSAGE_TYPE_STREAM.ST_FIRMWARE_CONFIG_RESPONSE.getText());
            _message.setPayload(Hex.encodeHexString(firmwareConfigResponse.getByteBuffer().array()).toUpperCase());
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
        // not supported or not implemented
    }

}
