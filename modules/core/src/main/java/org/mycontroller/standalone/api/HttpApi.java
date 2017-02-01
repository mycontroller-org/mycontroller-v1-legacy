/*
 * Copyright 2015-2017 Jeeva Kandasamy (jkandasa@gmail.com)
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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.mycontroller.restclient.core.TRUST_HOST_TYPE;
import org.mycontroller.restclient.core.jaxrs.McHttpClient;
import org.mycontroller.standalone.api.jaxrs.model.McHttpResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Slf4j
public class HttpApi {

    private HttpClient client = null;

    public HttpApi() {
        client = HttpClientBuilder.create().build();
    }

    public HttpApi(TRUST_HOST_TYPE trustHostType) {
        if (trustHostType == TRUST_HOST_TYPE.ANY) {
            client = new McHttpClient().getHttpClientTrustAll();
        }
        client = new McHttpClient().getHttpClient();
    }

    private Map<String, Object> getDefaultHeader() {
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("User-Agent", "Mozilla/5.0");
        return headers;
    }

    // HTTP GET request
    public McHttpResponse get(String url) {
        return get(url, getDefaultHeader(), null);
    }

    // HTTP GET request
    public McHttpResponse get(String url, Map<String, Object> queryParameters) {
        return get(url, getDefaultHeader(), queryParameters);
    }

    // HTTP GET request
    public McHttpResponse get(String url, Map<String, Object> headers, Map<String, Object> queryParameters) {
        try {
            HttpGet get = null;
            if (queryParameters != null && !queryParameters.isEmpty()) {
                List<NameValuePair> queryParams = new ArrayList<NameValuePair>();
                for (String key : queryParameters.keySet()) {
                    queryParams.add(new BasicNameValuePair(key, String.valueOf(queryParameters.get(key))));
                }
                get = new HttpGet(new URIBuilder(url).addParameters(queryParams).build());
            } else {
                get = new HttpGet(url);
            }

            for (String key : headers.keySet()) {
                get.setHeader(key, String.valueOf(headers.get(key)));
            }
            HttpResponse response = client.execute(get);
            McHttpResponse httpResponse = McHttpResponse.builder()
                    .uri(get.getURI())
                    .responseCode(response.getStatusLine().getStatusCode())
                    .entity(IOUtils.toString(response.getEntity().getContent()))
                    .headers(response.getAllHeaders())
                    .build();
            _logger.debug("{}", httpResponse);
            return httpResponse;

        } catch (Exception ex) {
            _logger.error("Exception when calling url:[{}], headers:[{}]", url, headers, ex);
        }
        return null;
    }

    // HTTP POST request
    public McHttpResponse post(String url, String entity) {
        return post(url, getDefaultHeader(), entity);
    }

    // HTTP POST request
    public McHttpResponse post(String url, Map<String, Object> headers, String entity) {
        try {
            return post(url, headers, new StringEntity(entity));
        } catch (UnsupportedEncodingException ex) {
            _logger.error("Exception when calling url:[{}], headers:[{}]", url, headers, ex);
            return McHttpResponse.builder()
                    .exception(ex.getMessage())
                    .build();
        }
    }

    // HTTP POST request
    public McHttpResponse post(String url, Map<String, Object> headers, HttpEntity entity) {
        try {
            HttpPost post = new HttpPost(url);
            for (String key : headers.keySet()) {
                post.setHeader(key, String.valueOf(headers.get(key)));
            }
            post.setEntity(entity);
            HttpResponse response = client.execute(post);
            McHttpResponse httpResponse = McHttpResponse.builder()
                    .uri(post.getURI())
                    .responseCode(response.getStatusLine().getStatusCode())
                    .entity(IOUtils.toString(response.getEntity().getContent()))
                    .headers(response.getAllHeaders())
                    .build();
            _logger.debug("{}", httpResponse);
            return httpResponse;

        } catch (Exception ex) {
            _logger.error("Exception when calling url:[{}], headers:[{}]", url, headers, ex);
            return McHttpResponse.builder()
                    .exception(ex.getMessage())
                    .build();
        }
    }

}
