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
package org.mycontroller.standalone.provider;

import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.ResourcesLogsUtils.LOG_DIRECTION;
import org.mycontroller.standalone.db.ResourcesLogsUtils.LOG_LEVEL;
import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.db.tables.ResourcesLogs;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.message.IMessage;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_SET_REQ;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_STREAM;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.2.0
 */
@Slf4j
public class ResourcesLogger implements Runnable {

    private static final int MAXIMUN_PAYLOAD_SIZE = 20;
    private IMessage _message = null;
    private RESOURCE_TYPE rType = null;
    private LOG_DIRECTION logDirection = null;

    public ResourcesLogger(IMessage _message) {
        this._message = _message;
    }

    private boolean isInPermittedLogLevel(LOG_LEVEL logLevel) {
        if (LOG_LEVEL.fromString(AppProperties.getInstance().getControllerSettings().getResourcesLogLevel())
                .ordinal() <= logLevel.ordinal()) {
            return true;
        }
        return false;
    }

    private Integer getResourceId() {
        switch (rType) {
            case GATEWAY:
                return _message.getGatewayId();
            case NODE:
                Node _node = DaoUtils.getNodeDao().get(_message.getGatewayId(), _message.getNodeEui());
                return _node.getId();
            case SENSOR:
                _node = DaoUtils.getNodeDao().get(_message.getGatewayId(), _message.getNodeEui());
                Sensor _s = DaoUtils.getSensorDao().get(_node.getId(), _message.getSensorId());
                return _s.getId();
            case SENSOR_VARIABLE:
                _node = DaoUtils.getNodeDao().get(_message.getGatewayId(), _message.getNodeEui());
                _s = DaoUtils.getSensorDao().get(_node.getId(), _message.getSensorId());
                SensorVariable _sv = DaoUtils.getSensorVariableDao().get(_s.getId(),
                        MESSAGE_TYPE_SET_REQ.fromString(_message.getSubType()));
                return _sv.getId();
            default:
                return null;

        }
    }

    private void log() {
        if (!isInPermittedLogLevel(LOG_LEVEL.INFO)) {
            return;
        }
        // update resource type
        if (_message.getSensorId().equalsIgnoreCase(IMessage.SENSOR_BROADCAST_ID)) {
            if (_message.getNodeEui().equalsIgnoreCase(IMessage.NODE_BROADCAST_ID)) {
                rType = RESOURCE_TYPE.GATEWAY;
            } else {
                rType = RESOURCE_TYPE.NODE;
            }
        } else if (_message.getType().equalsIgnoreCase(MESSAGE_TYPE.C_PRESENTATION.getText())) {
            rType = RESOURCE_TYPE.SENSOR;
        } else {
            rType = RESOURCE_TYPE.SENSOR_VARIABLE;
        }

        switch (MESSAGE_TYPE.fromString(_message.getType())) {
            case C_INTERNAL:
                if (!isInPermittedLogLevel(LOG_LEVEL.NOTICE)) {
                    return;
                }
                internal();
                break;
            case C_PRESENTATION:
                if (!isInPermittedLogLevel(LOG_LEVEL.NOTICE)) {
                    return;
                }
                presentation();
                break;
            case C_REQ:
                if (!isInPermittedLogLevel(LOG_LEVEL.INFO)) {
                    return;
                }
                request();
                break;
            case C_SET:
                if (!isInPermittedLogLevel(LOG_LEVEL.INFO)) {
                    return;
                }
                set();
                break;
            case C_STREAM:
                if (!isInPermittedLogLevel(LOG_LEVEL.NOTICE)) {
                    return;
                }
                stream();
                break;
            default:
                break;
        }
    }

    private void internal() {
        doLog(MESSAGE_TYPE.C_INTERNAL, LOG_LEVEL.NOTICE);
    }

    private void presentation() {
        doLog(MESSAGE_TYPE.C_PRESENTATION, LOG_LEVEL.NOTICE);
    }

    private void request() {
        doLog(MESSAGE_TYPE.C_REQ, LOG_LEVEL.INFO);
    }

    private void set() {
        doLog(MESSAGE_TYPE.C_SET, LOG_LEVEL.INFO);
    }

    private void stream() {
        switch (MESSAGE_TYPE_STREAM.fromString(_message.getSubType())) {
            case ST_FIRMWARE_CONFIG_REQUEST:
            case ST_FIRMWARE_CONFIG_RESPONSE:
                doLog(MESSAGE_TYPE.C_STREAM, LOG_LEVEL.NOTICE);
                break;
            case ST_FIRMWARE_REQUEST:
            case ST_FIRMWARE_RESPONSE:
                if (!isInPermittedLogLevel(LOG_LEVEL.TRACE)) {
                    return;
                }
                doLog(MESSAGE_TYPE.C_STREAM, LOG_LEVEL.TRACE);
                break;
            case ST_IMAGE:
            case ST_SOUND:
                // not supported at this moment
                return;
            default:
                break;

        }
    }

    private void doLog(MESSAGE_TYPE type, LOG_LEVEL logLevel) {
        StringBuilder _builder = new StringBuilder();
        _builder.append("[").append(_message.getSubType()).append("] ");
        if (type == MESSAGE_TYPE.C_STREAM
                && _message.getSubType().equalsIgnoreCase(MESSAGE_TYPE_STREAM.ST_FIRMWARE_RESPONSE.getText())) {
            if (_message.getPayload().length() > MAXIMUN_PAYLOAD_SIZE) {
                _builder.append(_message.getPayload().substring(0, MAXIMUN_PAYLOAD_SIZE - 3)).append("...");
            } else {
                _builder.append(_message.getPayload());
            }
        } else {
            _builder.append(_message.getPayload());
        }

        ResourcesLogs resLog = ResourcesLogs.builder()
                .resourceType(rType)
                .resourceId(getResourceId())
                .logDirection(logDirection)
                .logLevel(logLevel)
                .messageType(type)
                .message(_builder.toString())
                .timestamp(_message.getTimestamp())
                .logDirection(_message.isTxMessage() ? LOG_DIRECTION.SENT : LOG_DIRECTION.RECEIVED)
                .build();
        DaoUtils.getResourcesLogsDao().add(resLog);
    }

    @Override
    public void run() {
        try {
            log();
        } catch (Exception ex) {
            _logger.error("Error on {},", _message, ex);
        }
    }

}
