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
package org.mycontroller.standalone.message;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
public class RawMessage {
    private Integer gatewayId;
    private String data;
    private String subData;
    private boolean isTxMessage = false;

    public RawMessage(Integer gatewayId, String data, String subData) {
        this(gatewayId, data, subData, false);
    }

    public RawMessage(Integer gatewayId, String data, String subData, boolean isTxMessage) {
        this.gatewayId = gatewayId;
        this.data = data;
        this.subData = subData;
        this.isTxMessage = isTxMessage;
    }

    public RawMessage(Integer gatewayId, String data) {
        this(gatewayId, data, null);
    }

    public Integer getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(Integer gatewayId) {
        this.gatewayId = gatewayId;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getSubData() {
        return subData;
    }

    public void setSubData(String subData) {
        this.subData = subData;
    }

    //Return bytes
    public byte[] getGWBytes() {
        return this.data.getBytes();
    }

    public boolean isTxMessage() {
        return isTxMessage;
    }

    public void setTxMessage(boolean isTxMessage) {
        this.isTxMessage = isTxMessage;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("GatewayId:").append(this.gatewayId);
        builder.append(", Data:").append(this.data);
        builder.append(", SubData:").append(this.subData);
        builder.append(", IsTxMessage:").append(this.isTxMessage);
        return builder.toString();
    }

}
