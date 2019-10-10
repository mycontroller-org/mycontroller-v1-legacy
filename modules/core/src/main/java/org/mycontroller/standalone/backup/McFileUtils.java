/*
 * Copyright 2015-2019 Jeeva Kandasamy (jkandasa@gmail.com)
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
package org.mycontroller.standalone.backup;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.api.jaxrs.model.McFile;
import org.mycontroller.standalone.api.jaxrs.model.Query;
import org.mycontroller.standalone.api.jaxrs.model.QueryResponse;
import org.mycontroller.standalone.exceptions.McBadRequestException;
import org.mycontroller.standalone.utils.McUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class McFileUtils {
    public static final String KEY_NAME = "name";
    public static final String[] MC_FILE_SUFFIX_FILTER = { "zip", "ZIP" };

    public static void removeOldFiles(String prefix, int retainMax) throws IOException {
        String[] filter = { "zip" };
        Collection<File> zipFiles = FileUtils.listFiles(
                FileUtils.getFile(AppProperties.getInstance().getBackupSettings().getBackupLocation()),
                filter, true);

        List<McFile> backupFiles = new ArrayList<McFile>();
        for (File zipFile : zipFiles) {
            if (zipFile.getName().startsWith(prefix)) {//Filter with file name
                backupFiles.add(McFile.builder()
                        .name(zipFile.getName())
                        .size(zipFile.length())
                        .timestamp(zipFile.lastModified())
                        .canonicalPath(zipFile.getCanonicalPath())
                        .build());
            }
        }
        //Do order reverse
        Collections.sort(backupFiles, Collections.reverseOrder());

        if (backupFiles.size() > retainMax) {
            _logger.debug("Available files:{}, Maximum files to retain:{}", backupFiles.size(), retainMax);
            for (int deleteIndex = retainMax; deleteIndex < backupFiles.size(); deleteIndex++) {
                try {
                    FileUtils.forceDelete(FileUtils.getFile(backupFiles.get(deleteIndex).getCanonicalPath()));
                    _logger.debug("File deleted, {}", backupFiles.get(deleteIndex));
                } catch (Exception ex) {
                    _logger.error("File deletion failed", ex);
                }
            }
        } else {
            _logger.debug("Available files count:{}", backupFiles.size());
        }
    }

    public static String copyStaticFilesAndCreateZipFile(String applicationBackupDir) throws IOException {
        //Copy mycontroller.properties file
        FileUtils.copyFile(
                FileUtils.getFile(System.getProperty("mc.conf.file")),
                FileUtils.getFile(applicationBackupDir + File.separator + Commons.APP_PROPERTIES_FILENAME),
                true);
        FileUtils.copyFile(
                FileUtils.getFile(System.getProperty("logback.configurationFile")),
                FileUtils.getFile(applicationBackupDir + File.separator + "logback.xml"),
                true);
        if (AppProperties.getInstance().isWebHttpsEnabled()) {
            String KEY_STORE_FILE = applicationBackupDir + File.separator
                    + FileUtils.getFile(AppProperties.getInstance().getWebSslKeystoreFile()).getName();
            FileUtils.copyFile(
                    FileUtils.getFile(AppProperties.getInstance().getWebSslKeystoreFile()),
                    FileUtils.getFile(KEY_STORE_FILE),
                    true);
        }
        //Copy resources directory
        FileUtils.copyDirectory(
                FileUtils.getFile(AppProperties.getInstance().getResourcesLocation()),
                FileUtils.getFile(applicationBackupDir + File.separator + Commons.RESOURCES_LOCATION),
                true);
        _logger.debug("Copied all the files");
        //create zip file
        String zipFileName = applicationBackupDir + ".zip";
        McUtils.createZipFile(applicationBackupDir, zipFileName);
        _logger.debug("zip file creation done");
        //clean temporary files
        FileUtils.deleteDirectory(FileUtils.getFile(applicationBackupDir));
        return zipFileName;
    }

    public static void extractZipFile(String zipFileName, String destination)
            throws FileNotFoundException, IOException {
        _logger.info("Files, zip:{}, des:{}", zipFileName, destination);
        ZipFile zipFile = new ZipFile(zipFileName);
        Enumeration<?> enu = zipFile.entries();
        //create destination if not exists
        FileUtils.forceMkdir(FileUtils.getFile(destination));
        while (enu.hasMoreElements()) {
            ZipEntry zipEntry = (ZipEntry) enu.nextElement();
            String name = zipEntry.getName();
            long size = zipEntry.getSize();
            long compressedSize = zipEntry.getCompressedSize();
            _logger.debug("name:{} | size:{} | compressed size:{}", name, size, compressedSize);
            File file = FileUtils.getFile(destination + File.separator + name);
            //Create destination if it's not available
            if (name.endsWith(File.separator)) {
                file.mkdirs();
                continue;
            }

            File parent = file.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }

            InputStream is = zipFile.getInputStream(zipEntry);
            FileOutputStream fos = new FileOutputStream(file);
            byte[] bytes = new byte[1024];
            int length;
            while ((length = is.read(bytes)) >= 0) {
                fos.write(bytes, 0, length);
            }
            is.close();
            fos.close();
        }
        zipFile.close();
    }

    public static void restoreResourceFiles(String extractedLocation) throws IOException {
        //Restore resources directory
        //Remove old files
        FileUtils.deleteQuietly(FileUtils.getFile(AppProperties.getInstance().getResourcesLocation()));
        //restore resources directory, if exists
        File resourcesDir = FileUtils.getFile(extractedLocation + File.separator + Commons.RESOURCES_LOCATION);
        if (resourcesDir.exists()) {
            FileUtils.copyDirectory(
                    resourcesDir, FileUtils.getFile(AppProperties.getInstance().getResourcesLocation()), true);
        }
    }

    public static McFile getMcFile(String fileLocation, String suppliedFileName) throws IOException,
            McBadRequestException {
        if (suppliedFileName == null) {
            throw new McBadRequestException("File can not be null");
        }
        String backupCanonicalPath = FileUtils.getFile(fileLocation).getCanonicalPath();
        String fileFullName = fileLocation + suppliedFileName;
        if (McUtils.isInScope(backupCanonicalPath, fileFullName)) {
            File bkpFile = FileUtils.getFile(fileFullName);
            return McFile.builder()
                    .name(bkpFile.getName())
                    .canonicalPath(bkpFile.getCanonicalPath())
                    .timestamp(bkpFile.lastModified())
                    .size(bkpFile.length())
                    .build();
        } else {
            throw new McBadRequestException("Trying to access file from outside specified scope");
        }
    }

    public static void deleteMcFiles(String mcFileLocation, List<String> mcFiles) throws IOException {
        String fileLocation = McUtils.getDirectoryLocation(FileUtils.getFile(mcFileLocation).getCanonicalPath());
        for (String mcFile : mcFiles) {
            String fileFullPath = fileLocation + mcFile;
            if (McUtils.isInScope(fileLocation, fileFullPath)) {
                if (FileUtils.deleteQuietly(FileUtils.getFile(fileFullPath))) {
                    _logger.debug("File deleted successfully! {}", fileFullPath);
                } else {
                    _logger.warn("File deletion failed! {}", fileFullPath);
                }
            } else {
                _logger.warn("Trying to delete file from outside scope! Filepath:{}, CanonicalPath:{}",
                        fileFullPath, FileUtils.getFile(fileFullPath).getCanonicalPath());
            }
        }
    }

    public static QueryResponse getMcFiles(String baseLocation, String fileNameIdentity,
            HashMap<String, Object> filters)
            throws IOException {
        Query query = Query.get(filters);
        String locationCanonicalPath = McUtils
                .getDirectoryLocation(FileUtils.getFile(baseLocation).getCanonicalPath());

        if (FileUtils.getFile(locationCanonicalPath).exists()) {
            List<McFile> files = new ArrayList<McFile>();

            //Filters
            //Extension filter
            SuffixFileFilter extensionFilter = new SuffixFileFilter(MC_FILE_SUFFIX_FILTER, IOCase.INSENSITIVE);

            //name filter
            IOFileFilter nameFileFilter = null;
            @SuppressWarnings("unchecked")
            List<String> fileNames = (List<String>) query.getFilters().get(KEY_NAME);
            if (fileNames == null) {
                fileNames = new ArrayList<String>();
            }
            fileNames.add(fileNameIdentity);

            if (fileNames != null && !fileNames.isEmpty()) {
                for (String fileName : fileNames) {
                    if (nameFileFilter == null) {
                        nameFileFilter = FileFilterUtils.and(
                                new WildcardFileFilter("*" + fileName + "*", IOCase.INSENSITIVE));
                    } else {
                        nameFileFilter = FileFilterUtils.and(nameFileFilter,
                                new WildcardFileFilter("*" + fileName + "*", IOCase.INSENSITIVE));
                    }
                }
            }
            //Combine all filters
            IOFileFilter finalFileFilter = null;
            if (nameFileFilter != null) {
                finalFileFilter = FileFilterUtils.and(extensionFilter, nameFileFilter);
            } else {
                finalFileFilter = extensionFilter;
            }
            List<File> mcFiles = new ArrayList<File>(FileUtils.listFiles(FileUtils.getFile(locationCanonicalPath),
                    finalFileFilter, TrueFileFilter.INSTANCE));
            query.setFilteredCount((long) mcFiles.size());
            //Get total items without filter
            query.setTotalItems((long) FileUtils.listFiles(FileUtils.getFile(locationCanonicalPath),
                    extensionFilter, TrueFileFilter.INSTANCE).size());
            int fileFrom;
            int fileTo;
            if (query.getPageLimit() == -1) {
                fileTo = mcFiles.size();
                fileFrom = 0;
            } else {
                fileFrom = query.getStartingRow().intValue();
                fileTo = (int) (query.getPage() * query.getPageLimit());
            }
            for (File mcFile : mcFiles) {
                String name = mcFile.getCanonicalPath().replace(locationCanonicalPath, "");
                files.add(McFile.builder()
                        .name(name)
                        .size(mcFile.length())
                        .timestamp(mcFile.lastModified())
                        .canonicalPath(mcFile.getCanonicalPath())
                        .build());
            }

            if (!files.isEmpty()) {
                //Do order reverse
                Collections.sort(files, Collections.reverseOrder());
                if (fileFrom < files.size()) {
                    files = files.subList(Math.max(0, fileFrom), Math.min(fileTo, files.size()));
                }
            }
            return QueryResponse.builder().data(files).query(query).build();

        } else {
            throw new FileNotFoundException("File location not found: " + locationCanonicalPath);
        }
    }

}
