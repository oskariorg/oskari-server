package fi.nls.oskari.control;

import fi.nls.oskari.util.JSONHelper;
import org.json.JSONObject;

/**
 * Something went wrong but we know what it is and dont want a stack trace.
 * For example we didn't get all needed parameters
 * @author SMAKINEN
 */
public class ActionParamsException extends ActionException {
    
    private JSONObject options;
    private static final String KEY_ADDITIONAL_MSG = "error";

    public ActionParamsException(final String message) {
        super(message);
    }

    public ActionParamsException(final String message, final Exception e) {
        super(message, e);
    }

    public ActionParamsException(final String message, final JSONObject options) {
        super(message);
        this.options = options;
    }

    public ActionParamsException(final String message, final String additionalMsg) {
        super(message);
        this.options = JSONHelper.createJSONObject(KEY_ADDITIONAL_MSG, additionalMsg);
    }

    public ActionParamsException(final String message, final String additionalMsg, final Exception e) {
        super(message, e);
        this.options = JSONHelper.createJSONObject(KEY_ADDITIONAL_MSG, additionalMsg);
    }
    
    public JSONObject getOptions() {
        return options;
    }
}
