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
package org.mycontroller.standalone.db.alarm;

import java.text.DecimalFormat;

import org.mycontroller.standalone.NumericUtils;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.PayloadSpecialOperation;
import org.mycontroller.standalone.db.PayloadSpecialOperationUtils.SEND_PAYLOAD_OPERATIONS;
import org.mycontroller.standalone.db.tables.Sensor;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class SendPayLoad {
    public DecimalFormat decimalFormat = new DecimalFormat("#.####");
    private Integer sensorRefId;
    private String payLoad;

    public SendPayLoad() {

    }

    public SendPayLoad(String sensorRefId, String payLoad) {
        this.sensorRefId = Integer.valueOf(sensorRefId);
        this.payLoad = payLoad;
    }

    public Integer getSensorRefId() {
        return sensorRefId;
    }

    public String getPayLoad() {
        return payLoad;
    }

    public void setSensorRefId(Integer sensorRefId) {
        this.sensorRefId = sensorRefId;
    }

    public void setPayLoad(String payLoad) {
        this.payLoad = payLoad;
    }

    public void setPayLoad(Double payLoad) {
        this.payLoad = decimalFormat.format(payLoad);
    }

    public String toString() {
        Sensor sensor = DaoUtils.getSensorDao().get(sensorRefId);
        StringBuffer buffer = new StringBuffer();
        buffer.append("Node: ").append(sensor.getNameWithNode());
        buffer.append("[Nid:").append(sensor.getNode().getId())
                .append(",Sid:").append(sensor.getSensorId()).append("], ");

        buffer.append("PayLoad:");
        PayloadSpecialOperation specialOperation = new PayloadSpecialOperation(this.payLoad);
        if (specialOperation.getOperationType() != null) {
            if (specialOperation.getOperationType() == SEND_PAYLOAD_OPERATIONS.REBOOT) {
                buffer.append(" ").append(specialOperation.getOperationType().value());
            } else {
                if (specialOperation.getValue() != null) {
                    buffer.append(" {sen.value} ")
                            .append(specialOperation.getOperationType().value())
                            .append(" ")
                            .append(NumericUtils.getDoubleAsString(specialOperation.getValue()));
                } else {
                    buffer.append(" ")
                            .append(specialOperation.getOperationType().value())
                            .append(" {sen.value}");
                }
            }
        } else {
            buffer.append(this.payLoad);
        }
        return buffer.toString();
    }
}
