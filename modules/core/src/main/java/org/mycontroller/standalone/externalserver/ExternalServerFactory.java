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
package org.mycontroller.standalone.externalserver;

import java.util.HashMap;
import java.util.List;

import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.ExternalServerTable;
import org.mycontroller.standalone.externalserver.config.ExternalServerConfig;
import org.mycontroller.standalone.externalserver.config.ExternalServerConfigEmoncms;
import org.mycontroller.standalone.externalserver.config.ExternalServerConfigInfluxDB;
import org.mycontroller.standalone.externalserver.config.ExternalServerConfigMqtt;
import org.mycontroller.standalone.externalserver.config.ExternalServerConfigPhantIO;
import org.mycontroller.standalone.externalserver.config.ExternalServerConfigWUnderground;
import org.mycontroller.standalone.externalserver.driver.DriverEmoncms;
import org.mycontroller.standalone.externalserver.driver.DriverInfluxDB;
import org.mycontroller.standalone.externalserver.driver.DriverMQTT;
import org.mycontroller.standalone.externalserver.driver.DriverPhantIO;
import org.mycontroller.standalone.externalserver.driver.DriverWUnderground;
import org.mycontroller.standalone.externalserver.driver.IExternalServerDriver;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExternalServerFactory {
    private static final HashMap<Integer, IExternalServerDriver> _DRIVERS =
            new HashMap<Integer, IExternalServerDriver>();

    public enum EXTERNAL_SERVER_TYPE {
        PHANT_IO("Sparkfun [phant.io]"),
        EMONCMS("Emoncms.org"),
        INFLUXDB("Influxdb"),
        MQTT("MQTT"),
        WUNDERGROUND("WUnderground");
        public static EXTERNAL_SERVER_TYPE get(int id) {
            for (EXTERNAL_SERVER_TYPE type : values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

        private String value;

        private EXTERNAL_SERVER_TYPE(String value) {
            this.value = value;
        }

        public String getText() {
            return this.value;
        }

        public static EXTERNAL_SERVER_TYPE fromString(String text) {
            if (text != null) {
                for (EXTERNAL_SERVER_TYPE type : EXTERNAL_SERVER_TYPE.values()) {
                    if (text.equalsIgnoreCase(type.getText())) {
                        return type;
                    }
                }
            }
            return null;
        }
    }

    public static ExternalServerConfig getConfig(ExternalServerTable externalServerTable) {
        switch (externalServerTable.getType()) {
            case EMONCMS:
                return new ExternalServerConfigEmoncms(externalServerTable);
            case PHANT_IO:
                return new ExternalServerConfigPhantIO(externalServerTable);
            case INFLUXDB:
                return new ExternalServerConfigInfluxDB(externalServerTable);
            case MQTT:
                return new ExternalServerConfigMqtt(externalServerTable);
            case WUNDERGROUND:
                return new ExternalServerConfigWUnderground(externalServerTable);
            default:
                _logger.error("This type External Server not implemented!");
                break;
        }
        return null;
    }

    private static IExternalServerDriver getDriverByExtSerId(Integer extServerId) {
        try {
            ExternalServerTable extServerTable = DaoUtils.getExternalServerTableDao().getById(extServerId);
            if (extServerTable != null) {
                ExternalServerConfig _config = getConfig(extServerTable);
                switch (extServerTable.getType()) {
                    case EMONCMS:
                        return new DriverEmoncms((ExternalServerConfigEmoncms) _config);
                    case PHANT_IO:
                        return new DriverPhantIO((ExternalServerConfigPhantIO) _config);
                    case INFLUXDB:
                        return new DriverInfluxDB((ExternalServerConfigInfluxDB) _config);
                    case MQTT:
                        return new DriverMQTT((ExternalServerConfigMqtt) _config);
                    case WUNDERGROUND:
                        return new DriverWUnderground((ExternalServerConfigWUnderground) _config);
                    default:
                        _logger.error("This type driver not implemented yet.");
                        break;
                }
            }
        } catch (Exception ex) {
            _logger.error("Exception, ", ex);
        }
        return null;
    }

    public static IExternalServerDriver getDriver(Integer extServerId) {
        if (_DRIVERS.get(extServerId) == null) {
            IExternalServerDriver _driver = getDriverByExtSerId(extServerId);
            _driver.connect();
            _DRIVERS.put(extServerId, _driver);
        }
        return _DRIVERS.get(extServerId);
    }

    public static void removeDriver(Integer extServerId) {
        if (_DRIVERS.get(extServerId) != null) {
            IExternalServerDriver _driver = _DRIVERS.remove(extServerId);
            _driver.disconnect();
        }
    }

    public static synchronized void clearDrivers() {
        for (Integer key : _DRIVERS.keySet()) {
            removeDriver(key);
        }
    }

    public static void update(ExternalServerConfig externalServer) {
        removeDriver(externalServer.getId());
        DaoUtils.getExternalServerTableDao().update(externalServer.getExternalServerTable());
    }

    public static void add(ExternalServerConfig externalServer) {
        DaoUtils.getExternalServerTableDao().create(externalServer.getExternalServerTable());
    }

    public static void updateEnabled(List<Integer> ids, boolean enabled) {
        List<ExternalServerTable> externalServerTables = DaoUtils.getExternalServerTableDao().getAll(ids);
        for (ExternalServerTable externalServerTable : externalServerTables) {
            externalServerTable.setEnabled(enabled);
            removeDriver(externalServerTable.getId());
            DaoUtils.getExternalServerTableDao().update(externalServerTable);
        }
    }

    public static void delete(Integer extServerId) {
        removeDriver(extServerId);
        DaoUtils.getExternalServerResourceMapDao().deleteByExternalServerId(extServerId);
        DaoUtils.getExternalServerTableDao().deleteById(extServerId);
    }
}
