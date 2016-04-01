package fi.nls.oskari.control.statistics.user;

import fi.mml.map.mapwindow.service.db.UserIndicatorService;
import fi.mml.map.mapwindow.service.db.UserIndicatorServiceImpl;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.map.indicator.UserIndicator;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: EVALANTO
 * Date: 22.11.2013
 * Time: 16:49
 * To change this template use File | Settings | File Templates.
 * 
 */
@OskariActionRoute("GetUserIndicators")
public class GetUserIndicatorsHandler extends ActionHandler {

    private static UserIndicatorService userIndicatorService = new UserIndicatorServiceImpl();

    private static String PARAM_INDICATOR_ID = "id";
    private static final Logger log = LogFactory.getLogger(GetUserIndicatorsHandler.class);


    public void handleAction(ActionParameters params) throws ActionException {
        if (params.getUser().isGuest()) {
            throw new ActionDeniedException("Session expired");
        }
        int id  = -1;
        try {
            id = Integer.parseInt(getId(params.getHttpParam(PARAM_INDICATOR_ID, "-1")));
        } catch (NumberFormatException nfe) {
            throw  new ActionException(" Invalid number ");
        }


        if(id ==-1) {  //hae kaikille omille indikaattoreille
            long uid = params.getUser().getId();
            List<UserIndicator> uiList = userIndicatorService.findAllOfUser(uid);
            if ( uiList != null && uiList.size() > 0 )  {
                log.debug("GetUserIndicatorsHandler" + uiList.get(0).toString());

            }
            final JSONArray result = makeJson(uiList);
            ResponseHelper.writeResponse(params, result);
        } else {   //hae id:llä
            UserIndicator ui = userIndicatorService.find(id);
              log.debug("GetUserIndicatorsHandler: got "+ ui +" with id "+id);
            if ( ui != null && (params.getUser().getId() == ui.getUserId()) ) {
                final JSONObject result = makeJson(ui);
                ResponseHelper.writeResponse(params, result);
            }
        }

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

    private JSONObject makeJson(UserIndicator ui) {
        JSONObject descJSON =  ui.getDescription() == null ? new JSONObject() : JSONHelper.createJSONObject(ui.getDescription());
        JSONObject titleJSON =  ui.getTitle() == null ? new JSONObject() : JSONHelper.createJSONObject(ui.getTitle());
        JSONArray dataJSON =  ui.getData() == null ? new JSONArray() : JSONHelper.createJSONArray(ui.getData());
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
    private String getId(String id) {
        String [] parts = id.split("_");
        if (parts.length < 2) return id;
        return parts[parts.length-1];
    }
}


