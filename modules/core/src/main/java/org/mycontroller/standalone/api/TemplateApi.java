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

import java.io.IOException;
import java.util.HashMap;

import org.mycontroller.standalone.exceptions.McBadRequestException;
import org.mycontroller.standalone.model.McTemplate;
import org.mycontroller.standalone.utils.McTemplateUtils;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */

public class TemplateApi {
    public String get(String templateName, String scriptName, HashMap<String, Object> bindings) throws Exception {
        return McTemplateUtils.execute(templateName, scriptName, bindings);
    }

    public McTemplate getTemplate(String templateName)
            throws IllegalAccessException, McBadRequestException, IOException {
        return McTemplateUtils.get(templateName);
    }
}
