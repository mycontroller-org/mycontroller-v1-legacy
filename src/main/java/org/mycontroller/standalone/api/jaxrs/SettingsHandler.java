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
package org.mycontroller.standalone.api.jaxrs;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.mycontroller.standalone.ObjectFactory;
import org.mycontroller.standalone.api.jaxrs.mapper.ApiError;
import org.mycontroller.standalone.api.jaxrs.utils.RestUtils;
import org.mycontroller.standalone.settings.EmailSettings;
import org.mycontroller.standalone.settings.LocationSettings;
import org.mycontroller.standalone.settings.MyControllerSettings;
import org.mycontroller.standalone.settings.MySensorsSettings;
import org.mycontroller.standalone.settings.SmsSettings;
import org.mycontroller.standalone.settings.UnitsSettings;
import org.mycontroller.standalone.timer.TimerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */

@Path("/rest/settings")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RolesAllowed({ "user" })
public class SettingsHandler {
    private static final Logger _logger = LoggerFactory.getLogger(SettingsHandler.class.getName());

    @GET
    @Path("/location")
    public Response getLocation() {
        return RestUtils.getResponse(Status.OK, LocationSettings.get());
    }

    @POST
    @Path("/location")
    public Response saveLocation(LocationSettings locationSettings) {
        locationSettings.save();
        ObjectFactory.getAppProperties().loadPropertiesFromDb();
        try {
            TimerUtils.updateSunriseSunset();
        } catch (Exception ex) {
            _logger.error("Exception,", ex);
            return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError(ex.getMessage()));
        }
        return RestUtils.getResponse(Status.OK);
    }

    @GET
    @Path("/controller")
    public Response getController() {
        return RestUtils.getResponse(Status.OK, MyControllerSettings.get());
    }

    @POST
    @Path("/controller")
    public Response saveController(MyControllerSettings myControllerSettings) {
        myControllerSettings.save();
        ObjectFactory.getAppProperties().loadPropertiesFromDb();
        return RestUtils.getResponse(Status.OK);
    }

    @GET
    @Path("/email")
    public Response getEmail() {
        return RestUtils.getResponse(Status.OK, EmailSettings.get());
    }

    @POST
    @Path("/email")
    public Response saveEmail(EmailSettings emailSettings) {
        emailSettings.save();
        ObjectFactory.getAppProperties().loadPropertiesFromDb();
        return RestUtils.getResponse(Status.OK);
    }

    @GET
    @Path("/sms")
    public Response getSms() {
        return RestUtils.getResponse(Status.OK, SmsSettings.get());
    }

    @POST
    @Path("/sms")
    public Response saveSms(SmsSettings smsSettings) {
        smsSettings.save();
        ObjectFactory.getAppProperties().loadPropertiesFromDb();
        return RestUtils.getResponse(Status.OK);
    }

    @GET
    @Path("/mySensors")
    public Response getMySensors() {
        return RestUtils.getResponse(Status.OK, MySensorsSettings.get());
    }

    @POST
    @Path("/mySensors")
    public Response saveMySensors(MySensorsSettings mySensorsSettings) {
        mySensorsSettings.save();
        ObjectFactory.getAppProperties().loadPropertiesFromDb();
        return RestUtils.getResponse(Status.OK);
    }

    @GET
    @Path("/units")
    public Response getUnits() {
        return RestUtils.getResponse(Status.OK, UnitsSettings.get());
    }

    @POST
    @Path("/units")
    public Response saveUnits(UnitsSettings unitsSettings) {
        unitsSettings.save();
        ObjectFactory.getAppProperties().loadPropertiesFromDb();
        return RestUtils.getResponse(Status.OK);
    }

    //TODO: refer all the above only from Object factory

}
