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
package org.mycontroller.standalone.offheap;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mycontroller.standalone.AppProperties;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OffHeapFactory {
    private static final String MC_PERSISTENT_DIR = "mc/";
    private static DB database = null;
    private static final ScheduledExecutorService COMMIT_SCHEDULER = Executors.newScheduledThreadPool(1);
    private static final long COMMIT_FREQUENCY = 30;

    public static void init() {
        if (database != null && !database.isClosed()) {
            _logger.info("MapDB already running...");
            return;
        }
        String storesLocation = AppProperties.getInstance().getMcPersistentStoresLocation() + MC_PERSISTENT_DIR;
        // delete everything on start if clear message on start
        if (AppProperties.getInstance().getClearMessagesQueueOnStart()
                && AppProperties.getInstance().getClearSmartSleppMsgQueueOnStart()) {
            reset();
        }
        AppProperties.getInstance().createDirectoryLocation(storesLocation);
        database = DBMaker.newFileDB(FileUtils.getFile(storesLocation + "mc.mapdb")).make();
        COMMIT_SCHEDULER.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                if (database != null && !database.isClosed()) {
                    database.commit();
                } else {
                    _logger.debug("commit job called, when database is not available!");
                }
            }
        }, COMMIT_FREQUENCY, COMMIT_FREQUENCY, TimeUnit.SECONDS);
    }

    public static DB store() {
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
        //TODO: COMMIT_SCHEDULER.shutdown(); //When we call this, entire application gets shutdown.
        _logger.debug("Persistence commit scheduler is shutdown");
    }

    public static void reset() {
        String _location = AppProperties.getInstance().getMcPersistentStoresLocation() + MC_PERSISTENT_DIR;
        try {
            File _locationAsFile = FileUtils.getFile(_location);
            if (_locationAsFile.exists()) {
                FileUtils.deleteDirectory(_locationAsFile);
                _logger.info("Cleared McPersistent location[{}]", _location);
            } else {
                _logger.debug("McPersistent location[{}] not available", _location);
            }

        } catch (Exception ex) {
            _logger.error("Failed to clear McPersistent location[{}]", _location, ex);
        }

    }
}
