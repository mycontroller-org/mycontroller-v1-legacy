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

import java.util.HashMap;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.mycontroller.standalone.api.jaxrs.mapper.Query;
import org.mycontroller.standalone.api.jaxrs.mapper.QueryResponse;
import org.mycontroller.standalone.api.jaxrs.utils.RestUtils;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.Firmware;
import org.mycontroller.standalone.db.tables.FirmwareType;
import org.mycontroller.standalone.db.tables.FirmwareVersion;
import org.mycontroller.standalone.mysensors.firmware.FirmwareUtils;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */

@Path("/rest/firmwares")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RolesAllowed({ "admin" })
public class FirmwareHandler {

    //Firmware type handler
    @GET
    @Path("/types")
    public Response getAllFirmwareTypes(
            @QueryParam(FirmwareType.KEY_NAME) List<String> name,
            @QueryParam(Query.PAGE_LIMIT) Long pageLimit,
            @QueryParam(Query.PAGE) Long page,
            @QueryParam(Query.ORDER_BY) String orderBy,
            @QueryParam(Query.ORDER) String order) {
        HashMap<String, Object> filters = new HashMap<String, Object>();

        filters.put(FirmwareType.KEY_NAME, name);

        QueryResponse queryResponse = DaoUtils.getFirmwareTypeDao().getAll(
                Query.builder()
                        .order(order != null ? order : Query.ORDER_ASC)
                        .orderBy(orderBy != null ? orderBy : FirmwareType.KEY_ID)
                        .filters(filters)
                        .pageLimit(pageLimit != null ? pageLimit : Query.MAX_ITEMS_PER_PAGE)
                        .page(page != null ? page : 1l)
                        .build());
        return RestUtils.getResponse(Status.OK, queryResponse);
    }

    @GET
    @Path("/types/{id}")
    public Response getFirmwareType(@PathParam("id") int id) {
        return RestUtils.getResponse(Status.OK, DaoUtils.getFirmwareTypeDao().getById(id));
    }

    @PUT
    @Path("/types")
    public Response updateFirmwareType(FirmwareType firmwareType) {
        DaoUtils.getFirmwareTypeDao().update(firmwareType);
        return RestUtils.getResponse(Status.OK);
    }

    @POST
    @Path("/types")
    public Response createFirmwareType(FirmwareType firmwareType) {
        DaoUtils.getFirmwareTypeDao().create(firmwareType);
        return RestUtils.getResponse(Status.CREATED);
    }

    @POST
    @Path("/types/delete")
    public Response deleteFirmwareType(List<Integer> ids) {
        for (Integer id : ids) {
            FirmwareUtils.deleteFirmwareType(id);
        }
        return RestUtils.getResponse(Status.OK);
    }

    //Firmware version handler

    @GET
    @Path("/versions")
    public Response getAllFirmwareVersions(
            @QueryParam(FirmwareVersion.KEY_VERSION) List<String> version,
            @QueryParam(Query.PAGE_LIMIT) Long pageLimit,
            @QueryParam(Query.PAGE) Long page,
            @QueryParam(Query.ORDER_BY) String orderBy,
            @QueryParam(Query.ORDER) String order) {
        HashMap<String, Object> filters = new HashMap<String, Object>();

        filters.put(FirmwareVersion.KEY_VERSION, version);

        QueryResponse queryResponse = DaoUtils.getFirmwareVersionDao().getAll(
                Query.builder()
                        .order(order != null ? order : Query.ORDER_ASC)
                        .orderBy(orderBy != null ? orderBy : FirmwareVersion.KEY_ID)
                        .filters(filters)
                        .pageLimit(pageLimit != null ? pageLimit : Query.MAX_ITEMS_PER_PAGE)
                        .page(page != null ? page : 1l)
                        .build());
        return RestUtils.getResponse(Status.OK, queryResponse);
    }

    @GET
    @Path("/versions/{id}")
    public Response getFirmwareVersion(@PathParam("id") int id) {
        return RestUtils.getResponse(Status.OK, DaoUtils.getFirmwareVersionDao().getById(id));
    }

    @PUT
    @Path("/versions")
    public Response updateFirmwareVersion(FirmwareVersion firmwareVersion) {
        DaoUtils.getFirmwareVersionDao().update(firmwareVersion);
        return RestUtils.getResponse(Status.OK);
    }

    @POST
    @Path("/versions")
    public Response createFirmwareVersion(FirmwareVersion firmwareVersion) {
        DaoUtils.getFirmwareVersionDao().create(firmwareVersion);
        return RestUtils.getResponse(Status.CREATED);
    }

    @POST
    @Path("/versions/delete")
    public Response deleteFirmwareVersion(List<Integer> ids) {
        for (Integer id : ids) {
            FirmwareUtils.deleteFirmwareVersion(id);
        }
        return RestUtils.getResponse(Status.OK);
    }

    //Firmware handler

    @GET
    @Path("/firmwares")
    public Response getAllFirmwares(
            @QueryParam(Firmware.KEY_TYPE_ID) Integer typeId,
            @QueryParam(Firmware.KEY_VERSION_ID) Integer versionId,
            @QueryParam(Query.PAGE_LIMIT) Long pageLimit,
            @QueryParam(Query.PAGE) Long page,
            @QueryParam(Query.ORDER_BY) String orderBy,
            @QueryParam(Query.ORDER) String order) {
        HashMap<String, Object> filters = new HashMap<String, Object>();

        filters.put(Firmware.KEY_TYPE_ID, typeId);
        filters.put(Firmware.KEY_VERSION_ID, versionId);

        QueryResponse queryResponse = DaoUtils.getFirmwareDao().getAll(
                Query.builder()
                        .order(order != null ? order : Query.ORDER_ASC)
                        .orderBy(orderBy != null ? orderBy : FirmwareVersion.KEY_ID)
                        .filters(filters)
                        .pageLimit(pageLimit != null ? pageLimit : Query.MAX_ITEMS_PER_PAGE)
                        .page(page != null ? page : 1l)
                        .build());
        return RestUtils.getResponse(Status.OK, queryResponse);
    }

    @GET
    @Path("/firmwares/{id}")
    public Response getFirmware(@QueryParam("withData") Boolean withData, @PathParam("id") int id) {
        Firmware firmware = DaoUtils.getFirmwareDao().getById(id);
        if (withData == null || !withData) {
            firmware.setData(null);
        }
        return RestUtils.getResponse(Status.OK, firmware);
    }

    @PUT
    @Path("/firmwares")
    public Response updateFirmware(Firmware firmware) {
        FirmwareUtils.updateFirmwareFromHexString(firmware);
        firmware.setTimestamp(System.currentTimeMillis());
        DaoUtils.getFirmwareDao().update(firmware);
        return RestUtils.getResponse(Status.OK);
    }

    @POST
    @Path("/firmwares")
    public Response createFirmware(Firmware firmware) {
        FirmwareUtils.createFirmware(firmware);
        return RestUtils.getResponse(Status.CREATED);
    }

    @POST
    @Path("/firmwares/delete")
    public Response deleteFirmware(List<Integer> ids) {
        for (Integer id : ids) {
            FirmwareUtils.deleteFirmware(id);
        }
        return RestUtils.getResponse(Status.OK);
    }

}
