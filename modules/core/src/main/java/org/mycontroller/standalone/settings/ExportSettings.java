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
package org.mycontroller.standalone.settings;

import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.mycontroller.standalone.jobs.SystemExportJob;
import org.mycontroller.standalone.scheduler.SchedulerUtils;
import org.mycontroller.standalone.timer.TimerSimple;
import org.mycontroller.standalone.utils.McUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */

@Builder
@ToString(includeFieldNames = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class ExportSettings {

    private static final String JOB_NAME = "myController-export-job";
    public static final String KEY_EXPORT = "export_data";
    public static final String SKEY_ENABLED = "enabled";
    public static final String SKEY_PREFIX = "prefix";
    public static final String SKEY_INTERVAL = "interval";
    public static final String SKEY_RETAIN_MAX = "retainMax";
    public static final String SKEY_EXPORT_LOCATION = "exportLocation";
    public static final String SKEY_ROW_LIMIT = "rowLimit";

    private Boolean enabled;
    private String prefix;
    private Long interval;
    private Integer retainMax;
    private String exportLocation;
    private Long rowLimit;

    public String getExportLocation() {
        if (exportLocation == null) {
            exportLocation = "../export/";
        } else if (!exportLocation.endsWith("/")) {
            exportLocation = exportLocation + "/";
        }
        //Create backup location
        if (!FileUtils.getFile(exportLocation).exists()) {
            try {
                FileUtils.forceMkdir(FileUtils.getFile(exportLocation));
                _logger.debug("export location created.");
            } catch (IOException e) {
                _logger.error("Unable to create export location");
            }
        }

        return exportLocation;
    }

    public Boolean getEnabled() {
        if (enabled == null) {
            return false;
        }
        return enabled;
    }

    public Long getNextFire() {
        if (getEnabled()) {
            return SchedulerUtils.nextFireTime(JOB_NAME, null);
        }
        return null;
    }

    public static ExportSettings get() {
        ExportSettings _settings = ExportSettings.builder()
                .enabled(McUtils.getBoolean(getValue(SKEY_ENABLED)))
                .prefix(getValue(SKEY_PREFIX))
                .interval(McUtils.getLong(getValue(SKEY_INTERVAL)))
                .retainMax(McUtils.getInteger(getValue(SKEY_RETAIN_MAX)))
                .exportLocation(getValue(SKEY_EXPORT_LOCATION))
                .rowLimit(McUtils.getLong(getValue(SKEY_ROW_LIMIT)))
                .build();
        return _settings;
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
            if (interval < McUtils.ONE_MINUTE) {
                interval = McUtils.ONE_MINUTE;
            }
            updateValue(SKEY_INTERVAL, interval);
        }
        if (retainMax != null) {
            updateValue(SKEY_RETAIN_MAX, retainMax);
        }
        if (exportLocation != null) {
            updateValue(SKEY_EXPORT_LOCATION, exportLocation);
        }
        if (rowLimit != null) {
            updateValue(SKEY_ROW_LIMIT, rowLimit);
        }
    }

    private static String getValue(String subKey) {
        return SettingsUtils.getValue(KEY_EXPORT, subKey);
    }

    private void updateValue(String subKey, Object value) {
        SettingsUtils.updateValue(KEY_EXPORT, subKey, value);
    }

    public static void reloadJob() {
        ExportSettings settings = get();
        TimerSimple timerSimple = new TimerSimple(
                JOB_NAME,//Job Name
                settings.getEnabled(),
                SystemExportJob.class.getName(),
                settings.getInterval(),
                -1//Repeat count
        );
        SchedulerUtils.reloadTimerJob(timerSimple.getTimer());
    }
}
