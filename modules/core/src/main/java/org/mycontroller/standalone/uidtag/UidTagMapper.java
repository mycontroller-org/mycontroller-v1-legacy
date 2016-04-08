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
package org.mycontroller.standalone.uidtag;

import org.mycontroller.standalone.db.tables.UidTag;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class UidTagMapper {
    private boolean query;
    private Integer uid;
    private String payload;

    public UidTagMapper() {
    }

    public UidTagMapper(String uidString) {
        //Change this while change splitter value, if required
        String[] uidArray = uidString.split("\\" + UidTag.SPLITER);
        if (uidArray.length != 4) {
            throw new IllegalArgumentException("uidString should have exactly four values with spliter["
                    + UidTag.SPLITER + "], received: " + uidString);
        } else {
            //Update query or response message
            uid = Integer.valueOf(uidArray[1].trim());
            query = uidArray[2].trim().equalsIgnoreCase("0") ? true : false;
            payload = uidArray[3].trim();
        }
    }

    public UidTagMapper(boolean request, Integer uid, String payload) {
        this.query = request;
        this.uid = uid;
        this.payload = payload;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public boolean isQuery() {
        return query;
    }

    public void setQuery(boolean request) {
        this.query = request;
    }

    public Integer getUid() {
        return uid;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
    }

    public String getUidTagRawMessage() {
        StringBuilder builder = new StringBuilder(UidTag.STARTS_WITH);
        /*builder.append(UidTag.SPLITER).append(uid);
        builder.append(UidTag.SPLITER).append(isQuery() ? "0" : "1");
        builder.append(UidTag.SPLITER).append(payload);*/
        builder.append(",").append(uid);
        builder.append(",").append(isQuery() ? "0" : "1");
        builder.append(",").append(payload);

        return builder.toString();
    }
}
