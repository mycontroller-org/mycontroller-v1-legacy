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
 * @since 0.0.3
 */
@DatabaseTable(tableName = DB_TABLES.FIRMWARE_DATA)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = { "data" })
public class FirmwareData implements Serializable {
    /**  */
    private static final long serialVersionUID = -3932679458678495012L;
    public static final String KEY_ID = "id";
    public static final String KEY_FIRMWARE_ID = "firmwareId";
    public static final String KEY_DATA = "data";

    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true, columnName = KEY_ID)
    private Integer id;

    @DatabaseField(canBeNull = true, columnName = KEY_FIRMWARE_ID, foreign = true, unique = true,
            foreignAutoRefresh = true, maxForeignAutoRefreshLevel = 2)
    private Firmware firmware;

    @DatabaseField(canBeNull = false, columnName = KEY_DATA, dataType = DataType.SERIALIZABLE)
    private ArrayList<Byte> data;

}
