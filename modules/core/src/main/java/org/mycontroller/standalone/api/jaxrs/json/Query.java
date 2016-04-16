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
package org.mycontroller.standalone.api.jaxrs.json;

import java.util.HashMap;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.ToString;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
@Builder
@Data
@ToString(includeFieldNames = true)
public class Query {
    public static final Long MAX_ITEMS_PER_PAGE = 10L;
    public static final Long MAX_ITEMS_UNLIMITED = 100L;
    public static final String PAGE_LIMIT = "pageLimit";
    public static final String PAGE = "page";
    public static final String ORDER = "order";
    public static final String ORDER_BY = "orderBy";
    public static final String ORDER_ASC = "asc";
    public static final String KEY_ID = "id";

    private Long totalItems;
    private Long filteredCount;
    private long pageLimit;
    private long page;
    private String orderBy;

    @NonNull
    private String order;//asc or desc
    private HashMap<String, Object> filters;

    public Long getStartingRow() {
        return (getPage() - 1) * getPageLimit();
    }

    public static Query get(HashMap<String, Object> filters) {
        Query query = Query.builder()
                .order(filters.get(ORDER) != null ? (String) filters.get(ORDER) : ORDER_ASC)
                .orderBy(filters.get(ORDER_BY) != null ? (String) filters.get(ORDER_BY) : KEY_ID)
                .filters(filters)
                .pageLimit(filters.get(PAGE_LIMIT) != null ? (Long) filters.get(PAGE_LIMIT) : MAX_ITEMS_PER_PAGE)
                .page(filters.get(PAGE) != null ? (long) filters.get(PAGE) : 1L)
                .build();
        //remove unwanted from filters list
        filters.remove(ORDER);
        filters.remove(ORDER_BY);
        filters.remove(PAGE_LIMIT);
        filters.remove(PAGE);
        return query;
    }
}
