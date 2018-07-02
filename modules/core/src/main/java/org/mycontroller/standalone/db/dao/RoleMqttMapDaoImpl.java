/*
 * Copyright 2015-2018 Jeeva Kandasamy (jkandasa@gmail.com)
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
package org.mycontroller.standalone.db.dao;

import java.sql.SQLException;
import java.util.List;

import org.mycontroller.standalone.db.tables.RoleMqttMap;

import com.j256.ormlite.support.ConnectionSource;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
public class RoleMqttMapDaoImpl extends BaseAbstractDaoImpl<RoleMqttMap, Object> implements RoleMqttMapDao {

    public RoleMqttMapDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, RoleMqttMap.class);
    }

    @Override
    public RoleMqttMap get(RoleMqttMap tdao) {
        // not supported
        return null;
    }

    @Override
    public List<RoleMqttMap> getAll(List<Object> ids) {
        // not supported
        return null;
    }

    @Override
    public RoleMqttMap getByRoleId(Integer roleId) {
        List<RoleMqttMap> list = super.getAll(RoleMqttMap.KEY_ROLE_ID, roleId);
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    @Override
    public void deleteByRoleIds(List<Integer> roleIds) {
        super.delete(RoleMqttMap.KEY_ROLE_ID, roleIds);

    }

    @Override
    public void deleteByRoleId(Integer roleId) {
        super.delete(RoleMqttMap.KEY_ROLE_ID, roleId);
    }

}
