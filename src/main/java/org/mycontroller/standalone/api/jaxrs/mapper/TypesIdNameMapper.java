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
package org.mycontroller.standalone.api.jaxrs.mapper;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class TypesIdNameMapper {
    private Object id;
    private String displayName;
    private Object subId;
    private boolean ticked = false;

    public TypesIdNameMapper() {

    }

    public TypesIdNameMapper(Object id, String name) {
        this(id, null, name, false);
    }

    public TypesIdNameMapper(Object id, String name, boolean ticked) {
        this(id, null, name, ticked);
    }

    public TypesIdNameMapper(Object id, Object subId, String name) {
        this(id, subId, name, false);
    }

    public TypesIdNameMapper(Object id, Object subId, String name, boolean ticked) {
        this.id = id;
        this.subId = subId;
        this.displayName = name;
        this.ticked = ticked;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String name) {
        this.displayName = name;
    }

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public Object getSubId() {
        return subId;
    }

    public void setSubId(Object subId) {
        this.subId = subId;
    }

    public boolean isTicked() {
        return ticked;
    }

    public void setTicked(boolean ticked) {
        this.ticked = ticked;
    }

}
