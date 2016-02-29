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
package org.mycontroller.standalone.db.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.mycontroller.standalone.db.tables.NotificationAlarmDefinitionMap;

import com.j256.ormlite.support.ConnectionSource;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
public class NotificationAlarmDefinitionMapDaoImpl extends BaseAbstractDaoImpl<NotificationAlarmDefinitionMap, Object>
        implements NotificationAlarmDefinitionMapDao {

    public NotificationAlarmDefinitionMapDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, NotificationAlarmDefinitionMap.class);
    }

    @Override
    public NotificationAlarmDefinitionMap get(NotificationAlarmDefinitionMap tdao) {
        // not supported
        return null;
    }

    @Override
    public List<NotificationAlarmDefinitionMap> getAll(List<Object> ids) {
        // not supported
        return null;
    }

    @Override
    public List<NotificationAlarmDefinitionMap> getByAlarmDefinitionId(Integer alarmDefinitionId) {
        return super.getAll(NotificationAlarmDefinitionMap.KEY_ALARM_DEFINITION_ID, alarmDefinitionId);
    }

    @Override
    public List<NotificationAlarmDefinitionMap> getByNotificationId(Integer notificationId) {
        return super.getAll(NotificationAlarmDefinitionMap.KEY_NOTIFICATION_ID, notificationId);
    }

    @Override
    public void deleteByNotificationId(Integer notificationId) {
        super.delete(NotificationAlarmDefinitionMap.KEY_NOTIFICATION_ID, notificationId);
    }

    @Override
    public void deleteByAlarmDefinitionId(Integer alarmDefinitionId) {
        super.delete(NotificationAlarmDefinitionMap.KEY_ALARM_DEFINITION_ID, alarmDefinitionId);

    }

    @Override
    public List<Integer> getNotificationsIdsByAlarmDefinitionId(Integer alarmDefinitionId) {
        List<Integer> roleIds = new ArrayList<Integer>();
        try {
            if (alarmDefinitionId != null) {
                List<NotificationAlarmDefinitionMap> alarmDefinitionNotificationMaps = this.getDao().queryBuilder()
                        .where()
                        .eq(NotificationAlarmDefinitionMap.KEY_ALARM_DEFINITION_ID, alarmDefinitionId).query();
                for (NotificationAlarmDefinitionMap notificationAlarmDefinitionMap : alarmDefinitionNotificationMaps) {
                    roleIds.add(notificationAlarmDefinitionMap.getNotification().getId());
                }
            }
        } catch (SQLException ex) {
            _logger.error("Exception, ", ex);
        }
        return roleIds;
    }

}
