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

import org.mycontroller.standalone.api.jaxrs.model.ApiError;
import org.mycontroller.standalone.api.jaxrs.model.Query;
import org.mycontroller.standalone.api.jaxrs.utils.RestUtils;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.Firmware;
import org.mycontroller.standalone.db.tables.FirmwareType;
import org.mycontroller.standalone.db.tables.FirmwareVersion;
import org.mycontroller.standalone.exceptions.McBadRequestException;
import org.mycontroller.standalone.firmware.FirmwareUtils;
import org.mycontroller.standalone.firmware.FirmwareUtils.FILE_TYPE;

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

        //Query primary filters
        filters.put(Query.ORDER, order);
        filters.put(Query.ORDER_BY, orderBy);
        filters.put(Query.PAGE_LIMIT, pageLimit);
        filters.put(Query.PAGE, page);

        return RestUtils.getResponse(Status.OK, DaoUtils.getFirmwareTypeDao().getAll(Query.get(filters)));
    }

    @GET
    @Path("/types/{id}")
    public Response getFirmwareType(@PathParam("id") int id) {
        return RestUtils.getResponse(Status.OK, DaoUtils.getFirmwareTypeDao().getById(id));
    }

    @PUT
    @Path("/types")
    public Response updateFirmwareType(FirmwareType firmwareType) {
        FirmwareType fType = DaoUtils.getFirmwareTypeDao().get(FirmwareType.KEY_NAME, firmwareType.getName());
        if (fType != null && !fType.getId().equals(firmwareType.getId())) {
            return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError("Requested name already available!"));
        }
        if (!firmwareType.getId().equals(firmwareType.getNewId())) {
            if (DaoUtils.getFirmwareTypeDao().getById(firmwareType.getNewId()) != null) {
                return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError("Requested type id already available!"));
            }
            Integer oldId = firmwareType.getId();
            DaoUtils.getFirmwareTypeDao().updateId(firmwareType, firmwareType.getNewId());
            DaoUtils.getFirmwareDao().updateBulk(Firmware.KEY_TYPE_ID, firmwareType.getNewId(),
                    Firmware.KEY_TYPE_ID, oldId);
            firmwareType.setId(firmwareType.getNewId());
        }
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

        //Query primary filters
        filters.put(Query.ORDER, order);
        filters.put(Query.ORDER_BY, orderBy);
        filters.put(Query.PAGE_LIMIT, pageLimit);
        filters.put(Query.PAGE, page);

        return RestUtils.getResponse(Status.OK, DaoUtils.getFirmwareVersionDao().getAll(Query.get(filters)));
    }

    @GET
    @Path("/versions/{id}")
    public Response getFirmwareVersion(@PathParam("id") int id) {
        return RestUtils.getResponse(Status.OK, DaoUtils.getFirmwareVersionDao().getById(id));
    }

    @PUT
    @Path("/versions")
    public Response updateFirmwareVersion(FirmwareVersion firmwareVersion) {
        FirmwareVersion fVersion = DaoUtils.getFirmwareVersionDao().get(FirmwareVersion.KEY_VERSION,
                firmwareVersion.getVersion());
        if (fVersion != null && !fVersion.getId().equals(firmwareVersion.getId())) {
            return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError("Requested version already available!"));
        }
        if (!firmwareVersion.getId().equals(firmwareVersion.getNewId())) {
            if (DaoUtils.getFirmwareVersionDao().getById(firmwareVersion.getNewId()) != null) {
                return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError(
                        "Requested version id already available!"));
            }
            Integer oldId = firmwareVersion.getId();
            DaoUtils.getFirmwareVersionDao().updateId(firmwareVersion, firmwareVersion.getNewId());
            DaoUtils.getFirmwareDao().updateBulk(Firmware.KEY_VERSION_ID, firmwareVersion.getNewId(),
                    Firmware.KEY_VERSION_ID, oldId);
        }
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

        //Query primary filters
        filters.put(Query.ORDER, order);
        filters.put(Query.ORDER_BY, orderBy);
        filters.put(Query.PAGE_LIMIT, pageLimit);
        filters.put(Query.PAGE, page);

        return RestUtils.getResponse(Status.OK, DaoUtils.getFirmwareDao().getAll(Query.get(filters)));
    }

    @GET
    @Path("/firmwares/{id}")
    public Response getFirmware(@PathParam("id") int id) {
        Firmware firmware = DaoUtils.getFirmwareDao().getById(id);
        return RestUtils.getResponse(Status.OK, firmware);
    }

    @PUT
    @Path("/firmwares")
    public Response updateFirmware(Firmware firmware) {
        try {
            FirmwareUtils.createUpdateFirmware(firmware, FILE_TYPE.fromString(firmware.getFileType()));
            return RestUtils.getResponse(Status.OK);
        } catch (McBadRequestException ex) {
            return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError(ex.getMessage()));
        }
    }

    @POST
    @Path("/firmwares")
    public Response createFirmware(Firmware firmware) {
        try {
            FirmwareUtils.createUpdateFirmware(firmware, FILE_TYPE.fromString(firmware.getFileType()));
            return RestUtils.getResponse(Status.CREATED);
        } catch (McBadRequestException ex) {
            return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError(ex.getMessage()));
        }
    }

    @POST
    @Path("/firmwares/delete")
    public Response deleteFirmware(List<Integer> ids) {
        for (Integer id : ids) {
            FirmwareUtils.deleteFirmware(id);
        }
        return RestUtils.getResponse(Status.OK);
    }

    @GET
    @Path("/firmwaresData/{firmwareId}")
    public Response getFirmwareData(@PathParam("firmwareId") int firmwareId) {
        return RestUtils.getResponse(Status.OK, DaoUtils.getFirmwareDataDao().getByFirmwareId(firmwareId));
    }

}
