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
import java.util.HashMap;
import java.util.List;

import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.api.jaxrs.model.Query;
import org.mycontroller.standalone.api.jaxrs.model.QueryResponse;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.Role;
import org.mycontroller.standalone.db.tables.RoleGatewayMap;
import org.mycontroller.standalone.db.tables.RoleMqttMap;
import org.mycontroller.standalone.db.tables.RoleNodeMap;
import org.mycontroller.standalone.db.tables.RoleSensorMap;
import org.mycontroller.standalone.db.tables.RoleUserMap;
import org.mycontroller.standalone.db.tables.SensorVariable;

import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
@Slf4j
public class RoleDaoImpl extends BaseAbstractDaoImpl<Role, Integer> implements RoleDao {

    public RoleDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, Role.class);
    }

    @Override
    public List<Role> getAll(List<Integer> ids) {
        return super.getAll(Role.KEY_ID, ids);
    }

    @Override
    public Role get(Role role) {
        return super.getById(role.getId());
    }

    @Override
    public Role getByRoleName(String roleName) {
        List<Role> roles = super.getAll(Role.KEY_NAME, roleName);
        if (roles != null && !roles.isEmpty()) {
            return roles.get(0);
        }
        return null;
    }

    @Override
    public QueryResponse getAll(Query query) {
        try {
            query.setIdColumn(Role.KEY_ID);
            return super.getQueryResponse(query);
        } catch (SQLException ex) {
            _logger.error("unable to run query:[{}]", query, ex);
            return null;
        }
    }

    @Override
    public List<String> getPermissionsByUserId(Integer userId) {
        List<String> permissions = new ArrayList<String>();
        try {
            QueryBuilder<RoleUserMap, Object> roleUserQuery = DaoUtils.getRoleUserMapDao().getDao().queryBuilder();
            roleUserQuery.selectColumns(RoleUserMap.KEY_ROLE_ID).where().eq(RoleUserMap.KEY_USER_ID, userId);
            List<Role> roles = this.getDao().queryBuilder().selectColumns(Role.KEY_PERMISSION).distinct()
                    .join(roleUserQuery)
                    .query();
            for (Role role : roles) {
                permissions.add(role.getPermission().getText());
            }
        } catch (SQLException ex) {
            _logger.error("Exception, ", ex);
        }
        return permissions;
    }

    @Override
    public List<Integer> getGatewayIds(Integer userId) {
        List<Integer> ids = new ArrayList<Integer>();
        try {
            //Get role ids for this user
            List<Integer> roleIds = DaoUtils.getRoleUserMapDao().getRolesByUserId(userId);
            _logger.debug("RoleIds:{}", roleIds);
            //if role id is not null do not execute
            if (roleIds != null && roleIds.size() != 0) {
                QueryBuilder<RoleGatewayMap, Object> roleGatewayQuery = DaoUtils.getRoleGatewayMapDao().getDao()
                        .queryBuilder();
                List<RoleGatewayMap> roleGatewayMaps = roleGatewayQuery.where()
                        .in(RoleGatewayMap.KEY_ROLE_ID, roleIds).query();
                for (RoleGatewayMap roleGatewayMap : roleGatewayMaps) {
                    ids.add(roleGatewayMap.getGatewayTable().getId());
                }
            }

        } catch (SQLException ex) {
            _logger.error("Exception, ", ex);
        }
        return ids;
    }

    @Override
    public List<Integer> getNodeIds(Integer userId) {
        List<Integer> ids = new ArrayList<Integer>();
        try {
            //Get role ids for this user
            List<Integer> roleIds = DaoUtils.getRoleUserMapDao().getRolesByUserId(userId);
            //if role id is not null do not execute
            if (roleIds != null && roleIds.size() != 0) {
                QueryBuilder<RoleNodeMap, Object> roleNodeQuery = DaoUtils.getRoleNodeMapDao().getDao()
                        .queryBuilder();
                List<RoleNodeMap> roleNodeMaps = roleNodeQuery.where()
                        .in(RoleGatewayMap.KEY_ROLE_ID, roleIds).query();
                for (RoleNodeMap roleNodeMap : roleNodeMaps) {
                    ids.add(roleNodeMap.getNode().getId());
                }
            }
            if (AppProperties.getInstance().getControllerSettings().getGrantAccessToChildResources()) {
                ids.addAll(DaoUtils.getNodeDao().getNodeIdsByGatewayIds(getGatewayIds(userId)));
            }
        } catch (SQLException ex) {
            _logger.error("Exception, ", ex);
        }
        return ids;
    }

    @Override
    public List<Integer> getSensorIds(Integer userId) {
        List<Integer> ids = new ArrayList<Integer>();
        try {
            //Get role ids for this user
            List<Integer> roleIds = DaoUtils.getRoleUserMapDao().getRolesByUserId(userId);
            //if role id is not null do not execute
            if (roleIds != null && roleIds.size() != 0) {
                QueryBuilder<RoleSensorMap, Object> roleSensorQuery = DaoUtils.getRoleSensorMapDao().getDao()
                        .queryBuilder();
                List<RoleSensorMap> roleSensorMaps = roleSensorQuery.where()
                        .in(RoleSensorMap.KEY_ROLE_ID, roleIds).query();
                for (RoleSensorMap roleSensorMap : roleSensorMaps) {
                    ids.add(roleSensorMap.getSensor().getId());
                }
            }
            if (AppProperties.getInstance().getControllerSettings().getGrantAccessToChildResources()) {
                ids.addAll(DaoUtils.getSensorDao().getSensorIdsByNodeIds(getNodeIds(userId)));
            }
        } catch (SQLException ex) {
            _logger.error("Exception, ", ex);
        }
        return ids;
    }

    @Override
    public HashMap<String, List<String>> getMqttTopics(Integer userId) {
        HashMap<String, List<String>> topics = new HashMap<String, List<String>>();
        List<String> publishTopics = new ArrayList<String>();
        List<String> subscribeTopics = new ArrayList<String>();
        topics.put("publish", publishTopics);
        topics.put("subscribe", subscribeTopics);
        try {
            //Get role ids for this user
            List<Integer> roleIds = DaoUtils.getRoleUserMapDao().getRolesByUserId(userId);
            //if role id is not null do not execute
            if (roleIds != null && roleIds.size() != 0) {
                QueryBuilder<RoleMqttMap, Object> roleMqttQuery = DaoUtils.getRoleMqttMapDao().getDao().queryBuilder();
                List<RoleMqttMap> roleMqttMaps = roleMqttQuery.where()
                        .in(RoleSensorMap.KEY_ROLE_ID, roleIds).query();
                for (RoleMqttMap roleMqttMap : roleMqttMaps) {
                    publishTopics.addAll(roleMqttMap.getPublish());
                    subscribeTopics.addAll(roleMqttMap.getSubscribe());
                }
            }
        } catch (SQLException ex) {
            _logger.error("Exception, ", ex);
        }
        return topics;
    }

    @Override
    public List<Integer> getSensorVariableIds(Integer userId) {
        List<Integer> ids = new ArrayList<Integer>();
        //if role id is not null do not execute
        List<SensorVariable> sensorVariables = DaoUtils.getSensorVariableDao().getAllBySensorIds(getSensorIds(userId));
        for (SensorVariable sensorVariable : sensorVariables) {
            ids.add(sensorVariable.getId());
        }
        return ids;
    }

}
