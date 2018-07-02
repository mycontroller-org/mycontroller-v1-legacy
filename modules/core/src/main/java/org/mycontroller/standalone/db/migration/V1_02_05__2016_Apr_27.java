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
import org.mycontroller.standalone.db.tables.OperationTable;
import org.mycontroller.standalone.operation.OperationUtils;
import org.mycontroller.standalone.operation.OperationUtils.OPERATION_TYPE;
import org.mycontroller.standalone.operation.model.OperationSendEmail;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Slf4j
public class V1_02_05__2016_Apr_27 extends MigrationBase {

    @Override
    public void migrate(Connection connection) throws Exception {
        _logger.debug("Migration triggered.");

        //Load dao's
        loadDao();

        /** Migration comments
         *  Description:
         *  1. Add rule template to defined send email operations
         **/

        /** Migration #1
         * Add rule template to defined send email operations
         * steps
         * 1. Get all send email operations
         * 2. Add default template name
         * 3. Save
         * */

        List<OperationTable> operations = DaoUtils.getOperationDao().getAll(OperationTable.KEY_TYPE,
                OPERATION_TYPE.SEND_EMAIL);
        for (OperationTable operation : operations) {
            if (operation.getType() == OPERATION_TYPE.SEND_EMAIL) {
                OperationSendEmail sendEmail = (OperationSendEmail) OperationUtils.getOperation(operation);
                sendEmail.setTemplate("mc-default-rule-email-template.html");
                DaoUtils.getOperationDao().update(sendEmail.getOperationTable());
            }
        }
        _logger.debug("Successfully migrated {} send email operations", operations.size());

        _logger.info("Migration completed successfully.");
    }

}
