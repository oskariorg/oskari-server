package fi.mml.portti.service.search;

public class IllegalSearchCriteriaException extends Exception {
	private static final long serialVersionUID = -8242380659656936056L;

	public IllegalSearchCriteriaException() {
		super();
	}
	
	public IllegalSearchCriteriaException(String message) {
		super(message);
	}
	
	public IllegalSearchCriteriaException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public IllegalSearchCriteriaException(Throwable cause) {
		super(cause);
	}
}
