package org.mycontroller.standalone.api;

import java.io.IOException;

import org.apache.commons.io.FileUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileApi {
    public String readFile(String fileNameWithPath) {
        try {
            return FileUtils.readFileToString(FileUtils.getFile(fileNameWithPath));
        } catch (IOException ex) {
            _logger.error("Exception,", ex);
            return null;
        }
    }
}
