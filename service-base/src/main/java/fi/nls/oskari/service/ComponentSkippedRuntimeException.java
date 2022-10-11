package fi.nls.oskari.service;

public class ComponentSkippedRuntimeException extends ServiceRuntimeException {

	private static final long serialVersionUID = 1L;

	public ComponentSkippedRuntimeException(final String message, final Throwable e) {
        super(message, e);
    }

    public ComponentSkippedRuntimeException(final String message) {
        super(message);
    }
}