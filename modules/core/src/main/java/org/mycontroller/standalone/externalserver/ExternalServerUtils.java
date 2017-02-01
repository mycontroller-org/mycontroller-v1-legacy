/*
 * Copyright 2015-2017 Jeeva Kandasamy (jkandasa@gmail.com)
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
import org.mycontroller.standalone.exernalserver.model.ExternalServer;
import org.mycontroller.standalone.exernalserver.model.ExternalServerEmoncms;
import org.mycontroller.standalone.exernalserver.model.ExternalServerInfluxdb;
import org.mycontroller.standalone.exernalserver.model.ExternalServerMqtt;
import org.mycontroller.standalone.exernalserver.model.ExternalServerPhantIO;
import org.mycontroller.standalone.exernalserver.model.ExternalServerWUnderground;
import org.mycontroller.standalone.restclient.emoncms.EmoncmsClientImpl;
import org.mycontroller.standalone.restclient.influxdb.InfluxdbClientImpl;
import org.mycontroller.standalone.restclient.phantio.PhantIOClientImpl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExternalServerUtils {
    private static final HashMap<Integer, Object> EXTERNAL_SERVER_CLIENTS = new HashMap<Integer, Object>();

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

    public static ExternalServer getExternalServer(ExternalServerTable externalServerTable) {
        switch (externalServerTable.getType()) {
            case EMONCMS:
                return new ExternalServerEmoncms(externalServerTable);
            case PHANT_IO:
                return new ExternalServerPhantIO(externalServerTable);
            case INFLUXDB:
                return new ExternalServerInfluxdb(externalServerTable);
            case MQTT:
                return new ExternalServerMqtt(externalServerTable);
            case WUNDERGROUND:
                return new ExternalServerWUnderground(externalServerTable);
            default:
                _logger.error("This type External Server not implemented!");
                break;
        }
        return null;
    }

    private static Object getClientByExtSerId(Integer extServerId) {
        try {
            ExternalServerTable extServerTable = DaoUtils.getExternalServerTableDao().getById(extServerId);
            if (extServerTable != null) {
                ExternalServer externalServer = getExternalServer(extServerTable);
                switch (extServerTable.getType()) {
                    case EMONCMS:
                        ExternalServerEmoncms emoncmsServer = (ExternalServerEmoncms) externalServer;
                        return new EmoncmsClientImpl(emoncmsServer.getUrl(), emoncmsServer.getWriteApiKey(),
                                emoncmsServer.getTrustHostType());
                    case PHANT_IO:
                        ExternalServerPhantIO phantIOServer = (ExternalServerPhantIO) externalServer;
                        return new PhantIOClientImpl(phantIOServer.getUrl(), phantIOServer.getPublicKey(),
                                phantIOServer.getPrivateKey(), phantIOServer.getTrustHostType());
                    case INFLUXDB:
                        ExternalServerInfluxdb influxdbServer = (ExternalServerInfluxdb) externalServer;
                        if (influxdbServer.getUsername() != null && influxdbServer.getUsername().length() > 0) {
                            return new InfluxdbClientImpl(influxdbServer.getUrl(), influxdbServer.getUsername(),
                                    influxdbServer.getPassword(), influxdbServer.getDatabase(),
                                    influxdbServer.getTrustHostType());
                        } else {
                            return new InfluxdbClientImpl(influxdbServer.getUrl(),
                                    influxdbServer.getDatabase(),
                                    influxdbServer.getTrustHostType());
                        }
                    case MQTT:
                        ExternalServerMqtt mqttClient = (ExternalServerMqtt) externalServer;
                        return new ExternalMqttClient(
                                mqttClient.getUrl(),
                                mqttClient.getName(),
                                mqttClient.getUsername(),
                                mqttClient.getPassword(),
                                mqttClient.getTrustHostType());
                    default:
                        _logger.error("This type rest client not implemented yet.");
                        break;
                }
            }
        } catch (Exception ex) {
            _logger.error("Exception, ", ex);
        }
        return null;
    }

    public static Object getClient(Integer extServerId) {
        if (EXTERNAL_SERVER_CLIENTS.get(extServerId) == null) {
            EXTERNAL_SERVER_CLIENTS.put(extServerId, getClientByExtSerId(extServerId));
        }
        return EXTERNAL_SERVER_CLIENTS.get(extServerId);
    }

    public static void removeRestClient(Integer extServerId) {
        if (EXTERNAL_SERVER_CLIENTS.get(extServerId) != null) {
            ExternalServerTable extServer = DaoUtils.getExternalServerTableDao().getById(extServerId);
            if (extServer.getType() == EXTERNAL_SERVER_TYPE.MQTT) {
                ExternalMqttClient client = (ExternalMqttClient) EXTERNAL_SERVER_CLIENTS.get(extServerId);
                client.disconnect();
            }
            EXTERNAL_SERVER_CLIENTS.put(extServerId, null);
        }
    }

    public static synchronized void clearServers() {
        for (Integer key : EXTERNAL_SERVER_CLIENTS.keySet()) {
            removeRestClient(key);
        }
    }

    public static void update(ExternalServer externalServer) {
        removeRestClient(externalServer.getId());
        DaoUtils.getExternalServerTableDao().update(externalServer.getExternalServerTable());
    }

    public static void add(ExternalServer externalServer) {
        DaoUtils.getExternalServerTableDao().create(externalServer.getExternalServerTable());
    }

    public static void updateEnabled(List<Integer> ids, boolean enabled) {
        List<ExternalServerTable> externalServerTables = DaoUtils.getExternalServerTableDao().getAll(ids);
        for (ExternalServerTable externalServerTable : externalServerTables) {
            externalServerTable.setEnabled(enabled);
            removeRestClient(externalServerTable.getId());
            DaoUtils.getExternalServerTableDao().update(externalServerTable);
        }
    }

    public static void delete(Integer extServerId) {
        removeRestClient(extServerId);
        DaoUtils.getExternalServerResourceMapDao().deleteByExternalServerId(extServerId);
        DaoUtils.getExternalServerTableDao().deleteById(extServerId);
    }
}
