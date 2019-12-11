/*
 * Copyright 2015-2019 Jeeva Kandasamy (jkandasa@gmail.com)
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
import java.util.Map;

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
import org.mycontroller.standalone.api.ExportApi;
import org.mycontroller.standalone.api.jaxrs.model.ApiError;
import org.mycontroller.standalone.api.jaxrs.model.ApiMessage;
import org.mycontroller.standalone.api.jaxrs.model.Query;
import org.mycontroller.standalone.api.jaxrs.utils.RestUtils;
import org.mycontroller.standalone.backup.McFileUtils;
import org.mycontroller.standalone.settings.ExportSettings;
import org.mycontroller.standalone.utils.McUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.5.0
 */

@Slf4j
@Path("/rest/export")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RolesAllowed({ "Admin" })
public class ExportHandler extends AccessEngine {

    private ExportApi exportApi = new ExportApi();

    @GET
    @Path("/files")
    public Response getBackupList(@QueryParam(McFileUtils.KEY_NAME) List<String> name,
            @QueryParam(Query.PAGE_LIMIT) Long pageLimit,
            @QueryParam(Query.PAGE) Long page,
            @QueryParam(Query.ORDER_BY) String orderBy,
            @QueryParam(Query.ORDER) String order) {

        HashMap<String, Object> filters = new HashMap<String, Object>();
        filters.put(McFileUtils.KEY_NAME, name);

        if (orderBy == null) {
            orderBy = McFileUtils.KEY_NAME;
        }
        //Query primary filters
        filters.put(Query.ORDER, order);
        filters.put(Query.ORDER_BY, orderBy);
        filters.put(Query.PAGE_LIMIT, pageLimit);
        filters.put(Query.PAGE, page);

        try {
            return RestUtils.getResponse(Status.OK, exportApi.getExportFiles(filters));
        } catch (Exception ex) {
            _logger.error("Error,", ex);
            return RestUtils.getResponse(Status.INTERNAL_SERVER_ERROR, new ApiError(ex.getMessage()));
        }
    }

    @GET
    @Path("/settings")
    public Response getSettings() {
        return RestUtils.getResponse(Status.OK, AppProperties.getInstance().getExportSettings());
    }

    @PUT
    @Path("/settings")
    public Response updateBackupSettings(ExportSettings settings) {
        settings.save();
        ExportSettings.reloadJob();//Reload backup job
        AppProperties.getInstance().setExportSettings(ExportSettings.get());
        return RestUtils.getResponse(Status.OK);
    }

    @PUT
    @Path("/exportNow")
    public Response exportNow(Map<String, Object> data) {
        try {
            exportApi.exportNow(McUtils.getLong(data.get("rowLimit")));
            return RestUtils.getResponse(Status.OK);
        } catch (Exception ex) {
            _logger.error("Error,", ex);
            return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError(ex.getMessage()));
        }
    }

    @POST
    @Path("/delete")
    public Response deleteIds(List<String> files) {
        try {
            exportApi.deleteExportFiles(files);
        } catch (IOException ex) {
            RestUtils.getResponse(Status.BAD_REQUEST, new ApiError(ex.getMessage()));
        }
        return RestUtils.getResponse(Status.NO_CONTENT);
    }

    @POST
    @Path("/import")
    public Response restore(Map<String, Object> data) {
        try {
            exportApi.importNow((String) data.get("fileName"));
            return RestUtils.getResponse(Status.OK, new ApiMessage(
                    "Server is going to down now! Monitor log file of the server."));
        } catch (Exception ex) {
            _logger.error("Error in import,", ex);
            return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError(ex.getMessage()));
        }
    }
}
