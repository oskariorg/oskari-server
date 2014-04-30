package fi.nls.oskari.work;

/**
 * Can be used to hide the implementation of communicating results for OWSMapLayerJobs.
 * TransportService maps addResults to websocket send for example.
 */
public interface ResultProcessor {

    /**
     * Adds results for given task
     * @param clientId the client that requested processing
     * @param channel channel to message to about results
     * @param data actual results
     */
    public void addResults(final String clientId, final String channel, final Object data);
}
