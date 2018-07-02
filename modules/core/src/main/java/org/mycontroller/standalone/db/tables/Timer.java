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
package org.mycontroller.standalone.db.tables;

import java.util.ArrayList;
import java.util.List;

import org.mycontroller.standalone.db.DB_TABLES;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.operation.OperationUtils;
import org.mycontroller.standalone.operation.model.Operation;
import org.mycontroller.standalone.timer.TimerUtils;
import org.mycontroller.standalone.timer.TimerUtils.FREQUENCY_TYPE;
import org.mycontroller.standalone.timer.TimerUtils.TIMER_TYPE;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@DatabaseTable(tableName = DB_TABLES.TIMER)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(includeFieldNames = true)
public class Timer {
    public static final String KEY_ID = "id";
    public static final String KEY_NAME = "name";
    public static final String KEY_ENABLED = "enabled";
    public static final String KEY_TIMER_TYPE = "timerType";
    public static final String KEY_FREQUENCY = "frequency";
    public static final String KEY_FREQUENCY_DATA = "frequencyData";
    public static final String KEY_TRIGGER_TIME = "triggerTime";
    public static final String KEY_VALIDITY_FROM = "validityFrom";
    public static final String KEY_VALIDITY_TO = "validityTo";
    public static final String KEY_LAST_FIRE = "lastFire";
    public static final String KEY_INTERNAL_VARIABLE1 = "internalVariable1";

    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true, columnName = KEY_ID)
    private Integer id;

    @DatabaseField(canBeNull = false, columnName = KEY_ENABLED)
    private Boolean enabled;

    @DatabaseField(canBeNull = false, uniqueCombo = true, columnName = KEY_NAME)
    private String name;

    @DatabaseField(dataType = DataType.ENUM_STRING, canBeNull = false, columnName = KEY_TIMER_TYPE)
    private TIMER_TYPE timerType;

    @DatabaseField(dataType = DataType.ENUM_STRING, canBeNull = true, columnName = KEY_FREQUENCY)
    private FREQUENCY_TYPE frequencyType;

    @DatabaseField(canBeNull = true, columnName = KEY_FREQUENCY_DATA)
    private String frequencyData;

    @DatabaseField(canBeNull = true, columnName = KEY_TRIGGER_TIME)
    private Long triggerTime;

    @DatabaseField(canBeNull = true, columnName = KEY_VALIDITY_FROM)
    private Long validityFrom;

    @DatabaseField(canBeNull = true, columnName = KEY_VALIDITY_TO)
    private Long validityTo;

    @DatabaseField(canBeNull = true, columnName = KEY_LAST_FIRE)
    private Long lastFire;

    @DatabaseField(canBeNull = true, columnName = KEY_INTERNAL_VARIABLE1)
    private String internalVariable1;

    private String targetClass;

    private List<Integer> operationIds;

    @JsonIgnore
    private List<Operation> operations;

    public List<Integer> getOperationIds() {
        if (operationIds == null) {
            if (id != null) {
                operationIds = DaoUtils.getOperationTimerMapDao().getOperationIdsByTimerId(getId());
            } else {
                operationIds = new ArrayList<Integer>();
            }
        }
        return operationIds;
    }

    @JsonIgnore
    public List<Operation> getOperations() {
        if (operations == null) {
            operations = new ArrayList<Operation>();
            List<OperationTable> OperationsInDb = DaoUtils.getOperationDao().getByTimerId(getId());
            for (OperationTable operationTable : OperationsInDb) {
                Operation operationModel = OperationUtils.getOperation(operationTable);
                if (operationModel != null) {
                    operations.add(operationModel);
                }
            }
        }
        return operations;
    }

    @JsonGetter("operations")
    private List<String> getOperationsList() {
        List<String> operations = new ArrayList<String>();
        for (Operation operation : getOperations()) {
            operations.add(operation.getName());
        }
        return operations;
    }

    public String getTimerDataString() {
        try {
            return TimerUtils.getTimerDataString(this);
        } catch (Exception ex) {
            return "Error: " + ex.getMessage();
        }
    }

    public String getValidityString() {
        return TimerUtils.getValidityString(this);
    }

    public void setFrequencyData(String frequencyData) {
        if (frequencyData != null && frequencyData.length() > 0) {
            this.frequencyData = frequencyData;
        }
    }

}
