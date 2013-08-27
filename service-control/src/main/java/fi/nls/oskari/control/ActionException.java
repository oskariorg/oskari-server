package fi.nls.oskari.control;

/**
 * Generic "something went wrong" and we want additional debug info/stack trace.
 * More specific subclasses can be used when stacktrace is unnecessary.
 * @author SMAKINEN
 */
public class ActionException extends Exception {
    
    public ActionException(final String message, final Exception e) {
        super(message, e);
    }

    public ActionException(final String message) {
        super(message);
    }

}
