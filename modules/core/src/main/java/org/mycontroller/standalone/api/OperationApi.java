/*
 * Copyright 2015-2016 Jeeva Kandasamy (jkandasa@gmail.com)
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

import java.util.List;

import org.mycontroller.standalone.api.jaxrs.json.Query;
import org.mycontroller.standalone.api.jaxrs.json.QueryResponse;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.OperationTable;
import org.mycontroller.standalone.operation.OperationUtils;
import org.mycontroller.standalone.operation.model.Operation;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */

public class OperationApi {

    public OperationTable get(int id) {
        return DaoUtils.getOperationDao().getById(id);
    }

    public QueryResponse getAll(Query query) {
        return DaoUtils.getOperationDao().getAll(query);
    }

    public void add(Operation operation) {
        DaoUtils.getOperationDao().create(operation.getOperationTable());
    }

    public void update(Operation operation) {
        if (!operation.getEnabled()) {
            OperationUtils.unloadOperationTimerJobs(operation.getOperationTable());
        }
        DaoUtils.getOperationDao().update(operation.getOperationTable());
    }

    public void deleteIds(List<Integer> ids) {
        OperationUtils.unloadNotificationTimerJobs(ids);
        DaoUtils.getOperationDao().deleteByIds(ids);
    }

    public void enableIds(List<Integer> ids) {
        for (OperationTable operationTable : DaoUtils.getOperationDao().getAll(ids)) {
            operationTable.setEnabled(true);
            DaoUtils.getOperationDao().update(operationTable);
        }
    }

    public void disableIds(List<Integer> ids) {
        OperationUtils.unloadNotificationTimerJobs(ids);
        for (OperationTable operationTable : DaoUtils.getOperationDao().getAll(ids)) {
            operationTable.setEnabled(false);
            DaoUtils.getOperationDao().update(operationTable);
        }
    }

}
