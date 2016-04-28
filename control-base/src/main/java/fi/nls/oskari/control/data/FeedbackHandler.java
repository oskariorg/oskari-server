package fi.nls.oskari.control.data;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.feedback.FeedbackResponse;
import fi.nls.oskari.feedback.ServiceParams;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.feedback.FeedbackService;
import fi.nls.oskari.feedback.open311.FeedbackImpl;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONObject;

import java.util.Iterator;

import static fi.nls.oskari.control.ActionConstants.*;


/**
 * Send feedback to feedback services e.g. Open311
 * }
 */
@OskariActionRoute("Feedback")
public class FeedbackHandler extends ActionHandler {

    static final String API_PARAM_METHOD = "method";
    static final String API_PARAM_BASEURL = "baseUrl";
    static final String API_PARAM_SERVICEID = "serviceId";
    static final String API_PARAM_POSTSERVICEREQUEST = "postServiceRequest";
    static final String API_PARAM_GETSERVICEREQUEST = "getServiceRequests";

    private static final Logger log = LogFactory.getLogger(FeedbackHandler.class);

    private FeedbackService service = null;

    @Override
    public void init() {
        super.init();
       service = new FeedbackImpl();
    }

    @Override
    public void handleAction(final ActionParameters params)
            throws ActionException {

        //Method switch to to requested action
        final String method = params.getRequiredParam(API_PARAM_METHOD);
        FeedbackResponse result = new FeedbackResponse();

        ServiceParams sparams = getServiceParams(method, params);
        result.setRequestParameters(sparams.toJSON());

        JSONObject data = service.getServiceResult(sparams);

        if (data == null || data.length() == 0){
            result.setSuccess(false);
        }
        else {
            result.setSuccess(true);
            result.setData(data);
        }

        ResponseHelper.writeResponse(params, result.toJSON());
    }

    /**
     * Builds and validates request parameters according to requested method
     * @param method
     * @param params
     * @return
     * @throws ActionException
     */
    public ServiceParams getServiceParams(final String method, final ActionParameters params) throws ActionException {

        Boolean paramsOk = false;
        ServiceParams serviceParams = new ServiceParams();
        // base url value will be fetch from the embedded view in the future
        serviceParams.setBaseUrl(PropertyUtil.get("feedback.open311.url", null));
        if(serviceParams.getBaseUrl() == null){
            log.error("Open311  feedback service url is not available");
            throw new ActionParamsException("Open311  feedback service url is not available");
        }
        serviceParams.setMethod(method);
        serviceParams.setLocale(params.getLocale().getLanguage());


        switch (method) {
            case ServiceParams.API_METHOD_SERVICELIST:
                paramsOk = true;
                break;
            case ServiceParams.API_METHOD_SERVICEDEFINITION:
                serviceParams.setServiceId(params.getRequiredParam(API_PARAM_SERVICEID));
                paramsOk = true;
                break;
            case ServiceParams.API_METHOD_POST_FEEDBACK:
                serviceParams.setSourceEpsg(params.getRequiredParam(PARAM_SRS));
                final String postServiceRequest = params.getRequiredParam(API_PARAM_POSTSERVICEREQUEST);
                serviceParams.setPostServiceRequest(JSONHelper.createJSONObject(postServiceRequest));
                serviceParams.setApiKey(PropertyUtil.get("feedback.open311.key", null));
                if (serviceParams.getPostServiceRequest() == null || serviceParams.getApiKey() == null) {
                    // json corrupted/parsing failed
                    throw new ActionParamsException("Invalid Feedback (Open311) post parameters -  api key not available ?" );
                }
                // Transform coordinates
                if (!service.transformFeedbackLocation(serviceParams)) {
                    // json corrupted/parsing failed
                    throw new ActionParamsException("Invalid lat, long values in Feedback (Open311) post parameters" );
                }
                paramsOk = true;
                break;
            case ServiceParams.API_METHOD_GET_FEEDBACK:
                serviceParams.setSourceEpsg(params.getRequiredParam(PARAM_SRS));
                final String getServiceRequests = params.getRequiredParam(API_PARAM_GETSERVICEREQUEST);
                serviceParams.setGetServiceRequests(JSONHelper.createJSONObject(getServiceRequests));
                if (serviceParams.getGetServiceRequests() == null) {
                    // json corrupted/parsing failed
                    throw new ActionParamsException("Invalid parameters for to get Feedbacks (Open311)" );
                }
                paramsOk = true;
                break;

            default:
                log.error("Unknown Open311 method in parameters");
                throw new ActionParamsException("Feedback (Open311) request method is invalid " + method);

        }
         //TODO: Validate params in details
        if(paramsOk == false){
            throw new ActionParamsException("Feedback (Open311) request params are invalid");
        }

        return serviceParams;

    }


}
