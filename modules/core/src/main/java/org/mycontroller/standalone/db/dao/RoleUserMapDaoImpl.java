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
import java.util.ArrayList;
import java.util.List;

import org.mycontroller.standalone.db.tables.RoleUserMap;
import org.mycontroller.standalone.exceptions.McDatabaseException;

import com.j256.ormlite.support.ConnectionSource;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
@Slf4j
public class RoleUserMapDaoImpl extends BaseAbstractDaoImpl<RoleUserMap, Object> implements RoleUserMapDao {

    public RoleUserMapDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, RoleUserMap.class);
    }

    @Override
    public RoleUserMap get(RoleUserMap tdao) {
        // not supported
        return null;
    }

    @Override
    public List<RoleUserMap> getAll(List<Object> ids) {
        // not supported
        return null;
    }

    @Override
    public List<RoleUserMap> getByUserId(Integer userId) {
        return super.getAll(RoleUserMap.KEY_USER_ID, userId);
    }

    @Override
    public List<RoleUserMap> getByRoleId(Integer roleId) {
        return super.getAll(RoleUserMap.KEY_ROLE_ID, roleId);
    }

    @Override
    public void deleteByRoleId(Integer roleId) {
        super.delete(RoleUserMap.KEY_ROLE_ID, roleId);
    }

    @Override
    public void deleteByUserId(Integer userId) {
        super.delete(RoleUserMap.KEY_USER_ID, userId);

    }

    @Override
    public List<Integer> getRolesByUserId(Integer userId) {
        List<Integer> roleIds = new ArrayList<Integer>();
        try {
            if (userId != null) {
                List<RoleUserMap> roleUserMaps = this.getDao().queryBuilder().where()
                        .eq(RoleUserMap.KEY_USER_ID, userId).query();
                for (RoleUserMap roleUserMap : roleUserMaps) {
                    roleIds.add(roleUserMap.getRole().getId());
                }
            }
        } catch (SQLException ex) {
            _logger.error("Exception, ", ex);
            throw new McDatabaseException(ex);
        }
        return roleIds;
    }

}
