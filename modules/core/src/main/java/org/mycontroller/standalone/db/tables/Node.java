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
package org.mycontroller.standalone.db.tables;

import java.util.HashMap;

import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.AppProperties.STATE;
import org.mycontroller.standalone.db.DB_TABLES;
import org.mycontroller.standalone.db.NodeUtils.NODE_REGISTRATION_STATE;
import org.mycontroller.standalone.jobs.NodeAliveStatusJob;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_PRESENTATION;
import org.mycontroller.standalone.utils.McUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@DatabaseTable(tableName = DB_TABLES.NODE)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(includeFieldNames = true)
@Slf4j
public class Node {
    public static final String KEY_FIRMWARE_ID = "firmwareId";
    public static final String KEY_GATEWAY_ID = "gatewayId";
    public static final String KEY_GATEWAY_NAME = "gatewayName";
    public static final String KEY_STATE = "state";
    public static final String KEY_EUI = "eui";
    public static final String KEY_ID = "id";
    public static final String KEY_NAME = "name";
    public static final String KEY_TYPE = "type";
    public static final String KEY_VERSION = "version";
    public static final String KEY_LIB_VERSION = "libVersion";
    public static final String KEY_BATTERY_LEVEL = "batteryLevel";
    public static final String KEY_ERASE_CONFIG = "eraseConfig";
    public static final String KEY_LAST_SEEN = "lastSeen";
    public static final String KEY_RSSI = "rssi";
    public static final String KEY_PROPERTIES = "properties";
    public static final String KEY_PARENT_NODE_EUI = "parentNodeEui";
    public static final String KEY_REGISTRATION_STATE = "registrationState";
    public static final String KEY_SMART_SLEEP_ENABLED = "smartSleepEnabled";
    public static final String KEY_SMART_SLEEP_WAIT_DURATION = "smartSleepWaitDuration";
    public static final String KEY_SMART_SLEEP_DURATION = "smartSleepDuration";
    // firmware keys
    public static final String KEY_FW_OPERATION_LAST = "fwOpLast";
    public static final String KEY_FW_OPERATION_FIRST = "fwOpFirst";
    public static final String KEY_FW_OPERATION_LAST_DURATION = "fwOpLastDuration";
    public static final String KEY_FW_BLOCKS_SENT = "fwBksSent";
    public static final String KEY_FW_BLOCKS_TOTAL = "fwBksTotal";

