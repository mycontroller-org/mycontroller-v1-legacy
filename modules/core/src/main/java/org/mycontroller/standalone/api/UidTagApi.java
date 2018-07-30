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
package org.mycontroller.standalone.api;

import java.util.HashMap;
import java.util.List;

import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.api.jaxrs.model.Query;
import org.mycontroller.standalone.api.jaxrs.model.QueryResponse;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.UidTag;
import org.mycontroller.standalone.exceptions.McBadRequestException;
import org.mycontroller.standalone.exceptions.McDuplicateException;
import org.mycontroller.standalone.model.ResourceModel;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
public class UidTagApi {
    public QueryResponse getAll(HashMap<String, Object> filters) {
        return DaoUtils.getUidTagDao().getAll(Query.get(filters));
    }

    public UidTag get(int id) {
        return DaoUtils.getUidTagDao().getById(id);
    }

    public UidTag getByUid(String uid) {
        return DaoUtils.getUidTagDao().getByUid(uid);
    }

    public UidTag get(RESOURCE_TYPE resourceType, Integer resourceId) {
        return DaoUtils.getUidTagDao().get(resourceType, resourceId);
    }

    public void delete(List<Integer> ids) {
        DaoUtils.getUidTagDao().deleteByIds(ids);
    }

    public void deleteByUid(String uid) {
        DaoUtils.getUidTagDao().deleteByUid(uid);
    }

    public void update(UidTag uidTag) throws McDuplicateException, McBadRequestException {
        UidTag availabilityCheck = DaoUtils.getUidTagDao().getById(uidTag.getId());
        if (availabilityCheck != null) {
            UidTag uidAvailabilityCheck = DaoUtils.getUidTagDao().getByUid(uidTag.getUid());
            if (uidAvailabilityCheck != null && !uidAvailabilityCheck.getId().equals(availabilityCheck.getId())) {
                throw new McDuplicateException(
                        "This UID["
                                + uidTag.getUid()
                                + "] tagged with another resource variable["
                                + new ResourceModel(uidTag.getResourceType(), uidTag.getResourceId())
                                        .getResourceLessDetails() + "].");
            }
            UidTag rsAvailabilityCheck = DaoUtils.getUidTagDao().get(uidTag.getResourceType(), uidTag.getResourceId());
            if (rsAvailabilityCheck != null && !rsAvailabilityCheck.getId().equals(availabilityCheck.getId())) {
                throw new McDuplicateException(
                        "This resource variable["
                                + new ResourceModel(rsAvailabilityCheck.getResourceType(), rsAvailabilityCheck
                                        .getResourceId()).getResourceLessDetails() + "] tagged with another UID["
                                + rsAvailabilityCheck.getUid() + "] .");
            }
        } else {
            throw new McBadRequestException("Selected entry not available!");
        }
        DaoUtils.getUidTagDao().update(uidTag);
    }

    public void add(UidTag uidTag) throws McDuplicateException {
        UidTag availabilityCheck = DaoUtils.getUidTagDao().getByUid(uidTag.getUid());
        if (availabilityCheck != null) {
            throw new McDuplicateException("This UID[" + uidTag.getUid() + "] tagged with another resource["
                    + new ResourceModel(uidTag.getResourceType(), uidTag.getResourceId()).getResourceLessDetails()
                    + "].");
        }
        availabilityCheck = DaoUtils.getUidTagDao().get(uidTag.getResourceType(), uidTag.getResourceId());
        if (availabilityCheck != null) {
            throw new McDuplicateException("This resource["
                    + new ResourceModel(uidTag.getResourceType(), uidTag.getResourceId()).getResourceLessDetails()
                    + "] tagged with another UID[" + availabilityCheck.getUid() + "] .");
        }
        DaoUtils.getUidTagDao().create(uidTag);
    }
}
