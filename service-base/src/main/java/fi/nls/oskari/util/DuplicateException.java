package fi.nls.oskari.util;

public class DuplicateException extends Exception {
    
    public DuplicateException(final String message, final Exception e) {
        super(message, e);
    }

    public DuplicateException(final String message) {
        super(message);
    }

}
