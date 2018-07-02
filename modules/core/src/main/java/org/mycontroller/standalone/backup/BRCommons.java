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
package org.mycontroller.standalone.backup;

import java.io.File;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
public class BRCommons {
    public static final String DATABASE_FILENAME = "database_backup.zip";
    public static final String APP_PROPERTIES_FILENAME = "mycontroller.properties";
    public static final String APP_CONF_LOCATION = "../conf/";
    public static final String RESOURCES_LOCATION = "resources" + File.separator;
    public static final String FILE_NAME_IDENTITY = "_mc_backup";

    private static boolean isbackupRestoreRunning = false;

    public static synchronized boolean isBackupRestoreRunning() {
        return isbackupRestoreRunning;
    }

    public static synchronized void setBackupRestoreRunning(boolean isRunning) {
        BRCommons.isbackupRestoreRunning = isRunning;
    }

}
