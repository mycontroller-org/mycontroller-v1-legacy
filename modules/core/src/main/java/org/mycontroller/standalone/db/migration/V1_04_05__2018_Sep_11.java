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
package org.mycontroller.standalone.db.migration;

import java.sql.Connection;
import java.util.List;

import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.ResourceOperationUtils.SEND_PAYLOAD_OPERATIONS;
import org.mycontroller.standalone.db.tables.OperationTable;
import org.mycontroller.standalone.db.tables.ResourcesGroupMap;
import org.mycontroller.standalone.operation.OperationUtils;
import org.mycontroller.standalone.operation.OperationUtils.OPERATION_TYPE;
import org.mycontroller.standalone.operation.model.OperationSendPayload;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.4.0
 */
@Slf4j
public class V1_04_05__2018_Sep_11 extends MigrationBase {

    @Override
    public void migrate(Connection connection) throws Exception {
        _logger.debug("Migration triggered.");

        //Load dao's
        loadDao();

        /** Migration comments
         *  Description:
         *  1. There is a change in special payload.
         *  From this point special operation payload should starts with "sp:"
         **/

        // execute only if running on existing db
        int schemaVersion = sqlClient().getDatabaseSchemaVersionInt();
        _logger.debug("Schema version:{}", schemaVersion);
        if (schemaVersion != 0 && schemaVersion < 10405) {
            // read all the operations
            List<OperationTable> operationsRaw = DaoUtils.getOperationDao().getAll();
            for (OperationTable operationRaw : operationsRaw) {
                // update send payload operations
                if (operationRaw.getType() == OPERATION_TYPE.SEND_PAYLOAD) {
                    OperationSendPayload operation = (OperationSendPayload) OperationUtils.getOperation(operationRaw);
                    if (getOperationType(operation.getPayload()) != null) {
                        String payloadOrg = operation.getPayload();
                        operation.setPayload("sp:" + payloadOrg);
                        DaoUtils.getOperationDao().update(operation.getOperationTable());
                    }
                }
            }
            //update resources in resource groups
            List<ResourcesGroupMap> resources = DaoUtils.getResourcesGroupMapDao().getAll();
            for (ResourcesGroupMap resource : resources) {
                switch (resource.getResourceType()) {
                    case GATEWAY:
                    case NODE:
                    case SENSOR_VARIABLE:
                    case FORWARD_PAYLOAD:
                        String payloadOn = resource.getPayloadOn();
                        String payloadOff = resource.getPayloadOff();
                        if (getOperationType(payloadOn) != null) {
                            resource.setPayloadOn("sp:" + payloadOn);
                        }
                        if (getOperationType(payloadOff) != null) {
                            resource.setPayloadOff("sp:" + payloadOff);
                        }
                        //update resourceMap data
                        DaoUtils.getResourcesGroupMapDao().update(resource);
                    default:
                        break;
                }
            }
        }
        reloadDao();
        _logger.info("Migration completed successfully.");
    }

    private SEND_PAYLOAD_OPERATIONS getOperationType(String payload) {
        String pl = payload.toLowerCase().trim();
        SEND_PAYLOAD_OPERATIONS operationType = SEND_PAYLOAD_OPERATIONS.fromString(pl);
        if (operationType == null) {
            operationType = SEND_PAYLOAD_OPERATIONS.fromString(pl.substring(0, 1));
        }
        return operationType;
    }
}
