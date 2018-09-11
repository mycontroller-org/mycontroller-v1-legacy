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
package org.mycontroller.standalone.message;

import java.util.List;

import org.apache.commons.codec.binary.Hex;
import org.mycontroller.standalone.AppProperties.NETWORK_TYPE;
import org.mycontroller.standalone.McObjectManager;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.ResourceOperation;
import org.mycontroller.standalone.db.ResourceOperationUtils;
import org.mycontroller.standalone.db.tables.Firmware;
import org.mycontroller.standalone.db.tables.ForwardPayload;
import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_INTERNAL;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_STREAM;
import org.mycontroller.standalone.model.ResourceModel;
import org.mycontroller.standalone.provider.mycontroller.structs.McFirmwareConfig;
import org.mycontroller.standalone.provider.mysensors.structs.FirmwareConfigResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */

@Slf4j
public class McActionEngine implements IMcActionEngine {

    @Override
    public void executeSendPayload(ResourceModel resourceModel, ResourceOperation operation) {
        switch (resourceModel.getResourceType()) {
            case GATEWAY:
                _logger.warn("I do not know how to handle gateway commands! do not assign me on gateway tasks!");
                return;
            case NODE:
                executeNodeOperationSendPayload((Node) resourceModel.getResource(), operation);
                break;
            case SENSOR:
                //TODO: Implement Sensor related operations
                break;
            case SENSOR_VARIABLE:
                executeSensorVariableOperationSendPayload((SensorVariable) resourceModel.getResource(), operation);
                break;
            default:
                break;
        }
    }

    @Override
    public void executeRequestPayload(ResourceModel resourceModel) {
        switch (resourceModel.getResourceType()) {
            case SENSOR_VARIABLE:
                executeSensorVariableOperationRequestPayload((SensorVariable) resourceModel.getResource());
                break;
            default:
                _logger.warn("I do not know how to handle other than sensor variable! received:{}", resourceModel
                        .getResourceType().getText());
                break;
        }
    }

    //Private Methods

    private void send(IMessage message) {
        if (McObjectManager.getEngine(message.getGatewayId()) != null) {
            Node _node = DaoUtils.getNodeDao().get(message.getGatewayId(), message.getNodeEui());
            if (_node != null && _node.getSmartSleepEnabled()) {
                McObjectManager.getEngine(message.getGatewayId()).sendSleepNode(message);
            } else {
                McObjectManager.getEngine(message.getGatewayId()).send(message);
            }
        } else {
            _logger.warn("Engine not available to send {}", message);
        }
    }

    // Execute Node related operations
    private void executeNodeOperationSendPayload(Node node, ResourceOperation operation) {
        IMessage message = null;
        if (operation.getOperationType() != null) {
            switch (operation.getOperationType()) {
                case REBOOT:
                    message = MessageImpl.builder()
                            .gatewayId(node.getGatewayTable().getId())
                            .nodeEui(node.getEui())
                            .sensorId(IMessage.SENSOR_BROADCAST_ID)
                            .type(MESSAGE_TYPE.C_INTERNAL.getText())
                            .ack(IMessage.NO_ACK)
                            .subType(MESSAGE_TYPE_INTERNAL.I_REBOOT.getText())
                            .payload(IMessage.PAYLOAD_EMPTY)
                            .isTxMessage(true)
                            .build();
                    break;

                default:
                    _logger.warn("Not supported opration node:[{}]", operation.getOperationType().getText());
                    return;
            }
        }

        if (message != null) {
            send(message);
        }

    }

    //Execute Sensor Variable related operations
    private void executeSensorVariableOperationSendPayload(SensorVariable sensorVariable, ResourceOperation operation) {
        String payload = null;
        IMessage message = null;
        if (operation.getOperationType() != null) {
            switch (operation.getOperationType()) {
                case ADD:
                case DECREMENT:
                case DIVIDE:
                case INCREMENT:
                case TOGGLE:
                case MODULUS:
                case MULTIPLIE:
                case SUBTRACT:
                    if (sensorVariable != null && sensorVariable.getValue() != null) {
                        payload = ResourceOperationUtils.getPayload(operation, sensorVariable.getValue());
                    } else {
                        _logger.warn("Unable to run special oprtaion, there is no value available "
                                + "for this sensor variable, Operation:[{}], SensorVariable",
                                operation, sensorVariable);
                        //TODO: return exception
                        return;
                    }

                    break;
                case REBOOT:
                case STOP:
                case RELOAD:
                case START:
                default:
                    //TODO: not supported operations
                    _logger.warn("Not supported opration! sensor variable:[{}]", operation
                            .getOperationType()
                            .getText());
                    return;
            }

        } else {
            payload = operation.getPayload();
        }
        message = MessageImpl.builder()
                .gatewayId(sensorVariable.getSensor().getNode().getGatewayTable().getId())
                .nodeEui(sensorVariable.getSensor().getNode().getEui())
                .sensorId(sensorVariable.getSensor().getSensorId())
                .type(MESSAGE_TYPE.C_SET.getText())
                .ack(IMessage.NO_ACK)
                .subType(sensorVariable.getVariableType().getText())
                .payload(payload)
                .isTxMessage(true)
                .build();
        send(message);
    }

