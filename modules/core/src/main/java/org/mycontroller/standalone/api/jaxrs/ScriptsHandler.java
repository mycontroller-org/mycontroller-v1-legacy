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
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.mycontroller.standalone.api.jaxrs.mixins.serializers.PyTypeSerializer;
import org.mycontroller.standalone.api.jaxrs.model.ApiError;
import org.mycontroller.standalone.api.jaxrs.model.Query;
import org.mycontroller.standalone.api.jaxrs.utils.RestUtils;
import org.mycontroller.standalone.scripts.McScript;
import org.mycontroller.standalone.scripts.McScriptEngineUtils.SCRIPT_TYPE;
import org.mycontroller.standalone.utils.McScriptFileUtils;
import org.python.core.PyType;

import com.fasterxml.jackson.core.type.TypeReference;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */

@Path("/rest/scripts")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RolesAllowed({ "Admin" })
@Slf4j
public class ScriptsHandler extends AccessEngine {
    public static final String KEY_TYPE = "type";
    public static final String KEY_NAME = "name";
    public static final String KEY_EXTENSION = "extension";
    public static final String KEY_LESS_INFO = "lessInfo";

    @GET
    @Path("/")
    public Response getAll(@QueryParam(KEY_NAME) List<String> name,
            @QueryParam(KEY_TYPE) String type,
            @QueryParam(KEY_EXTENSION) String extension,
            @QueryParam(KEY_LESS_INFO) Boolean lessInfo,
            @QueryParam(Query.PAGE_LIMIT) Long pageLimit,
            @QueryParam(Query.PAGE) Long page,
            @QueryParam(Query.ORDER_BY) String orderBy,
            @QueryParam(Query.ORDER) String order) {
        //If lessInfo is null load as false.
        if (lessInfo == null) {
            lessInfo = false;
        }
        HashMap<String, Object> filters = new HashMap<String, Object>();
        filters.put(KEY_NAME, name);
        filters.put(KEY_TYPE, SCRIPT_TYPE.fromString(type));
        filters.put(KEY_EXTENSION, extension);
        filters.put(KEY_LESS_INFO, lessInfo);

        if (orderBy == null) {
            orderBy = KEY_NAME;
        }
        //Query primary filters
        filters.put(Query.ORDER, order);
        filters.put(Query.ORDER_BY, orderBy);
        filters.put(Query.PAGE_LIMIT, pageLimit);
        filters.put(Query.PAGE, page);

        Query query = Query.get(filters);
        try {
            if (lessInfo) {
                return RestUtils.getResponse(Status.OK, McScriptFileUtils.getScriptFiles(query).getData());
            } else {
                return RestUtils.getResponse(Status.OK, McScriptFileUtils.getScriptFiles(query));
            }
        } catch (Exception ex) {
            return RestUtils.getResponse(Status.EXPECTATION_FAILED, new ApiError(ex.getMessage()));
        }
    }

    @POST
    @Path("/delete")
    public Response deleteIds(List<String> scriptFiles) {
        try {
            McScriptFileUtils.deleteScriptFiles(scriptFiles);
        } catch (IOException ex) {
            RestUtils.getResponse(Status.BAD_REQUEST, new ApiError(ex.getMessage()));
        }
        return RestUtils.getResponse(Status.NO_CONTENT);
    }

    @GET
    @Path("/get")
    public Response get(@QueryParam("name") String scriptName) {
        if (scriptName == null) {
            return RestUtils.getResponse(Status.BAD_REQUEST);
        }
        try {
            return RestUtils.getResponse(Status.OK, McScriptFileUtils.getScriptFile(scriptName));
        } catch (Exception ex) {
            return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError(ex.getMessage()));
        }
    }

    @GET
    @Path("/runNow")
    public Response runNow(@QueryParam("script") String scriptName,
            @QueryParam("scriptBindings") String jsonBindings) {
        try {
            HashMap<String, Object> bindings = null;
            if (jsonBindings != null) {
                bindings = RestUtils.getObjectMapper().readValue(
                        jsonBindings, new TypeReference<HashMap<String, Object>>() {
                        });
            } else {
                bindings = new HashMap<String, Object>();
            }
            HashMap<String, Object> bindingsFinal = McScriptFileUtils.executeScript(scriptName, bindings);
            //If script type is python, add mixins,
            //refer: https://github.com/mycontroller-org/mycontroller/issues/223
            if (scriptName.endsWith("py")) {
                RestUtils.getObjectMapper().addMixIn(PyType.class, PyTypeSerializer.class);
            }
            return RestUtils.getResponse(Status.OK, RestUtils.getObjectMapper().writeValueAsString(bindingsFinal));
        } catch (Exception ex) {
            _logger.error("Exception,", ex);
            return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError(ex.getMessage()));
        }
    }

    @POST
    @Path("/")
    public Response upload(McScript mcScript) {
        try {
            McScriptFileUtils.uploadScript(mcScript);
            return RestUtils.getResponse(Status.OK);
        } catch (Exception ex) {
            return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError(ex.getMessage()));
        }
    }

}