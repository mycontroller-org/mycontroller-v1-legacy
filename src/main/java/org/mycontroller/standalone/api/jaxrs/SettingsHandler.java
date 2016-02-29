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
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.mycontroller.standalone.MycUtils;
import org.mycontroller.standalone.ObjectFactory;
import org.mycontroller.standalone.AppProperties.MC_LANGUAGE;
import org.mycontroller.standalone.api.jaxrs.mapper.ApiError;
import org.mycontroller.standalone.api.jaxrs.utils.RestUtils;
import org.mycontroller.standalone.auth.AuthUtils;
import org.mycontroller.standalone.notification.PushbulletUtils;
import org.mycontroller.standalone.notification.SMSUtils;
import org.mycontroller.standalone.restclient.pushbullet.model.User;
import org.mycontroller.standalone.settings.EmailSettings;
import org.mycontroller.standalone.settings.LocationSettings;
import org.mycontroller.standalone.settings.MetricsDataRetentionSettings;
import org.mycontroller.standalone.settings.MetricsGraphSettings;
import org.mycontroller.standalone.settings.MyControllerSettings;
import org.mycontroller.standalone.settings.MySensorsSettings;
import org.mycontroller.standalone.settings.PushbulletSettings;
import org.mycontroller.standalone.settings.SettingsUtils;
import org.mycontroller.standalone.settings.SmsSettings;
import org.mycontroller.standalone.settings.UnitsSettings;
import org.mycontroller.standalone.settings.UserNativeSettings;
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
@RolesAllowed({ "admin" })
public class SettingsHandler extends AccessEngine {
    private static final Logger _logger = LoggerFactory.getLogger(SettingsHandler.class.getName());

    @RolesAllowed({ "User" })
    @GET
    @Path("/userSettings")
    public Response getUserNativeSettings() {
        return RestUtils.getResponse(Status.OK, UserNativeSettings.get(AuthUtils.getUser(securityContext)));
    }

    @RolesAllowed({ "User" })
    @POST
    @Path("/userSettings")
    public Response saveUserNativeSettings(UserNativeSettings userNativeSettings) {
        userNativeSettings.save(AuthUtils.getUser(securityContext));
        return RestUtils.getResponse(Status.OK);
    }

    @RolesAllowed({ "User" })
    @GET
    @Path("/location")
    public Response getLocation() {
        return RestUtils.getResponse(Status.OK, ObjectFactory.getAppProperties().getLocationSettings());
    }

    @POST
    @Path("/location")
    public Response saveLocation(LocationSettings locationSettings) {
        locationSettings.save();
        SettingsUtils.updateAllSettings();
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
        return RestUtils.getResponse(Status.OK, ObjectFactory.getAppProperties().getControllerSettings());
    }

