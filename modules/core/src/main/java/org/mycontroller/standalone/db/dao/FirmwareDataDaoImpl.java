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
package org.mycontroller.standalone.db.dao;

import java.sql.SQLException;
import java.util.List;

import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.Firmware;
import org.mycontroller.standalone.db.tables.FirmwareData;

import com.j256.ormlite.support.ConnectionSource;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
public class FirmwareDataDaoImpl extends BaseAbstractDaoImpl<FirmwareData, Integer> implements FirmwareDataDao {

    public FirmwareDataDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, FirmwareData.class);
    }

    @Override
    public FirmwareData get(FirmwareData firmwareData) {
        return super.getById(firmwareData.getId());
    }

    @Override
    public List<FirmwareData> getAll(List<Integer> ids) {
        // Not supported
        return null;
    }

    @Override
    public void deleteByFirmwareId(Integer firmwareId) {
        super.delete(FirmwareData.KEY_FIRMWARE_ID, firmwareId);
    }

    @Override
    public FirmwareData getByFirmwareId(Integer firmwareId) {
        return super.get(FirmwareData.KEY_FIRMWARE_ID, firmwareId);
    }

    @Override
    public FirmwareData getByTypeVersion(Integer typeId, Integer versionId) {
        Firmware firmware = DaoUtils.getFirmwareDao().get(typeId, versionId);
        if (firmware != null) {
            return getByFirmwareId(firmware.getId());
        }
        return null;
    }

}
