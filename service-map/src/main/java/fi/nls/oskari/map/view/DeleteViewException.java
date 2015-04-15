package fi.nls.oskari.map.view;

public class DeleteViewException extends ViewException {

    public DeleteViewException(final String message) {
        super(message);
    }
    public DeleteViewException(final String message, final Exception e) {
        super(message, e);
    }

}
