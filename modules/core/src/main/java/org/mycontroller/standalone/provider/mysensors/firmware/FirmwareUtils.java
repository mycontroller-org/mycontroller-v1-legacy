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
package org.mycontroller.standalone.provider.mysensors.firmware;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.Firmware;

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
    public static final int FIRMWARE_BLOCK_SIZE = 16;

    public static Firmware getFirmware(String fileName) {
        return getFirmware(new File(fileName));
    }

    public static void updateFirmwareFromHexString(Firmware firmware) {
        if (firmware != null && firmware.getFileString() != null) {
            getFirmware(new BufferedReader(new StringReader(firmware.getFileString())), firmware);
        } else {
            _logger.warn("Invalid Firmware! [{}]", firmware);
        }
    }

    public static Firmware getFirmware(File file) {
        try {
            return getFirmware(new BufferedReader(new FileReader(file)), null);
        } catch (FileNotFoundException ex) {
            _logger.warn("Invalid File!", ex);
        }
        return null;
    }

    public static Firmware getFirmware(BufferedReader reader, Firmware firmware) {
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
            int blocks = (end - start) / FIRMWARE_BLOCK_SIZE;
            int crc = 0xFFFF;
            for (int i = 0; i < blocks * FIRMWARE_BLOCK_SIZE; ++i) {
                crc = crcUpdate(crc, (fwdata.get(i) & 0xFF));
            }
            if (firmware == null) {
                firmware = new Firmware();
            }
            firmware.setBlocks(blocks);
            firmware.setCrc(crc);
            firmware.setData(fwdata);
            firmware.setFileString(null);

            return firmware;
        } catch (IOException ex) {
            _logger.error("Exception, ", ex);
        }
        return null;
    }

    public static int crcUpdate(int old, int value) {
        int c = old ^ value;
        for (int i = 0; i < 8; ++i) {
            if ((c & 1) > 0) {
                c = ((c >> 1) ^ 0xA001);
            } else {
                c = (c >> 1);
            }
        }
        return c;
    }

    public static synchronized void deleteFirmwareType(int id) {
        List<Firmware> firmwares = DaoUtils.getFirmwareDao().getAllFirmwareByType(id);
        for (Firmware firmware : firmwares) {
            DaoUtils.getFirmwareDao().delete(firmware);
        }
        DaoUtils.getFirmwareTypeDao().deleteById(id);
    }

    public static synchronized void deleteFirmwareVersion(int id) {
        List<Firmware> firmwares = DaoUtils.getFirmwareDao().getAllFirmwareByVersion(id);
        for (Firmware firmware : firmwares) {
            DaoUtils.getFirmwareDao().delete(firmware);
        }
        DaoUtils.getFirmwareVersionDao().deleteById(id);
    }

    public static synchronized void deleteFirmware(int id) {
        DaoUtils.getFirmwareDao().deleteById(id);
    }

    public static synchronized void createFirmware(Firmware firmware) {
        FirmwareUtils.updateFirmwareFromHexString(firmware);
        firmware.setTimestamp(System.currentTimeMillis());
        DaoUtils.getFirmwareDao().create(firmware);
    }
}
