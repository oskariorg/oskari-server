package fi.nls.oskari.feedback;

import org.json.JSONObject;



/**
 * interface to Feedback services e.g. Open311
 * Created by Oskari Team on 12.4.2016.
 */
public interface FeedbackService {

    JSONObject getServiceResult(ServiceParams params);
    Boolean transformFeedbackLocation(ServiceParams serviceParams);
    Boolean transformGetFeedbackLocation(ServiceParams serviceParams);
}
