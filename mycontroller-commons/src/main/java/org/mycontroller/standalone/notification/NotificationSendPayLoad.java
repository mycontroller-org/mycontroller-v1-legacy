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
package org.mycontroller.standalone.notification;

import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.alarm.AlarmUtils;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.PayloadOperation;
import org.mycontroller.standalone.db.tables.AlarmDefinition;
import org.mycontroller.standalone.db.tables.Notification;
import org.mycontroller.standalone.gateway.GatewayUtils;
import org.mycontroller.standalone.group.ResourcesGroupUtils;
import org.mycontroller.standalone.model.ResourceModel;
import org.mycontroller.standalone.scheduler.SchedulerUtils;
import org.mycontroller.standalone.timer.TimerSimple;
import org.mycontroller.standalone.timer.TimerUtils;
import org.mycontroller.standalone.MycUtils;
import org.mycontroller.standalone.ObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.ToString;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Builder
@Data
@AllArgsConstructor
@ToString(includeFieldNames = true)
public class NotificationSendPayLoad implements INotificationEngine {
    private static final Logger _logger = LoggerFactory.getLogger(NotificationSendPayLoad.class);

    private Notification notification;
    private AlarmDefinition alarmDefinition;
    private RESOURCE_TYPE resourceType;
    private Integer resourceId;
    private String payload;
    private long delayTime;
    private String actualValue;

    public NotificationSendPayLoad update() {
        this.resourceType = RESOURCE_TYPE.fromString(notification.getVariable1());
        this.resourceId = Integer.valueOf(notification.getVariable2());
        this.payload = notification.getVariable3();
        this.delayTime = notification.getVariable4() == null || notification.getVariable4().length() == 0
                ? 0 : Long.valueOf(notification.getVariable4());
        return this;
    }

    public void setPayLoad(Double payLoad) {
        this.payload = MycUtils.getDoubleAsString(payLoad);
    }

    public String getString() {
        StringBuilder builder = new StringBuilder();
        ResourceModel resourceModel = new ResourceModel(this.resourceType, this.resourceId);
        builder.append("Target=[").append(resourceModel.getResourceLessDetails()).append("]");
        builder.append(", Payload=").append(this.payload);
        builder.append(", Delay=").append(this.delayTime / 1000).append(" Seconds");
        return builder.toString();
    }

    @Override
    public void execute() {
        if (this.getDelayTime() == 0) { //Send payload immediately
            ResourceModel resourceModel = new ResourceModel(this.getResourceType(),
                    this.getResourceId());
            PayloadOperation payloadOperation = new PayloadOperation(this.getPayload());
            //we have to handle gateway,alarm,resource groups and timer operations
            switch (resourceModel.getResourceType()) {
                case GATEWAY:
                    GatewayUtils.executeGatewayOperation(resourceModel, payloadOperation);
                    break;
                case ALARM_DEFINITION:
                    AlarmUtils.executeAlarmDefinitionOperation(resourceModel, payloadOperation);
                    break;
                case TIMER:
                    TimerUtils.executeTimerOperation(resourceModel, payloadOperation);
                case RESOURCES_GROUP:
                    ResourcesGroupUtils.executeResourceGroupsOperation(resourceModel, payloadOperation);
                    break;
                default:
                    ObjectFactory.getIActionEngine(
                            resourceModel.getNetworkType()).executeSendPayload(resourceModel, payloadOperation);
                    break;
            }
        } else {  //Create timer to send payload
            TimerSimple timerSimple = new TimerSimple(
                    NotificationUtils.getSendPayloadTimerJobName(alarmDefinition, notification),//Job Name
                    this.getResourceType(),
                    this.getResourceId(),
                    this.getPayload(),
                    this.getDelayTime(),
                    1//Repeat count
            );
            SchedulerUtils.loadTimerJob(timerSimple.getTimer());//Adding a job to send payload with specified delay
        }

        //Update last execution
        notification.setLastExecution(System.currentTimeMillis());
        DaoUtils.getNotificationDao().update(notification);
        _logger.debug("Executed 'Send payload' notification, AlarmDefinition:[{}]", this.alarmDefinition);
    }
}
