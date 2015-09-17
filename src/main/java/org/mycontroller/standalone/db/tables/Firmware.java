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

import java.util.ArrayList;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@DatabaseTable
public class Firmware {

    public static final String TYPE_ID = "type_id";
    public static final String VERSION_ID = "version_id";

    public Firmware() {

    }

    public Firmware(Integer id) {
        this.id = id;
    }

    public Firmware(Integer typeId, Integer versionId) {
        this.type.setId(typeId);
        this.version.setId(versionId);
    }

    @DatabaseField(generatedId = true)
    private Integer id;

    @DatabaseField(canBeNull = false, uniqueCombo = true, columnName = TYPE_ID,
            foreign = true, foreignAutoRefresh = true, maxForeignAutoRefreshLevel = 1)
    private FirmwareType type;

    @DatabaseField(canBeNull = false, uniqueCombo = true, columnName = VERSION_ID,
            foreign = true, foreignAutoRefresh = true, maxForeignAutoRefreshLevel = 1)
    private FirmwareVersion version;

    @DatabaseField(canBeNull = false)
    private Long timestamp;

    @DatabaseField(canBeNull = false)
    private Integer blocks;

    @DatabaseField(canBeNull = false)
    private Integer crc;

    @DatabaseField(canBeNull = false, dataType = DataType.SERIALIZABLE)
    private ArrayList<Byte> data;
    
    private String hexFileString;

    public Integer getId() {
        return id;
    }

    public FirmwareType getType() {
        return type;
    }

    public FirmwareVersion getVersion() {
        return version;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public Integer getBlocks() {
        return blocks;
    }

    public Integer getCrc() {
        return crc;
    }

    public ArrayList<Byte> getData() {
        return data;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setType(FirmwareType type) {
        this.type = type;
    }

    public void setVersion(FirmwareVersion version) {
        this.version = version;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public void setBlocks(Integer blocks) {
        this.blocks = blocks;
    }

    public void setCrc(Integer crc) {
        this.crc = crc;
    }

    public void setData(ArrayList<Byte> data) {
        this.data = data;
    }
    
    public void setFirmwareName(String firmwareName) {
        //This is ignore json serialization error
    }

    public String getFirmwareName() {
        if (type != null && version != null) {
            return type.getName() + ":" + version.getName();
        }
        return "-";
    }

    public String getHexFileString() {
        return hexFileString;
    }

    public void setHexFileString(String hexFileString) {
        this.hexFileString = hexFileString;
    }
    
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Id:").append(this.id);
        builder.append(", Timestamp:").append(this.timestamp);
        builder.append(", Blocks:").append(this.blocks);
        builder.append(", CRC:").append(this.crc);
        builder.append(", Type:[").append(this.type);
        builder.append("], Version:[").append(this.version).append("]");
        return builder.toString();
    }

}
