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
package org.mycontroller.standalone.api.jaxrs;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.mycontroller.standalone.McObjectManager;
import org.mycontroller.standalone.api.BackupApi;
import org.mycontroller.standalone.api.jaxrs.json.ApiError;
import org.mycontroller.standalone.api.jaxrs.json.ApiMessage;
import org.mycontroller.standalone.api.jaxrs.json.BackupFile;
import org.mycontroller.standalone.api.jaxrs.utils.RestUtils;
import org.mycontroller.standalone.settings.BackupSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */

@Path("/rest/backup")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RolesAllowed({ "admin" })
public class BackupHandler {
    private static final Logger _logger = LoggerFactory.getLogger(BackupHandler.class.getName());

    private BackupApi backupApi = new BackupApi();

    @GET
    @Path("/backupList")
    public Response getBackupList() {
        try {
            return RestUtils.getResponse(Status.OK, backupApi.getBackupList());
        } catch (Exception ex) {
            _logger.error("Error,", ex);
            return RestUtils.getResponse(Status.INTERNAL_SERVER_ERROR, new ApiError(ex.getMessage()));
        }

    }

    @GET
    @Path("/backupSettings")
    public Response getBackupSettings() {
        return RestUtils.getResponse(Status.OK, McObjectManager.getAppProperties().getBackupSettings());
    }

    @PUT
    @Path("/backupSettings")
    public Response updateBackupSettings(BackupSettings backupSettings) {
        backupSettings.save();
        BackupSettings.reloadJob();//Reload backup job
        McObjectManager.getAppProperties().setBackupSettings(BackupSettings.get());
        return RestUtils.getResponse(Status.OK);
    }

    @PUT
    @Path("/backupNow")
    public Response backupNow() {
        try {
            return RestUtils.getResponse(Status.OK, new ApiMessage(backupApi.backupNow()));
        } catch (Exception ex) {
            _logger.error("Error,", ex);
            return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError(ex.getMessage()));
        }
    }

    @POST
    @Path("/delete")
    public Response deleteBackup(BackupFile backupFile) {
        try {
            backupApi.deleteBackup(backupFile);
            return RestUtils.getResponse(Status.OK);
        } catch (Exception ex) {
            return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError(ex.getMessage()));
        }
    }

    @POST
    @Path("/restore")
    public Response restore(BackupFile backupFile) {
        try {
            backupApi.restore(backupFile);
            return RestUtils.getResponse(Status.OK, new ApiMessage(
                    "Server is going to down now! Monitor log file of the server."));
        } catch (Exception ex) {
            _logger.error("Error in restore,", ex);
            return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError(ex.getMessage()));
        }
    }
}
