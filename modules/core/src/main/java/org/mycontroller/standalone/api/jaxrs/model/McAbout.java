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

import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.DataBaseUtils;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ToString
@Slf4j
public class McAbout extends McAboutBase {
    private String applicationVersion;
    private String applicationDbVersion;
    private String applicationLocation;
    private String databaseType;
    private String databaseVersion;

    public McAbout() {
        applicationVersion = AppProperties.getInstance().getControllerSettings().getVersion();
        applicationDbVersion = AppProperties.getInstance().getControllerSettings().getDbVersion();
        applicationLocation = AppProperties.getInstance().getAppDirectory();
        databaseType = AppProperties.getInstance().getDbType().getText();
        try {
            String[] queryResult = DaoUtils.getUserDao().getDao().queryRaw(DataBaseUtils.getDatabaseVersionQuery())
                    .getFirstResult();
            if (queryResult != null && queryResult.length > 0
                    && queryResult[0] != null && queryResult[0].length() > 0) {
                databaseVersion = queryResult[0].trim();
            } else {
                databaseVersion = "Version not found";
            }
        } catch (Exception ex) {
            databaseVersion = "Error: " + ex.getMessage();
            _logger.error("Exception, ", ex);
        }
    }
}