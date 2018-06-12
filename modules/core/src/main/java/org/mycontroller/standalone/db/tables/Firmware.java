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

import java.io.Serializable;
import java.util.HashMap;

import org.mycontroller.standalone.db.DB_TABLES;

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
@DatabaseTable(tableName = DB_TABLES.FIRMWARE)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Firmware implements Serializable {

    /**  */
    private static final long serialVersionUID = 8953054072663748162L;
    public static final String KEY_ID = "id";
    public static final String KEY_TYPE_ID = "typeId";
    public static final String KEY_VERSION_ID = "versionId";
    public static final String KEY_TIMESTAMP = "timestamp";
    public static final String KEY_FIRMWARE_DATA_ID = "firmwareDataId";
    public static final String KEY_PROPERTIES = "properties";
    public static final String KEY_PROP_CRC = "crc";
    public static final String KEY_PROP_MD5_HEX = "md5hex";
    public static final String KEY_PROP_BLOCKS = "blocks";
    public static final String KEY_PROP_TYPE = "type";
    public static final String KEY_PROP_BLOCK_SIZE = "blockSize";

    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true, columnName = KEY_ID)
    private Integer id;

    @DatabaseField(canBeNull = false, uniqueCombo = true, columnName = KEY_TYPE_ID,
            foreign = true, foreignAutoRefresh = true, maxForeignAutoRefreshLevel = 1)
    private FirmwareType type;

    @DatabaseField(canBeNull = false, uniqueCombo = true, columnName = KEY_VERSION_ID,
            foreign = true, foreignAutoRefresh = true, maxForeignAutoRefreshLevel = 1)
    private FirmwareVersion version;

    @DatabaseField(canBeNull = false, columnName = KEY_TIMESTAMP)
    private Long timestamp;

    @DatabaseField(canBeNull = true, columnName = KEY_PROPERTIES, dataType = DataType.SERIALIZABLE)
    private HashMap<String, Object> properties;

    //These values are not stored on database, only to get from user
    private String fileString;
    private byte[] fileBytes;
    private String fileType;
    private Integer blockSize;

    public String getFirmwareName() {
        if (type != null && version != null) {
            return type.getName() + ":" + version.getVersion();
        }
        return "-";
    }

    public HashMap<String, Object> getProperties() {
        if (properties == null) {
            properties = new HashMap<String, Object>();
        }
        return properties;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Firmware)) {
            return false;
        }
        Firmware _other = (Firmware) other;
        if (!this.type.getId().equals(_other.type.getId())) {
            return false;
        }
        if (!this.version.getId().equals(_other.version.getId())) {
            return false;
        }
        if (!this.getFirmwareName().equals(_other.getFirmwareName())) {
            return false;
        }
        if (!this.properties.equals(_other.properties)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
        return result;
    }

}
