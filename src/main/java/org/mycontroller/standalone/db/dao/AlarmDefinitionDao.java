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

import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.alarm.AlarmUtils.DAMPENING_TYPE;
import org.mycontroller.standalone.api.jaxrs.mapper.Query;
import org.mycontroller.standalone.api.jaxrs.mapper.QueryResponse;
import org.mycontroller.standalone.db.tables.AlarmDefinition;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public interface AlarmDefinitionDao extends BaseDao<AlarmDefinition, Integer> {

    void delete(RESOURCE_TYPE resourceType, Integer resourceId);

    List<AlarmDefinition> getAll(RESOURCE_TYPE resourceType);

    List<AlarmDefinition> getAll(DAMPENING_TYPE dampeningType);

    List<AlarmDefinition> getAll(RESOURCE_TYPE resourceType, Integer resourceId);

    List<AlarmDefinition> getAllEnabled(RESOURCE_TYPE resourceType, Integer resourceId);

    List<AlarmDefinition> getAll(RESOURCE_TYPE resourceType, Integer resourceId, Boolean enabled);

    void disableAllTriggered();

    long countOf(RESOURCE_TYPE resourceType, Integer resourceId);

    long countOf(RESOURCE_TYPE resourceType, List<Integer> resourceIds);

    QueryResponse getAll(Query query);

    List<AlarmDefinition> getAllByResourceIds(RESOURCE_TYPE resourceType, List<Integer> resourceIds);

}