    //Execute Sensor Variable related operations
    private void executeSensorVariableOperationRequestPayload(SensorVariable sensorVariable) {
        IMessage message = MessageImpl.builder()
                .gatewayId(sensorVariable.getSensor().getNode().getGatewayTable().getId())
                .nodeEui(sensorVariable.getSensor().getNode().getEui())
                .sensorId(sensorVariable.getSensor().getSensorId())
                .type(MESSAGE_TYPE.C_REQ.getText())
                .ack(IMessage.NO_ACK)
                .subType(sensorVariable.getVariableType().getText())
                .payload(IMessage.PAYLOAD_EMPTY)
                .isTxMessage(true)
                .build();
        send(message);
    }

    @Override
    public String sendAliveStatusRequest(Node node) {
        IMessage message = MessageImpl.builder()
                .gatewayId(node.getGatewayTable().getId())
                .nodeEui(node.getEui())
                .sensorId(IMessage.SENSOR_BROADCAST_ID)
                .type(MESSAGE_TYPE.C_INTERNAL.getText())
                .ack(IMessage.NO_ACK)
                .subType(MESSAGE_TYPE_INTERNAL.I_HEARTBEAT.getText())
                .payload(IMessage.PAYLOAD_EMPTY)
                .isTxMessage(true)
                .build();
        send(message);
        return message.getEventTopic();
    }

    /*    @Override
        public boolean checkEthernetGatewayAliveState(GatewayConfigEthernet gatewayConfigEthernet) {
            IMessage message = MessageImpl.builder()
                    .gatewayId(gatewayConfigEthernet.getId())
                    .nodeEui(McMessageUtils.getGatewayNodeId(gatewayConfigEthernet.getNetworkType()))
                    .sensorId(IMessage.SENSOR_BROADCAST_ID)
                    .type(MESSAGE_TYPE.C_INTERNAL)
                    .ack(IMessage.NO_ACK)
                    .subType(MESSAGE_TYPE_INTERNAL.I_VERSION.getText())
                    .payload(IMessage.PAYLOAD_EMPTY)
                    .isTxMessage(true)
                    .build();
            try {
                if (McObjectManager.getGateway(gatewayConfigEthernet.getId()) != null) {
                    send(message);
                    return true;
                } else {
                    _logger.warn("GatewayTable not available! GatewayTable[{}]", gatewayConfigEthernet);
                    return false;
                }
            } catch (Exception ex) {
                _logger.error("Exception while checking gateway connection status: {}", ex.getMessage());
                return false;
            }

        }*/

    @Override
    public void executeForwardPayload(ForwardPayload forwardPayload, String payload) {
        IMessage message = MessageImpl.builder()
                .gatewayId(forwardPayload.getDestination().getSensor().getNode().getGatewayTable().getId())
                .nodeEui(forwardPayload.getDestination().getSensor().getNode().getEui())
                .sensorId(forwardPayload.getDestination().getSensor().getSensorId())
                .type(MESSAGE_TYPE.C_SET.getText())
                .ack(IMessage.NO_ACK)
                .subType(forwardPayload.getDestination().getVariableType().getText())
                .payload(payload)
                .isTxMessage(true)
                .build();
        send(message);
    }

    @Override
    public void rebootNode(Node node) {
        IMessage message = MessageImpl.builder()
                .gatewayId(node.getGatewayTable().getId())
                .nodeEui(node.getEui())
                .sensorId(IMessage.SENSOR_BROADCAST_ID)
                .type(MESSAGE_TYPE.C_INTERNAL.getText())
                .ack(IMessage.NO_ACK)
                .subType(MESSAGE_TYPE_INTERNAL.I_REBOOT.getText())
                .payload(IMessage.PAYLOAD_EMPTY)
                .isTxMessage(true)
                .build();
        send(message);
    }

