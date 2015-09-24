/**
 * Copyright (C) 2015 Jeeva Kandasamy (jkandasa@gmail.com)
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
package org.mycontroller.standalone.db;

import java.util.ArrayList;
import java.util.List;

import org.mycontroller.standalone.db.tables.Firmware;
import org.mycontroller.standalone.db.tables.Settings;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class SettingsUtils {

    private static final String HIDE_DATA = "*****";

    private SettingsUtils() {

    }

    public static List<Settings> getNodeDefaults() {
        ArrayList<String> keys = new ArrayList<String>();
        keys.add(Settings.AUTO_NODE_ID);
        keys.add(Settings.DEFAULT_FIRMWARE);
        keys.add(Settings.ENABLE_NOT_AVAILABLE_TO_DEFAULT_FIRMWARE);
        List<Settings> settings = DaoUtils.getSettingsDao().get(keys);
        for (Settings setting : settings) {
            if (setting.getKey().equals(Settings.DEFAULT_FIRMWARE)) {
                if (setting.getValue() != null) {
                    Firmware firmware = DaoUtils.getFirmwareDao().get(Integer.valueOf(setting.getValue()));
                    if (firmware != null) {
                        setting.setValue(firmware.getFirmwareName());
                    } else {
                        setting.setValue(null);
                        DaoUtils.getSettingsDao().update(setting);
                    }
                }
                break;
            }
        }
        return settings;
    }

    public static List<Settings> getSunRiseSet() {
        ArrayList<String> keys = new ArrayList<String>();
        keys.add(Settings.CITY_LATITUDE);
        keys.add(Settings.CITY_LONGITUDE);
        keys.add(Settings.SUNRISE_TIME);
        keys.add(Settings.SUNSET_TIME);
        return DaoUtils.getSettingsDao().get(keys);
    }

    public static List<Settings> getEmailSettings() {
        ArrayList<String> keys = new ArrayList<String>();
        keys.add(Settings.EMAIL_SMTP_HOST);
        keys.add(Settings.EMAIL_SMTP_PORT);
        keys.add(Settings.EMAIL_FROM);
        keys.add(Settings.EMAIL_ENABLE_SSL);
        keys.add(Settings.EMAIL_SMTP_USERNAME);
        keys.add(Settings.EMAIL_SMTP_PASSWORD);
        List<Settings> settings = DaoUtils.getSettingsDao().get(keys);
        for (Settings setting : settings) {
            if (setting.getKey().equals(Settings.EMAIL_SMTP_PASSWORD)) {
                setting.setValue(HIDE_DATA);
                break;
            }
        }
        return settings;
    }

    public static List<Settings> getDisplayUnits() {
        ArrayList<String> keys = new ArrayList<String>();
        keys.add(Settings.DEFAULT_UNIT_DISTANCE);
        keys.add(Settings.DEFAULT_UNIT_PERCENTAGE);
        keys.add(Settings.DEFAULT_UNIT_TEMPERATURE);
        return DaoUtils.getSettingsDao().get(keys);
    }

    public static List<Settings> getVersionInfo() {
        ArrayList<String> keys = new ArrayList<String>();
        keys.add(Settings.MC_VERSION);
        keys.add(Settings.MC_DB_VERSION);
        return DaoUtils.getSettingsDao().get(keys);
    }

    public static List<Settings> getSMSSettings() {
        ArrayList<String> keys = new ArrayList<String>();
        keys.add(Settings.SMS_AUTH_ID);
        keys.add(Settings.SMS_AUTH_TOKEN);
        keys.add(Settings.SMS_FROM_PHONE_NUMBER);
        List<Settings> settings = DaoUtils.getSettingsDao().get(keys);
        for (Settings setting : settings) {
            if (setting.getKey().equals(Settings.SMS_AUTH_TOKEN)) {
                setting.setValue(HIDE_DATA);
                break;
            }
        }
        return settings;
    }

    public static List<Settings> getGraphSettings() {
        ArrayList<String> keys = new ArrayList<String>();
        keys.add(Settings.GRAPH_INTERPOLATE_TYPE);
        List<Settings> settings = DaoUtils.getSettingsDao().get(keys);
        return settings;
    }
}
