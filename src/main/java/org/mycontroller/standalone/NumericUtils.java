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
package org.mycontroller.standalone;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class NumericUtils {
    public static final int DOUBLE_ROUND = 3;
    public static final long SECOND = 1000;
    public static final long MINUTE = SECOND * 60;
    public static final long HOUR = MINUTE * 60;
    public static final long DAY = HOUR * 24;
    public static final DecimalFormat decimalFormat = new DecimalFormat("#.##");

    private NumericUtils() {

    }

    public static double round(double value, int places) {
        if (places < 0)
            throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static Double getDouble(String value) {
        return round(Double.valueOf(value), DOUBLE_ROUND);
    }

    public static String getDoubleAsString(double value) {
        if (value % 1 != 0) {
            return String.valueOf(value);
        } else {
            return decimalFormat.format(value);
        }
    }

    public static String getDoubleAsString(String value) {
        if (value != null) {
            return getDoubleAsString(Double.valueOf(value));
        } else {
            return "-";
        }
    }

    public static Integer getInteger(String value) {
        if (value != null) {
            return Integer.valueOf(value);
        } else {
            return null;
        }
    }

    public static String getStatusAsString(String value) {
        if (value != null) {
            return value.equalsIgnoreCase("0") ? "OFF" : "ON";
        } else {
            return "-";
        }
    }

    public static String getArmedAsString(String value) {
        if (value != null) {
            return value.equalsIgnoreCase("0") ? "Bypassed" : "Armed";
        } else {
            return "-";
        }
    }

    public static String getTrippedAsString(String value) {
        if (value != null) {
            return value.equalsIgnoreCase("0") ? "Untripped" : "Tripped";
        } else {
            return "-";
        }
    }

    public static String getLockStatusAsString(String value) {
        if (value != null) {
            return value.equalsIgnoreCase("0") ? "Unlocked" : "Locked";
        } else {
            return "-";
        }
    }

    public static String getDifferenceFriendlyTime(long timestamp) {
        StringBuilder builder = new StringBuilder();
        long diffMills = (System.currentTimeMillis() - timestamp);
        long diffSeconds = diffMills / SECOND;
        long diffMinutes = diffMills / MINUTE;
        long diffHours = diffMills / HOUR;
        long diffDays = diffMills / DAY;
        if (diffDays > 0) {
            builder.append(diffDays);
            if (diffDays == 1) {
                builder.append(" Day ago");
            } else {
                builder.append(" Days ago");
            }
        } else if (diffHours > 0) {
            builder.append(diffHours);
            if (diffHours == 1) {
                builder.append(" Hour ago");
            } else {
                builder.append(" Hours ago");
            }
        } else if (diffMinutes > 0) {
            builder.append(diffMinutes);
            if (diffMinutes == 1) {
                builder.append(" Minute ago");
            } else {
                builder.append(" Minutes ago");
            }
        } else if (diffSeconds > 0) {
            builder.append(diffSeconds);
            if (diffSeconds == 1) {
                builder.append(" Second ago");
            } else {
                builder.append(" Seconds ago");
            }
        } else {
            builder.append("Now");
        }
        return builder.toString();
    }

}
