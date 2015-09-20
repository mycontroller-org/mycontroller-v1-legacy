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
            return String.valueOf((int) value);
        }
    }

}
