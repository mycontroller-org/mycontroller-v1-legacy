/*
 * Copyright 2015-2017 Jeeva Kandasamy (jkandasa@gmail.com)
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
package org.mycontroller.standalone.loggers;

import com.mysql.jdbc.log.Log;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */

@Slf4j
public class LoggerMySql implements Log {

    public LoggerMySql(String name) {
        _logger.trace("MySql logger created[{}]", name);
    }

    @Override
    public boolean isDebugEnabled() {
        return _logger.isDebugEnabled();
    }

    @Override
    public boolean isErrorEnabled() {
        return _logger.isErrorEnabled();
    }

    @Override
    public boolean isFatalEnabled() {
        return _logger.isErrorEnabled();
    }

    @Override
    public boolean isInfoEnabled() {
        return _logger.isInfoEnabled();
    }

    @Override
    public boolean isTraceEnabled() {
        return _logger.isTraceEnabled();
    }

    @Override
    public boolean isWarnEnabled() {
        return _logger.isWarnEnabled();
    }

    @Override
    public void logDebug(Object msg) {
        _logger.debug(msg.toString());
    }

    @Override
    public void logDebug(Object msg, Throwable ex) {
        _logger.debug(msg.toString(), ex);
    }

    @Override
    public void logError(Object msg) {
        _logger.error(msg.toString());
    }

    @Override
    public void logError(Object msg, Throwable ex) {
        _logger.error(msg.toString(), ex);
    }

    @Override
    public void logFatal(Object msg) {
        _logger.error(msg.toString());
    }

    @Override
    public void logFatal(Object msg, Throwable ex) {
        _logger.error(msg.toString(), ex);
    }

    @Override
    public void logInfo(Object msg) {
        _logger.info(msg.toString());
    }

    @Override
    public void logInfo(Object msg, Throwable ex) {
        _logger.info(msg.toString(), ex);
    }

    @Override
    public void logTrace(Object msg) {
        _logger.trace(msg.toString());
    }

    @Override
    public void logTrace(Object msg, Throwable ex) {
        _logger.trace(msg.toString(), ex);
    }

    @Override
    public void logWarn(Object msg) {
        _logger.warn(msg.toString());
    }

    @Override
    public void logWarn(Object msg, Throwable ex) {
        _logger.warn(msg.toString(), ex);
    }

}
