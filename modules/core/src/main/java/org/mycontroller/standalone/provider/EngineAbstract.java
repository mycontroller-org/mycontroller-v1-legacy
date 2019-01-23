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
package org.mycontroller.standalone.provider;

import org.mycontroller.standalone.McThreadPoolFactory;
import org.mycontroller.standalone.eventbus.McEventBus;
import org.mycontroller.standalone.eventbus.MessageStatus;
import org.mycontroller.standalone.eventbus.MessageStatusHandler;
import org.mycontroller.standalone.exceptions.NotSupportedException;
import org.mycontroller.standalone.gateway.IGateway;
import org.mycontroller.standalone.gateway.config.GatewayConfig;
import org.mycontroller.standalone.message.IMessage;
import org.mycontroller.standalone.message.McMessageUtils;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_STATUS;
import org.mycontroller.standalone.offheap.MessageQueueImpl;
import org.mycontroller.standalone.offheap.MessageQueueSleepImpl;

import io.vertx.core.eventbus.MessageConsumer;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 1.2.0
 */
@Slf4j
public abstract class EngineAbstract implements IEngine {
    private volatile boolean exit = false;
    private volatile boolean stopped = true;
    protected MessageQueueImpl _queue;
    protected MessageQueueSleepImpl _queueSleep;
    protected IGateway _gateway;
    protected IExecutor _executor;
    private long auditStartTime = 0;
    private long gatewayAuditTime = 0;
    private EngineStatistics _statistics = new EngineStatistics();
    private static final String STREAM_MESSAGE = McMessageUtils.MESSAGE_TYPE.C_STREAM.getText();

    public EngineAbstract(GatewayConfig _config) {
        if (_queue == null) {
            _queue = new MessageQueueImpl(String.valueOf(_config.getId()));
            _queueSleep = new MessageQueueSleepImpl(String.valueOf(_config.getId()));
        }
    }

    public EngineStatistics processingRate() {
        return _statistics.clone();
    }

    public void routineTasks() {
        // override this and do not block!
    }

    @Override
    public void start() {
        // Add it in to thread pool
        McThreadPoolFactory.execute(this);
        _logger.debug("{}", _gateway.config());
    }

    @Override
    public GatewayConfig config() {
        return _gateway.config();
    }

    @Override
    public void send(IMessage message) {
        if (_gateway.isUp()) {
            _queue.add(message);
        } else {
            McEventBus.getInstance().publish(
                    message.getEventTopic(),
                    MessageStatus.builder()
                            .status(MESSAGE_STATUS.GATEWAY_NOT_AVAILABLE)
                            .message("Gateway down! - " + _gateway.config().getName())
                            .build());
        }
    }

    @Override
    public void sendSleepNode(IMessage message) {
        _queueSleep.put(message);
        McEventBus.getInstance().publish(
                message.getEventTopic(),
                MessageStatus.builder()
                        .status(MESSAGE_STATUS.ADDED_TO_SLEEP_QUEUE)
                        .message("Will be sent when receive a request from node.")
                        .build());
    }

    public void clearSleepQueue(String nodeEui) {
        _queueSleep.remove(nodeEui);
    }

    @Override
    public boolean isRunning() {
        return !stopped;
    }

    @Override
    public void distory() {
        _gateway.disconnect();
        stop();
        _queue.delete();
        _queueSleep.delete();
    }

    @Override
    public void run() {
        // clear statistics table
        _statistics.clear();
        stopped = false;
        // start the gateway
        _gateway.connect();
        _logger.debug("Gateway started successfully. {}", _gateway.config());

        while (!exit) {
            try {
                auditGateway();
                auditQueue();
                routineTasks();
                _statistics.updateLastMinuteStatus();
            } catch (Exception ex) {
                _logger.error("Exception,", ex);
            }
        }
        _gateway.disconnect();
        _logger.debug("Terminatted... ");
        stopped = true;
    }

    @Override
    public void stop() {
        exit = true;
    }

    //  checks gateway status, if it is not running make it UP
    public void auditGateway() {
        if (_gateway.isUp()) {
            return;
        }
        long statusSince = System.currentTimeMillis() - gatewayAuditTime;
        if (statusSince >= _gateway.config().getReconnectDelay() * 1000L) {
            gatewayAuditTime = System.currentTimeMillis();
            _logger.debug("Gateway is in down state. Trying to reconnect...");
            _gateway.reconnect();
        }
    }

    private void sleep(long sleepDuration) {
        sleep(sleepDuration, null);
    }

