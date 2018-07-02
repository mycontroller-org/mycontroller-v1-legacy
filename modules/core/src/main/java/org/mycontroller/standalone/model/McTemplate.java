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
package org.mycontroller.standalone.model;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.mycontroller.standalone.AppProperties;

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
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class McTemplate {
    private String extension;
    private String name;
    private String canonicalPath;
    private long size;
    private long lastModified;
    private String data;

    @JsonIgnore
    public static McTemplate get(String fileName) throws IllegalAccessException, IOException {
        File templateFile = FileUtils.getFile(AppProperties.getInstance().getTemplatesLocation() + fileName);
        return McTemplate.builder()
                .extension(FilenameUtils.getExtension(templateFile.getCanonicalPath()))
                .canonicalPath(templateFile.getCanonicalPath())
                .name(templateFile.getCanonicalPath())
                .lastModified(templateFile.lastModified())
                .size(templateFile.length())
                .build();
    }

    @JsonIgnore
    public String getMappedString(Map<String, Object> keyValueMap) {
        return null;
    }
}
