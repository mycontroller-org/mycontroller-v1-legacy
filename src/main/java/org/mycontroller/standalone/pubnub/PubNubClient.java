package org.mycontroller.standalone.pubnub;

import org.json.JSONException;
import org.json.JSONObject;
import org.mycontroller.standalone.ObjectFactory;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.mysensors.MyMessages.MESSAGE_TYPE_SET_REQ;
import org.mycontroller.standalone.mysensors.RawMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;

public class PubNubClient {

    private static final Logger _logger = LoggerFactory.getLogger(PubNubClient.class.getName());
    private static boolean isRunning = false;
    private static Pubnub pubnub = null;

    public PubNubClient() {
        PubNubClient.pubnub = ObjectFactory.getPubNubClient();
    }

    public static synchronized void start() {

        if (!ObjectFactory.getAppProperties().isPubNubEnabled()) {
            _logger.debug("PubNubClient is not enabled... Skipping start...");
            return;
        }
        if (isRunning()) {
            _logger.info("PubNubClient already running, nothing to do...");
            return;
        }

        final String PUBLISHKEY = ObjectFactory.getAppProperties().getPubNubPublisherKey();
        final String SUBSCRIBERKEY = ObjectFactory.getAppProperties().getPubNubSubscriberKey();
        final String SECRETKEY = ObjectFactory.getAppProperties().getPubNubSecretKey();

        pubnub = new Pubnub(PUBLISHKEY, SUBSCRIBERKEY, SECRETKEY, "", true);

        setRunning(true);
        ObjectFactory.setPubNubClient(pubnub);

        _logger.debug("PubNubClient started successfully");
    }

    public static synchronized void stop() {

        if (!ObjectFactory.getAppProperties().isPubNubEnabled()) {
            _logger.debug("PubNubClient is not enabled.... Skipping stop...");
            return;
        }
        if (!isRunning()) {
            _logger.info("PubNubClient is not running, nothing to do...");
            return;
        }

        pubnub.shutdown();
        pubnub = null;

        setRunning(false);
        ObjectFactory.setPubNubClient(pubnub);

        _logger.debug("PubNubClient has been stopped successfully");
    }

    public void publish(Sensor sensor, RawMessage rawMessage) {

        final String channel = sensor.getNode().getName();
        final String sensorType = sensor.getTypeString();
        final String newValue = rawMessage.getPayload();
        final String varType = MESSAGE_TYPE_SET_REQ.get(rawMessage.getSubType()).toString();

        JSONObject data = new JSONObject();

        try {
            data.put("SensorName", sensor.getName());
            data.put("SensorType", sensorType);
            data.put("VariableType", varType);
            data.put("Payload", newValue);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        pubnub.publish(channel, data, new Callback() {
        });
    }

    public void unsubscribe(final String channelName) {

        pubnub.unsubscribe(channelName);
    }

    public static boolean isRunning() {
        return isRunning;
    }

    private static void setRunning(boolean isRunning) {
        PubNubClient.isRunning = isRunning;
    }

}
