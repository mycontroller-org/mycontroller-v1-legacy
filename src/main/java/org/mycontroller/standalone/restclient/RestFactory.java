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
package org.mycontroller.standalone.restclient;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jboss.resteasy.client.jaxrs.ProxyBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
public class RestFactory<T> {
    private Class<T> proxyClazz;

    public RestFactory(Class<T> clazz) {
        proxyClazz = clazz;
    }

    public T createAPI(URI uri, String userName, String password) {
        CloseableHttpClient httpclient = HttpClientBuilder.create().build();
        HttpHost targetHost = new HttpHost(uri.getHost(), uri.getPort());
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(targetHost.getHostName(), targetHost.getPort()),
                new UsernamePasswordCredentials(userName, password));
        // Create AuthCache instance
        AuthCache authCache = new BasicAuthCache();
        // Generate BASIC scheme object and add it to the local auth cache
        BasicScheme basicAuth = new BasicScheme();
        authCache.put(targetHost, basicAuth);
        // Add AuthCache to the execution context
        HttpClientContext context = HttpClientContext.create();
        context.setCredentialsProvider(credsProvider);
        context.setAuthCache(authCache);
        ApacheHttpClient4Engine engine = new ApacheHttpClient4Engine(httpclient, context);

        ResteasyClient client = new ResteasyClientBuilder().httpEngine(engine).build();
        client.register(JacksonJaxbJsonProvider.class);
        client.register(RequestLogger.class);
        client.register(ResponseLogger.class);
        ProxyBuilder<T> proxyBuilder = client.target(uri).proxyBuilder(proxyClazz);
        return proxyBuilder.build();
    }

    public T createAPI(String targetUrl, String userName, String password) throws URISyntaxException {
        URI targetUri = new URI(targetUrl);
        return createAPI(targetUri, userName, password);
    }
}