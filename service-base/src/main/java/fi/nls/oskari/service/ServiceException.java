package fi.nls.oskari.service;

public class ServiceException extends Exception {
    
	private static final long serialVersionUID = -3303389634376753825L;

	public ServiceException(final String message, final Exception e) {
        super(message, e);
    }

    public ServiceException(final String message) {
        super(message);
    }

}