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
package org.mycontroller.standalone.operation.model;

import org.mycontroller.standalone.db.tables.OperationTable;
import org.mycontroller.standalone.db.tables.User;
import org.mycontroller.standalone.operation.IOperationEngine;
import org.mycontroller.standalone.operation.OperationUtils.OPERATION_TYPE;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.ToString;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Data
@ToString
public abstract class Operation implements IOperationEngine {
    private Integer id;
    private Boolean enabled;
    private User user;
    private String name;
    private OPERATION_TYPE type;
    private Long lastExecution;

    public abstract String getOperationString();

    public void updateOperation(OperationTable operationTable) {
        id = operationTable.getId();
        enabled = operationTable.getEnabled();
        user = operationTable.getUser();
        name = operationTable.getName();
        type = operationTable.getType();
        lastExecution = operationTable.getLastExecution();
    }

    @JsonIgnore
    public OperationTable getOperationTable() {
        return OperationTable.builder()
                .id(id)
                .enabled(enabled)
                .user(user)
                .name(name)
                .type(type)
                .lastExecution(lastExecution)
                .build();
    }

    //These methods are used for JSON
    @JsonGetter("type")
    private String getTypeString() {
        return type.getText();
    }

    @JsonGetter("user")
    @JsonIgnoreProperties({ "enabled", "username", "fullName", "email", "password", "validity", "permissions", "name",
            "permission", "permissions", "allowedResources" })
    private User getUserJson() {
        return user;
    }

}
