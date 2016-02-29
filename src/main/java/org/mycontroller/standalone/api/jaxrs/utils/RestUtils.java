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
package org.mycontroller.standalone.api.jaxrs.utils;

import java.io.IOException;
import java.util.List;
import java.util.StringTokenizer;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.util.Base64;
import org.mycontroller.standalone.db.tables.User;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class RestUtils {

    private static final String AUTHORIZATION_PROPERTY = "Authorization";
    private static final String AUTHENTICATION_SCHEME = "Basic";

    private RestUtils() {

    }

    public static Response getResponse(Status status, Object entity) {
        return Response
                .status(status)
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
                .header("Access-Control-Allow-Credentials", "true")
                .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
                .header("Access-Control-Max-Age", "1209600")
                .entity(entity)
                .build();
    }

    public static Response getResponse(Status status) {
        return Response
                .status(status)
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
                .header("Access-Control-Allow-Credentials", "true")
                .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
                .header("Access-Control-Max-Age", "1209600")
                .build();
    }

    public static User getUser(HttpRequest request) {
        //Get request headers
        final HttpHeaders headers = request.getHttpHeaders();

        //Fetch authorization header
        final List<String> authorization = headers.getRequestHeader(AUTHORIZATION_PROPERTY);

        //If no authorization information present; nothing to do
        if (authorization == null || authorization.isEmpty())
        {
            return null;
        }

        //Get encoded username and password
        final String encodedUserPassword = authorization.get(0).replaceFirst(AUTHENTICATION_SCHEME + " ", "");

        //Decode username and password
        String usernameAndPassword;
        try {
            usernameAndPassword = new String(Base64.decode(encodedUserPassword));
        } catch (IOException e) {
            return null;
        }

        //Split username and password tokens
        final StringTokenizer tokenizer = new StringTokenizer(usernameAndPassword, ":");
        final String username = tokenizer.nextToken();
        //final String password = tokenizer.nextToken();
        return UserMapper.getUser(username);
    }
}
