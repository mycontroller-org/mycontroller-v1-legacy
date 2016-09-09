/*
 * Copyright 2015-2016 Jeeva Kandasamy (jkandasa@gmail.com)
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
package org.mycontroller.standalone.api.jaxrs.mixins;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.plugins.providers.jackson.ResteasyJackson2Provider;
import org.mycontroller.standalone.api.jaxrs.json.SensorVariableJson;
import org.mycontroller.standalone.api.jaxrs.json.SensorVariablePurge;
import org.mycontroller.standalone.db.tables.ExternalServerTable;
import org.mycontroller.standalone.db.tables.Firmware;
import org.mycontroller.standalone.db.tables.ForwardPayload;
import org.mycontroller.standalone.db.tables.GatewayTable;
import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.db.tables.OperationTable;
import org.mycontroller.standalone.db.tables.Resource;
import org.mycontroller.standalone.db.tables.ResourcesGroup;
import org.mycontroller.standalone.db.tables.ResourcesGroupMap;
import org.mycontroller.standalone.db.tables.ResourcesLogs;
import org.mycontroller.standalone.db.tables.Role;
import org.mycontroller.standalone.db.tables.RuleDefinitionTable;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.db.tables.SensorVariable;
import org.mycontroller.standalone.db.tables.Timer;
import org.mycontroller.standalone.db.tables.UidTag;
import org.mycontroller.standalone.db.tables.User;
import org.mycontroller.standalone.exernalserver.model.ExternalServer;
import org.mycontroller.standalone.gateway.model.Gateway;
import org.mycontroller.standalone.message.McMessage;
import org.mycontroller.standalone.operation.model.Operation;
import org.mycontroller.standalone.rule.model.RuleDefinition;
import org.mycontroller.standalone.scripts.McScript;
import org.mycontroller.standalone.settings.Dashboard;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Slf4j
public class McJacksonJson2Provider extends ResteasyJackson2Provider {

    @Context
    HttpHeaders headers;

    @Override
    public void writeTo(Object value, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException {
        ObjectMapper mapper = locateMapper(type, mediaType);
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true); // this creates a 'configured' mapper
        mapper.setSerializationInclusion(Include.NON_NULL);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        //AddMixIns
        mapper.addMixIn(Dashboard.class, DashboardMixin.class);
        mapper.addMixIn(ExternalServerTable.class, ExternalServerMixin.class);
        mapper.addMixIn(Firmware.class, FirmwareMixin.class);
        mapper.addMixIn(ForwardPayload.class, ForwardPayloadMixin.class);
        mapper.addMixIn(GatewayTable.class, GatewayMixin.class);
        mapper.addMixIn(McScript.class, McScriptMixin.class);
        mapper.addMixIn(Node.class, NodeMixin.class);
        mapper.addMixIn(OperationTable.class, OperationMixin.class);
        mapper.addMixIn(Resource.class, ResourceMixin.class);
        mapper.addMixIn(ResourcesGroup.class, ResourcesGroupMixin.class);
        mapper.addMixIn(ResourcesGroupMap.class, ResourcesGroupMapMixin.class);
        mapper.addMixIn(ResourcesLogs.class, ResourcesLogsMixin.class);
        mapper.addMixIn(Role.class, RoleMixin.class);
        mapper.addMixIn(RuleDefinitionTable.class, RuleDefinitionMixin.class);
        mapper.addMixIn(Sensor.class, SensorMixin.class);
        mapper.addMixIn(SensorVariable.class, SensorVariableMixin.class);
        mapper.addMixIn(SensorVariableJson.class, SensorVariableJsonMixin.class);
        mapper.addMixIn(Timer.class, TimerMixin.class);
        mapper.addMixIn(User.class, UserMixin.class);

        if (_logger.isDebugEnabled()) {
            _logger.debug("Response: Headers:{}", httpHeaders);
            _logger.debug("Response: Value:{}", value);
            _logger.debug("Request headers:{}", headers.getRequestHeaders());
        }

        super.writeTo(value, type, genericType, annotations, mediaType, httpHeaders, entityStream);
    }

    @Override
    public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException {
        ObjectMapper mapper = locateMapper(type, mediaType);

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        //AddMixIns
        mapper.addMixIn(Dashboard.class, DashboardMixin.class);
        mapper.addMixIn(ExternalServer.class, ExternalServerMixin.class);
        mapper.addMixIn(ForwardPayload.class, ForwardPayloadMixin.class);
        mapper.addMixIn(Gateway.class, GatewayMixin.class);
        mapper.addMixIn(McMessage.class, McMessageMixin.class);
        mapper.addMixIn(McScript.class, McScriptMixin.class);
        mapper.addMixIn(Node.class, NodeMixin.class);
        mapper.addMixIn(Operation.class, OperationMixin.class);
        mapper.addMixIn(Resource.class, ResourceMixin.class);
        mapper.addMixIn(ResourcesGroup.class, ResourcesGroupMixin.class);
        mapper.addMixIn(ResourcesGroupMap.class, ResourcesGroupMapMixin.class);
        mapper.addMixIn(ResourcesLogs.class, ResourcesLogsMixin.class);
        mapper.addMixIn(Role.class, RoleMixin.class);
        mapper.addMixIn(RuleDefinition.class, RuleDefinitionMixin.class);
        mapper.addMixIn(Sensor.class, SensorMixin.class);
        mapper.addMixIn(SensorVariableJson.class, SensorVariableJsonMixin.class);
        mapper.addMixIn(SensorVariablePurge.class, SensorVariablePurgeMixin.class);
        mapper.addMixIn(Timer.class, TimerMixin.class);
        mapper.addMixIn(UidTag.class, UidTagMixin.class);
        mapper.addMixIn(User.class, UserMixin.class);

        _logger.debug("Request: Headers:{}", httpHeaders);

        return super.readFrom(type, genericType, annotations, mediaType, httpHeaders, entityStream);
    }

}
