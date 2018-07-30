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
package org.mycontroller.standalone.api.jaxrs;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.api.BackupApi;
import org.mycontroller.standalone.api.jaxrs.model.ApiError;
import org.mycontroller.standalone.api.jaxrs.model.ApiMessage;
import org.mycontroller.standalone.api.jaxrs.model.Query;
import org.mycontroller.standalone.api.jaxrs.utils.RestUtils;
import org.mycontroller.standalone.settings.BackupSettings;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */

@Path("/rest/backup")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RolesAllowed({ "admin" })
@Slf4j
public class BackupHandler {

    private BackupApi backupApi = new BackupApi();

    @GET
    @Path("/backupFiles")
    public Response getBackupList(@QueryParam(BackupApi.KEY_NAME) List<String> name,
            @QueryParam(Query.PAGE_LIMIT) Long pageLimit,
            @QueryParam(Query.PAGE) Long page,
            @QueryParam(Query.ORDER_BY) String orderBy,
            @QueryParam(Query.ORDER) String order) {

        HashMap<String, Object> filters = new HashMap<String, Object>();
        filters.put(BackupApi.KEY_NAME, name);

        if (orderBy == null) {
            orderBy = BackupApi.KEY_NAME;
        }
        //Query primary filters
        filters.put(Query.ORDER, order);
        filters.put(Query.ORDER_BY, orderBy);
        filters.put(Query.PAGE_LIMIT, pageLimit);
        filters.put(Query.PAGE, page);

        try {
            return RestUtils.getResponse(Status.OK, backupApi.getBackupFiles(filters));
        } catch (Exception ex) {
            _logger.error("Error,", ex);
            return RestUtils.getResponse(Status.INTERNAL_SERVER_ERROR, new ApiError(ex.getMessage()));
        }
    }

    @GET
    @Path("/backupSettings")
    public Response getBackupSettings() {
        return RestUtils.getResponse(Status.OK, AppProperties.getInstance().getBackupSettings());
    }

    @PUT
    @Path("/backupSettings")
    public Response updateBackupSettings(BackupSettings backupSettings) {
        backupSettings.save();
        BackupSettings.reloadJob();//Reload backup job
        AppProperties.getInstance().setBackupSettings(BackupSettings.get());
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
    public Response deleteIds(List<String> backupFiles) {
        try {
            backupApi.deleteBackupFiles(backupFiles);
        } catch (IOException ex) {
            RestUtils.getResponse(Status.BAD_REQUEST, new ApiError(ex.getMessage()));
        }
        return RestUtils.getResponse(Status.NO_CONTENT);
    }

    @POST
    @Path("/restore")
    public Response restore(String backupFile) {
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
