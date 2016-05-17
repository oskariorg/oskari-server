package fi.nls.oskari.control.statistics.plugins;

/**
 * This is used to signal incorrect uses of the Oskari API by the frontend.
 */
public class APIException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public APIException(String message) {
        super(message);
    }
    public APIException(String message, Throwable cause) {
        super(message, cause);
    }
}
