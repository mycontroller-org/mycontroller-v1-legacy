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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.FileUtils;
import org.mycontroller.standalone.ObjectFactory;
import org.mycontroller.standalone.api.jaxrs.mapper.About;
import org.mycontroller.standalone.api.jaxrs.mapper.ApiError;
import org.mycontroller.standalone.api.jaxrs.utils.McServerLogFile;
import org.mycontroller.standalone.api.jaxrs.utils.RestUtils;
import org.mycontroller.standalone.api.jaxrs.utils.StatusJVM;
import org.mycontroller.standalone.api.jaxrs.utils.StatusOS;
import org.mycontroller.standalone.mysensors.MySensorsRawMessage;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */

@Path("/rest")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RolesAllowed({ "User" })
public class MyControllerHandler extends AccessEngine {

    @GET
    @Path("/ping")
    public Response ping() {
        return RestUtils.getResponse(Status.OK, "{\"message\":\"Hello!\"}");
    }

    @GET
    @Path("/timestamp")
    public Response timestamp() {
        return RestUtils.getResponse(Status.OK, "{\"timestamp\":" + System.currentTimeMillis() + "}");
    }

    @GET
    @Path("/time")
    public Response time() {
        return RestUtils.getResponse(Status.OK, "{\"time\":" + new Date().toString() + "}");
    }

    @GET
    @Path("/about")
    public Response about() {
        return RestUtils.getResponse(Status.OK, new About());
    }

    @RolesAllowed({ "Admin" })
    @GET
    @Path("/mcServerLogFile")
    public Response getMcServerLogFile(
            @QueryParam("lastKnownPosition") Long lastKnownPosition,
            @QueryParam("lastNPosition") Long lastNPosition,
            @QueryParam("download") Boolean download) throws IOException {

        if (download != null && download) {
            String zipFileName = McServerLogFile.getLogsZipFile();
            String fileName = FileUtils.getFile(zipFileName).getName();

            StreamingOutput fileStream = new StreamingOutput() {
                @Override
                public void write(java.io.OutputStream output) throws IOException, WebApplicationException {
                    try {
                        java.nio.file.Path path = Paths.get(zipFileName);
                        byte[] data = Files.readAllBytes(path);
                        output.write(data);
                        output.flush();
                    } catch (Exception ex) {
                        throw new WebApplicationException("File Not Found !!");
                    } finally {
                        FileUtils.deleteQuietly(FileUtils.getFile(zipFileName));
                    }
                }
            };
            return Response
                    .ok(fileStream, MediaType.APPLICATION_OCTET_STREAM)
                    .header("content-disposition", "attachment; filename = " + fileName)
                    .build();
        } else {
            return RestUtils.getResponse(Status.OK, McServerLogFile.getLogUpdate(lastKnownPosition, lastNPosition));
        }
    }

    //TODO: remove this method, no longer in use
    @GET
    @Path("/gatewayInfo")
    public Response serialport() {
        return RestUtils.getResponse(Status.OK, null);
    }

    @POST
    @Path("/sendRawMessage")
    public Response sendRawMessage(MySensorsRawMessage mySensorsRawMessage) {
        try {
            mySensorsRawMessage.setTxMessage(true);
            ObjectFactory.getRawMessageQueue().putMessage(mySensorsRawMessage.getRawMessage());
            return RestUtils.getResponse(Status.OK);
        } catch (Exception ex) {
            return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError(ex.getMessage()));
        }
    }

    @GET
    @Path("/osStatus")
    public Response getOsStatus() {
        return RestUtils.getResponse(Status.OK, new StatusOS());
    }

    @GET
    @Path("/jvmStatus")
    public Response getJvmStatus() {
        return RestUtils.getResponse(Status.OK, new StatusJVM());
    }
}
