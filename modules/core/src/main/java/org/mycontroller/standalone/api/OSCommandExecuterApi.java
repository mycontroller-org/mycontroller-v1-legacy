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

import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.mycontroller.standalone.api.jaxrs.model.OSCommandRequest;
import org.mycontroller.standalone.api.jaxrs.model.OSCommandResponse;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.3.0
 */

public class OSCommandExecuterApi {

    public OSCommandResponse executeCommand(String... command) {
        OSCommandResponse result = null;
        try {
            Process process = Runtime.getRuntime().exec(command);
            String input = IOUtils.toString(process.getInputStream(), StandardCharsets.UTF_8.name());
            String error = IOUtils.toString(process.getErrorStream(), StandardCharsets.UTF_8.name());
            result = OSCommandResponse.builder()
                    .result(input.length() > 0 ? input : null)
                    .error(error.length() > 0 ? error : null)
                    .build();
        } catch (Exception ex) {
            result = OSCommandResponse.builder()
                    .error(ExceptionUtils.getMessage(ex))
                    .stackTrace(ExceptionUtils.getStackTrace(ex))
                    .build();
        }
        return result;
    }

    public OSCommandResponse executeLinuxCommand(String command) {
        return executeCommand("/bin/sh", "-c", command);
    }

    public OSCommandResponse execute(OSCommandRequest request) {
        if (request.getOs().equalsIgnoreCase("linux")) {
            return executeLinuxCommand(request.getCommand());
        } else {
            return executeCommand(request.getCommand());
        }
    }

}
