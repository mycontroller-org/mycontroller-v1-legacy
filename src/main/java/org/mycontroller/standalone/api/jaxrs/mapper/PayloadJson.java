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
package org.mycontroller.standalone.api.jaxrs.mapper;

import org.mycontroller.standalone.MYCMessages.MESSAGE_TYPE_SET_REQ;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
public class PayloadJson {
    private Integer nodeId;
    private Integer sensorId;
    private Integer sensorRefId;
    private MESSAGE_TYPE_SET_REQ variableType;
    private String payload;
    private String buttonType;

    public enum BUTTON_TYPE {
        ON_OFF,
        ARMED,
        TRIPPED,
        LOCK_UNLOCK,
        INCREASE,
        DECREASE,
        UP,
        DOWN,
        STOP,
        RGB,
        RGBW;

        public static BUTTON_TYPE get(int id) {
            for (BUTTON_TYPE type : values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }
    }

    public PayloadJson() {

    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Node Id:").append(this.nodeId);
        builder.append(", Sensor Id:").append(this.sensorId);
        builder.append(", SensorRefId:").append(this.sensorRefId);
        builder.append(", Variable Type:").append(this.variableType);
        builder.append(", Button Type:").append(this.buttonType);
        builder.append(", Payload:").append(this.payload);
        return builder.toString();
    }

    public Integer getNodeId() {
        return nodeId;
    }

    public Integer getSensorId() {
        return sensorId;
    }

    public MESSAGE_TYPE_SET_REQ getVariableType() {
        return variableType;
    }

    public String getPayload() {
        return payload;
    }

    public void setNodeId(Integer nodeId) {
        this.nodeId = nodeId;
    }

    public void setSensorId(Integer sensorId) {
        this.sensorId = sensorId;
    }

    public void setVariableType(MESSAGE_TYPE_SET_REQ variableType) {
        this.variableType = variableType;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public Integer getSensorRefId() {
        return sensorRefId;
    }

    public void setSensorRefId(Integer sensorRefId) {
        this.sensorRefId = sensorRefId;
    }

    public String getButtonType() {
        return buttonType;
    }

    public void setButtonType(String buttonType) {
        this.buttonType = buttonType;
    }
}
