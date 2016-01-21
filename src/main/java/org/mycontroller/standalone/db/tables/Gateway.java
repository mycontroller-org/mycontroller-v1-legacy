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
package org.mycontroller.standalone.db.tables;

import org.mycontroller.standalone.AppProperties.NETWORK_TYPE;
import org.mycontroller.standalone.AppProperties.STATE;
import org.mycontroller.standalone.db.DB_TABLES;
import org.mycontroller.standalone.gateway.GatewayUtils;
import org.mycontroller.standalone.gateway.GatewayUtils.TYPE;

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
 * @since 0.0.2
 */

@DatabaseTable(tableName = DB_TABLES.GATEWAY)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(includeFieldNames = true)
public class Gateway {
    public static final String KEY_ID = "id";
    public static final String KEY_NAME = "name";
    public static final String KEY_ENABLED = "enabled";
    public static final String KEY_TYPE = "type";
    public static final String KEY_STATE = "state";
    public static final String KEY_NETWORK_TYPE = "networkType";

    @DatabaseField(generatedId = true, columnName = KEY_ID)
    private Integer id;

    @DatabaseField(canBeNull = false, columnName = KEY_ENABLED)
    private Boolean enabled;

    @DatabaseField(canBeNull = false, unique = true, columnName = KEY_NAME)
    private String name;

    @DatabaseField(canBeNull = false, dataType = DataType.ENUM_STRING, columnName = KEY_TYPE)
    private TYPE type;

    @DatabaseField(canBeNull = false, dataType = DataType.ENUM_STRING, columnName = KEY_NETWORK_TYPE)
    private NETWORK_TYPE networkType;

    @DatabaseField(canBeNull = true)
    private Long timestamp;

    @DatabaseField(canBeNull = false, dataType = DataType.ENUM_STRING, columnName = KEY_STATE)
    private STATE state = STATE.UNAVAILABLE;

    @DatabaseField(canBeNull = true)
    private String statusMessage;

    @DatabaseField(canBeNull = true)
    private Long statusSince;

    @DatabaseField(canBeNull = true)
    private String variable1;

    @DatabaseField(canBeNull = true)
    private String variable2;

    @DatabaseField(canBeNull = true)
    private String variable3;

    @DatabaseField(canBeNull = true)
    private String variable4;

    @DatabaseField(canBeNull = true)
    private String variable5;

    @DatabaseField(canBeNull = true)
    private String variable6;

    @DatabaseField(canBeNull = true)
    private String variable7;

    @DatabaseField(canBeNull = true)
    private String variable8;

    @DatabaseField(canBeNull = true)
    private String variable9;

    @DatabaseField(canBeNull = true)
    private String variable10;

    public String getConnectionDetails() {
        return GatewayUtils.getConnectionDetails(this);
    }

    public void setState(STATE state) {
        if (this.state != state) {
            this.setStatusSince(System.currentTimeMillis());
        }
        this.state = state;
    }

}