    @POST
    @Path("/controller")
    public Response saveController(MyControllerSettings myControllerSettings) {
        myControllerSettings.save();
        SettingsUtils.updateAllSettings();
        //update locale
        MycUtils.updateLocale();
        //Update sunriuse and sun set timings
        try {
            TimerUtils.updateSunriseSunset();
        } catch (Exception ex) {
            return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError("Error: " + ex.getMessage()));
        }
        return RestUtils.getResponse(Status.OK);
    }

    @PUT
    @Path("/updateLanguage")
    public Response updateLanguage(String language) {
        if (language != null && MC_LANGUAGE.fromString(language) != null) {
            MyControllerSettings.builder().language(language).build().save();
            SettingsUtils.updateAllSettings();
            //update locale
            MycUtils.updateLocale();
            return RestUtils.getResponse(Status.OK);
        }
        return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError("Unknown language: " + language));
    }

    @GET
    @Path("/email")
    public Response getEmail() {
        return RestUtils.getResponse(Status.OK, ObjectFactory.getAppProperties().getEmailSettings());
    }

    @POST
    @Path("/email")
    public Response saveEmail(EmailSettings emailSettings) {
        emailSettings.save();
        SettingsUtils.updateAllSettings();
        return RestUtils.getResponse(Status.OK);
    }

    @GET
    @Path("/sms")
    public Response getSms() {
        return RestUtils.getResponse(Status.OK, ObjectFactory.getAppProperties().getSmsSettings());
    }

    @POST
    @Path("/sms")
    public Response saveSms(SmsSettings smsSettings) {
        smsSettings.save();
        SMSUtils.clearClients();//Clear clients when SMS updated
        SettingsUtils.updateAllSettings();
        //When update sms notification settings, clear clients
        SMSUtils.clearClients();
        return RestUtils.getResponse(Status.OK);
    }

    @GET
    @Path("/pushbullet")
    public Response getPushbullet() {
        return RestUtils.getResponse(Status.OK, ObjectFactory.getAppProperties().getPushbulletSettings());
    }

    @POST
    @Path("/pushbullet")
    public Response savePushbullet(PushbulletSettings pushbulletSettings) {
        try {
            pushbulletSettings.save();
            ObjectFactory.getAppProperties().setPushbulletSettings(PushbulletSettings.get());
            //Clear everything
            PushbulletSettings.builder()
                    .active(null)
                    .name(null)
                    .email(null)
                    .imageUrl(null)
                    .iden(null).build().updateInternal();
            ObjectFactory.getAppProperties().setPushbulletSettings(PushbulletSettings.get());
            PushbulletUtils.clearClient();//Clear client when updated

            User user = PushbulletUtils.getCurrentUser();
            PushbulletSettings.builder()
                    .active(user.getActive())
                    .name(user.getName())
                    .email(user.getEmail())
                    .imageUrl(user.getImageUrl())
                    .iden(user.getIden()).build().updateInternal();
            ObjectFactory.getAppProperties().setPushbulletSettings(PushbulletSettings.get());
            return RestUtils.getResponse(Status.OK);
        } catch (Exception ex) {
            return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError(ex.getMessage()));
        }
    }

    @GET
    @Path("/mySensors")
    public Response getMySensors() {
        return RestUtils.getResponse(Status.OK, ObjectFactory.getAppProperties().getMySensorsSettings());
    }

    @POST
    @Path("/mySensors")
    public Response saveMySensors(MySensorsSettings mySensorsSettings) {
        mySensorsSettings.save();
        SettingsUtils.updateAllSettings();
        return RestUtils.getResponse(Status.OK);
    }

    @GET
    @Path("/units")
    public Response getUnits() {
        return RestUtils.getResponse(Status.OK, ObjectFactory.getAppProperties().getUnitsSettings());
    }

    @POST
    @Path("/units")
    public Response saveUnits(UnitsSettings unitsSettings) {
        unitsSettings.save();
        SettingsUtils.updateAllSettings();
        return RestUtils.getResponse(Status.OK);
    }

    @GET
    @Path("/metricsGraph")
    public Response getMetrics() {
        return RestUtils.getResponse(Status.OK, ObjectFactory.getAppProperties().getMetricsGraphSettings());
    }

    @POST
    @Path("/metricsGraph")
    public Response saveMetrics(MetricsGraphSettings metricsGraphSettings) {
        metricsGraphSettings.save();
        SettingsUtils.updateAllSettings();
        return RestUtils.getResponse(Status.OK);
    }

    @GET
    @Path("/metricsRetention")
    public Response getMetricsRetention() {
        return RestUtils.getResponse(Status.OK, ObjectFactory.getAppProperties().getMetricsDataRetentionSettings());
    }

    @POST
    @Path("/metricsRetention")
    public Response saveMetricsRetention(MetricsDataRetentionSettings metricsDataRetentionSettings) {
        metricsDataRetentionSettings.save();
        SettingsUtils.updateAllSettings();
        return RestUtils.getResponse(Status.OK);
    }
}
