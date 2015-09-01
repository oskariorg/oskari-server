package fi.nls.oskari.util;

/**
 * Generic Oskari specific runtime exception.
 */
public class OskariRuntimeException extends RuntimeException {

    public OskariRuntimeException(final String message, final Exception e) {
        super(message, e);
    }

    public OskariRuntimeException(final String message) {
        super(message);
    }

}
