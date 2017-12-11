package fi.nls.oskari.control.data;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.*;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.myplaces.MyPlacesService;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.*;

import java.util.ArrayList;
import java.util.List;

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
    private static final String JSKEY_UPDATED = "updated";
    private static final String JSKEY_DELETED = "deleted";
    private static final String JSKEY_LAYERID = "category_id";
    private static final String JSKEY_ID = "id";

    private GeoServerRequestBuilder requestBuilder;
    private GeoServerResponseBuilder responseBuilder;
    private MyPlacesService service;

    @Override
    public void init() {
        super.init();
        requestBuilder =  new GeoServerRequestBuilder();
        responseBuilder = new GeoServerResponseBuilder();
        service = OskariComponentManager.getComponentOfType(MyPlacesService.class);
    }

    @Override
    public void preProcess(ActionParameters params) throws ActionException {
        super.preProcess(params);
        //only for logged in user
        params.requireLoggedInUser();
    }

    public void handleGet(ActionParameters params) throws ActionException {
        //params.requireLoggedInUser();
        try {
            JSONObject responseJson = new JSONObject();
            OMElement request;
            String layerId = params.getHttpParam(PARAM_LAYER_ID);
            String featureIds = params.getHttpParam(PARAM_FEATURE_ID);
            User user = params.getUser();
            if (featureIds != null){
                String [] idList = featureIds.split(",");
                for (String id : idList){
                    if (!service.canModifyPlace(user, Long.parseLong(id))){
                        throw new ActionDeniedException("Tried to get feature: " + id);
                    }
                }
                log.debug("Requested GetFeatures by feature ids: ", featureIds);
                request = requestBuilder.buildFeaturesGetByIds(idList);
            }else if (layerId != null){
                if (!service.canModifyCategory(user, Long.parseLong(layerId))){
                    throw new ActionDeniedException("Tried to get features from my places layer: " + layerId);
                }
                log.debug("Requested GetFeatures by layer ids: ", layerId);
                request = requestBuilder.buildFeaturesGetByLayer(layerId);
            }else{
                log.debug("Requested GetFeatures by uuid: ", params.getUser().getUuid());
                request = requestBuilder.buildFeaturesGet(user.getUuid());
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
            User user = params.getUser();
            JSONObject responseJson = new JSONObject();
            JSONArray jsonArray = new JSONArray(params.getHttpParam(PARAM_FEATURES));
            for (int i = 0 ; i < jsonArray.length() ; i++) {
                long categoryId = jsonArray.getJSONObject(i).getLong(JSKEY_LAYERID);
                if (!service.canInsert(user, categoryId)){
                    throw new ActionDeniedException("Tried to insert feature into category: " + categoryId);
                }
            }
            OMElement request = requestBuilder.buildFeaturesInsert(user.getUuid(), jsonArray);
            String response = GeoServerHelper.sendRequest(request);
            List <Integer> idList = responseBuilder.buildFeaturesInsert(response);
            JSONHelper.putValue(responseJson, JSKEY_IDLIST, idList);
            request = requestBuilder.buildFeaturesGetByIds(idList);
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
            User user = params.getUser();
            JSONObject responseJson = new JSONObject();
            JSONArray jsonArray = new JSONArray(params.getHttpParam(PARAM_FEATURES));
            List <Integer> idList = new ArrayList<Integer>();
            for (int i = 0 ; i < jsonArray.length() ; i++) {
                int id = jsonArray.getJSONObject(i).getInt(JSKEY_ID);
                //store feature ids
                idList.add(id);
                if (!service.canModifyPlace(user, id)){
                    throw new ActionDeniedException("Tried to modify place: " + id);
                }
            }
            OMElement request = requestBuilder.buildFeaturesUpdate(user.getUuid(), jsonArray);
            String response = GeoServerHelper.sendRequest(request);
            int updated = responseBuilder.buildFeaturesUpdate(response);
            JSONHelper.putValue(responseJson, JSKEY_UPDATED, updated);
            request = requestBuilder.buildFeaturesGetByIds(idList);
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
        try {
            User user = params.getUser();
            JSONObject responseJson = new JSONObject();
            String [] idList = params.getHttpParam(PARAM_FEATURES).split(",");
            for (String id : idList){
                if (!service.canModifyPlace(user, Long.parseLong(id))){
                    throw new ActionDeniedException("Tried to delete place: " + id);
                }
            }
            OMElement request = requestBuilder.buildFeaturesDelete(idList);
            String response = GeoServerHelper.sendRequest(request);
            int deleted = responseBuilder.buildFeaturesDelete(response);
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