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
package org.mycontroller.standalone;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.mycontroller.standalone.auth.McCrypt;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.User;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Slf4j
public class ResetPassword {
    public static final String[] PASSWORD_FILES = { "conf/password", "conf/password.txt" };
    public static final String SPLITTER = ",";

    public static void executeResetPassword() {
        File passwordFile = null;
        for (String pwdFile : PASSWORD_FILES) {
            passwordFile = FileUtils.getFile(AppProperties.getInstance().getAppDirectory() + pwdFile);
            try {
                _logger.debug("Password check on {}", passwordFile.getCanonicalPath());
            } catch (Exception ex) {
                _logger.error("Exception, ", ex);
            }
            if (passwordFile.exists()) {
                break;
            }
        }
        if (passwordFile != null && passwordFile.exists()) {
            try {
                List<String> userPasswords = FileUtils.readLines(passwordFile);
                for (String userPassword : userPasswords) {
                    if (userPassword.trim().length() > 0 && userPassword.contains(SPLITTER)) {
                        String[] userData = userPassword.split(SPLITTER, 2);
                        resetPassword(userData[0], userData[1]);
                    } else {
                        _logger.warn("Invalid format! >> [{}]", userPassword);
                    }
                }
                //Delete password file
                if (passwordFile.delete()) {
                    _logger.debug("Password reset file deleted successfully. [{}]", passwordFile.getCanonicalPath());
                }else{
                    _logger.warn("Failed to delete password reset file[{}]", passwordFile.getCanonicalPath());
                }
            } catch (IOException ex) {
                _logger.error("Exception, ", ex);
            }
        } else {
            _logger.debug("There was no password reset file!");
        }
    }

    public static void resetPassword(String username, String password) {
        _logger.debug("Resetting password for the user[{}]", username);
        User user = DaoUtils.getUserDao().getByUsername(username);
        if (user == null) {
            _logger.warn("Selected user[{}] not found in database!", username);
            return;
        }
        user.setPassword(McCrypt.encrypt(password));
        DaoUtils.getUserDao().update(user);
        _logger.info("Password successfully update for the user[{}]", username);
    }
}
