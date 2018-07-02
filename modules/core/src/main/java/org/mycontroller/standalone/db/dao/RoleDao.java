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

import java.util.HashMap;
import java.util.List;

import org.mycontroller.standalone.api.jaxrs.model.Query;
import org.mycontroller.standalone.api.jaxrs.model.QueryResponse;
import org.mycontroller.standalone.db.tables.Role;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
public interface RoleDao extends BaseDao<Role, Integer> {
    Role getByRoleName(String roleName);

    List<String> getPermissionsByUserId(Integer userId);

    List<Integer> getGatewayIds(Integer userId);

    List<Integer> getNodeIds(Integer userId);

    HashMap<String, List<String>> getMqttTopics(Integer userId);

    List<Integer> getSensorIds(Integer userId);

    List<Integer> getSensorVariableIds(Integer userId);

    QueryResponse getAll(Query query);

}
