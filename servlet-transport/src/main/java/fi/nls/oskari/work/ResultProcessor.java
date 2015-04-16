package fi.nls.oskari.work;

/**
 * Can be used to hide the implementation of communicating results for OWSMapLayerJobs.
 * TransportService maps addResults to websocket send for example.
 */
public interface ResultProcessor {

    public static final String CHANNEL_ERROR = "/error";
    public static final String CHANNEL_IMAGE = "/wfs/image";
    public static final String CHANNEL_PROPERTIES = "/wfs/properties";
    public static final String CHANNEL_FEATURE = "/wfs/feature";
    public static final String CHANNEL_MAP_CLICK = "/wfs/mapClick";
    public static final String CHANNEL_FILTER = "/wfs/filter";
    public static final String CHANNEL_RESET = "/wfs/reset";
    public static final String CHANNEL_FEATURE_GEOMETRIES = "/wfs/featureGeometries";
    public static final String CHANNEL_STATUS = "/status";

    // Error messages
    public static String ERROR_NO_PERMISSIONS = "wfs_no_permissions";
    public static String ERROR_CONFIGURATION_FAILED = "wfs_configuring_layer_failed";
    public static String ERROR_WFS_REQUEST_FAILED = "wfs_request_failed";
    public static String ERROR_FEATURE_PARSING = "features_parsing_failed";
    public static String ERROR_WFS_IMAGE_PARSING = "wfs_image_parsing_failed";

    /**
     * Adds results for given task
     * @param clientId the client that requested processing
     * @param channel channel to message to about results
     * @param data actual results
     */
    public void addResults(final String clientId, final String channel, final Object data);
}
