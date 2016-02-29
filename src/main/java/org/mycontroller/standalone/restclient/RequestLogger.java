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

import java.io.IOException;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Provider
public class RequestLogger implements ClientRequestFilter {
    private static final Logger _logger = LoggerFactory.getLogger(RequestLogger.class);
    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public void filter(ClientRequestContext clientRequestContext) throws IOException {
        if (_logger.isTraceEnabled()) {
            _logger.trace("Request [Method:{}, URI:{}, Headers:{}, Data:{}]",
                    clientRequestContext.getMethod(),
                    clientRequestContext.getUri(),
                    clientRequestContext.getHeaders(),
                    mapper.writerWithDefaultPrettyPrinter().writeValueAsString(clientRequestContext.getEntity()));
        }
    }
}