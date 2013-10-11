package fi.nls.oskari.map.view;

public class ViewException extends Exception {

    public ViewException(final String message) {
        super(message);
    }
    
    public ViewException(final String message, final Exception e) {
        super(message, e);
    }

}
