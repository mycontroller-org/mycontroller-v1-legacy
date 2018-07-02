/*
 * Copyright 2015-2018 Jeeva Kandasamy (jkandasa@gmail.com)
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
package org.mycontroller.standalone.scripts.api;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Slf4j
public class LoggerApi {

    public void trace(String arg0, Object... arg1) {
        if (_logger.isTraceEnabled()) {
            _logger.trace(arg0, arg1);
        }
    }

    public void debug(String arg0, Object... arg1) {
        if (_logger.isDebugEnabled()) {
            _logger.debug(arg0, arg1);
        }
    }

    public void info(String arg0, Object... arg1) {
        if (_logger.isInfoEnabled()) {
            _logger.info(arg0, arg1);
        }
    }

    public void warn(String arg0, Object... arg1) {
        if (_logger.isWarnEnabled()) {
            _logger.warn(arg0, arg1);
        }
    }

    public void error(String arg0, Object... arg1) {
        if (_logger.isErrorEnabled()) {
            _logger.error(arg0, arg1);
        }
    }
}
