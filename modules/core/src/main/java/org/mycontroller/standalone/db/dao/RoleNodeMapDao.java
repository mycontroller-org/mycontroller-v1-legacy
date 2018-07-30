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

import java.util.List;

import org.mycontroller.standalone.db.tables.RoleNodeMap;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
public interface RoleNodeMapDao extends BaseDao<RoleNodeMap, Object> {
    List<RoleNodeMap> getByGatewayId(Integer nodeId);

    List<RoleNodeMap> getByRoleId(Integer roleId);

    void deleteByGatewayIds(List<Integer> nodeIds);

    void deleteByRoleIds(List<Integer> roleIds);

    void deleteByRoleId(Integer roleId);
}
