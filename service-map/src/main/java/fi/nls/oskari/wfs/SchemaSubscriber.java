package fi.nls.oskari.wfs;

import fi.nls.oskari.cache.JedisSubscriber;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;

public class SchemaSubscriber extends JedisSubscriber {
    private final static Logger log = LogFactory.getLogger(SchemaSubscriber.class);

    public static final String SCHEMA_CHANNEL = "schemaInfo";

    /**
     * Handles the received message if the channel is "schemaInfo"
     *
     * @param channel
     * @param message
     */
    @Override
    public void onMessage(String channel, String message) {
        log.warn(channel, message);
        if(channel.equals(SCHEMA_CHANNEL)) {
            log.warn("Message:" + message);
        }
    }

}