    private void sleep(long sleepDuration, MessageStatusHandler handler) {
        try {
            while (sleepDuration > 0) {
                sleepDuration -= 10L;
                Thread.sleep(10L);
                if (exit) {
                    return;
                }
                if (handler != null && handler.getStatusMessage() != null) {
                    return;
                }
            }
        } catch (InterruptedException ex) {
            _logger.warn("Sleep interrupted", ex);
        }
    }

    public void auditQueue() {
        // Update queue size
        _statistics.setSizeQueue(_queue.size());
        if (_statistics.getSizeQueue() > 0) {
            IMessage message = _queue.take();
            // if null message return it.
            if (message == null) {
                return;
            }
            if (!_gateway.isUp()) {
                // TODO: notify it is failed, "Gateway not ready"
                McEventBus.getInstance().publish(
                        message.getEventTopic(),
                        MessageStatus.builder()
                                .status(MESSAGE_STATUS.GATEWAY_NOT_AVAILABLE).message("Gateway not available")
                                .build());
                return;
            }
            auditStartTime = System.currentTimeMillis();
            _logger.debug("Processing:[{}]", message);
            try {
                if (message.isTxMessage()) {

                    boolean ackEnabled = false;
                    if (_gateway.config().getAckEnabled()) {
                        // check ack enabled and if it is node broadcast message we will not get ack
                        if (message.getNodeEui().equalsIgnoreCase(IMessage.NODE_BROADCAST_ID)) {
                            ackEnabled = false;
                        } else if (STREAM_MESSAGE.equalsIgnoreCase(message.getType())) {
                            ackEnabled = _gateway.config().getStreamAckEnabled();
                        } else {
                            ackEnabled = true;
                        }
                    }

                    if (ackEnabled) {
                        // set acknowledgement request
                        message.setAck(IMessage.ACK_REQUEST);
                        MessageConsumer<MessageStatus> _consumer = null;
                        try {
                            MessageStatusHandler _handler = new MessageStatusHandler();
                            _consumer = McEventBus.getInstance().registerConsumer(message.getEventTopic(), _handler);
                            for (int retry = 1; retry <= _gateway.config().getFailedRetryCount(); retry++) {
                                _logger.debug("Retry count {} of {}, {}",
                                        retry, _gateway.config().getFailedRetryCount(), message);
                                _gateway.write(message); // send to _gateway
                                sleep(_gateway.config().getAckWaitTime()); // wait for ack delay
                                if (exit) {
                                    return;
                                }
                                // if we received ack send it to next process
                                if (_handler.getStatusMessage() != null
                                        && _handler.getStatusMessage().getStatus() == MESSAGE_STATUS.ACK_RECEIVED) {
                                    _executor.execute(message);
                                    McEventBus.getInstance().publish(
                                            message.getEventTopic(),
                                            MessageStatus.builder()
                                                    .status(MESSAGE_STATUS.SUCCESS).message("Retry count: " + retry)
                                                    .build());
                                    break;
                                }
                                if (retry == _gateway.config().getFailedRetryCount()) {
                                    _logger.info("Seems like failed to send this message. "
                                            + "There is no ACK received! Retried {} time(s). {}",
                                            _gateway.config().getFailedRetryCount(), message);
                                    _statistics.incrementFailureCount();
                                    // notify it is failed, "ack not received"
                                    McEventBus.getInstance().publish(
                                            message.getEventTopic(),
                                            MessageStatus.builder().status(MESSAGE_STATUS.NO_ACK_RECEIVED)
                                                    .message("Failed retry count:" + retry).build()
                                            );
                                }
                            }
                        } finally {
                            if (_consumer != null) {
                                _consumer.unregister();
                            }
                        }
                    } else {
                        _gateway.write(message); // send to _gateway
                        _executor.execute(message);
                        McEventBus.getInstance().publish(
                                message.getEventTopic(),
                                MessageStatus.builder()
                                        .status(MESSAGE_STATUS.SUCCESS).message("Retry count: 0").build());
                    }
                    // A delay to avoid collisions on any networks with continues messages. Only for Tx message
                    sleep(_gateway.config().getTxDelay());
                } else {
                    _executor.execute(message);
                }
            } catch (NotSupportedException ex) {
                _logger.error("NotSupported: {}. Dropping {}", ex.getMessage(), message);
            } catch (Exception ex) {
                _logger.error("Throws exception while processing!, [{}]", message, ex);
            } finally {
                // update last message time to processing rate table
                _statistics.update(System.currentTimeMillis() - auditStartTime, message.isTxMessage());
                _logger.debug("{}", _statistics);
            }
        }
        // sleep here to reduce CPU load, in nanoseconds
        try {
            Thread.sleep(0, 333333);
        } catch (InterruptedException ex) {
            _logger.error("Exception, ", ex);
        }
    }
}
