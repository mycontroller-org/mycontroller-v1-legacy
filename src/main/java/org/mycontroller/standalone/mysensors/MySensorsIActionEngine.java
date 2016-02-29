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

import org.apache.commons.codec.binary.Hex;
import org.mycontroller.standalone.MYCMessages.MESSAGE_TYPE;
import org.mycontroller.standalone.MYCMessages.MESSAGE_TYPE_INTERNAL;
import org.mycontroller.standalone.MYCMessages.MESSAGE_TYPE_STREAM;
import org.mycontroller.standalone.ObjectFactory;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.PayloadOperation;
import org.mycontroller.standalone.db.PayloadOperationUtils;
import org.mycontroller.standalone.db.tables.AlarmDefinition;
import org.mycontroller.standalone.db.tables.ForwardPayload;
import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.db.tables.Timer;
import org.mycontroller.standalone.gateway.GatewayEthernet;
import org.mycontroller.standalone.gateway.GatewayException;
import org.mycontroller.standalone.interfaces.IActionEngine;
import org.mycontroller.standalone.model.ResourceModel;
import org.mycontroller.standalone.mysensors.structs.FirmwareConfigResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MySensorsIActionEngine implements IActionEngine {
    private static final Logger _logger = LoggerFactory.getLogger(MySensorsIActionEngine.class);

    @Override
    public void executeAlarm(AlarmDefinition alarmDefinition) {
        // TODO Auto-generated method stub

    }

    @Override
    public void executeTimer(ResourceModel resourceModel, Timer timer) {
        PayloadOperation operation = new PayloadOperation(timer.getPayload());
        this.executeSendPayload(resourceModel, operation);
        //TODO: add log timer executed
    }

    @Override
    public void executeSendPayload(ResourceModel resourceModel, PayloadOperation operation) {
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
    private void executeNodeOperationSendPayload(Node node, PayloadOperation operation) {
        MySensorsRawMessage mySensorsRawMessage = null;
        if (operation.getOperationType() != null) {
            switch (operation.getOperationType()) {
                case REBOOT:
                    mySensorsRawMessage = new MySensorsRawMessage(
                            node.getGateway().getId(),
                            node.getEuiInt(),
                            MySensorsUtils.SENSOR_ID_BROADCAST,
                            MESSAGE_TYPE.C_INTERNAL.ordinal(), //messageType
                            MySensorsUtils.NO_ACK, //ack
                            MESSAGE_TYPE_INTERNAL.I_REBOOT.ordinal(),//subType
                            MySensorsUtils.EMPTY_DATA,
                            true);// isTxMessage
                    break;

                default:
                    _logger.warn("Not supported opration node:[{}]", operation.getOperationType().getText());
                    return;
            }
        }

        if (mySensorsRawMessage != null) {
            ObjectFactory.getRawMessageQueue().putMessage(mySensorsRawMessage.getRawMessage());
        }

    }

    //Execute Sensor Variable related operations
    private void executeSensorVariableOperationSendPayload(SensorVariable sensorVariable, PayloadOperation operation) {
        String payload = null;
        MySensorsRawMessage mySensorsRawMessage = null;
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
                        payload = PayloadOperationUtils.getPayload(operation, sensorVariable.getValue());
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
                    _logger.warn("Not supported opration! Operation for sensor variable:[{}]", operation
                            .getOperationType()
                            .getText());
                    return;
            }

        } else {
            payload = operation.getPayload();
        }
        mySensorsRawMessage = new MySensorsRawMessage(
                sensorVariable.getSensor().getNode().getGateway().getId(),
                sensorVariable.getSensor().getNode().getEuiInt(),
                sensorVariable.getSensor().getSensorIdInt(),
                MESSAGE_TYPE.C_SET.ordinal(), //messageType
                MySensorsUtils.NO_ACK, //ack
                sensorVariable.getVariableType().ordinal(),//subType
                payload,
                true);// isTxMessage

        ObjectFactory.getRawMessageQueue().putMessage(mySensorsRawMessage.getRawMessage());
    }

    @Override
    public void sendAliveStatusRequest(Node node) {
        MySensorsRawMessage mySensorsRawMessage = new MySensorsRawMessage(
                node.getGateway().getId(),
                node.getEuiInt(),   //Node Id
                MySensorsUtils.SENSOR_ID_BROADCAST,    //Sensor Id
                MESSAGE_TYPE.C_INTERNAL.ordinal(), //Message Type
                MySensorsUtils.NO_ACK,  //Ack
                MESSAGE_TYPE_INTERNAL.I_HEARTBEAT.ordinal(), //Message Sub Type
                String.valueOf(System.currentTimeMillis()), //Payload
                true    //Is TX Message?
        );
        ObjectFactory.getRawMessageQueue().putMessage(mySensorsRawMessage.getRawMessage());
        _logger.debug("Hearbeat message sent for node:[{}], rawMessage:[{}]", node, mySensorsRawMessage);

    }

    @Override
    public boolean checkEthernetGatewayAliveState(GatewayEthernet gatewayEthernet) {
        MySensorsRawMessage mySensorsRawMessage = new MySensorsRawMessage(
                gatewayEthernet.getId(),
                MySensorsUtils.GATEWAY_ID,
                MySensorsUtils.SENSOR_ID_BROADCAST,
                MESSAGE_TYPE.C_INTERNAL.ordinal(),
                MySensorsUtils.NO_ACK,
                MESSAGE_TYPE_INTERNAL.I_VERSION.ordinal(),
                MySensorsUtils.EMPTY_DATA);
        try {
            if (ObjectFactory.getGateway(gatewayEthernet.getId()) != null) {
                ObjectFactory.getGateway(gatewayEthernet.getId()).write(mySensorsRawMessage.getRawMessage());
                return true;
            } else {
                _logger.warn("Gateway not available! Gateway[{}]", gatewayEthernet);
                return false;
            }
        } catch (GatewayException ex) {
            _logger.error("Exception while checking gateway connection status: {}", ex.getMessage());
            return false;
        }

    }

    @Override
    public void executeForwardPayload(ForwardPayload forwardPayload, String payload) {
        MySensorsRawMessage mySensorsRawMessage = new MySensorsRawMessage(
                forwardPayload.getDestination().getSensor().getNode().getGateway().getId(),
                forwardPayload.getDestination().getSensor().getNode().getEuiInt(),
                forwardPayload.getDestination().getSensor().getSensorIdInt(),
                MESSAGE_TYPE.C_SET.ordinal(), //messageType
                MySensorsUtils.NO_ACK, //ack
                forwardPayload.getDestination().getVariableType().ordinal(),//subType
                payload,
                true);// isTxMessage
        ObjectFactory.getRawMessageQueue().putMessage(mySensorsRawMessage.getRawMessage());
    }

    @Override
    public void rebootNode(Node node) {
        MySensorsRawMessage mySensorsRawMessage = new MySensorsRawMessage(
                node.getGateway().getId(),
                node.getEuiInt(),
                MySensorsUtils.SENSOR_ID_BROADCAST,
                MESSAGE_TYPE.C_INTERNAL.ordinal(),
                MySensorsUtils.NO_ACK,
                MESSAGE_TYPE_INTERNAL.I_REBOOT.ordinal(),
                MySensorsUtils.EMPTY_DATA,
                true);
        ObjectFactory.getRawMessageQueue().putMessage(mySensorsRawMessage.getRawMessage());
    }

    @Override
    public void uploadFirmware(Node node) {
        FirmwareConfigResponse firmwareConfigResponse = new FirmwareConfigResponse();
        firmwareConfigResponse.setByteBufferPosition(0);
        firmwareConfigResponse.setType(node.getFirmware().getType().getId());
        firmwareConfigResponse.setVersion(node.getFirmware().getVersion().getId());
        firmwareConfigResponse.setBlocks(node.getFirmware().getBlocks());
        firmwareConfigResponse.setCrc(node.getFirmware().getCrc());

        MySensorsRawMessage mySensorsRawMessage = new MySensorsRawMessage(
                node.getGateway().getId(),
                node.getEuiInt(),
                MySensorsUtils.SENSOR_ID_BROADCAST,
                MESSAGE_TYPE.C_STREAM.ordinal(),
                MySensorsUtils.NO_ACK,
                MESSAGE_TYPE_STREAM.ST_FIRMWARE_CONFIG_RESPONSE.ordinal(),
                Hex.encodeHexString(firmwareConfigResponse.getByteBuffer().array()).toUpperCase(),
                true);
        ObjectFactory.getRawMessageQueue().putMessage(mySensorsRawMessage.getRawMessage());
    }

    @Override
    public void discover(Integer gatewayId) {
        if (MySensorsUtils.isDiscoverRunning(gatewayId)) {
            throw new RuntimeException("Discover already running! nothing to do..");
        } else {
            new Thread(new MySensorsNodeDiscover(gatewayId)).start();
        }
    }

    @Override
    public void addNode(Node node) {
        MySensorsUtils.addUpdateNode(node, true);
    }

    @Override
    public void updateNode(Node node) {
        MySensorsUtils.addUpdateNode(node, false);
    }

    @Override
    public void eraseConfiguration(Node node) {
        node.setEraseConfig(true);
        DaoUtils.getNodeDao().update(node);
        rebootNode(node);
    }

    @Override
    public void addSensor(Sensor sensor) {
        MySensorsUtils.addUpdateSensor(sensor, true);
    }

    @Override
    public void updateSensor(Sensor sensor) {
        MySensorsUtils.addUpdateSensor(sensor, false);
    }

    @Override
    public void sendPayload(SensorVariable sensorVariable) {
        MySensorsRawMessage mySensorsRawMessage = new MySensorsRawMessage(
                sensorVariable.getSensor().getNode().getGateway().getId(),
                sensorVariable.getSensor().getNode().getEuiInt(),
                sensorVariable.getSensor().getSensorIdInt(),
                MESSAGE_TYPE.C_SET.ordinal(), // messageType
                MySensorsUtils.NO_ACK, // ack
                sensorVariable.getVariableType().ordinal(), // subType
                sensorVariable.getValue(),
                true);// isTxMessage
        ObjectFactory.getRawMessageQueue().putMessage(mySensorsRawMessage.getRawMessage());
    }

}
