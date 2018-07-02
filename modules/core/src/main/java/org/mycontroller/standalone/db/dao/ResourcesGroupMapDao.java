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

import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.api.jaxrs.model.Query;
import org.mycontroller.standalone.api.jaxrs.model.QueryResponse;
import org.mycontroller.standalone.db.tables.ResourcesGroup;
import org.mycontroller.standalone.db.tables.ResourcesGroupMap;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public interface ResourcesGroupMapDao {
    void create(ResourcesGroupMap resourcesGroupMap);

    void createOrUpdate(ResourcesGroupMap resourcesGroupMap);

    void update(ResourcesGroupMap resourcesGroupMap);

    void delete(ResourcesGroupMap resourcesGroupMap);

    void delete(RESOURCE_TYPE resourceType, Integer resourceId);

    void delete(ResourcesGroup resourcesGroup);

    void delete(Integer id);

    void delete(List<Integer> ids);

    List<ResourcesGroupMap> getAll();

    List<ResourcesGroupMap> getAll(ResourcesGroup resourcesGroup);

    List<ResourcesGroupMap> getAll(Integer resourceGroupId);

    List<ResourcesGroupMap> getAll(RESOURCE_TYPE resourceType, Integer resourceId);

    ResourcesGroupMap get(Integer id);

    long countOf(RESOURCE_TYPE resourceType, Integer resourceId);

    QueryResponse getAll(Query query);

}
