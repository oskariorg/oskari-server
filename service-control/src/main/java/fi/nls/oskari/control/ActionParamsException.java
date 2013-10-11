package fi.nls.oskari.control;

import org.json.JSONObject;

/**
 * Something went wrong but we know what it is and dont want a stack trace.
 * For example we didn't get all needed parameters
 * @author SMAKINEN
 */
public class ActionParamsException extends ActionException {
    
    private JSONObject options;

    public ActionParamsException(final String message) {
        super(message);
    }

    public ActionParamsException(final String message, final JSONObject options) {
        super(message);
        this.options = options;
    }
    
    public JSONObject getOptions() {
        return options;
    }
}
