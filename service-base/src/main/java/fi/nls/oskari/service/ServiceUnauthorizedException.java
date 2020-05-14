package fi.nls.oskari.service;

public class ServiceUnauthorizedException extends ServiceException {
	
	private static final long serialVersionUID = 2323366761769429030L;

	public ServiceUnauthorizedException(final String message, final Exception e) {
        super(message, e);
    }

    public ServiceUnauthorizedException(final String message) {
        super(message);
    }
}
