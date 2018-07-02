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

import java.security.Principal;

import javax.annotation.security.PermitAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.resteasy.spi.HttpRequest;
import org.mycontroller.standalone.api.jaxrs.model.Authentication;
import org.mycontroller.standalone.api.jaxrs.model.UserCredential;
import org.mycontroller.standalone.api.jaxrs.utils.RestUtils;
import org.mycontroller.standalone.auth.BasicAthenticationSecurityDomain;
import org.mycontroller.standalone.db.DaoUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */

@Path("/rest/authentication")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@PermitAll
@Slf4j
public class AuthenticationHandler {
    @Context
    HttpRequest request;

    @POST
    @Path("/login")
    public Response login(UserCredential userCredential) throws InterruptedException {
        _logger.debug("User Detail:{}", RestUtils.getUser(request));
        _logger.debug("Login user: " + userCredential.getUsername());
        try {
            Principal principal = new BasicAthenticationSecurityDomain().authenticate(userCredential.getUsername(),
                    userCredential.getPassword());
            if (principal != null) {
                Authentication authJson = Authentication.builder().success(true)
                        .user(DaoUtils.getUserDao().getByUsername(userCredential.getUsername())).build();
                return RestUtils.getResponse(Status.OK, authJson);
            } else {
                return RestUtils.getResponse(Status.UNAUTHORIZED,
                        Authentication.builder().success(false).message("Invalid user or passowrd!").build());
            }
        } catch (SecurityException ex) {
            return RestUtils.getResponse(Status.UNAUTHORIZED,
                    Authentication.builder().success(false).message(ex.getMessage()).build());
        }
    }
}
