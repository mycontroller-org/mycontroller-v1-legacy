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
package org.mycontroller.standalone.firmware;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.mapdb.HTreeMap;
import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.Firmware;
import org.mycontroller.standalone.db.tables.FirmwareData;
import org.mycontroller.standalone.exceptions.McBadRequestException;
import org.mycontroller.standalone.offheap.OffHeapFactory;
import org.mycontroller.standalone.utils.JsonUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FirmwareUtils {
    public static final int FIRMWARE_BLOCK_SIZE_HEX = 16;
    public static final int FIRMWARE_BLOCK_SIZE_BIN = 200;

    public static final String FIRMWARE_MAP = "mc_firmware_map";
    private static HTreeMap<String, FirmwareData> FIRMWARE_OFFLINE_MAP = null;

    private static final String FIRMWARE_DATA_FILE_FORMAT = "%s.json";

    public static HTreeMap<String, FirmwareData> getFirmwareMap() {
        if (FIRMWARE_OFFLINE_MAP == null) {
            OffHeapFactory.store().delete(FIRMWARE_MAP);//Remove existing map
            FIRMWARE_OFFLINE_MAP = OffHeapFactory.store().getHashMap(FIRMWARE_MAP);
            FIRMWARE_OFFLINE_MAP.clear();
        }
        return FIRMWARE_OFFLINE_MAP;
    }

    public enum FILE_TYPE {
        HEX("Hex"),
        BIN("bin");

        public static FILE_TYPE get(int id) {
            for (FILE_TYPE type : values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

        private String text;

        public String getText() {
            return this.text;
        }

        private FILE_TYPE(String text) {
            this.text = text;
        }

        public static FILE_TYPE fromString(String text) {
            if (text != null) {
                for (FILE_TYPE type : FILE_TYPE.values()) {
                    if (text.equalsIgnoreCase(type.getText())) {
                        return type;
                    }
                }
            }
            return null;
        }
    }

    private static void updateFirmwareBin(byte[] fileBytes, Firmware firmware, FirmwareData firmwareData) {
        int blankSize = fileBytes.length % FIRMWARE_BLOCK_SIZE_BIN;
        int blocks = fileBytes.length / FIRMWARE_BLOCK_SIZE_BIN;
        if (blankSize > 0) {
            blocks++;
        }
        ArrayList<Byte> fwdata = new ArrayList<Byte>();
        for (int index = 0; index < fileBytes.length; index++) {
            fwdata.add(fileBytes[index]);
        }
        String md5String = DigestUtils.md5Hex(fileBytes);
        _logger.debug("Blocks:{}, bytes:{}, actual bytes:{}", blocks, blocks * FIRMWARE_BLOCK_SIZE_BIN, fwdata.size());
        firmware.getProperties().put(Firmware.KEY_PROP_MD5_HEX, md5String);
        firmware.getProperties().put(Firmware.KEY_PROP_BLOCKS, blocks);
        firmwareData.setData(fwdata);
        firmware.setFileString(null);
    }

    private static void updateFirmwareHex(BufferedReader reader, Firmware firmware, FirmwareData firmwareData) {
        try {
            String line;
            int start = 0;
            int end = 0;
            ArrayList<Byte> fwdata = new ArrayList<Byte>();

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.length() > 0) {
                    while (!line.substring(0, 1).endsWith(":")) {
                        line = line.substring(1);
                    }
                    int reclen = Integer.parseInt(line.substring(1, 3), 16);
                    int offset = Integer.parseInt(line.substring(3, 7), 16);
                    int rectype = Integer.parseInt(line.substring(7, 9), 16);
                    String data = line.substring(9, 9 + 2 * reclen);
                    //int chksum = Integer.parseInt(line.substring(9 + (2 * reclen), 9 + (2 * reclen) + 2), 16);
                    if (rectype == 0) {
                        if ((start == 0) && (end == 0)) {
                            if (offset % 128 > 0) {
                                throw new Error("error loading hex file - offset can't be devided by 128");
                            }
                            start = offset;
                            end = offset;
                        }
                        if (offset < end) {
                            throw new Error("error loading hex file - offset lower than end");
                        }
                        while (offset > end) {
                            fwdata.add((byte) 255);
                            end++;
                        }
                        for (int i = 0; i < reclen; i++) {
                            fwdata.add((byte) Integer.parseInt(data.substring(i * 2, (i * 2) + 2), 16));
                        }
                        end += reclen;
                    }
                }
            }

            int pad = end % 128; // ATMega328 has 64 words per page / 128 bytes per page
            for (int i = 0; i < 128 - pad; i++) {
                fwdata.add((byte) 255);
                end++;
            }
            int blocks = (end - start) / FIRMWARE_BLOCK_SIZE_HEX;
            int crc = 0xFFFF;
            for (int index = 0; index < fwdata.size(); ++index) {
                crc ^= fwdata.get(index) & 0xFF;
                for (int bit = 0; bit < 8; ++bit) {
                    if ((crc & 0x01) == 0x01) {
                        crc = ((crc >> 1) ^ 0xA001);
                    } else {
                        crc = (crc >> 1);
                    }
                }
            }
            firmware.getProperties().put(Firmware.KEY_PROP_CRC, crc);
            firmware.getProperties().put(Firmware.KEY_PROP_BLOCKS, blocks);
            firmwareData.setData(fwdata);
            firmware.setFileString(null);
        } catch (IOException ex) {
            _logger.error("Exception, ", ex);
        }
    }

    private static void deleteFirmwares(List<Firmware> firmwares) {
        for (Firmware firmware : firmwares) {
            deleteFirmware(firmware);
        }
    }

    private static void deleteFirmware(Firmware firmware) {
        removeFirmewareOnOfflineMap(firmware);
        //DaoUtils.getFirmwareDataDao().deleteByFirmwareId(firmware.getId());
        firmware = DaoUtils.getFirmwareDao().getById(firmware.getId());
        deleteFirmwareDateFromDisk(firmware.getType().getId(), firmware.getVersion().getId());
        DaoUtils.getFirmwareDao().delete(firmware);
    }

    public static synchronized void deleteFirmwareType(int id) {
        deleteFirmwares(DaoUtils.getFirmwareDao().getAllFirmwareByType(id));
        DaoUtils.getFirmwareTypeDao().deleteById(id);
    }

    public static synchronized void deleteFirmwareVersion(int id) {
        deleteFirmwares(DaoUtils.getFirmwareDao().getAllFirmwareByVersion(id));
        DaoUtils.getFirmwareVersionDao().deleteById(id);
    }

    public static synchronized void deleteFirmware(int id) {
        deleteFirmware(DaoUtils.getFirmwareDao().getById(id));
    }

    public static void createUpdateFirmware(Firmware firmware, FILE_TYPE fileType) throws McBadRequestException {
        if (firmware == null) {
            throw new McBadRequestException("Firmware should not be NULL");
        } else if (firmware.getFileString() == null && firmware.getFileBytes() == null) {
            throw new McBadRequestException("Firmware data should not be NULL");
        }
        removeFirmewareOnOfflineMap(firmware);
        FirmwareData firmwareData = null;
        if (firmware.getId() != null) {
            //firmwareData = DaoUtils.getFirmwareDataDao().getByFirmwareId(firmware.getId());
            firmwareData = loadFirmwareDataFromDisk(firmware.getId());
        } else {
            firmwareData = FirmwareData.builder().build();
        }
        updateProperties(firmware, fileType);
        switch (fileType) {
            case BIN:
                //updateFirmwareBin(firmware.getFileString().getBytes(), firmware);
                updateFirmwareBin(firmware.getFileBytes(), firmware, firmwareData);
                break;
            case HEX:
                if (firmware != null && firmware.getFileString() != null) {
                    updateFirmwareHex(new BufferedReader(new StringReader(firmware.getFileString())), firmware,
                            firmwareData);
                } else {
                    _logger.warn("Invalid Firmware! [{}]", firmware);
                }
                break;
            default:
                break;
        }
        firmware.setTimestamp(System.currentTimeMillis());
        if (firmware.getId() != null) {
            DaoUtils.getFirmwareDao().update(firmware);
            firmware = DaoUtils.getFirmwareDao().getById(firmware.getId());
            firmwareData.setFirmware(firmware);
            //DaoUtils.getFirmwareDataDao().update(firmwareData);
            writeFirmwareDataToDisk(firmwareData);
        } else {
            DaoUtils.getFirmwareDao().create(firmware);
            firmware = DaoUtils.getFirmwareDao().get(firmware.getType().getId(), firmware.getVersion().getId());
            firmwareData.setFirmware(firmware);
            //DaoUtils.getFirmwareDataDao().create(firmwareData);
            writeFirmwareDataToDisk(firmwareData);
        }

    }

    private static void updateProperties(Firmware firmware, FILE_TYPE fileType) {
        if (firmware.getBlockSize() != null) {
            return;
        }
        firmware.getProperties().put(Firmware.KEY_PROP_TYPE, fileType.getText());
        switch (fileType) {
            case BIN:
                firmware.getProperties().put(Firmware.KEY_PROP_BLOCK_SIZE, FIRMWARE_BLOCK_SIZE_BIN);
                firmware.getProperties().remove(Firmware.KEY_PROP_CRC);
                break;
            case HEX:
                firmware.getProperties().put(Firmware.KEY_PROP_BLOCK_SIZE, FIRMWARE_BLOCK_SIZE_HEX);
                firmware.getProperties().remove(Firmware.KEY_PROP_MD5_HEX);
                break;
            default:
                break;
        }
        firmware.setBlockSize((Integer) firmware.getProperties().get(Firmware.KEY_PROP_BLOCK_SIZE));
    }

    private static String getFirmwareDataOfflineMapName(Integer typeId, Integer versionId) {
        return typeId + "_" + versionId;
    }

    private static void removeFirmewareOnOfflineMap(Firmware firmware) {
        getFirmwareMap().remove(
                getFirmwareDataOfflineMapName(firmware.getType().getId(), firmware.getVersion().getId()));
    }

    public static FirmwareData getFirmwareDataFromOfflineMap(Integer typeId, Integer versionId) {
        FirmwareData firmwareData = getFirmwareMap().get(getFirmwareDataOfflineMapName(typeId, versionId));
        if (firmwareData == null) {
            //firmwareData = DaoUtils.getFirmwareDataDao().getByTypeVersion(typeId, versionId);
            firmwareData = loadFirmwareDataFromDisk(typeId, versionId);
            if (firmwareData != null) {
                getFirmwareMap().put(getFirmwareDataOfflineMapName(typeId, versionId), firmwareData);
            }
        }
        return firmwareData;
    }

    public static boolean deleteFirmwareDateFromDisk(int typeId, int versionId) {
        File fwdFile = FileUtils.getFile(AppProperties.getInstance().getFirmwaresDataDirectory(),
                String.format(FIRMWARE_DATA_FILE_FORMAT, typeId, versionId));
        boolean isDeleted = fwdFile.delete();
        _logger.debug("Firmware data deleted from disk. File:{}, isDeleted? ", fwdFile.getAbsolutePath(), isDeleted);
        return isDeleted;
    }

    public static FirmwareData loadFirmwareDataFromDisk(int typeId, int versionId) {
        Firmware firmware = DaoUtils.getFirmwareDao().get(typeId, versionId);
        return loadFirmwareDataFromDisk(firmware.getId());
    }

    public static FirmwareData loadFirmwareDataFromDisk(int firmwareId) {
        FirmwareData firmwareData = (FirmwareData) JsonUtils.loads(
                FirmwareData.class, AppProperties.getInstance().getFirmwaresDataDirectory(),
                String.format(FIRMWARE_DATA_FILE_FORMAT, firmwareId));
        // update firmware information
        Firmware firmware = DaoUtils.getFirmwareDao().getById(firmwareData.getFirmware().getId());
        if (!firmware.equals(firmwareData.getFirmware())) {
            firmwareData.setFirmware(firmware);
            // update the changes in to disk
            writeFirmwareDataToDisk(firmwareData);
        }
        _logger.debug("Firmware data loads from disk. {}", firmwareData.getFirmware());
        return firmwareData;
    }

    public static void writeFirmwareDataToDisk(FirmwareData firmwareData) {
        int firmwareId = firmwareData.getFirmware().getId();
        JsonUtils.dumps(firmwareData, AppProperties.getInstance().getFirmwaresDataDirectory(),
                String.format(FIRMWARE_DATA_FILE_FORMAT, firmwareId));
        _logger.debug("Firmware data stored in to disk. {}", firmwareData.getFirmware());
    }
}
