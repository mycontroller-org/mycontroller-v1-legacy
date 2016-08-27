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
package org.mycontroller.standalone;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MapDbFactory {
    private static DB database = null;
    private static final ScheduledExecutorService COMMIT_SCHEDULER = Executors.newScheduledThreadPool(1);
    public static final long COMMIT_FREQUENCY = 30;

    public static void init() {
        if (database != null && !database.isClosed()) {
            _logger.info("MapDB already running...");
            return;
        }
        String storesLocation = AppProperties.getInstance().getMcPersistentStoresLocation() + "mc/";
        AppProperties.getInstance().createDirectoryLocation(storesLocation);
        database = DBMaker.newFileDB(FileUtils.getFile(storesLocation + "mc.mapdb")).make();
        COMMIT_SCHEDULER.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                database.commit();
            }
        }, COMMIT_FREQUENCY, COMMIT_FREQUENCY, TimeUnit.SECONDS);
    }

    public static DB getDbStore() {
        return database;
    }

    public static void close() {
        if (database == null) {
            _logger.info("Not initialized yet!");
        }
        if (database.isClosed()) {
            _logger.debug("already closed");
            return;
        }
        database.commit();
        database.close();
        _logger.debug("closed disk storage");
        COMMIT_SCHEDULER.shutdown();
        _logger.debug("Persistence commit scheduler is shutdown");
    }
}
