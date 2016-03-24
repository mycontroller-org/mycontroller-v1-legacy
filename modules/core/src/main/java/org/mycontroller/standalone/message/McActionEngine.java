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

import org.apache.commons.codec.binary.Hex;
import org.mycontroller.standalone.ObjectFactory;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.ResourceOperation;
import org.mycontroller.standalone.db.ResourceOperationUtils;
import org.mycontroller.standalone.db.tables.ForwardPayload;
import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.gateway.model.GatewayEthernet;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_INTERNAL;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_STREAM;
import org.mycontroller.standalone.model.ResourceModel;
import org.mycontroller.standalone.provider.mysensors.structs.FirmwareConfigResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */

public class McActionEngine implements IMcActionEngine {
    private static final Logger _logger = LoggerFactory.getLogger(McActionEngine.class);

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

    //Private Methods
    // Execute Node related operations
    private void executeNodeOperationSendPayload(Node node, ResourceOperation operation) {
        McMessage mcMessage = null;
        if (operation.getOperationType() != null) {
            switch (operation.getOperationType()) {
                case REBOOT:
                    mcMessage = McMessage.builder()
                            .gatewayId(node.getGatewayTable().getId())
                            .nodeEui(node.getEui())
                            .SensorId(McMessage.SENSOR_BROADCAST_ID)
                            .type(MESSAGE_TYPE.C_INTERNAL)
                            .acknowledge(false)
                            .subType(MESSAGE_TYPE_INTERNAL.I_REBOOT.getText())
                            .payload(McMessage.PAYLOAD_EMPTY)
                            .isTxMessage(true)
                            .build();
                    break;

                default:
                    _logger.warn("Not supported opration node:[{}]", operation.getOperationType().getText());
                    return;
            }
        }

        if (mcMessage != null) {
            McMessageUtils.sendToProviderBridge(mcMessage);
        }

    }

