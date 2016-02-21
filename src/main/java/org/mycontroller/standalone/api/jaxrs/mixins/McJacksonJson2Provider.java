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
package org.mycontroller.standalone.api.jaxrs.mixins;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.plugins.providers.jackson.ResteasyJackson2Provider;
import org.mycontroller.standalone.db.tables.AlarmDefinition;
import org.mycontroller.standalone.db.tables.ForwardPayload;
import org.mycontroller.standalone.db.tables.Gateway;
import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.db.tables.Notification;
import org.mycontroller.standalone.db.tables.ResourcesGroup;
import org.mycontroller.standalone.db.tables.ResourcesGroupMap;
import org.mycontroller.standalone.db.tables.ResourcesLogs;
import org.mycontroller.standalone.db.tables.Role;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.db.tables.Timer;
import org.mycontroller.standalone.db.tables.User;
import org.mycontroller.standalone.settings.Dashboard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class McJacksonJson2Provider extends ResteasyJackson2Provider {
    private static final Logger _logger = LoggerFactory.getLogger(McJacksonJson2Provider.class);

    @Override
    public void writeTo(Object value, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException {
        ObjectMapper mapper = locateMapper(type, mediaType);
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true); // this creates a 'configured' mapper
        mapper.setSerializationInclusion(Include.NON_NULL);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        //AddMixIns
        mapper.addMixIn(AlarmDefinition.class, AlarmDefinitionMixin.class);
        mapper.addMixIn(Dashboard.class, DashboardMixin.class);
        mapper.addMixIn(ForwardPayload.class, ForwardPayloadMixin.class);
        mapper.addMixIn(Gateway.class, GatewayMixin.class);
        mapper.addMixIn(Node.class, NodeMixin.class);
        mapper.addMixIn(Notification.class, NotificationMixin.class);
        mapper.addMixIn(ResourcesGroup.class, ResourcesGroupMixin.class);
        mapper.addMixIn(ResourcesGroupMap.class, ResourcesGroupMapMixin.class);
        mapper.addMixIn(ResourcesLogs.class, ResourcesLogsMixin.class);
        mapper.addMixIn(Role.class, RoleMixin.class);
        mapper.addMixIn(Sensor.class, SensorMixin.class);
        mapper.addMixIn(Timer.class, TimerMixin.class);
        mapper.addMixIn(User.class, UserMixin.class);

        _logger.debug("Request: Headers:{}", httpHeaders);
        super.writeTo(value, type, genericType, annotations, mediaType, httpHeaders, entityStream);
    }

    @Override
    public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException {
        ObjectMapper mapper = locateMapper(type, mediaType);

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        //AddMixIns
        mapper.addMixIn(AlarmDefinition.class, AlarmDefinitionMixin.class);
        mapper.addMixIn(Dashboard.class, DashboardMixin.class);
        mapper.addMixIn(ForwardPayload.class, ForwardPayloadMixin.class);
        mapper.addMixIn(Gateway.class, GatewayMixin.class);
        mapper.addMixIn(Node.class, NodeMixin.class);
        mapper.addMixIn(Notification.class, NotificationMixin.class);
        mapper.addMixIn(ResourcesGroup.class, ResourcesGroupMixin.class);
        mapper.addMixIn(ResourcesGroupMap.class, ResourcesGroupMapMixin.class);
        mapper.addMixIn(Role.class, RoleMixin.class);
        mapper.addMixIn(Sensor.class, SensorMixin.class);
        mapper.addMixIn(Timer.class, TimerMixin.class);
        mapper.addMixIn(User.class, UserMixin.class);

        _logger.debug("Request: Headers:{}", httpHeaders);

        return super.readFrom(type, genericType, annotations, mediaType, httpHeaders, entityStream);
    }

}
