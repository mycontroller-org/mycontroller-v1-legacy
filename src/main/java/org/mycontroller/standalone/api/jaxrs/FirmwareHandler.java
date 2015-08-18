/**
 * Copyright (C) 2015 Jeeva Kandasamy (jkandasa@gmail.com)
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

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

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
public class FirmwareHandler {
    @GET
    @Path("/types")
    public Response getAllFirmwareTypes() {
        return RestUtils.getResponse(Status.OK, DaoUtils.getFirmwareTypeDao().getAll());
    }

    @GET
    @Path("/versions")
    public Response getAllFirmwareVersions() {
        return RestUtils.getResponse(Status.OK, DaoUtils.getFirmwareVersionDao().getAll());
    }

    @GET
    @Path("/")
    public Response getAllFirmwares() {
        return RestUtils.getResponse(Status.OK, DaoUtils.getFirmwareDao().getAll());
    }

    @GET
    @Path("/types/{id}")
    public Response getFirmwareType(@PathParam("id") int id) {
        return RestUtils.getResponse(Status.OK, DaoUtils.getFirmwareTypeDao().get(id));
    }

    @GET
    @Path("/versions/{id}")
    public Response getFirmwareVersion(@PathParam("id") int id) {
        return RestUtils.getResponse(Status.OK, DaoUtils.getFirmwareVersionDao().get(id));
    }

    @GET
    @Path("/{id}")
    public Response getFirmware(@PathParam("id") int id) {
        return RestUtils.getResponse(Status.OK, DaoUtils.getFirmwareDao().get(id));
    }

    @PUT
    @Path("/types")
    public Response updateFirmwareType(FirmwareType firmwareType) {
        DaoUtils.getFirmwareTypeDao().update(firmwareType);
        return RestUtils.getResponse(Status.OK);
    }

    @PUT
    @Path("/versions")
    public Response updateFirmwareVersion(FirmwareVersion firmwareVersion) {
        DaoUtils.getFirmwareVersionDao().update(firmwareVersion);
        return RestUtils.getResponse(Status.OK);
    }

    @PUT
    @Path("/")
    public Response updateFirmware(Firmware firmware) {
        FirmwareUtils.updateFirmwareFromHexString(firmware);
        firmware.setTimestamp(System.currentTimeMillis());
        DaoUtils.getFirmwareDao().update(firmware);
        return RestUtils.getResponse(Status.OK);
    }

    @POST
    @Path("/types")
    public Response createFirmwareType(FirmwareType firmwareType) {
        DaoUtils.getFirmwareTypeDao().create(firmwareType);
        return RestUtils.getResponse(Status.CREATED);
    }

    @POST
    @Path("/versions")
    public Response createFirmwareVersion(FirmwareVersion firmwareVersion) {
        DaoUtils.getFirmwareVersionDao().create(firmwareVersion);
        return RestUtils.getResponse(Status.CREATED);
    }

    @POST
    @Path("/")
    public Response createFirmware(Firmware firmware) {
        FirmwareUtils.updateFirmwareFromHexString(firmware);
        firmware.setTimestamp(System.currentTimeMillis());
        DaoUtils.getFirmwareDao().create(firmware);
        return RestUtils.getResponse(Status.CREATED);
    }

    @DELETE
    @Path("/types/{id}")
    public Response deleteFirmwareType(@PathParam("id") int id) {
        DaoUtils.getFirmwareTypeDao().delete(id);
        return RestUtils.getResponse(Status.OK);
    }

    @DELETE
    @Path("/versions/{id}")
    public Response deleteFirmwareVersion(@PathParam("id") int id) {
        DaoUtils.getFirmwareVersionDao().delete(id);
        return RestUtils.getResponse(Status.OK);
    }

    @DELETE
    @Path("/{id}")
    public Response deleteFirmware(@PathParam("id") int id) {
        DaoUtils.getFirmwareDao().delete(id);
        return RestUtils.getResponse(Status.OK);
    }

}