    //Execute Sensor Variable related operations
    private void executeSensorVariableOperationSendPayload(SensorVariable sensorVariable, ResourceOperation operation) {
        String payload = null;
        McMessage mcMessage = null;
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
                                + "for this sensor variable, OperationTable:[{}], SensorVariable",
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
                    _logger.warn("Not supported opration! OperationTable for sensor variable:[{}]", operation
                            .getOperationType()
                            .getText());
                    return;
            }

        } else {
            payload = operation.getPayload();
        }
        mcMessage = McMessage.builder()
                .gatewayId(sensorVariable.getSensor().getNode().getGatewayTable().getId())
                .nodeEui(sensorVariable.getSensor().getNode().getEui())
                .SensorId(sensorVariable.getSensor().getSensorId())
                .type(MESSAGE_TYPE.C_SET)
                .acknowledge(false)
                .subType(sensorVariable.getVariableType().getText())
                .payload(payload)
                .isTxMessage(true)
                .build();
        McMessageUtils.sendToProviderBridge(mcMessage);
    }

    @Override
    public void sendAliveStatusRequest(Node node) {
        McMessage mcMessage = McMessage.builder()
                .gatewayId(node.getGatewayTable().getId())
                .nodeEui(node.getEui())
                .SensorId(McMessage.SENSOR_BROADCAST_ID)
                .type(MESSAGE_TYPE.C_INTERNAL)
                .acknowledge(false)
                .subType(MESSAGE_TYPE_INTERNAL.I_HEARTBEAT.getText())
                .payload(McMessage.PAYLOAD_EMPTY)
                .isTxMessage(true)
                .build();

        McMessageUtils.sendToProviderBridge(mcMessage);
    }

    @Override
    public boolean checkEthernetGatewayAliveState(GatewayEthernet gatewayEthernet) {
        McMessage mcMessage = McMessage.builder()
                .gatewayId(gatewayEthernet.getId())
                .nodeEui(McMessage.GATEWAY_NODE_ID)
                .SensorId(McMessage.SENSOR_BROADCAST_ID)
                .type(MESSAGE_TYPE.C_INTERNAL)
                .acknowledge(false)
                .subType(MESSAGE_TYPE_INTERNAL.I_VERSION.getText())
                .payload(McMessage.PAYLOAD_EMPTY)
                .isTxMessage(true)
                .build();
        try {
            if (ObjectFactory.getGateway(gatewayEthernet.getId()) != null) {
                McMessageUtils.sendToProviderBridge(mcMessage);
                return true;
            } else {
                _logger.warn("GatewayTable not available! GatewayTable[{}]", gatewayEthernet);
                return false;
            }
        } catch (Exception ex) {
            _logger.error("Exception while checking gateway connection status: {}", ex.getMessage());
            return false;
        }

    }

    @Override
    public void executeForwardPayload(ForwardPayload forwardPayload, String payload) {
        McMessage mcMessage = McMessage.builder()
                .gatewayId(forwardPayload.getDestination().getSensor().getNode().getGatewayTable().getId())
                .nodeEui(forwardPayload.getDestination().getSensor().getNode().getEui())
                .SensorId(forwardPayload.getDestination().getSensor().getSensorId())
                .type(MESSAGE_TYPE.C_SET)
                .acknowledge(false)
                .subType(forwardPayload.getDestination().getVariableType().getText())
                .payload(payload)
                .isTxMessage(true)
                .build();
        McMessageUtils.sendToProviderBridge(mcMessage);
    }

    @Override
    public void rebootNode(Node node) {
        McMessage mcMessage = McMessage.builder()
                .gatewayId(node.getGatewayTable().getId())
                .nodeEui(node.getEui())
                .SensorId(McMessage.SENSOR_BROADCAST_ID)
                .type(MESSAGE_TYPE.C_INTERNAL)
                .acknowledge(false)
                .subType(MESSAGE_TYPE_INTERNAL.I_REBOOT.getText())
                .payload(McMessage.PAYLOAD_EMPTY)
                .isTxMessage(true)
                .build();
        McMessageUtils.sendToProviderBridge(mcMessage);
    }

    @Override
    public void uploadFirmware(Node node) {
        FirmwareConfigResponse firmwareConfigResponse = new FirmwareConfigResponse();
        firmwareConfigResponse.setByteBufferPosition(0);
        firmwareConfigResponse.setType(node.getFirmware().getType().getId());
        firmwareConfigResponse.setVersion(node.getFirmware().getVersion().getId());
        firmwareConfigResponse.setBlocks(node.getFirmware().getBlocks());
        firmwareConfigResponse.setCrc(node.getFirmware().getCrc());

        McMessage mcMessage = McMessage.builder()
                .gatewayId(node.getGatewayTable().getId())
                .nodeEui(node.getEui())
                .SensorId(McMessage.SENSOR_BROADCAST_ID)
                .type(MESSAGE_TYPE.C_STREAM)
                .acknowledge(false)
                .subType(MESSAGE_TYPE_STREAM.ST_FIRMWARE_CONFIG_RESPONSE.getText())
                .payload(Hex.encodeHexString(firmwareConfigResponse.getByteBuffer().array()).toUpperCase())
                .isTxMessage(true)
                .build();
        McMessageUtils.sendToProviderBridge(mcMessage);
    }

    @Override
    public void discover(Integer gatewayId) {
        if (McMessageUtils.isDiscoverRunning(gatewayId)) {
            throw new RuntimeException("Discover already running! nothing to do..");
        } else {
            new Thread(new McNodeDiscover(gatewayId)).start();
        }
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
    public void eraseConfiguration(Node node) {
        node.setEraseConfig(true);
        DaoUtils.getNodeDao().update(node);
        rebootNode(node);
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
    public void sendPayload(SensorVariable sensorVariable) {
        McMessage mcMessage = McMessage.builder()
                .gatewayId(sensorVariable.getSensor().getNode().getGatewayTable().getId())
                .nodeEui(sensorVariable.getSensor().getNode().getEui())
                .SensorId(sensorVariable.getSensor().getSensorId())
                .type(MESSAGE_TYPE.C_SET)
                .subType(sensorVariable.getVariableType().getText())
                .acknowledge(false)
                .payload(sensorVariable.getValue())
                .isTxMessage(true)
                .build();
        McMessageUtils.sendToProviderBridge(mcMessage);
    }

}
