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
package org.mycontroller.standalone.jobs.mysensors;

import java.util.List;

import org.mycontroller.standalone.NumericUtils;
import org.mycontroller.standalone.ObjectFactory;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.db.tables.Settings;
import org.mycontroller.standalone.mysensors.MyMessages.MESSAGE_TYPE;
import org.mycontroller.standalone.mysensors.MyMessages.MESSAGE_TYPE_INTERNAL;
import org.mycontroller.standalone.mysensors.RawMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xeiam.sundial.Job;
import com.xeiam.sundial.exceptions.JobInterruptException;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
public class HeartbeatJob extends Job {
    public static final String NAME = "mysensors_hearbeat_job";
    public static final String TRIGGER_NAME = "mysensors_hearbeat_trigger";
    private static final Logger _logger = LoggerFactory.getLogger(HeartbeatJob.class);
    private static final long WAIT_TIME_TO_CHECK_ALIVE_STATUS = NumericUtils.SECOND * 30;
    public static final long DEFAULT_HEARTBEAT_INTERVAL = 30;
    public static final long MYS_MSG_DELAY = 100; // in milliseconds

    @Override
    public void doRun() throws JobInterruptException {
        try {
            this.sendHearbeat();
            Thread.sleep(WAIT_TIME_TO_CHECK_ALIVE_STATUS);
            this.checkHearbeat();
        } catch (Exception ex) {
            _logger.error("Exception, ", ex);
        }
    }

    private void sendHearbeat() {
        List<Node> nodes = DaoUtils.getNodeDao().getAll();
        RawMessage rawMessage = new RawMessage();
        rawMessage.setAck(0);
        rawMessage.setMessageType(MESSAGE_TYPE.C_INTERNAL.ordinal());
        rawMessage.setSubType(MESSAGE_TYPE_INTERNAL.I_HEARTBEAT.ordinal());
        rawMessage.setTxMessage(true);
        rawMessage.setChildSensorId(255);
        for (Node node : nodes) {
            rawMessage.setNodeId(node.getId());
            rawMessage.setPayload(System.currentTimeMillis());
            ObjectFactory.getRawMessageQueue().putMessage(rawMessage);
            _logger.debug("Hearbeat message sent for node:[{}], rawMessage:[{}]", node, rawMessage);
            try {
                Thread.sleep(MYS_MSG_DELAY);
            } catch (InterruptedException ex) {
                _logger.error("Error,", ex);
            }
        }
    }

    private void checkHearbeat() {
        List<Node> nodes = DaoUtils.getNodeDao().getAll();
        Settings interval = DaoUtils.getSettingsDao().get(Settings.MYS_HEARTBEAT_INTERVAL);
        long heartbeatInterval = (Long.valueOf(interval.getValue()) * NumericUtils.MINUTE) + NumericUtils.MINUTE;
        for (Node node : nodes) {
            if (node.getLastHeartbeat() == null
                    || node.getLastHeartbeat() <= (System.currentTimeMillis() - heartbeatInterval)) {
                node.setReachable(false);
                DaoUtils.getNodeDao().update(node);
                _logger.debug("Node is in not reachable state, Node:[{}]", node);
            }
        }
    }

}
