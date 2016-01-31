package org.mycontroller.standalone.auth;

import javax.ws.rs.core.SecurityContext;

import org.mycontroller.standalone.db.tables.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthUtils {
    public static final Logger _logger = LoggerFactory.getLogger(AuthUtils.class);

    private AuthUtils() {

    }

    public enum PERMISSION_TYPE {
        SUPER_ADMIN("Super admin"),
        USER("User"),
        MQTT_USER("MQTT user");
        private final String name;

        private PERMISSION_TYPE(String name) {
            this.name = name;
        }

        public String getText() {
            return this.name;
        }

        public static PERMISSION_TYPE get(int id) {
            for (PERMISSION_TYPE type : values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

        public static PERMISSION_TYPE fromString(String text) {
            if (text != null) {
                for (PERMISSION_TYPE type : PERMISSION_TYPE.values()) {
                    if (text.equalsIgnoreCase(type.getText())) {
                        return type;
                    }
                }
            }
            return null;
        }
    }

    public static User getUser(SecurityContext securityContext) {
        return (User) securityContext.getUserPrincipal();
    }

    public static boolean isSuperAdmin(SecurityContext securityContext) {
        _logger.debug("User:{}", securityContext.getUserPrincipal());
        return ((User) securityContext.getUserPrincipal()).getPermissions().contains(
                PERMISSION_TYPE.SUPER_ADMIN.getText());
    }

}
