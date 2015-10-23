package fi.nls.oskari.transport;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.work.ResultProcessor;
import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.bayeux.server.ServerSession;

import java.util.Map;

/**
 * Created by SMAKINEN on 27.3.2015.
 */
public class TransportResultProcessor implements ResultProcessor {

    private final static Logger LOG = LogFactory.getLogger(TransportResultProcessor.class);
    private long reqId = -1;
    private ServerSession local;
    private BayeuxServer bayeux;

    public TransportResultProcessor(final ServerSession local, final BayeuxServer bayeux) {
        this(local, bayeux, -1);
    }
    public TransportResultProcessor(final ServerSession local, final BayeuxServer bayeux, final long requestId) {
        this.local = local;
        this.bayeux = bayeux;
        reqId = requestId;
    }

    /**
     * Call through implementation of ResultProcessor
     * @param clientId
     * @param channel
     * @param data
     */
    public void addResults(final String clientId, final String channel, final Object data) {
        if(data instanceof Map) {
            ((Map)data).put("reqId", reqId);
        }
        else {
            LOG.debug("Results data not a map:", data);
        }
        send(clientId, channel, data);
    }
    /**
     * Sends data to certain client on a given channel
     *
     * @param clientId
     * @param channel
     * @param data
     */
    public void send(String clientId, String channel, Object data) {
        send(local, bayeux, clientId, channel, data);
    }

    public static void send(final ServerSession session, final BayeuxServer bayeux, String clientId, String channel, Object data) {
        ServerSession client = bayeux.getSession(clientId);
        if(client != null) {
            client.deliver(session, channel, data, null);
        }
        else {
            LOG.info("Client disconnected before results were sent:", clientId);
        }
    }
}
