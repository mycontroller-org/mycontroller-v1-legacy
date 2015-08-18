/**
 * Copyright (C) 2015 Jeeva Kandasamy (jkandasa@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mycontroller.standalone.auth;

import java.security.Principal;

import org.jboss.resteasy.plugins.server.embedded.SecurityDomain;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.USER_ROLE;
import org.mycontroller.standalone.db.tables.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class BasicAthenticationSecurityDomain implements SecurityDomain {
    private static final Logger _logger = LoggerFactory.getLogger(BasicAthenticationSecurityDomain.class.getName());

    @Override
    public Principal authenticate(String aUsername, String aPassword) throws SecurityException {
        _logger.debug("User:{},Password:{}", aUsername, aPassword);
        User user = DaoUtils.getUserDao().get(aUsername);
        if (user != null) {
            _logger.debug("User Found...User:{}", user);
            if (user.getPassword().equals(aPassword)) {
                user.setPassword(null);
                return user;
            }
        }
        throw new SecurityException("Access denied to user " + aUsername);
    }

    @Override
    public boolean isUserInRole(Principal principal, String role) {
        User user = (User) principal;
        _logger.debug("isUserInRole called with role[{}], user[{}]", role, user);
        if (USER_ROLE.ADMIN.toString().equalsIgnoreCase(user.getRole())) {
            return true;
        } else if (role.equalsIgnoreCase(user.getRole())) {
            return true;
        } else {
            _logger.info("Roles Mismatch, api role[{}], user role[{}]", role, user.getRole());
        }
        return false;
    }

    public static boolean login(String aUsername, String aPassword) {
        if (aUsername == null || aPassword == null) {
            return false;
        }
        _logger.debug("User:{},Password:{}", aUsername, aPassword);
        User user = DaoUtils.getUserDao().get(aUsername);
        if (user != null) {
            _logger.debug("User Found...User:{}", user);
            if (user.getPassword().equals(aPassword)) {
                return true;
            }
        }
        return false;
    }
}
