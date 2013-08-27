package fi.nls.oskari.control;

import org.json.JSONObject;

/**
 * User is not authorized to execute requested action
 * for example delete a layer.
 * @author SMAKINEN
 */
public class ActionDeniedException extends ActionException {

    private JSONObject options;

    public ActionDeniedException(final String message) {
        super(message);
    }

    public ActionDeniedException(final String message, final JSONObject options) {
        super(message);
        this.options = options;
    }
    
    public JSONObject getOptions() {
        return options;
    }
}
