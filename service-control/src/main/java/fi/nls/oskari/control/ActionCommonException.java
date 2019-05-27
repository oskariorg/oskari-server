package fi.nls.oskari.control;

/**
 * Generic "something went wrong" exception but one that is rather common
 * and we don't want to log the stack trace (except at debug log level).
 */
public class ActionCommonException extends ActionException {

    public ActionCommonException(final String message, final Exception e) {
        super(message, e);
    }

    public ActionCommonException(final String message) {
        super(message);
    }

}
