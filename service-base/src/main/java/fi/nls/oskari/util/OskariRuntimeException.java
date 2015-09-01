package fi.nls.oskari.util;

/**
 * Used if the operation results in duplicate data.
 */
public class OskariRuntimeException extends Exception {

    public OskariRuntimeException(final String message, final Exception e) {
        super(message, e);
    }

    public OskariRuntimeException(final String message) {
        super(message);
    }

}
