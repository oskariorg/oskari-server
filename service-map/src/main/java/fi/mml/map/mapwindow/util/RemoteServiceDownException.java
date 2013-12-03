package fi.mml.map.mapwindow.util;


/**
 * Exception should be thrown if web map service is down
 *
 */
public class RemoteServiceDownException extends Exception {

	private static final long serialVersionUID = 1L;

	public RemoteServiceDownException(String message) {
		super(message);
	}
	
	public RemoteServiceDownException(String message, Exception e) {
		super(message, e);
	}
}
