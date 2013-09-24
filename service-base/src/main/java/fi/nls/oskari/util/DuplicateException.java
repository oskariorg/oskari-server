package fi.nls.oskari.util;

/**
 * Used if the operation results in duplicate data.
 */
public class DuplicateException extends Exception {
    
    public DuplicateException(final String message, final Exception e) {
        super(message, e);
    }

    public DuplicateException(final String message) {
        super(message);
    }

}
