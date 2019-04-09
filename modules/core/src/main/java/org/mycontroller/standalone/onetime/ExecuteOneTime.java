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
package org.mycontroller.standalone.onetime;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.db.DaoUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.5.0
 */
@Slf4j
public class ExecuteOneTime {
    public static final String[] ONE_TIME_FILES = { "conf/onetime_reset", "conf/onetime_reset.txt" };

    public void executeOnetimeReset() {
        File onetimeFile = null;
        for (String _file : ONE_TIME_FILES) {
            onetimeFile = FileUtils.getFile(AppProperties.getInstance().getAppDirectory() + _file);
            try {
                _logger.debug("Onetime file check on {}", onetimeFile.getCanonicalPath());
            } catch (Exception ex) {
                _logger.error("Exception, ", ex);
            }
            if (onetimeFile.exists()) {
                break;
            }
        }
        if (onetimeFile != null && onetimeFile.exists()) {
            try {
                List<String> dataArray = FileUtils.readLines(onetimeFile);
                // execute one time reset commands
                for (String rawData : dataArray) {
                    OnetimeResetCommand cmd = new OnetimeResetCommand(rawData);
                    if (cmd.isValid()) {
                        switch (cmd.getCommand()) {
                            case "reset_password":
                                resetPassword(cmd);
                                break;
                            case "purge_sensor_data":
                                purgeSensorData(cmd);
                                break;
                            case "purge_battery_data":
                                purgeBatteryData(cmd);
                                break;
                            default:
                                _logger.warn("Unknown command, raw:{}, {}", rawData, cmd);
                                break;
                        }
                    }
                }
                //Delete onetime reset file
                if (onetimeFile.delete()) {
                    _logger.debug("onetime reset file deleted successfully. [{}]", onetimeFile.getCanonicalPath());
                } else {
                    _logger.warn("Failed to delete onetime reset file[{}]", onetimeFile.getCanonicalPath());
                }
            } catch (IOException ex) {
                _logger.error("Exception, ", ex);
            }
        } else {
            _logger.debug("There was no password reset file!");
        }
    }

    private void resetPassword(OnetimeResetCommand cmd) {
        try {
            String[] userData = cmd.getData().split(",", 2);
            if (userData.length > 1) {
                ResetPassword.resetPassword(userData[0], userData[1]);
            } else {
                _logger.warn("Invalid password reset data");
            }
        } catch (Exception ex) {
            _logger.error("Exception,", ex);
        }
    }

    private void purgeSensorData(OnetimeResetCommand cmd) {
        try {
            // all [or] node=[name_or_eui],sensor=[name]
            // TODO: implement other options
            if (cmd.getData().toLowerCase().startsWith("all")) {
                _logger.info("Purged binary data, count:{}", DaoUtils.getMetricsBinaryTypeDeviceDao().purgeAll());
                _logger.info("Purged double data, count:{}", DaoUtils.getMetricsDoubleTypeDeviceDao().purgeAll());
                _logger.info("Purged counter data, count:{}", DaoUtils.getMetricsCounterTypeDeviceDao().purgeAll());
                _logger.info("Purged gps data, count:{}", DaoUtils.getMetricsGPSTypeDeviceDao().purgeAll());

            } else if (cmd.getData().toLowerCase().startsWith("node")) {

            } else if (cmd.getData().toLowerCase().startsWith("sensor")) {

            }
        } catch (Exception ex) {
            _logger.error("Exception,", ex);
        }
    }

    private void purgeBatteryData(OnetimeResetCommand cmd) {
        try {
            // all [or] node=[name_or_eui]
            // TODO: implement other options
            if (cmd.getData().toLowerCase().startsWith("all")) {
                _logger.info("Purged battery data, count:{}", DaoUtils.getMetricsBatteryUsageDao().purgeAll());
            } else if (cmd.getData().toLowerCase().startsWith("node")) {

            }
        } catch (Exception ex) {
            _logger.error("Exception,", ex);
        }
    }
}
