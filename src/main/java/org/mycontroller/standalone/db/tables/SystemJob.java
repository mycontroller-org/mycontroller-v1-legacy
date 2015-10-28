/**
 * Copyright (C) 2015 Jeeva Kandasamy (jkandasa@gmail.com)
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
package org.mycontroller.standalone.db.tables;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@DatabaseTable(tableName = "system_job")
@JsonIgnoreProperties(ignoreUnknown = true)
public class SystemJob {
    public static final String ENABLED = "enabled";

    public SystemJob() {

    }

    public SystemJob(String name, String cron, Boolean enabled, String className) {
        this.name = name;
        this.cron = cron;
        this.enabled = enabled;
        this.className = className;
        this.updateTime = System.currentTimeMillis();
    }

    @DatabaseField(generatedId = true)
    private Integer id;
    @DatabaseField(unique = true, canBeNull = false)
    private String name;
    @DatabaseField(canBeNull = false)
    private String cron;
    @DatabaseField(canBeNull = false, columnName = ENABLED)
    private Boolean enabled;
    @DatabaseField(canBeNull = false)
    private String className;
    @DatabaseField(canBeNull = false)
    private Long updateTime;

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
