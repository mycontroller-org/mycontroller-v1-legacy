package org.mycontroller.standalone.auth;

public class AuthUtils {
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

}