    //Properties key
    public static final String KEY_ALIVE_CHECK_INTERVAL = "aliveCheckInterval";
    public static final String KEY_HEARTBEAT_LAST_TX_TIME = "hbTx";
    public static final String KEY_NAME_LOCKED = "nameLocked";

    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true, columnName = KEY_ID)
    private Integer id;

    @DatabaseField(uniqueCombo = true, canBeNull = false, columnName = KEY_EUI)
    private String eui;

    @DatabaseField(uniqueCombo = true, canBeNull = false, columnName = KEY_GATEWAY_ID, foreign = true,
            foreignAutoRefresh = true, maxForeignAutoRefreshLevel = 1)
    private GatewayTable gatewayTable;

    @DatabaseField(columnName = KEY_NAME)
    private String name;

    @DatabaseField(columnName = KEY_VERSION)
    private String version;

    @DatabaseField(dataType = DataType.ENUM_STRING, columnName = KEY_TYPE)
    private MESSAGE_TYPE_PRESENTATION type;

    @DatabaseField(columnName = KEY_LIB_VERSION)
    private String libVersion;

    @DatabaseField(columnName = KEY_BATTERY_LEVEL)
    private String batteryLevel;

    @DatabaseField(canBeNull = true, columnName = KEY_ERASE_CONFIG)
    private Boolean eraseConfig;

    @DatabaseField(canBeNull = true, columnName = KEY_FIRMWARE_ID, foreign = true,
            foreignAutoRefresh = true, maxForeignAutoRefreshLevel = 2)
    private Firmware firmware;

    @DatabaseField(canBeNull = false, dataType = DataType.ENUM_STRING, columnName = KEY_STATE)
    private STATE state = STATE.UNAVAILABLE;

    @DatabaseField(canBeNull = true, columnName = KEY_LAST_SEEN)
    private Long lastSeen;

    @DatabaseField(canBeNull = true, columnName = KEY_RSSI)
    private String rssi;

    @DatabaseField(canBeNull = true, columnName = KEY_PROPERTIES, dataType = DataType.SERIALIZABLE)
    private HashMap<String, Object> properties;

    @DatabaseField(canBeNull = true, columnName = KEY_PARENT_NODE_EUI)
    private String parentNodeEui;

    @DatabaseField(canBeNull = false, dataType = DataType.ENUM_STRING, columnName = KEY_REGISTRATION_STATE)
    private NODE_REGISTRATION_STATE registrationState = NODE_REGISTRATION_STATE.NEW;

    @DatabaseField(canBeNull = true, columnName = KEY_SMART_SLEEP_ENABLED)
    private Boolean smartSleepEnabled;

    public HashMap<String, Object> getProperties() {
        if (properties == null) {
            properties = new HashMap<String, Object>();
        }
        return properties;
    }

    public Boolean getSmartSleepEnabled() {
        if (smartSleepEnabled == null) {
            smartSleepEnabled = false;
        }
        return smartSleepEnabled;
    }

    @JsonIgnore
    public Object getProperty(String key) {
        return getProperties().get(key);
    }

    @JsonIgnore
    public HashMap<String, Object> setProperty(String key, Object value) {
        getProperties().put(key, value);
        return getProperties();
    }

    @JsonIgnore
    private Long getInterval(String key) {
        Long _interval = null;
        if (getProperty(key) != null) {
            try {
                Object value = getProperty(key);
                if (value instanceof Integer) {
                    _interval = (Integer) value * McUtils.MINUTE;
                } else {
                    _interval = Integer.valueOf(String.valueOf(value)) * McUtils.MINUTE;
                }
            } catch (Exception ex) {
                _logger.warn("Unable to convert the property[{}:{}] to Integer value. Using default value for {}",
                        key, getProperty(key), this, ex);
            }
        }
        if (_interval == null) {
            _interval = AppProperties.getInstance().getControllerSettings().getAliveCheckInterval();
        }
        if (_interval < NodeAliveStatusJob.MIN_ALIVE_CHECK_DURATION) {
            return NodeAliveStatusJob.MIN_ALIVE_CHECK_DURATION;
        }
        return _interval;
    }

    @JsonIgnore
    public Long getAliveCheckInterval() {
        return getInterval(KEY_ALIVE_CHECK_INTERVAL);
    }

    @JsonIgnore
    public Long getHeartbeatInterval() {
        return getAliveCheckInterval();
    }

    @JsonIgnore
    public Long getLastHeartbeatTxTime() {
        if (getProperty(KEY_HEARTBEAT_LAST_TX_TIME) == null) {
            return 0L;
        } else {
            try {
                return (Long) getProperty(KEY_HEARTBEAT_LAST_TX_TIME);
            } catch (Exception ex) {
                return 0L;
            }
        }
    }

    @JsonIgnore
    public boolean isNameLocked() {
        Boolean nameLocked = (Boolean) this.getProperty(KEY_NAME_LOCKED);
        if (nameLocked == null) {
            return false;
        }
        return nameLocked;
    }

    public void firmwareUpdateFinished() {
        Long startTime = (Long) this.getProperty(KEY_FW_OPERATION_FIRST);
        Long endTime = (Long) this.getProperty(KEY_FW_OPERATION_LAST);
        if (startTime != null && endTime != null) {
            this.getProperties().put(KEY_FW_OPERATION_LAST_DURATION, endTime - startTime);
        }
    }

    public void firmwareUpdateStart(int totalBlocks) {
        this.getProperties().put(KEY_FW_OPERATION_FIRST, System.currentTimeMillis());
        this.getProperties().put(KEY_FW_BLOCKS_TOTAL, totalBlocks);
    }

    public void updateFirmwareStatus(int blocksSent) {
        this.getProperties().put(KEY_FW_OPERATION_LAST, System.currentTimeMillis());
        this.getProperties().put(KEY_FW_BLOCKS_SENT, blocksSent);
    }

    public void clearFirmwareStatus() {
        this.getProperties().remove(KEY_FW_OPERATION_LAST);
        this.getProperties().remove(KEY_FW_OPERATION_FIRST);
        this.getProperties().remove(KEY_FW_OPERATION_LAST_DURATION);
        this.getProperties().remove(KEY_FW_BLOCKS_SENT);
        this.getProperties().remove(KEY_FW_BLOCKS_TOTAL);
    }
}