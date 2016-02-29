package fi.nls.oskari.control.rating;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.rating.Rating;
import fi.nls.oskari.util.ResponseHelper;
import fi.nls.oskari.rating.RatingService;
import fi.nls.oskari.rating.RatingServiceMybatisImpl;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.List;

/**
 * Created by MHURME on 16.9.2015.
 */
@OskariActionRoute("UserFeedback")
public class UserFeedbackHandler extends RestActionHandler {


    private static final Logger log = LogFactory.getLogger(UserFeedbackHandler.class);

    private final RatingService ratingService = new RatingServiceMybatisImpl();

    @Override
    public void init() {

    }

    @Override
    public void handleGet(ActionParameters params) throws ActionException {
        JSONArray result = new JSONArray();
        try {
            JSONObject jsonData = getRequestParameters(params.getHttpParam("data"));
            String resource = (String)jsonData.get("category");
            String resourceId = (String)jsonData.get("categoryItem");

            String[] average = ratingService.getAverageRatingFor(resource, resourceId);
            log.debug("average: " + average[0] + ", amount: " + average[1]);

            result.put(FeedbackJSONFormatter.getAverageJSON(resourceId, average));
            result.put(getRatingsJSON(resource, resourceId));
        } catch (Exception e) {
            e.printStackTrace();
            throw new ActionException("Failed to get feedback");
        }
        ResponseHelper.writeResponse(params, result);
    }

    @Override
    public void handlePut(ActionParameters params) throws ActionException {
        handlePost(params);
    }

    @Override
    public void handlePost(ActionParameters params) throws ActionException {
        params.requireLoggedInUser();

        try {
            Rating result = saveFeedBackToServer(params);
            org.json.JSONObject averageJSON = FeedbackJSONFormatter.getAverageJSON(
                    result.getCategoryItem(), ratingService.getAverageRatingFor(result.getCategory(), result.getCategoryItem())
            );
            ResponseHelper.writeResponse(params, averageJSON);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ActionException("Failed to save feedback");
        }
    }

    private Rating saveFeedBackToServer(ActionParameters params){
        try{
            JSONObject requestParameters = getRequestParameters(params.getHttpParam("data"));
            Rating feedback = createRatingFromRequest(requestParameters, params.getUser());
            if (!ratingService.validateRequiredStrings(feedback))
                throw new ActionException("Failed to save feedback, category information missing");
            return ratingService.saveRating(feedback);
        } catch (Exception e){
            log.error(e.getMessage());
            log.error(e.toString());
        }
        return new Rating();
    }

    private JSONArray getRatingsJSON(String resource, String resourceId) throws JSONException {
        List<Rating> ratings = ratingService.getAllRatingsFor(resource, resourceId);
        JSONArray ratingsJSON = new JSONArray();
        for (Rating rating : ratings) {
            ratingsJSON.put(FeedbackJSONFormatter.getRatingsJSON(rating));
        }
        return ratingsJSON;
    }

    private Rating createRatingFromRequest(JSONObject requestParameters, User user) throws JSONException {
        Rating rating = new Rating();
        if (requestParameters.get("id") != null && !((String) requestParameters.get("id")).isEmpty()) {
            rating.setId(Long.valueOf((String) requestParameters.get("id")));
        }
        rating.setCategory((String) requestParameters.get("category"));
        rating.setCategoryItem((String) requestParameters.get("categoryItem"));
        rating.setComment((String) requestParameters.get("userComment"));
        rating.setRating(Integer.parseInt((String) requestParameters.get("score")));
        rating.setUserId(user.getId());
        rating.setUserRole((String) requestParameters.get("userRole"));

        return rating;
    }

    private JSONObject getRequestParameters(String data) throws ActionException {
        if(log.isDebugEnabled()) printRequestData(data);
        try {
            JSONParser parser = new JSONParser();
            JSONObject jsonData = (JSONObject) parser.parse(data);
            return jsonData;
        } catch (Exception e) {
            e.printStackTrace();
            log.debug(e.toString());
            throw new ActionException("Couldn't parse rating form's JSON");
        }
    }


    private String printRequestData(String data) throws ActionException{
        log.debug("data from FrontEnd: " + data);
        try{
            JSONParser parser = new JSONParser();
            JSONObject jsonData = (JSONObject)parser.parse(data);
            log.debug(jsonData.get("id"));
            log.debug(jsonData.get("score"));
            log.debug(jsonData.get("userRole"));
            log.debug(jsonData.get("userComment"));
            log.debug(jsonData.get("categoryItem"));
            log.debug(jsonData.get("category"));
            log.debug(jsonData.get("username"));
        }catch(Exception e){
            e.printStackTrace();
            log.debug(e.toString());
            throw new ActionException("Couldn't parse rating form's JSON");
        }
        return null;
    }
}
