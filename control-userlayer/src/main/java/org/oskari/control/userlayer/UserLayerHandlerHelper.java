package org.oskari.control.userlayer;

import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.oskari.map.userlayer.service.UserLayerDbService;

import fi.nls.oskari.control.ActionConstants;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.domain.map.userlayer.UserLayer;
import fi.nls.oskari.util.JSONHelper;

public class UserLayerHandlerHelper {

    public static UserLayer getUserLayer(UserLayerDbService service, ActionParameters params) throws ActionException {
        params.requireLoggedInUser();
        long id = params.getRequiredParamLong(ActionConstants.KEY_ID);
        UserLayer userLayer = service.getUserLayerById(id);
        if (userLayer == null) {
            throw new ActionParamsException("UserLayer doesn't exist: " + id);
        }
        if (!userLayer.isOwnedBy(params.getUser().getUuid())) {
            throw new ActionDeniedException("UserLayer belongs to another user");
        }
        return userLayer;
    }
    public static JSONObject createErrorJSON (String error, String key, Set<String> set) {
        JSONObject obj = createErrorJSON (error);
        JSONHelper.put(obj, key, parseStringSet(set));
        return obj;
    }
    public static JSONObject createErrorJSON (String error, String key, String value) {
        JSONObject obj = JSONHelper.createJSONObject("error", error);
        JSONHelper.putValue(obj, key, value);
        return obj;
    }
    public static JSONObject createErrorJSON (String error) {
        return JSONHelper.createJSONObject("error", error);
    }
    public static void addSetToErrorJSON (JSONObject obj, String key, Set<String> set) {
        if (set.isEmpty()){
            return;
        }
        if (obj == null) {
            obj = new JSONObject();
        }
        JSONHelper.put(obj, key, parseStringSet(set));
    }
    public static void addMapToErrorJSON (JSONObject obj, String key, Map<String,String> map) {
        if(map.isEmpty()){
            return;
        }
        if (obj == null) {
            obj = new JSONObject();
        }
        JSONHelper.putValue(obj, key, new JSONObject(map));
    }
    public static JSONObject addStringToErrorJSON (JSONObject obj, String key, String str) {
        if (obj == null) {
            obj = new JSONObject();
        }
        JSONHelper.putValue(obj, key, str);
        return obj;
    }
    public static JSONArray parseStringSet (Set<String> set){
        JSONArray json = new JSONArray();
        set.stream()
            .forEach(e -> json.put(e));
        return json;
    }
}
