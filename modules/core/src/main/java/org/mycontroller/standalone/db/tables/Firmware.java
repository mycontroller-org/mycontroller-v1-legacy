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
package org.mycontroller.standalone.db.tables;

import java.util.ArrayList;

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
@ToString(exclude = { "data" })
public class Firmware {

    public static final String KEY_ID = "id";
    public static final String KEY_TYPE_ID = "typeId";
    public static final String KEY_VERSION_ID = "versionId";

    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true, columnName = KEY_ID)
    private Integer id;

    @DatabaseField(canBeNull = false, uniqueCombo = true, columnName = KEY_TYPE_ID,
            foreign = true, foreignAutoRefresh = true, maxForeignAutoRefreshLevel = 1)
    private FirmwareType type;

    @DatabaseField(canBeNull = false, uniqueCombo = true, columnName = KEY_VERSION_ID,
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

    private String fileString;

    public String getFirmwareName() {
        if (type != null && version != null) {
            return type.getName() + ":" + version.getVersion();
        }
        return "-";
    }

}
