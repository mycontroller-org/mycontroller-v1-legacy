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
package org.mycontroller.standalone.db;

import org.mycontroller.standalone.db.alarm.DampeningConsecutive;
import org.mycontroller.standalone.db.alarm.DampeningLastNEvaluations;
import org.mycontroller.standalone.db.alarm.SendEmail;
import org.mycontroller.standalone.db.alarm.SendPayLoad;
import org.mycontroller.standalone.db.alarm.SendSMS;
import org.mycontroller.standalone.db.tables.Alarm;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class AlarmUtils {
    private AlarmUtils() {

    }

    public enum TYPE {
        SEND_PAYLOAD("Send Payload"),
        SEND_SMS("Send SMS"),
        SEND_EMAIL("Send Email");
        public static TYPE get(int id) {
            for (TYPE type : values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

        private String value;

        private TYPE(String value) {
            this.value = value;
        }

        public String value() {
            return this.value;
        }
    }

    public enum TRIGGER {
        GREATER_THAN(">"),
        GREATER_THAN_EQUAL(">="),
        LESSER_THAN("<"),
        LESSER_THAN_EQUAL("<="),
        EQUAL("="),
        NOT_EQUAL("!=");

        public static TRIGGER get(int id) {
            for (TRIGGER type : values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

        private String value;

        private TRIGGER(String value) {
            this.value = value;
        }

        public String value() {
            return this.value;
        }
    }

    public enum DAMPENING_TYPE {
        NONE("None"),
        CONSECUTIVE("Consecutive"),
        LAST_N_EVALUATIONS("Last N Evaluations");
        public static DAMPENING_TYPE get(int id) {
            for (DAMPENING_TYPE type : values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

        private String value;

        private DAMPENING_TYPE(String value) {
            this.value = value;
        }

        public String value() {
            return this.value;
        }
    }

    //http://www.tutorialspoint.com/cprogramming/c_operators.htm
    public enum SEND_PAYLOAD_OPERATIONS {
        INVERT("!"),
        INCREMENT("++"),
        DECREMENT("--"),
        ADD("+"),
        SUBTRACT("-"),
        MULTIPLIE("*"),
        DIVIDE("/"),
        MODULUS("%");
        public static SEND_PAYLOAD_OPERATIONS get(int id) {
            for (SEND_PAYLOAD_OPERATIONS type : values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

        private String value;

        private SEND_PAYLOAD_OPERATIONS(String value) {
            this.value = value;
        }

        public String value() {
            return this.value;
        }

        public static SEND_PAYLOAD_OPERATIONS findByValue(String value) {
            for (SEND_PAYLOAD_OPERATIONS type : values()) {
                if (type.value().equals(value)) {
                    return type;
                }
            }
            return null;
        }
    }

    public static SendPayLoad getSendPayLoad(Alarm alarm) {
        return new SendPayLoad(alarm.getVariable1(), alarm.getVariable2());
    }

    public static void setSendPayLoad(Alarm alarm, SendPayLoad sendPayLoad) {
        alarm.setVariable1(String.valueOf(sendPayLoad.getSensorRefId()));
        alarm.setVariable2(sendPayLoad.getPayLoad());
    }

    public static void setSendSMS(Alarm alarm, SendSMS sendSMS) {
        alarm.setVariable1(String.valueOf(sendSMS.getToPhoneNumber()));
    }

    public static SendSMS getSendSMS(Alarm alarm) {
        return new SendSMS(alarm.getVariable1());
    }

    public static void setSendEmail(Alarm alarm, SendEmail sendEmail) {
        alarm.setVariable1(String.valueOf(sendEmail.getToEmailAddress()));
    }

    public static SendEmail getSendEmail(Alarm alarm) {
        return new SendEmail(alarm.getVariable1());
    }

    public static DampeningConsecutive getDampeningConsecutive(Alarm alarm) {
        return new DampeningConsecutive(Integer.valueOf(alarm.getDampeningVar1()));
    }

    public static void setDampeningConsecutive(Alarm alarm, DampeningConsecutive dampeningConsecutive) {
        alarm.setDampeningType(DAMPENING_TYPE.CONSECUTIVE.ordinal());
        alarm.setDampeningVar1(String.valueOf(dampeningConsecutive.getOccurrences()));
    }

    public static DampeningLastNEvaluations getDampeningLastNEvaluations(Alarm alarm) {
        return new DampeningLastNEvaluations(Integer.valueOf(alarm.getDampeningVar1()), Integer.valueOf(alarm
                .getDampeningVar2()));
    }

    public static void setDampeningLastNEvaluations(Alarm alarm, DampeningLastNEvaluations dampeningLastNEvaluations) {
        alarm.setDampeningType(DAMPENING_TYPE.LAST_N_EVALUATIONS.ordinal());
        alarm.setDampeningVar1(String.valueOf(dampeningLastNEvaluations.getOccurrences()));
        alarm.setDampeningVar2(String.valueOf(dampeningLastNEvaluations.getEvaluations()));
    }

    public static String getNotificationString(Alarm alarm) {
        switch (AlarmUtils.TYPE.get(alarm.getType())) {
            case SEND_PAYLOAD:
                return getSendPayLoad(alarm).toString();
            case SEND_EMAIL:
                return getSendEmail(alarm).toString();
            case SEND_SMS:
                return getSendSMS(alarm).toString();
            default:
                return "-";
        }
    }

    public static String getDampeningString(Alarm alarm) {
        switch (AlarmUtils.DAMPENING_TYPE.get(alarm.getDampeningType())) {
            case NONE:
                return "None";
            case CONSECUTIVE:
                return getDampeningConsecutive(alarm).toString();
            case LAST_N_EVALUATIONS:
                return getDampeningLastNEvaluations(alarm).toString();
            default:
                return "-";
        }
    }
}
