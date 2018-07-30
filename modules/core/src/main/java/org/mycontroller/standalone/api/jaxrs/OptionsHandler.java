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

import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.mycontroller.standalone.api.jaxrs.utils.RestUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */

@Path("/{path : .*}")
@Slf4j
public class OptionsHandler {

    //https://gist.github.com/tganzarolli/8520728
    /*http://stackoverflow.com/questions/21221688/
     * angularjs-resource-makes-http-options-request-instead-of-http-post-for-save-me
     */
    @OPTIONS
    public Response sendPreFlightResponse() {
        if (_logger.isTraceEnabled()) {
            _logger.trace("Called pre flight options...");
        }
        return RestUtils.getResponse(Status.OK);
    }

}
