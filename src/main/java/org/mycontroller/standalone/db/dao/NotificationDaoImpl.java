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
import java.util.List;

import org.mycontroller.standalone.api.jaxrs.mapper.Query;
import org.mycontroller.standalone.api.jaxrs.mapper.QueryResponse;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.j256.ormlite.support.ConnectionSource;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
public class NotificationDaoImpl extends BaseAbstractDaoImpl<Notification, Integer> implements NotificationDao {
    private static final Logger _logger = LoggerFactory.getLogger(NotificationDaoImpl.class);

    public NotificationDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, Notification.class);
    }

    @Override
    public List<Notification> getAll(List<Integer> ids) {
        return super.getAll(Notification.KEY_ID, ids);
    }

    @Override
    public Notification get(Notification notification) {
        return super.getById(notification.getId());
    }

    @Override
    public Notification getByNotificationName(String notificationName) {
        List<Notification> notifications = super.getAll(Notification.KEY_NAME, notificationName);
        if (notifications != null && !notifications.isEmpty()) {
            return notifications.get(0);
        }
        return null;
    }

    @Override
    public QueryResponse getAll(Query query) {
        try {
            return super.getQueryResponse(query, Notification.KEY_ID);
        } catch (SQLException ex) {
            _logger.error("unable to run query:[{}]", query, ex);
            return null;
        }
    }

    @Override
    public List<Notification> getByAlarmDefinitionId(Integer alarmDefinitionId) {
        List<Integer> ids = DaoUtils.getNotificationAlarmDefinitionMapDao().getNotificationsIdsByAlarmDefinitionId(
                alarmDefinitionId);
        return super.getAll(Notification.KEY_ID, ids);
    }

    @Override
    public List<Notification> getByAlarmDefinitionIdEnabled(Integer alarmDefinitionId) {
        List<Integer> ids = DaoUtils.getNotificationAlarmDefinitionMapDao().getNotificationsIdsByAlarmDefinitionId(
                alarmDefinitionId);
        try {
            return this.getDao().queryBuilder().where().in(Notification.KEY_ID, ids).and()
                    .eq(Notification.KEY_ENABLED, true).query();
        } catch (SQLException ex) {
            
        }
        return null;
    }

}
