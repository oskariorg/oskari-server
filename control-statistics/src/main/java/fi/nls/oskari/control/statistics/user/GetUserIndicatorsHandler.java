package fi.nls.oskari.control.statistics.user;

import fi.nls.oskari.control.*;
import org.oskari.statistics.user.UserIndicatorService;
import org.oskari.statistics.user.UserIndicatorServiceImpl;
import fi.nls.oskari.annotation.OskariActionRoute;
import org.oskari.statistics.user.UserIndicator;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

@OskariActionRoute("GetUserIndicators")
public class GetUserIndicatorsHandler extends ActionHandler {

    private static UserIndicatorService userIndicatorService = new UserIndicatorServiceImpl();

    private static String PARAM_INDICATOR_ID = "id";
    private static final Logger log = LogFactory.getLogger(GetUserIndicatorsHandler.class);


    public void handleAction(ActionParameters params) throws ActionException {
        params.requireLoggedInUser();

        int id = getRequestedId(params.getHttpParam(PARAM_INDICATOR_ID, "-1"));

        if(id != -1) {
            handleSingleIndicator(params, id);
            return;
        }

        long uid = params.getUser().getId();
        List<UserIndicator> uiList = userIndicatorService.findAllOfUser(uid);
        if ( uiList != null && uiList.size() > 0 )  {
            log.debug("GetUserIndicatorsHandler" + uiList.get(0).toString());
        }
        final JSONArray result = makeJson(uiList);
        ResponseHelper.writeResponse(params, result);
    }

    private void handleSingleIndicator(ActionParameters params, int id) throws ActionException {
        UserIndicator ui = userIndicatorService.find(id);
        if(ui == null) {
            throw new ActionParamsException("Unknown indicator");
        }
        log.debug("Requested id", id, "found:", ui);
        // should we allow download if indicator is published?
        if(params.getUser().getId() != ui.getUserId()) {
            throw new ActionDeniedException("Wrong user");
        }
        final JSONObject result = makeJson(ui);
        ResponseHelper.writeResponse(params, result);
    }

    private int getRequestedId(String paramValue) throws ActionException {
        try {
            return Integer.parseInt(getId(paramValue));
        } catch (NumberFormatException nfe) {
            throw new ActionParamsException("Invalid id");
        }
    }

    private String getId(String id) {
        // FIXME: the id shouldn't include _ with the new statsgrid bundle.
        String [] parts = id.split("_");
        if (parts.length < 2) return id;
        return parts[parts.length-1];
    }

    private JSONArray makeJson(List<UserIndicator> uiList) {
        JSONArray arr = new JSONArray();
        for (UserIndicator ui : uiList ) {
             arr.put(makeJson(ui.getId(), ui.getTitle(),  ui.getDescription(), ui.getSource(), ui.getYear(), ui.isPublished(), ui.getCategory()));
        }
        return arr;
    }

    private JSONObject makeJson(long id, String title, String desc, String source, int year, Boolean pub, String category) {
        JSONObject obj = new JSONObject();
        JSONHelper.putValue(obj, "id",id);
        JSONHelper.putValue(obj, "title", JSONHelper.createJSONObject(title));
        JSONObject descJSON = desc == null ? new JSONObject() : JSONHelper.createJSONObject(desc);
        JSONHelper.putValue(obj, "description", descJSON);
        JSONObject orgJSON = source == null ? new JSONObject() : JSONHelper.createJSONObject(source);
        JSONHelper.putValue(obj, "organization", orgJSON);
        JSONHelper.putValue(obj, "year", year);
        JSONHelper.putValue(obj, "public" , pub);
        JSONHelper.putValue(obj, "category", category);
        return  obj;
    }

    public static JSONObject makeJson(UserIndicator ui) {
        JSONObject descJSON =  ui.getDescription() == null ? new JSONObject() : JSONHelper.createJSONObject(ui.getDescription());
        JSONObject titleJSON =  ui.getTitle() == null ? new JSONObject() : JSONHelper.createJSONObject(ui.getTitle());
        JSONObject dataJSON =  ui.getData() == null ? new JSONObject() : JSONHelper.createJSONObject(ui.getData());
        JSONObject organizationJSON =  ui.getSource() == null ? new JSONObject() : JSONHelper.createJSONObject(ui.getSource());
        JSONObject obj = new JSONObject();
        JSONHelper.putValue(obj, "id", ui.getId());
        JSONHelper.putValue(obj, "title", titleJSON);
        JSONHelper.putValue(obj, "description", descJSON);
        JSONHelper.putValue(obj, "organization", organizationJSON);
        JSONHelper.putValue(obj, "public" , ui.isPublished());
        JSONHelper.putValue(obj, "year", ui.getYear());
        JSONHelper.putValue(obj, "data", dataJSON);
        JSONHelper.putValue(obj, "category", ui.getCategory());
        return obj;
    }

    public void setUserIndicatorService(UserIndicatorService uis) {
        userIndicatorService = uis;
    }
}


