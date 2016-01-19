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
package org.mycontroller.standalone.alarm;

import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.db.PayloadOperation;
import org.mycontroller.standalone.db.tables.AlarmDefinition;
import org.mycontroller.standalone.gateway.GatewayUtils;
import org.mycontroller.standalone.model.ResourceModel;
import org.mycontroller.standalone.scheduler.SchedulerUtils;
import org.mycontroller.standalone.timer.TimerSimple;
import org.mycontroller.standalone.NumericUtils;
import org.mycontroller.standalone.ObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class NotificationSendPayLoad implements INotification {
    private static final Logger _logger = LoggerFactory.getLogger(NotificationSendPayLoad.class);

    private AlarmDefinition alarmDefinition;
    private RESOURCE_TYPE resourceType;
    private Integer resourceId;
    private String payload;
    private long delayTime;

    public NotificationSendPayLoad(AlarmDefinition alarmDefinition) {
        this.alarmDefinition = alarmDefinition;
        this.resourceType = RESOURCE_TYPE.fromString(alarmDefinition.getVariable1());
        this.resourceId = Integer.valueOf(alarmDefinition.getVariable2());
        this.payload = alarmDefinition.getVariable3();
        this.delayTime = alarmDefinition.getVariable4() == null || alarmDefinition.getVariable4().length() == 0
                ? 0 : Long.valueOf(alarmDefinition.getVariable4());
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payLoad) {
        this.payload = payLoad;
    }

    public void setPayLoad(Double payLoad) {
        this.payload = NumericUtils.getDoubleAsString(payLoad);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        ResourceModel resourceModel = new ResourceModel(this.resourceType, this.resourceId);
        builder.append("Target=[").append(resourceModel.getResourceLessDetails()).append("]");
        builder.append(", Payload=").append(this.payload);
        builder.append(", Delay=").append(this.delayTime).append(" Seconds");
        return builder.toString();
    }

    public RESOURCE_TYPE getResourceType() {
        return resourceType;
    }

    public Integer getResourceId() {
        return resourceId;
    }

    public long getDelayTime() {
        return delayTime;
    }

    @Override
    public AlarmDefinition getAlarmDefinition() {
        return this.alarmDefinition;
    }

    @Override
    public void execute(String actualValue) {
        if (this.getDelayTime() == 0) { //Send payload immediately
            ResourceModel resourceModel = new ResourceModel(this.getResourceType(),
                    this.getResourceId());
            PayloadOperation payloadOperation = new PayloadOperation(this.getPayload());
            //You have to handle gateway operations
            if (resourceModel.getResourceType() == RESOURCE_TYPE.GATEWAY) {
                GatewayUtils.executeGatewayOperation(resourceModel, payloadOperation);
            } else {
                ObjectFactory.getIActionEngine(resourceModel.getNetworkType()).executeSendPayload(resourceModel,
                        payloadOperation);
            }
        } else {  //Create timer to send payload
            TimerSimple timerSimple = new TimerSimple(
                    AlarmUtils.getAlarmTimerJobName(alarmDefinition),//Job Name
                    this.getResourceType(),
                    this.getResourceId(),
                    this.getPayload(),
                    1,//Repeat count
                    this.getDelayTime());
            SchedulerUtils.loadTimerJob(timerSimple.getTimer());//Adding a job to send payload with specified delay
        }
        _logger.debug("Executed 'Send payload' notification, AlarmDefinition:[{}]", this.alarmDefinition);
    }
}
