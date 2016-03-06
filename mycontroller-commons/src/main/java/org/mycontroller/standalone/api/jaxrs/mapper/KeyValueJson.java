/*
 * Copyright 2015-2016 Jeeva Kandasamy (jkandasa@gmail.com)
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
package org.mycontroller.standalone.api.jaxrs.mapper;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
public class KeyValueJson {
    private String key;
    private String value;
    private Integer id;
    private TYPE type;

    public enum TYPE {
        UNIT,
        ENABLE_PAYLOAD,
        VARIABLE_MAPPER
    }

    public KeyValueJson() {

    }

    public KeyValueJson(String key, String value, TYPE type) {
        this(key, value, null, type);
    }

    public KeyValueJson(String key, String value, Integer id, TYPE type) {
        this.key = key;
        this.value = value;
        this.id = id;
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public TYPE getType() {
        return type;
    }

    public void setType(TYPE type) {
        this.type = type;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
