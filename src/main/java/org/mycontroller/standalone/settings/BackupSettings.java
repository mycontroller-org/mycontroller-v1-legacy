/**
 * Copyright (C) 2015-2016 Jeeva Kandasamy (jkandasa@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mycontroller.standalone.settings;

import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.mycontroller.standalone.NumericUtils;
import org.mycontroller.standalone.TIME_REF;
import org.mycontroller.standalone.jobs.SystemBackupJob;
import org.mycontroller.standalone.scheduler.SchedulerUtils;
import org.mycontroller.standalone.timer.TimerSimple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */

@Builder
@ToString(includeFieldNames = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BackupSettings {
    private static final Logger _logger = LoggerFactory.getLogger(BackupSettings.class.getName());

    private static final String JOB_NAME = "myController-backup-job";
    public static final String KEY_BACKUP = "backup";
    public static final String SKEY_ENABLED = "enabled";
    public static final String SKEY_PREFIX = "prefix";
    public static final String SKEY_INTERVAL = "interval";
    public static final String SKEY_RETAIN_MAX = "retainMax";
    public static final String SKEY_BACKUP_LOCATION = "backupLocation";

    private Boolean enabled;
    private String prefix;
    private Long interval;
    private Integer retainMax;
    private String backupLocation;

    public String getBackupLocation() {
        if (backupLocation == null) {
            backupLocation = "../backup/";
        } else if (!backupLocation.endsWith("/")) {
            backupLocation = backupLocation + "/";
        }
        //Create backup location
        if (!FileUtils.getFile(backupLocation).exists()) {
            try {
                FileUtils.forceMkdir(FileUtils.getFile(backupLocation));
                _logger.debug("backup location created.");
            } catch (IOException e) {
                _logger.error("Unable to create backup location");
            }
        }

        return backupLocation;
    }

    public Boolean getEnabled() {
        if (enabled == null) {
            return false;
        }
        return enabled;
    }

    public static BackupSettings get() {
        return BackupSettings.builder()
                .enabled(NumericUtils.getBoolean(getValue(SKEY_ENABLED)))
                .prefix(getValue(SKEY_PREFIX))
                .interval(NumericUtils.getLong(getValue(SKEY_INTERVAL)))
                .retainMax(NumericUtils.getInteger(getValue(SKEY_RETAIN_MAX)))
                .backupLocation(getValue(SKEY_BACKUP_LOCATION))
                .build();
    }

    public void save() {
        if (enabled != null) {
            updateValue(SKEY_ENABLED, enabled);
        }
        if (prefix != null) {
            updateValue(SKEY_PREFIX, prefix.trim());
        }
        if (interval != null) {
            //Should not allow to take backup less than a minute frequency
            if (interval < TIME_REF.ONE_MINUTE) {
                interval = TIME_REF.ONE_MINUTE;
            }
            updateValue(SKEY_INTERVAL, interval);
        }
        if (retainMax != null) {
            updateValue(SKEY_RETAIN_MAX, retainMax);
        }
        if (backupLocation != null) {
            updateValue(SKEY_BACKUP_LOCATION, backupLocation);
        }
    }

    private static String getValue(String subKey) {
        return SettingsUtils.getValue(KEY_BACKUP, subKey);
    }

    private void updateValue(String subKey, Object value) {
        SettingsUtils.updateValue(KEY_BACKUP, subKey, value);
    }

    public static void reloadJob() {
        BackupSettings settings = get();
        TimerSimple timerSimple = new TimerSimple(
                JOB_NAME,//Job Name
                settings.getEnabled(),
                SystemBackupJob.class.getName(),
                settings.getInterval(),
                -1//Repeat count
        );
        SchedulerUtils.reloadTimerJob(timerSimple.getTimer());
    }
}
