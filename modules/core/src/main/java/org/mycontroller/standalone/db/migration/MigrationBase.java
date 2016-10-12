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
package org.mycontroller.standalone.db.migration;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.api.SystemApi;
import org.mycontroller.standalone.db.DaoUtils;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
public abstract class MigrationBase implements JdbcMigration {

    private IMigrationClient sqlClient = null;

    public MigrationBase() {
        switch (AppProperties.getInstance().getDbType()) {
            case H2DB:
            case H2DB_EMBEDDED:
                sqlClient = new ClientH2DB();
                break;
            case MYSQL:
            case MARIADB:
                sqlClient = new ClientMysql();
                break;
            case POSTGRESQL:
                sqlClient = new ClientPostgreSql();
                break;
            default:
                break;
        }
    }

    protected void loadDao() {
        //Load Dao's if not loaded already
        if (!DaoUtils.isDaoInitialized()) {
            DaoUtils.loadAllDao();
        }

        //Load properties from database
        AppProperties.getInstance().loadPropertiesFromDb();
    }

    protected void reloadDao() {
        DaoUtils.setIsDaoInitialized(false);
        loadDao();
    }

    protected String getApplicationDbVersion() {
        return new SystemApi().getAbout().getApplicationDbVersion();
    }

    public IMigrationClient sqlClient() {
        return sqlClient;
    }
}
