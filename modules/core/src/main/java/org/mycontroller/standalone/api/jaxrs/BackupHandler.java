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
package org.mycontroller.standalone.api.jaxrs;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.FileUtils;
import org.mycontroller.standalone.BackupRestore;
import org.mycontroller.standalone.ObjectFactory;
import org.mycontroller.standalone.api.jaxrs.json.ApiError;
import org.mycontroller.standalone.api.jaxrs.json.BackupFile;
import org.mycontroller.standalone.api.jaxrs.utils.RestUtils;
import org.mycontroller.standalone.settings.BackupSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */

@Path("/rest/backup")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RolesAllowed({ "admin" })
public class BackupHandler {
    private static final Logger _logger = LoggerFactory.getLogger(BackupHandler.class.getName());

    @GET
    @Path("/backupList")
    public Response getBackupList() {
        try {
            String[] filter = { "zip" };
            Collection<File> zipFiles = FileUtils.listFiles(
                    FileUtils.getFile(ObjectFactory.getAppProperties().getBackupSettings().getBackupLocation()),
                    filter, true);
            List<BackupFile> backupFiles = new ArrayList<BackupFile>();
            for (File zipFile : zipFiles) {
                if (zipFile.getName().contains(BackupRestore.FILE_NAME_IDENTITY)) {
                    backupFiles.add(BackupFile.builder()
                            .name(zipFile.getName())
                            .size(zipFile.length())
                            .timestamp(zipFile.lastModified())
                            .absolutePath(zipFile.getAbsolutePath())
                            .build());
                }

            }
            //Do order reverse
            Collections.sort(backupFiles, Collections.reverseOrder());
            return RestUtils.getResponse(Status.OK, backupFiles);
        } catch (Exception ex) {
            _logger.error("Error,", ex);
            return RestUtils.getResponse(Status.INTERNAL_SERVER_ERROR, new ApiError(ex.getMessage()));
        }

    }

    @GET
    @Path("/backupSettings")
    public Response getBackupSettings() {
        return RestUtils.getResponse(Status.OK, ObjectFactory.getAppProperties().getBackupSettings());
    }

    @PUT
    @Path("/backupSettings")
    public Response updateBackupSettings(BackupSettings backupSettings) {
        backupSettings.save();
        BackupSettings.reloadJob();//Reload backup job
        ObjectFactory.getAppProperties().setBackupSettings(BackupSettings.get());
        return RestUtils.getResponse(Status.OK);
    }

    @PUT
    @Path("/backupNow")
    public Response backupNow() {
        try {
            _logger.debug("Backup triggered.");
            BackupRestore.backup("on-demand");
        } catch (Exception ex) {
            _logger.error("Error,", ex);
            return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError(ex.getMessage()));
        }
        return RestUtils.getResponse(Status.OK);
    }

    @POST
    @Path("/delete")
    public Response deleteBackup(BackupFile backupFile) {
        try {
            FileUtils.forceDelete(FileUtils.getFile(backupFile.getAbsolutePath()));
            return RestUtils.getResponse(Status.OK);
        } catch (Exception ex) {
            return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError(ex.getMessage()));
        }
    }

    @POST
    @Path("/restore")
    public Response restore(BackupFile backupFile) {
        if (backupFile != null) {
            _logger.info("Restore triggered.");
            try {
                BackupRestore.restore(backupFile);
            } catch (Exception ex) {
                _logger.error("Error in restore,", ex);
                return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError(ex.getMessage()));
            }
        }
        return RestUtils.getResponse(Status.OK);
    }
}
