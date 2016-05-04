package fi.nls.oskari.feedback;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Gathers response result to Oskari frontend
 * Created by oskari-team 18.4.2016.
 */
public class FeedbackResponse {
    private static final Logger LOGGER = LogFactory.getLogger(FeedbackResponse.class);

    private static final String PARAM_DATA = "data";
    private static final String PARAM_REQUEST_PARAMETERS = "requestParameters";
    private static final String PARAM_ERROR_MESSAGE = "errorMessage";
    private static final String PARAM_SUCCESS = "success";


    private JSONObject requestParameters;
    private JSONObject data;
    private boolean success;
    private String errorMessage;

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        try {
            if (success) {
                json.put(PARAM_SUCCESS, true);
                json.put(PARAM_DATA, data);
                json.put(PARAM_REQUEST_PARAMETERS, requestParameters);
            } else {
                json.put(PARAM_SUCCESS, false);
                json.put(PARAM_ERROR_MESSAGE, errorMessage);
                json.put(PARAM_REQUEST_PARAMETERS, requestParameters);
            }
        } catch (JSONException ex) {
            LOGGER.error("Cannot get JSON feedback response", ex);
        }
        return json;
    }


    public JSONObject getRequestParameters() {
        return requestParameters;
    }

    public void setRequestParameters(JSONObject requestParameters) {
        this.requestParameters = requestParameters;
    }

    public JSONObject getData() {
        return data;
    }

    public void setData(JSONObject data) {
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }


}
