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

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */

@DatabaseTable(tableName = DB_TABLES.GATEWAY)
public class Gateway {
    public static final String KEY_ID = "id";
    public static final String KEY_NAME = "name";
    public static final String KEY_ENABLED = "enabled";
    public static final String KEY_TYPE = "type";
    public static final String KEY_STATE = "state";
    public static final String KEY_NETWORK_TYPE = "networkType";

    public Gateway(Integer id) {
        this.id = id;
    }

    public Gateway() {

    }

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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getVariable1() {
        return variable1;
    }

    public void setVariable1(String variable1) {
        this.variable1 = variable1;
    }

    public String getVariable2() {
        return variable2;
    }

    public void setVariable2(String variable2) {
        this.variable2 = variable2;
    }

    public String getVariable3() {
        return variable3;
    }

    public void setVariable3(String variable3) {
        this.variable3 = variable3;
    }

    public String getVariable4() {
        return variable4;
    }

    public void setVariable4(String variable4) {
        this.variable4 = variable4;
    }

    public String getVariable5() {
        return variable5;
    }

    public void setVariable5(String variable5) {
        this.variable5 = variable5;
    }

    public String getVariable6() {
        return variable6;
    }

    public void setVariable6(String variable6) {
        this.variable6 = variable6;
    }

    public String getVariable7() {
        return variable7;
    }

    public void setVariable7(String variable7) {
        this.variable7 = variable7;
    }

    public String getVariable8() {
        return variable8;
    }

    public void setVariable8(String variable8) {
        this.variable8 = variable8;
    }

    public String getVariable9() {
        return variable9;
    }

    public void setVariable9(String variable9) {
        this.variable9 = variable9;
    }

    public String getVariable10() {
        return variable10;
    }

    public void setVariable10(String variable10) {
        this.variable10 = variable10;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String connectionStatus) {
        this.statusMessage = connectionStatus;
    }

    public Long getStatusSince() {
        return statusSince;
    }

    public void setStatusSince(Long statusSince) {
        this.statusSince = statusSince;
    }

    public String getConnectionDetails() {
        return GatewayUtils.getConnectionDetails(this);
    }

    public TYPE getType() {
        return type;
    }

    public void setType(TYPE type) {
        this.type = type;
    }

    public NETWORK_TYPE getNetworkType() {
        return networkType;
    }

    public void setNetworkType(NETWORK_TYPE networkType) {
        this.networkType = networkType;
    }

    public STATE getState() {
        return state;
    }

    public void setState(STATE state) {
        if (this.state != state) {
            this.setStatusSince(System.currentTimeMillis());
        }
        this.state = state;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Id:").append(this.id);
        builder.append(", Name:").append(this.name);
        builder.append(", Type:").append(this.type.getText());
        builder.append(", NetworkType:").append(this.networkType.getText());
        builder.append(", Enabled?:").append(this.enabled);
        builder.append(", Status:").append(this.state.getText());
        builder.append(", Connection Details:[").append(this.getConnectionDetails()).append("]");
        return builder.toString();
    }
}
