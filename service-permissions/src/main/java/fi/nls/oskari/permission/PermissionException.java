package fi.nls.oskari.permission;

public class PermissionException extends Exception {
    
    public PermissionException(final String message, final Exception e) {
        super(message, e);
    }

    public PermissionException(final String message) {
        super(message);
    }

}
