package fi.nls.oskari.service;

import org.json.JSONObject;

public class ServiceException extends Exception {
    private JSONObject options;
    private static final long serialVersionUID = -3303389634376753825L;

	public ServiceException(final String message, final Exception e) {
        super(message, e);
    }
    public ServiceException(final String message) {
        super(message);
    }

    public ServiceException (final String message, final JSONObject options) {
        super(message);
        this.options = options;
    }
    public JSONObject getOptions() {
        return options;
    }
}
