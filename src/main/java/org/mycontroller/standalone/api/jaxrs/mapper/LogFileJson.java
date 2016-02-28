package org.mycontroller.standalone.api.jaxrs.mapper;

import lombok.AllArgsConstructor;

import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Builder;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LogFileJson {
    private Long lastKnownPosition;
    private String data;
}
