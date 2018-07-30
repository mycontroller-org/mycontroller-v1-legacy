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
package org.mycontroller.standalone.scripts;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.io.FilenameUtils;
import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.scripts.McScriptEngineUtils.SCRIPT_TYPE;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Data
@Builder
@ToString(exclude = { "data" })
@NoArgsConstructor
@AllArgsConstructor
public class McScript {
    private String engineName;
    private String mimeType;
    private String extension;
    private String name;
    private String canonicalPath;
    private SCRIPT_TYPE type;
    private long size;
    private long lastModified;
    private String data;
    private HashMap<String, Object> bindings;

    @JsonIgnore
    public boolean isValid() {
        if (engineName == null && mimeType == null && extension == null) {
            return false;
        }
        return true;
    }

    public SCRIPT_TYPE getType() {
        if (type == null) {
            if (name.startsWith(AppProperties.CONDITIONS_SCRIPTS_DIRECTORY)) {
                type = SCRIPT_TYPE.CONDITION;
            } else {
                type = SCRIPT_TYPE.OPERATION;
            }
        }
        return type;
    }

    @JsonIgnore
    public static McScript getMcScript(String scriptFileName) throws IllegalAccessException, IOException {
        File scriptFile = McScriptEngineUtils.getScriptFile(scriptFileName);
        return McScript.builder()
                .extension(FilenameUtils.getExtension(scriptFile.getCanonicalPath()))
                .name(scriptFile.getCanonicalPath())
                .build();
    }
}
