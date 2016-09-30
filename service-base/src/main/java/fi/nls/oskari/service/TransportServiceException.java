package fi.nls.oskari.service;

public class TransportServiceException extends RuntimeException {

	private static final long serialVersionUID = 1L;
    private String messageKey = "unknown";
    private String level = "error";

    public static String ERROR_LEVEL = "error";
    public static String WARNING_LEVEL = "warning";
    public static String ERROR_REDIS_CONNECTION_FAILURE = "redis_connection_failure";
    public static String ERROR_REDIS_BROKEN_CONNECTION = "redis_broken_connection";
    public static String ERROR_REDIS_GET_FAILURE = "redis_get_failure";
    public static String ERROR_COMMON_PROCESS_REQUEST_FAILURE = "common_process_request_failure";
    public static String ERROR_SESSION_CREATION_FAILED = "session_creation_failed";
    public static String ERROR_WFSLAYERSTORE_PARSING_FAILED = "WFSLayerStore_parsing_failed";
    public static String ERROR_LAYER_ADD_FAILED = "layer_add_failed";
    public static String ERROR_LAYER_REMOVE_FAILED = "layer_remove_failed";
    public static String ERROR_NO_FEATURES_DEFINED = "no_features_defined";
    public static String ERROR_SET_LOCATION_FAILED = "set_location_failed";
    public static String ERROR_SET_MAP_SIZE_FAILED = "set_map_size_failed";
    public static String ERROR_SET_LAYER_STYLE_FAILED = "set_layer_style_failed";
    public static String ERROR_SET_LAYER_CUSTOMSTYLE_FAILED = "set_layer_customstyle_failed";
    public static String ERROR_SET_MAP_CLICK_FAILED = "set_map_click_failed";
    public static String ERROR_SET_MAP_VISIBILITY_FAILED = "set_map_visibility_failed";
    public static String ERROR_SET_FILTER_FAILED = "set_filter_failed";
    public static String ERROR_SET_PROPERTY_FILTER_FAILED = "set_property_filter_failed";

	public TransportServiceException(final String message, final Throwable e) {
        super(message, e);
    }

    public TransportServiceException(final String message, final Throwable e, final String key, final String level) {
        super(message, e);
        this.messageKey = key;
        this.level = level;
    }
    public TransportServiceException(final String message, final Throwable e, final String key) {
        super(message, e);
        this.messageKey = key;
    }
    public TransportServiceException(final String message) {
        super(message);
    }

    public TransportServiceException(final String message, final String key) {
        super(message);
        this.messageKey = key;
    }

    public TransportServiceException(final String message, final String key, final String level) {
        super(message);
        this.messageKey = key;
        this.level = level;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public String getLevel() {
        return level;
    }
}