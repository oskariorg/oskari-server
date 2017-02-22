package fi.nls.oskari.routing;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by HVELLONEN on 1.7.2015.
 */
public class RouteResponse {
    private static final Logger LOGGER = LogFactory.getLogger(RouteResponse.class);

    private static final String PARAM_PLAN = "plan";
    private static final String PARAM_REQUEST_PARAMETERS = "requestParameters";
    private static final String PARAM_ERROR_MESSAGE = "errorMessage";
    private static final String PARAM_SUCCESS = "success";


    private JSONObject requestParameters;
    private JSONObject plan;
    private boolean success;
    private String errorMessage;
    private String requestUrl;

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        try{
            if(success){
                json.put(PARAM_SUCCESS, true);
                json.put(PARAM_PLAN, plan);
                json.put(PARAM_REQUEST_PARAMETERS, requestParameters);
            } else {
                json.put(PARAM_SUCCESS, false);
                json.put(PARAM_ERROR_MESSAGE, errorMessage);
                json.put(PARAM_REQUEST_PARAMETERS, requestParameters);
            }
        } catch(JSONException ex){
            LOGGER.error("Cannot get JSON route response", ex);
        }
        return json;
    }


    public JSONObject getRequestParameters() {
        return requestParameters;
    }

    public void setRequestParameters(JSONObject requestParameters) {
        this.requestParameters = requestParameters;
    }

    public JSONObject getPlan() {
        return plan;
    }

    public void setPlan(JSONObject plan) {
        this.plan = plan;
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

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }


}
