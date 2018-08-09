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

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.mycontroller.standalone.api.jaxrs.model.ApiError;
import org.mycontroller.standalone.api.jaxrs.utils.RestUtils;
import org.mycontroller.standalone.auth.AuthUtils;
import org.mycontroller.standalone.settings.Dashboard;
import org.mycontroller.standalone.settings.DashboardSettings;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */

@Path("/rest/dashboard")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RolesAllowed({ "User" })
@Slf4j
public class DashboardHandler extends AccessEngine {

    @GET
    @Path("/")
    public Response getDashboard(
            @QueryParam("title") String title,
            @QueryParam("dId") Integer id,
            @QueryParam("getDefault") Boolean getDefault,
            @QueryParam("getNew") Boolean getNew,
            @QueryParam("lessInfo") Boolean lessInfo) {
        if (id != null) {
            try {
                return RestUtils.getResponse(Status.OK,
                        DashboardSettings.getDashboard(AuthUtils.getUser(securityContext), id));
            } catch (IllegalAccessException ex) {
                return RestUtils.getResponse(Status.FORBIDDEN, new ApiError(ex.getMessage()));
            }
        } else if (getDefault != null && getDefault) {
            return RestUtils.getResponse(Status.OK,
                    DashboardSettings.getDefaultDashboard(AuthUtils.getUser(securityContext), title));
        } else if (getNew != null && getNew) {
            if (title == null) {
                title = "New dashboard";
            }
            return RestUtils.getResponse(Status.OK,
                    DashboardSettings.getDefaultDashboard(AuthUtils.getUser(securityContext), title));
        } else if (title != null) {
            return RestUtils.getResponse(Status.OK,
                    DashboardSettings.getDashboard(AuthUtils.getUser(securityContext), title));
        } else {
            List<Dashboard> dashboards = DashboardSettings.getDashboards(AuthUtils.getUser(securityContext));
            //If there is no dashboard available for this user create default dashboard
            if (dashboards.size() == 0) {
                dashboards.add(DashboardSettings.getDefaultDashboard(AuthUtils.getUser(securityContext),
                        "Default dashboard"));
            }
            if (lessInfo != null && !lessInfo) {
                for (Dashboard dashboard : dashboards) {
                    dashboard.loadRows();
                }
            }
            return RestUtils.getResponse(Status.OK, dashboards);
        }
    }

    @PUT
    @Path("/")
    public Response updateDashboard(Dashboard dashboard) {
        dashboard.setUserId(AuthUtils.getUser(securityContext).getId());
        _logger.debug("Dashboard: {}", dashboard);
        dashboard.update();
        return RestUtils.getResponse(Status.OK);
    }

    @DELETE
    @Path("/")
    public Response deleteDashboard(@QueryParam("dId") Integer id) {
        try {
            DashboardSettings.deleteDashboard(AuthUtils.getUser(securityContext), id);
            return RestUtils.getResponse(Status.OK);
        } catch (IllegalAccessException ex) {
            return RestUtils.getResponse(Status.FORBIDDEN, new ApiError(ex.getMessage()));
        }
    }
}
