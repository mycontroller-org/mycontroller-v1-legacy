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

import java.util.List;

import org.mycontroller.standalone.api.jaxrs.mapper.Query;
import org.mycontroller.standalone.api.jaxrs.mapper.QueryResponse;
import org.mycontroller.standalone.db.tables.Node;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public interface NodeDao {

    void create(Node node);

    void createOrUpdate(Node node);

    void delete(Node node);

    void delete(Integer gatewayId, String nodeEui);

    void delete(Integer id);

    void delete(List<Integer> ids);

    void update(Node node);

    List<Node> getAll();

    List<Node> getAll(Integer gatewayId);

    List<Node> get(List<Integer> ids);

    Node get(Node node);

    Node get(Integer gatewayId, String nodeEui);

    Node get(Integer id);

    List<Node> getByName(String name);

    QueryResponse getAll(Query query);

    long countOf(Integer gatewayId);
}
