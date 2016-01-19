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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.mycontroller.standalone.ObjectFactory;
import org.mycontroller.standalone.api.jaxrs.mapper.ApiError;
import org.mycontroller.standalone.api.jaxrs.utils.RestUtils;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.Settings;
import org.mycontroller.standalone.scheduler.SchedulerUtils;
import org.mycontroller.standalone.settings.SettingsUtils;
import org.mycontroller.standalone.timer.TimerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */

@Path("/rest/usersettings")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RolesAllowed({ "user" })
public class UserSettingsHandler {
    private static final Logger _logger = LoggerFactory.getLogger(UserSettingsHandler.class.getName());
    /*
        @PUT
        @Path("/")
        public Response updateSettings(Settings settings) {
            Settings settingsOld = DaoUtils.getSettingsDao().get(settings.getKey());
            if (settingsOld.getUserEditable()) {
                if (settings.getKey().equals(Settings.CITY_LONGITUDE) || settings.getKey().equals(Settings.CITY_LATITUDE)) {
                    DaoUtils.getSettingsDao().update(settings);
                    try {
                        TimerUtils.updateSunriseSunset();
                    } catch (Exception ex) {
                        return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError(ex.getMessage()));
                    }
                } else if (settings.getKey().equals(Settings.MC_LANGUAGE)) {
                    DaoUtils.getSettingsDao().update(settings);
                    ObjectFactory.getAppProperties().updatePropertiesFromDb();
                } else if (settings.getKey().equals(Settings.MYS_HEARTBEAT_INTERVAL)) {
                    try {
                        long interval = Long.valueOf(settings.getValue());
                        if (interval > 0) {
                            DaoUtils.getSettingsDao().update(settings);
                            SchedulerUtils.reloadMySensorHearbeatJob();
                        } else {
                            return RestUtils.getResponse(Status.NOT_ACCEPTABLE, new ApiError(
                                    "Heartbeat interval should not be <= zero"));
                        }
                    } catch (Exception ex) {
                        _logger.debug("Error,", ex);
                        return RestUtils.getResponse(
                                Status.NOT_ACCEPTABLE,
                                new ApiError("Invalid Heartbeat interval! value:["
                                        + settings.getValue() + "], Error:[" + ex.getMessage() + "]"));
                    }
                } else {
                    DaoUtils.getSettingsDao().update(settings);
                }
                return RestUtils.getResponse(Status.OK);
            } else {
                return RestUtils.getResponse(Status.NOT_ACCEPTABLE, new ApiError("'" + settings.getFrindlyName()
                        + "' is not a user editable field!"));
            }

        }

        @GET
        @Path("/sunriseSunset")
        public Response getSunRiseSunSet() {
            List<Settings> settings = SettingsUtils.getSunRiseSet();
            for (Settings setting : settings) {
                if (setting.getKey().equals(Settings.SUNRISE_TIME) || setting.getKey().equals(Settings.SUNSET_TIME)) {
                    if (setting.getValue() != null) {
                        setting.setValue(new SimpleDateFormat(ObjectFactory.getAppProperties()
                                .getJavaDateWithoutSecondsFormat()).format(new Date(Long
                                .valueOf(setting.getValue()))));
                    }
                } else if (setting.getKey().equals(Settings.DEFAULT_FIRMWARE)) {
                    if (setting.getValue() != null) {
                        setting.setValue(DaoUtils.getFirmwareDao().getById(Integer.valueOf(setting.getValue()))
                                .getFirmwareName());
                    }
                }
            }
            return RestUtils.getResponse(Status.OK, settings);
        }

        @GET
        @Path("/nodeDefaults")
        public Response getNodeDefaults() {
            return RestUtils.getResponse(Status.OK, SettingsUtils.getNodeDefaults());
        }

        @GET
        @Path("/email")
        public Response getEmailSettings() {
            return RestUtils.getResponse(Status.OK, SettingsUtils.getEmailSettings());
        }

        @GET
        @Path("/sms")
        public Response getSMSSettings() {
            return RestUtils.getResponse(Status.OK, SettingsUtils.getSMSSettings());
        }

        @GET
        @Path("/version")
        public Response getVersion() {
            return RestUtils.getResponse(Status.OK, SettingsUtils.getVersionInfo());
        }

        @GET
        @Path("/units")
        public Response getUnits() {
            return RestUtils.getResponse(Status.OK, SettingsUtils.getDisplayUnits());
        }

        @GET
        @Path("/graph")
        public Response getGraph() {
            return RestUtils.getResponse(Status.OK, SettingsUtils.getGraphSettings());
        }

        @GET
        @Path("/settings/{key}")
        public Response getSettings(@PathParam("key") String key) {
            Settings settings = DaoUtils.getSettingsDao().get(key);
            return RestUtils.getResponse(Status.OK, settings);
        }
        */

}
