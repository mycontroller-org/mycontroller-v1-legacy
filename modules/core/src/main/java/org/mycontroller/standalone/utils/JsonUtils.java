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
package org.mycontroller.standalone.utils;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonUtils {
    private static final String ENCODING = "UTF-8";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void dumps(Object data, String... names) {
        MAPPER.configure(SerializationFeature.INDENT_OUTPUT, true);
        MAPPER.setSerializationInclusion(Include.NON_NULL);
        try {
            String stringData = MAPPER.writeValueAsString(data);
            File file = FileUtils.getFile(names);
            FileUtils.write(file, stringData, ENCODING);
            _logger.trace("Json saved: {}", file.getAbsolutePath());
        } catch (IOException ex) {
            _logger.error("Exception,", ex);
        }
    }

    public static Object loads(Class<?> clazz, String... names) {
        return loads(clazz, FileUtils.getFile(names));
    }

    public static Object loads(Class<?> clazz, File file) {
        try {
            String content = FileUtils.readFileToString(file, ENCODING);
            MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return MAPPER.readValue(content, clazz);
        } catch (IOException ex) {
            _logger.error("Exception when loading {}", file.getAbsolutePath(), ex);
        }
        return null;
    }

}