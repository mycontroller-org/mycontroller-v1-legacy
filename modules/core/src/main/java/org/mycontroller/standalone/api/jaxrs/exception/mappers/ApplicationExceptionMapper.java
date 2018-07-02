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
package org.mycontroller.standalone.api.jaxrs.exception.mappers;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.spi.ApplicationException;
import org.jboss.resteasy.spi.UnhandledException;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
@Provider
@Slf4j
public class ApplicationExceptionMapper implements ExceptionMapper<ApplicationException> {

    //Refer: https://issues.jboss.org/browse/RESTEASY-891
    // Any exception mappers for 'handled' exceptions
    UnhandledExceptionMapper unhandledExceptionMapper = new UnhandledExceptionMapper();

    @Override
    public Response toResponse(ApplicationException exception) {
        _logger.error("ApplicationException,", exception);
        // As this mapper will override all others, we need to manually delegate exception handling
        Throwable cause = exception.getCause();
        if (cause instanceof UnhandledException) {
            return unhandledExceptionMapper.toResponse((UnhandledException) cause);
        } else {
            return ExceptionMapperUtils.buildResponseWithCors(exception,
                    Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
}
