package fi.nls.oskari.control.data;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.*;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.myplaces.MyPlacesService;
import fi.nls.oskari.myplaces.MyPlacesServiceMybatisImpl;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.*;

import org.apache.axiom.om.OMElement;
import org.json.JSONArray;
import org.json.JSONObject;


@OskariActionRoute("MyPlacesFeatures")
public class MyPlacesFeaturesHandler extends RestActionHandler {

    private final static Logger log = LogFactory.getLogger(MyPlacesFeaturesHandler.class);
    private static final String PARAM_FEATURES = "features";
    private static final String PARAM_LAYER_ID = "layerId";
    private static final String PARAM_FEATURE_ID = "featureId";
    private static final String JSKEY_FEATURES = "features";
    private static final String JSKEY_IDLIST = "idList";
    private static final String JSKEY_SUCCESS = "success";
    private static final String JSKEY_UPDATED = "updated";
    private static final String JSKEY_DELETED = "deleted";

    private GeoServerRequestBuilder requestBuilder;
    private GeoServerResponseBuilder responseBuilder;

    @Override
    public void init() {
        super.init();
        requestBuilder =  new GeoServerRequestBuilder();
        responseBuilder = new GeoServerResponseBuilder();
    }

    @Override
    public void preProcess(ActionParameters params) throws ActionException {
        super.preProcess(params);
        //only for logged in user
        params.requireLoggedInUser();
    }

    public void handleGet(ActionParameters params) throws ActionException {
        //params.requireLoggedInUser();
        JSONObject responseJson = new JSONObject();
        try {
            OMElement request;
            String layerId = params.getHttpParam(PARAM_LAYER_ID);
            String featureIds = params.getHttpParam(PARAM_FEATURE_ID);
            if (featureIds != null){
                log.debug("Requested GetFeatures by feature ids: ", featureIds);
                request = requestBuilder.buildFeaturesGetByIds(params.getUser(), featureIds);
            }else if (layerId != null){
                log.debug("Requested GetFeatures by layer ids: ", layerId);
                request = requestBuilder.buildFeaturesGetByLayer(params.getUser(), layerId);
            }else{
                log.debug("Requested GetFeatures by uuid: ", params.getUser().getUuid());
                request = requestBuilder.buildFeaturesGet(params.getUser());
            }
            String response = GeoServerHelper.sendRequest(request);
            JSONArray features = responseBuilder.buildFeaturesGet(response);
            JSONHelper.putValue(responseJson, JSKEY_FEATURES, features);
            ResponseHelper.writeResponse(params, responseJson);
        }
        catch (Exception e) {
            throw new ActionException(e.getMessage());
        }
    }

    public void handlePost(ActionParameters params) throws ActionException {
        //params.requireLoggedInUser();
        try {
            JSONObject responseJson = new JSONObject();
            String jsonString = params.getHttpParam(PARAM_FEATURES);
            OMElement request = requestBuilder.buildFeaturesInsert(params.getUser(), jsonString);
            String response = GeoServerHelper.sendRequest(request);
            JSONArray idList = responseBuilder.buildFeaturesInsert(response);
            JSONHelper.putValue(responseJson, JSKEY_IDLIST, idList);
            request = requestBuilder.buildFeaturesGetByIds(params.getUser(), idList.join(","));
            response = GeoServerHelper.sendRequest(request);
            JSONArray features = responseBuilder.buildFeaturesGet(response);
            JSONHelper.putValue(responseJson, JSKEY_FEATURES, features);
            ResponseHelper.writeResponse(params, responseJson);
        }
        catch (Exception e) {
            throw new ActionException(e.getMessage());
        }
    }

    public void handlePut(ActionParameters params) throws ActionException {
        //params.requireLoggedInUser();
        try {
            JSONObject responseJson = new JSONObject();
            String jsonString = params.getHttpParam(PARAM_FEATURES);
            OMElement request = requestBuilder.buildFeaturesUpdate(params.getUser(), jsonString);
            String response = GeoServerHelper.sendRequest(request);
            int updated = responseBuilder.buildFeaturesUpdate(response);
            JSONHelper.putValue(responseJson, JSKEY_SUCCESS, true); //TODO add checking
            JSONHelper.putValue(responseJson, JSKEY_UPDATED, updated);
            //get feature ids
            JSONArray jsonArray = new JSONArray(jsonString);
            String idList = "";
            for (int i=0; i < jsonArray.length(); i++){
                idList += jsonArray.getJSONObject(i).get("id");
            }
            request = requestBuilder.buildFeaturesGetByIds(params.getUser(), idList);
            response = GeoServerHelper.sendRequest(request);
            //add features to response
            JSONArray features = responseBuilder.buildFeaturesGet(response);
            JSONHelper.putValue(responseJson, JSKEY_FEATURES, features);
            ResponseHelper.writeResponse(params, responseJson);
        }
        catch (Exception e) {
            throw new ActionException(e.getMessage());
        }
    }

    public void handleDelete(ActionParameters params) throws ActionException {
        //params.requireLoggedInUser();
        JSONObject responseJson = new JSONObject();
        try {
            String jsonString = params.getHttpParam(PARAM_FEATURES);
            OMElement request = requestBuilder.buildFeaturesDelete(params.getUser(), jsonString);
            String response = GeoServerHelper.sendRequest(request);
            int deleted = responseBuilder.buildFeaturesDelete(response);
            JSONHelper.putValue(responseJson, JSKEY_SUCCESS, true); //TODO add checking
            JSONHelper.putValue(responseJson, JSKEY_DELETED, deleted);
            ResponseHelper.writeResponse(params, responseJson);
        }
        catch (Exception e) {
            throw new ActionException(e.getMessage(), e);
        }
    }

    private String readPayload(ActionParameters params) throws ActionException {
        try {
            return IOHelper.readString(params.getRequest().getInputStream());
        } catch (Exception e) {
            throw new ActionException(e.getMessage());
        }
    }
 }