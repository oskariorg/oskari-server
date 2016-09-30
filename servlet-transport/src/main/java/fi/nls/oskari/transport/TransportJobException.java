package fi.nls.oskari.transport;

public class TransportJobException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private String messageKey = "unknown";
    private String level = "error";

    public static String ERROR_LEVEL = "error";
    public static String WARNING_LEVEL = "warning";
    public static String ERROR_NO_GEOMETRY_NAMESPACE = "no_geometry_namespace";
    public static String ERROR_GETFEATURE_PAYLOAD_FAILED = "getfeature_payload_failed";
    public static String ERROR_GETFEATURE_POSTREQUEST_FAILED = "getfeature_postrequest_failed";
    public static String ERROR_WFS_REQUEST_FAILED = "wfs_request_failed";
    public static String ERROR_FEATURE_PARSING = "features_parsing_failed";
    public static String ERROR_CREATE_FILTER_FAILED = "create_filter_failed";
    public static String ERROR_COMMON_JOB_FAILURE = "common_job_failure";


    public TransportJobException(final String message, final Throwable e) {
        super(message, e);
    }

    public TransportJobException(final String message, final Throwable e, final String key) {
        super(message, e);
        this.messageKey = key;
    }


    public TransportJobException(final String message, final Throwable e, final String key,  final String level) {
        super(message, e);
        this.messageKey = key;
        this.level = level;
    }

    public TransportJobException(final String message) {
        super(message);
    }

    public TransportJobException(final String message, final String key) {
        super(message);
        this.messageKey = key;
    }

    public TransportJobException(final String message, final String key,  final String level) {
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
