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
package org.mycontroller.standalone.api.jaxrs;

import java.util.List;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

import org.mycontroller.standalone.auth.AuthUtils;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.AlarmDefinition;
import org.mycontroller.standalone.db.tables.Notification;
import org.mycontroller.standalone.db.tables.Timer;
import org.mycontroller.standalone.db.tables.User;
import org.mycontroller.standalone.notification.NotificationSendPayLoad;
import org.mycontroller.standalone.notification.NotificationUtils.NOTIFICATION_TYPE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
public class AccessEngine {
    protected static final Logger _logger = LoggerFactory.getLogger(AccessEngine.class);

    protected static final String NO_ACCESS_MESSAGE = "You do not have access for this resource!";

    @Context
    SecurityContext securityContext;

    protected User getUser() {
        return AuthUtils.getUser(securityContext);
    }

    protected boolean isSuperAdmin() {
        return AuthUtils.isSuperAdmin(securityContext);
    }

    //For sensors
    protected void updateSensorIds(List<Integer> ids) {
        if (!AuthUtils.isSuperAdmin(securityContext)) {
            for (Integer id : ids) {
                if (!AuthUtils.getUser(securityContext).getAllowedResources().getSensorIds().contains(id)) {
                    ids.remove(id);
                }
            }
        }
    }

    protected void hasAccessSensor(Integer id) {
        if (!AuthUtils.isSuperAdmin(securityContext)) {
            if (!AuthUtils.getUser(securityContext).getAllowedResources().getSensorIds().contains(id)) {
                throw new ForbiddenException(NO_ACCESS_MESSAGE);
            }
        }
    }

    protected void updateSensorVariableIds(List<Integer> ids) {
        if (!AuthUtils.isSuperAdmin(securityContext)) {
            for (Integer id : ids) {
                if (!AuthUtils.getUser(securityContext).getAllowedResources().getSensorVariableIds().contains(id)) {
                    ids.remove(id);
                }
            }
        }
    }

    protected void hasAccessSensorVariable(Integer id) {
        if (!AuthUtils.isSuperAdmin(securityContext)) {
            if (!AuthUtils.getUser(securityContext).getAllowedResources().getSensorVariableIds().contains(id)) {
                throw new ForbiddenException(NO_ACCESS_MESSAGE);
            }
        }
    }

    //For nodes
    protected void updateNodeIds(List<Integer> ids) {
        if (!AuthUtils.isSuperAdmin(securityContext)) {
            for (Integer id : ids) {
                if (!AuthUtils.getUser(securityContext).getAllowedResources().getNodeIds().contains(id)) {
                    ids.remove(id);
                }
            }
        }
    }

    protected void hasAccessNode(Integer nodeId) {
        if (!AuthUtils.isSuperAdmin(securityContext)) {
            if (!AuthUtils.getUser(securityContext).getAllowedResources().getNodeIds().contains(nodeId)) {
                throw new ForbiddenException(NO_ACCESS_MESSAGE);
            }
        }
    }

    //For gateways
    protected void updateGatewayIds(List<Integer> ids) {
        if (!AuthUtils.isSuperAdmin(securityContext)) {
            for (Integer id : ids) {
                if (!AuthUtils.getUser(securityContext).getAllowedResources().getGatewayIds().contains(id)) {
                    ids.remove(id);
                }
            }
        }
    }

    protected void hasAccessGateway(Integer gatewayId) {
        if (!AuthUtils.isSuperAdmin(securityContext)) {
            if (!AuthUtils.getUser(securityContext).getAllowedResources().getGatewayIds().contains(gatewayId)) {
                throw new ForbiddenException(NO_ACCESS_MESSAGE);
            }
        }
    }

    //For alarm definition
    protected void updateAlarmDefinitionIds(List<Integer> ids) {
        if (!AuthUtils.isSuperAdmin(securityContext)) {
            List<AlarmDefinition> alarmDefinitions = DaoUtils.getAlarmDefinitionDao().getAll(ids);
            for (AlarmDefinition alarmDefinition : alarmDefinitions) {
                if (!AuthUtils.hasAccess(securityContext, alarmDefinition.getResourceType(),
                        alarmDefinition.getResourceId())) {
                    ids.remove(alarmDefinition.getId());
                }
            }
        }
    }

    protected void hasAccessAlarmDefinition(AlarmDefinition alarmDefinition) {
        _logger.debug("Alarm definition:{}", alarmDefinition);
        if (!AuthUtils.isSuperAdmin(securityContext)) {
            if (!AuthUtils.hasAccess(securityContext, alarmDefinition.getResourceType(),
                    alarmDefinition.getResourceId())) {
                throw new ForbiddenException(NO_ACCESS_MESSAGE);
            }
            List<Notification> notifications = DaoUtils.getNotificationDao().getByAlarmDefinitionId(
                    alarmDefinition.getId());
            for (Notification notification : notifications) {
                if (notification.getType() == NOTIFICATION_TYPE.SEND_PAYLOAD) {
                    NotificationSendPayLoad sendPayLoad = NotificationSendPayLoad.builder()
                            .alarmDefinition(alarmDefinition).notification(notification).build().update();
                    if (!AuthUtils.hasAccess(securityContext, sendPayLoad.getResourceType(),
                            sendPayLoad.getResourceId())) {
                        throw new ForbiddenException(NO_ACCESS_MESSAGE);
                    }
                }
            }
        }
    }

    protected void hasAccessAlarmDefinition(Integer id) {
        if (!AuthUtils.isSuperAdmin(securityContext)) {
            AlarmDefinition alarmDefinition = DaoUtils.getAlarmDefinitionDao().getById(id);
            if (!AuthUtils.hasAccess(securityContext, alarmDefinition.getResourceType(),
                    alarmDefinition.getResourceId())) {
                throw new ForbiddenException(NO_ACCESS_MESSAGE);
            }
        }
    }

    //For timers
    protected void updateTimerIds(List<Integer> ids) {
        if (!AuthUtils.isSuperAdmin(securityContext)) {
            List<Timer> timers = DaoUtils.getTimerDao().getAll(ids);
            for (Timer timer : timers) {
                if (!AuthUtils.hasAccess(securityContext, timer.getResourceType(),
                        timer.getResourceId())) {
                    ids.remove(timer.getId());
                }
            }
        }
    }

    protected void hasAccessTimer(Timer timer) {
        if (!AuthUtils.isSuperAdmin(securityContext)) {
            if (!AuthUtils.hasAccess(securityContext, timer.getResourceType(), timer.getResourceId())) {
                throw new ForbiddenException(NO_ACCESS_MESSAGE);
            }
        }
    }

    protected void hasAccessTimer(Integer id) {
        if (!AuthUtils.isSuperAdmin(securityContext)) {
            Timer timer = DaoUtils.getTimerDao().getById(id);
            if (!AuthUtils.hasAccess(securityContext, timer.getResourceType(), timer.getResourceId())) {
                throw new ForbiddenException(NO_ACCESS_MESSAGE);
            }
        }
    }
}
