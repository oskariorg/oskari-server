package fi.nls.oskari.control.data;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.feedback.FeedbackResponse;
import fi.nls.oskari.feedback.ServiceParams;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.feedback.FeedbackService;
import fi.nls.oskari.feedback.open311.FeedbackImpl;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.map.view.ViewServiceIbatisImpl;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONObject;

import static fi.nls.oskari.control.ActionConstants.*;


/**
 * Send feedback to feedback services e.g. Open311
 * }
 */
@OskariActionRoute("Feedback")
public class FeedbackHandler extends ActionHandler {

    static final String API_PARAM_METHOD = "method";
    static final String API_PARAM_SERVICEID = "serviceId";
    static final String API_PARAM_PAYLOAD = "payload";
    static final String KEY_BUNDLENAME = "feedbackService";
    static final String KEY_EXTENSIONS = "extensions";
    static final String KEY_KEY = "key";

    private static final Logger log = LogFactory.getLogger(FeedbackHandler.class);

    private FeedbackService service = null;
    private ViewService viewService = null;

    @Override
    public void init() {
        super.init();
        service = new FeedbackImpl();
        viewService = new ViewServiceIbatisImpl();
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

        if (data == null || data.length() == 0) {
            result.setSuccess(false);
        } else {
            result.setSuccess(true);
            result.setData(data);
        }

        ResponseHelper.writeResponse(params, result.toJSON());
    }

    /**
     * Builds and validates request parameters according to requested method
     *
     * @param method
     * @param params
     * @return
     * @throws ActionException
     */
    public ServiceParams getServiceParams(final String method, final ActionParameters params) throws ActionException {

        Boolean paramsOk = false;
        ServiceParams serviceParams = new ServiceParams();
        // base url, key and extensions values will be fetch from the embedded view metadata
        View view = viewService.getViewWithConfByUuId(params.getRequiredParam(PARAM_UUID));
        if (view == null) {
            log.error("Open311 feedback - embedded view is not available");
            throw new ActionParamsException("Open311 feedback - embedded view is not available");
        }
        JSONObject fbMetadata = JSONHelper.getJSONObject(view.getMetadata(), KEY_BUNDLENAME);
        serviceParams.setBaseUrl(JSONHelper.getStringFromJSON(fbMetadata, KEY_URL, null));
        if (serviceParams.getBaseUrl() == null) {
            log.error("Open311  feedback service url is not available");
            throw new ActionParamsException("Open311 feedback service url is not available");
        }
        serviceParams.setBaseUrlExtensions(JSONHelper.getStringFromJSON(fbMetadata, KEY_EXTENSIONS, ""));
        serviceParams.setPostContentType(PropertyUtil.get("feedback.open311.postContentType", ServiceParams.DEFAULT_POST_CONTENTTYPE));
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
                final String postServiceRequest = params.getRequiredParam(API_PARAM_PAYLOAD);
                serviceParams.setPostServiceRequest(JSONHelper.createJSONObject(postServiceRequest));
                // Default is, that key is needed for posting data, but it is not always so
                serviceParams.setApiKey(JSONHelper.getStringFromJSON(fbMetadata, KEY_KEY, null));
                // Transform coordinates
                if (!service.transformFeedbackLocation(serviceParams)) {
                    // json corrupted/parsing failed
                    throw new ActionParamsException("Invalid lat, long values or geometry in Feedback (Open311) post parameters");
                }
                paramsOk = true;
                break;
            case ServiceParams.API_METHOD_GET_FEEDBACK:
                serviceParams.setSourceEpsg(params.getRequiredParam(PARAM_SRS));
                final String getServiceRequests = params.getRequiredParam(API_PARAM_PAYLOAD);
                serviceParams.setGetServiceRequests(JSONHelper.createJSONObject(getServiceRequests));
                if (serviceParams.getGetServiceRequests() == null) {
                    // json corrupted/parsing failed
                    throw new ActionParamsException("Invalid parameters for to get Feedbacks (Open311)");
                }
                // Transform coordinates in feedback params, if any  (bbox, long, lat)
                if (!service.transformGetFeedbackLocation(serviceParams)) {
                    // json corrupted/parsing failed
                    throw new ActionParamsException("Invalid lat, long or bbox in get Feedback (Open311) parameters");
                }
                paramsOk = true;
                break;

            default:
                log.error("Unknown Open311 method in parameters");
                throw new ActionParamsException("Feedback (Open311) request method is invalid " + method);

        }
        //TODO: Validate params in details
        if (paramsOk == false) {
            throw new ActionParamsException("Feedback (Open311) request params are invalid");
        }

        return serviceParams;

    }


}