    @Override
    public void uploadFirmware(Node node) {
        IMessage message = MessageImpl.builder()
                .gatewayId(node.getGatewayTable().getId())
                .nodeEui(node.getEui())
                .sensorId(IMessage.SENSOR_BROADCAST_ID)
                .type(MESSAGE_TYPE.C_STREAM.getText())
                .ack(IMessage.NO_ACK)
                .subType(MESSAGE_TYPE_STREAM.ST_FIRMWARE_CONFIG_RESPONSE.getText())
                .isTxMessage(true)
                .build();
        if (node.getGatewayTable().getNetworkType() == NETWORK_TYPE.MY_SENSORS) {
            FirmwareConfigResponse fwCfgResponse = new FirmwareConfigResponse();
            fwCfgResponse.setByteBufferPosition(0);
            fwCfgResponse.setType(node.getFirmware().getType().getId());
            fwCfgResponse.setVersion(node.getFirmware().getVersion().getId());
            fwCfgResponse.setBlocks((Integer) node.getFirmware().getProperties().get(Firmware.KEY_PROP_BLOCKS));
            fwCfgResponse.setCrc((Integer) node.getFirmware().getProperties().get(Firmware.KEY_PROP_CRC));
            message.setPayload(Hex.encodeHexString(fwCfgResponse.getByteBuffer().array()).toUpperCase());
        } else if (node.getGatewayTable().getNetworkType() == NETWORK_TYPE.MY_CONTROLLER) {
            McFirmwareConfig fwCfgResponse = new McFirmwareConfig();
            fwCfgResponse.setByteBufferPosition(0);
            fwCfgResponse.setType(node.getFirmware().getType().getId());
            fwCfgResponse.setVersion(node.getFirmware().getVersion().getId());
            fwCfgResponse.setBlocks((Integer) node.getFirmware().getProperties().get(Firmware.KEY_PROP_BLOCKS));
            fwCfgResponse.setMd5Sum((String) node.getFirmware().getProperties().get(Firmware.KEY_PROP_MD5_HEX));
            message.setPayload(Hex.encodeHexString(fwCfgResponse.getByteBuffer().array()).toUpperCase());
        }
        send(message);
    }

    @Override
    public void discover(Integer gatewayId) {
        _logger.debug("Sending Node discover");
        //Before start node discover, remove existing map for this gateway
        DaoUtils.getNodeDao().updateBulk(Node.KEY_PARENT_NODE_EUI, null, Node.KEY_GATEWAY_ID, gatewayId);
        //Send discover broadcast message
        IMessage message = MessageImpl.builder()
                .gatewayId(gatewayId)
                .nodeEui(IMessage.NODE_BROADCAST_ID)
                .sensorId(IMessage.SENSOR_BROADCAST_ID)
                .type(MESSAGE_TYPE.C_INTERNAL.getText())
                .subType(MESSAGE_TYPE_INTERNAL.I_DISCOVER.getText())
                .ack(IMessage.NO_ACK)
                .payload(IMessage.PAYLOAD_EMPTY)
                .isTxMessage(true)
                .build();
        send(message);
    }

    @Override
    public void addNode(Node node) {
        //MySensorsUtils.addUpdateNode(node, true);
    }

    @Override
    public void updateNode(Node node) {
        //MySensorsUtils.addUpdateNode(node, false);
    }

    @Override
    public String eraseConfiguration(Node node) {
        if (node.getGatewayTable().getNetworkType() == NETWORK_TYPE.MY_CONTROLLER) {
            IMessage message = MessageImpl.builder()
                    .gatewayId(node.getGatewayTable().getId())
                    .nodeEui(node.getEui())
                    .sensorId(IMessage.SENSOR_BROADCAST_ID)
                    .type(MESSAGE_TYPE.C_INTERNAL.getText())
                    .subType(MESSAGE_TYPE_INTERNAL.I_FACTORY_RESET.getText())
                    .ack(IMessage.NO_ACK)
                    .payload(IMessage.PAYLOAD_EMPTY)
                    .isTxMessage(true)
                    .build();
            send(message);
            return message.getEventTopic();
        } else if (node.getGatewayTable().getNetworkType() == NETWORK_TYPE.MY_SENSORS) {
            node.setEraseConfig(true);
            DaoUtils.getNodeDao().update(node);
            rebootNode(node);
        }
        return null;
    }

    @Override
    public void addSensor(Sensor sensor) {
        //MySensorsUtils.addUpdateSensor(sensor, true);
    }

    @Override
    public void updateSensor(Sensor sensor) {
        // MySensorsUtils.addUpdateSensor(sensor, false);
    }

    @Override
    public String sendPayload(SensorVariable sensorVariable) {
        if (sensorVariable.getReadOnly()) {
            _logger.warn("For 'readOnly' sensor variable, cannot send payload!, {}", sensorVariable);
            return null;
        }
        IMessage message = MessageImpl.builder()
                .gatewayId(sensorVariable.getSensor().getNode().getGatewayTable().getId())
                .nodeEui(sensorVariable.getSensor().getNode().getEui())
                .sensorId(sensorVariable.getSensor().getSensorId())
                .type(MESSAGE_TYPE.C_SET.getText())
                .subType(sensorVariable.getVariableType().getText())
                .ack(IMessage.NO_ACK)
                .payload(sensorVariable.getValue())
                .isTxMessage(true)
                .build();
        send(message);
        return message.getEventTopic();
    }

    @Override
    public void updateNodeInformations(Integer gatewayId, List<Integer> nodeIds) {
        if (gatewayId != null && McMessageUtils.isNodeInfoUpdateRunning(gatewayId)) {
            //Nothing to do already running
            return;
        }
        //Trigger node info update function
        new Thread(new McNodeInfoUpdate(gatewayId, nodeIds)).run();
    }

}
