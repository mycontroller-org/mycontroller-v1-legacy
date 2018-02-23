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
package org.mycontroller.standalone;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.2.0
 */
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import lombok.extern.slf4j.Slf4j;

public class McThreadPoolFactory {
    private static final int CORE_POOL_SIZE = 10;
    private static final int MAXIMUM_POOL_SIZE = 70;
    private static final int KEEP_ALIVE_TIME = 60;

    private static final ThreadFactory _THREAD_FACTORY = new ThreadFactoryBuilder()
            .setNameFormat("mc-th-pool-%d")
            .setUncaughtExceptionHandler(new McUncaughtException())
            .build();
    private static final BlockingQueue<Runnable> _WORK_QUEUE = new ArrayBlockingQueue<Runnable>(100);
    private static final ThreadPoolExecutor _EXECUTOR = new ThreadPoolExecutor(
            CORE_POOL_SIZE,
            MAXIMUM_POOL_SIZE,
            KEEP_ALIVE_TIME, TimeUnit.SECONDS,
            _WORK_QUEUE,
            _THREAD_FACTORY);

    public static void execute(Runnable command) {
        _EXECUTOR.execute(command);
    }

    public static void shutdown() {
        if (!_EXECUTOR.isShutdown()) {
            _EXECUTOR.shutdown();
        }
    }

    public static void shutdownNow() {
        if (!_EXECUTOR.isShutdown()) {
            _EXECUTOR.shutdownNow();
        }
    }
}

@Slf4j
class McUncaughtException implements UncaughtExceptionHandler {

    @Override
    public void uncaughtException(Thread th, Throwable ex) {
        _logger.error("Exception,", ex);
    }

}