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
package org.mycontroller.standalone.api.jaxrs.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.mycontroller.standalone.auth.AuthUtils.PERMISSION_TYPE;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.GatewayTable;
import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.db.tables.Role;
import org.mycontroller.standalone.db.tables.RoleGatewayMap;
import org.mycontroller.standalone.db.tables.RoleMqttMap;
import org.mycontroller.standalone.db.tables.RoleNodeMap;
import org.mycontroller.standalone.db.tables.RoleSensorMap;
import org.mycontroller.standalone.db.tables.RoleUserMap;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.db.tables.User;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
@NoArgsConstructor
@ToString(includeFieldNames = true)
@Data
public class RoleJson {

    private Integer id;
    private String name;
    private String description;
    private String permission;
    private List<Integer> users;
    private List<Integer> gateways;
    private List<Integer> nodes;
    private List<Integer> sensors;
    private String topicsPublish;
    private String topicsSubscribe;

    @JsonIgnore
    public void mapResources(Role role) {
        this.id = role.getId();
        this.name = role.getName();
        this.description = role.getDescription();
        this.permission = role.getPermission().getText();
        //TODO: map resources
        List<RoleUserMap> roleUserMaps = DaoUtils.getRoleUserMapDao().getByRoleId(this.id);
        users = new ArrayList<Integer>();
        for (RoleUserMap userMap : roleUserMaps) {
            users.add(userMap.getUser().getId());
        }
        if (role.getPermission() == PERMISSION_TYPE.USER) {
            //GatewayTable map
            List<RoleGatewayMap> roleGatewayMaps = DaoUtils.getRoleGatewayMapDao().getByRoleId(this.id);
            gateways = new ArrayList<Integer>();
            for (RoleGatewayMap gatewayMap : roleGatewayMaps) {
                gateways.add(gatewayMap.getGatewayTable().getId());
            }
            //Node map
            List<RoleNodeMap> roleNodeMaps = DaoUtils.getRoleNodeMapDao().getByRoleId(this.id);
            nodes = new ArrayList<Integer>();
            for (RoleNodeMap nodeMap : roleNodeMaps) {
                nodes.add(nodeMap.getNode().getId());
            }
            //Sensor map
            List<RoleSensorMap> roleSensorMaps = DaoUtils.getRoleSensorMapDao().getByRoleId(this.id);
            sensors = new ArrayList<Integer>();
            for (RoleSensorMap sensorMap : roleSensorMaps) {
                sensors.add(sensorMap.getSensor().getId());
            }
        } else if (role.getPermission() == PERMISSION_TYPE.MQTT_USER) {
            //update mqtt
            RoleMqttMap roleMqttMap = DaoUtils.getRoleMqttMapDao().getByRoleId(this.id);
            if (roleMqttMap != null) {
                if (roleMqttMap.getPublish() != null) {
                    topicsPublish = String.join(", ", roleMqttMap.getPublish());
                }
                if (roleMqttMap.getSubscribe() != null) {
                    topicsSubscribe = String.join(", ", roleMqttMap.getSubscribe());
                }
            }
        }
    }

    @JsonIgnore
    public Role getRole() {
        return Role.builder()
                .id(id)
                .name(name)
                .description(description)
                .permission(PERMISSION_TYPE.fromString(permission))
                .build();
    }

    @JsonIgnore
    public void createOrUpdateRole() {
        Role role = this.getRole();
        if (role.getId() != null) {
            //clear all old mapping
            removeMapping(role);
            //Update role
            DaoUtils.getRoleDao().update(role);
        } else {
            DaoUtils.getRoleDao().create(role);
        }

        //Map resources
        //Update users map
        if (users != null) {
            User user = User.builder().build();
            RoleUserMap roleUserMap = RoleUserMap.builder().role(role).build();
            for (Integer userId : users) {
                user.setId(userId);
                roleUserMap.setUser(user);
                DaoUtils.getRoleUserMapDao().create(roleUserMap);
            }
        }

        if (role.getPermission() == PERMISSION_TYPE.SUPER_ADMIN) {

        } else if (role.getPermission() == PERMISSION_TYPE.USER) {
            //Update gateway map
            if (gateways != null) {
                GatewayTable gatewayTable = GatewayTable.builder().build();
                RoleGatewayMap roleGatewayMap = RoleGatewayMap.builder().role(role).build();
                for (Integer gatewayId : gateways) {
                    gatewayTable.setId(gatewayId);
                    roleGatewayMap.setGatewayTable(gatewayTable);
                    DaoUtils.getRoleGatewayMapDao().create(roleGatewayMap);
                }
            }
            //Update node map
            if (nodes != null) {
                Node node = Node.builder().build();
                RoleNodeMap roleNodeMap = RoleNodeMap.builder().role(role).build();
                for (Integer nodeId : nodes) {
                    node.setId(nodeId);
                    roleNodeMap.setNode(node);
                    DaoUtils.getRoleNodeMapDao().create(roleNodeMap);
                }
            }
            //Update sensor map
            if (sensors != null) {
                Sensor sensor = Sensor.builder().build();
                RoleSensorMap roleSensorMap = RoleSensorMap.builder().role(role).build();
                for (Integer sensorId : sensors) {
                    sensor.setId(sensorId);
                    roleSensorMap.setSensor(sensor);
                    DaoUtils.getRoleSensorMapDao().create(roleSensorMap);
                }
            }
        } else if (role.getPermission() == PERMISSION_TYPE.MQTT_USER) {
            RoleMqttMap roleMqttMap = RoleMqttMap.builder().role(role).build();
            if (topicsPublish != null) {
                ArrayList<String> topics = new ArrayList<String>();
                topics.addAll(Arrays.asList(topicsPublish.split("\\s*,\\s*")));
                roleMqttMap.setPublish(topics);
            }
            if (topicsSubscribe != null) {
                ArrayList<String> topics = new ArrayList<String>();
                topics.addAll(Arrays.asList(topicsSubscribe.split("\\s*,\\s*")));
                roleMqttMap.setSubscribe(topics);
            }
            DaoUtils.getRoleMqttMapDao().create(roleMqttMap);
        }
    }

    @JsonIgnore
    public void deleteRoles(List<Integer> roleIds) {
        List<Role> roles = DaoUtils.getRoleDao().getAll(roleIds);
        for (Role role : roles) {
            removeMapping(role);
            DaoUtils.getRoleDao().delete(role);
        }
    }

    @JsonIgnore
    private void removeMapping(Role role) {
        //clear all old mapping
        //TODO: Logged in user should not be removed
        DaoUtils.getRoleUserMapDao().deleteByRoleId(role.getId());
        //clear gateways
        DaoUtils.getRoleGatewayMapDao().deleteByRoleId(role.getId());
        //clear nodes
        DaoUtils.getRoleNodeMapDao().deleteByRoleId(role.getId());
        //clear sensors
        DaoUtils.getRoleSensorMapDao().deleteByRoleId(role.getId());
        //clear mqtt map
        DaoUtils.getRoleMqttMapDao().deleteByRoleId(role.getId());
        //Update role
        DaoUtils.getRoleDao().update(role);
    }
}
