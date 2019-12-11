/*
 * Copyright 2015-2019 Jeeva Kandasamy (jkandasa@gmail.com)
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
package org.mycontroller.standalone.jobs;

import java.util.concurrent.atomic.AtomicBoolean;

import org.knowm.sundial.Job;
import org.knowm.sundial.exceptions.JobInterruptException;
import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.backup.Backup;
import org.mycontroller.standalone.backup.McFileUtils;
import org.mycontroller.standalone.settings.BackupSettings;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Slf4j
public class SystemBackupJob extends Job {
    private static final AtomicBoolean IS_RUNNING = new AtomicBoolean(false);

    @Override
    public void doRun() throws JobInterruptException {
        if (IS_RUNNING.get()) {
            _logger.warn("A backup already running.");
            return;
        }
        try {
            IS_RUNNING.set(true);
            _logger.debug("Backup job triggered");
            BackupSettings settings = AppProperties.getInstance().getBackupSettings();
            Backup.backup(settings.getPrefix());
            // Retain max backup
            McFileUtils.removeOldFiles(settings.getPrefix(), settings.getRetainMax());
            _logger.debug("Backup job completed");
        } catch (Exception ex) {
            _logger.error("Exception, ", ex);
        } finally {
            IS_RUNNING.set(false);
        }
    }
}
