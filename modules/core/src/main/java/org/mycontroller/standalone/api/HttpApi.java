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
package org.mycontroller.standalone.api;

import java.util.Map;

import org.mycontroller.restclient.core.RestHeader;
import org.mycontroller.restclient.core.RestHttpClient;
import org.mycontroller.restclient.core.RestHttpResponse;
import org.mycontroller.restclient.core.TRUST_HOST_TYPE;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */

public class HttpApi extends RestHttpClient {

    public HttpApi() {
        this(TRUST_HOST_TYPE.DEFAULT);
    }

    public HttpApi(TRUST_HOST_TYPE trustHostType) {
        super(trustHostType == null ? TRUST_HOST_TYPE.DEFAULT : trustHostType);
    }

    // HTTP GET request
    public RestHttpResponse get(String url) {
        return doGet(url, null);
    }

    // HTTP GET request
    public RestHttpResponse get(String url, Map<String, Object> queryParameters) {
        return doGet(url, queryParameters, RestHeader.getDefault(), null);
    }

    // HTTP GET request
    public RestHttpResponse get(String url, Map<String, Object> queryParameters, RestHeader header) {
        return doGet(url, queryParameters, header, null);
    }

    // HTTP POST request
    public RestHttpResponse post(String url, String entity) {
        return doPost(url, RestHeader.getDefault(), entity, null);
    }

    // HTTP POST request
    public RestHttpResponse post(String url, RestHeader header, String entity) {
        return doPost(url, header, entity, null);
    }

    // HTTP DELETE request
    public RestHttpResponse delete(String url) {
        return doDelete(url, null);
    }

    // HTTP DELETE request
    public RestHttpResponse delete(String url, RestHeader header) {
        return doDelete(url, header, null);
    }

    // HTTP PUT request
    public RestHttpResponse put(String url, String entity) {
        return doPut(url, null, entity, null);
    }

    // HTTP PUT request
    public RestHttpResponse put(String url, RestHeader header, String entity) {
        return doPut(url, header, entity, null);
    }
}
