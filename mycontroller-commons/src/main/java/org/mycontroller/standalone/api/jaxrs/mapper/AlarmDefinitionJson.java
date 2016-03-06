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
package org.mycontroller.standalone.api.jaxrs.mapper;

import java.util.ArrayList;
import java.util.List;

import org.mycontroller.standalone.alarm.AlarmUtils;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.AlarmDefinition;
import org.mycontroller.standalone.db.tables.Notification;
import org.mycontroller.standalone.db.tables.NotificationAlarmDefinitionMap;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@NoArgsConstructor
@ToString(includeFieldNames = true)
@Data
public class AlarmDefinitionJson {

    private AlarmDefinition alarmDefinition;
    private List<Integer> notifications;

    public AlarmDefinitionJson(AlarmDefinition alarmDefinition) {
        this.alarmDefinition = alarmDefinition;
    }

    @JsonIgnore
    public AlarmDefinitionJson mapResources() {
        if (alarmDefinition.getId() != null) {
            List<NotificationAlarmDefinitionMap> notificationAlarmDefinitionMaps = DaoUtils
                    .getNotificationAlarmDefinitionMapDao().getByAlarmDefinitionId(alarmDefinition.getId());
            notifications = new ArrayList<Integer>();
            for (NotificationAlarmDefinitionMap notificationAlarmDefinitionMap : notificationAlarmDefinitionMaps) {
                notifications.add(notificationAlarmDefinitionMap.getNotification().getId());
            }
        }
        return this;
    }

    @JsonIgnore
    public void createOrUpdate() {
        if (alarmDefinition.getId() != null) {
            //Update alarmDefinition
            AlarmUtils.updateAlarmDefinition(alarmDefinition);
            //clear all old mapping
            removeMapping(alarmDefinition.getId());
        } else {
            AlarmUtils.addAlarmDefinition(alarmDefinition);
            //update created alarm definition id
            alarmDefinition = DaoUtils.getAlarmDefinitionDao().getByName(alarmDefinition.getName());
        }

        //Update notifications
        if (notifications != null && !notifications.isEmpty()) {
            Notification notification = Notification.builder().build();
            NotificationAlarmDefinitionMap notificationAlarmDefinitionMap = NotificationAlarmDefinitionMap.builder()
                    .alarmDefinition(alarmDefinition).build();
            for (Integer notificationId : notifications) {
                notification.setId(notificationId);
                notificationAlarmDefinitionMap.setNotification(notification);
                DaoUtils.getNotificationAlarmDefinitionMapDao().create(notificationAlarmDefinitionMap);
            }
        }
    }

    @JsonIgnore
    public void delete(List<Integer> alarmDefinitionIds) {
        for (Integer id : alarmDefinitionIds) {
            removeMapping(id);
        }
        AlarmUtils.deleteAlarmDefinitionIds(alarmDefinitionIds);
    }

    @JsonIgnore
    private void removeMapping(Integer id) {
        //clear all old mapping
        DaoUtils.getNotificationAlarmDefinitionMapDao().deleteByAlarmDefinitionId(id);
    }
}
