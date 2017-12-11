package fi.nls.oskari.myplaces.handler;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.*;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.myplaces.MyPlacesService;
import fi.nls.oskari.myplaces.util.GeoServerHelper;
import fi.nls.oskari.myplaces.util.GeoServerRequestBuilder;
import fi.nls.oskari.myplaces.util.GeoServerResponseBuilder;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.*;

import java.io.IOException;
import java.util.Arrays;
import org.apache.axiom.om.OMElement;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@OskariActionRoute("MyPlacesFeatures")
public class MyPlacesFeaturesHandler extends RestActionHandler {

    private static final Logger LOG = LogFactory.getLogger(MyPlacesFeaturesHandler.class);

    private static final String PARAM_FEATURES = "features";
    private static final String PARAM_LAYER_ID = "layerId";
    private static final String PARAM_FEATURE_ID = "featureId";
    private static final String JSKEY_FEATURES = "features";
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
        params.requireLoggedInUser();
    }

    @Override
    public void handleGet(ActionParameters params) throws ActionException {
        final User user = params.getUser();

        final String layerId = params.getHttpParam(PARAM_LAYER_ID);
        final String featureIds = params.getHttpParam(PARAM_FEATURE_ID);

        final OMElement request;
        if (featureIds != null && !featureIds.isEmpty()) {
            String[] idList = featureIds.split(",");
            long[] ids = Arrays.stream(idList)
                    .mapToLong(Long::parseLong)
                    .toArray();
            for (long id : ids) {
                if (!service.canModifyPlace(user, id)) {
                    throw new ActionDeniedException("User " + user.getId() +
                            "tried to GET feature " + id);
                }
            }
            request = requestBuilder.buildFeaturesGetByIds(ids);
        } else if (layerId != null && !layerId.isEmpty()) {
            if (!service.canModifyCategory(user, Long.parseLong(layerId))) {
                throw new ActionDeniedException("User " + user.getId() +
                        " tried to GET features from layer " + layerId);
            }
            request = requestBuilder.buildFeaturesGetByLayer(layerId);
        } else {
            request = requestBuilder.buildFeaturesGet(user.getUuid());
        }

        try {
            String response = GeoServerHelper.sendRequest(request);
            JSONArray features = responseBuilder.buildFeaturesGet(response);
            JSONObject responseJson = new JSONObject();
            JSONHelper.putValue(responseJson, JSKEY_FEATURES, features);
            ResponseHelper.writeResponse(params, responseJson);
            return;
        } catch (IOException e) {
            LOG.warn(e, "Failed to get response for request:", request);
        } catch (JSONException e) {
            LOG.warn(e, "Failed to parse response for request:", request);
        }
        throw new ActionException("Failed to get Features!");
    }

    @Override
    public void handlePost(ActionParameters params) throws ActionException {
        // TODO: I'm ugly, please help me get rid of using JSONArrays and JSONObjects
        // and all the JSONExceptions by replacing the model with appropriate Java class, thanks!

        final User user = params.getUser();

        final JSONArray features;
        try {
            features = new JSONArray(params.getRequiredParam(PARAM_FEATURES));
        } catch (JSONException e) {
            throw new ActionParamsException("Invalid parameter for key " + PARAM_FEATURES);
        }

        try {
            // Check that user has has rights
            for (int i = 0; i < features.length(); i++) {
                JSONObject feature = features.getJSONObject(i);
                long categoryId = feature.getLong(JSKEY_LAYERID);
                if (!service.canInsert(user, categoryId)) {
                    throw new ActionDeniedException("User " + user.getId() +
                            " tried to insert feature into category: " + categoryId);
                }
            }
        } catch (JSONException e) {
            throw new ActionParamsException("Invalid parameter for key " + PARAM_FEATURES, e);
        }


        OMElement insertFeaturesRequest;
        try {
            insertFeaturesRequest = requestBuilder.buildFeaturesInsert(user.getUuid(), features);
        } catch (JSONException e) {
            throw new ActionParamsException("Invalid parameter for key " + PARAM_FEATURES, e);
        }

        try {
            String insertFeaturesResponse = GeoServerHelper.sendRequest(insertFeaturesRequest);
            long[] insertedIds = responseBuilder.getInsertedIds(insertFeaturesResponse);

            OMElement getFeaturesRequest = requestBuilder.buildFeaturesGetByIds(insertedIds);
            String getFeaturesResponse = GeoServerHelper.sendRequest(getFeaturesRequest);
            JSONArray insertedFeatures = responseBuilder.buildFeaturesGet(getFeaturesResponse);

            JSONObject response = new JSONObject();
            JSONHelper.putValue(response, JSKEY_FEATURES, insertedFeatures);
            ResponseHelper.writeResponse(params, response);
        } catch (IOException e) {
            throw new ActionException("IOException occured", e);
        } catch (JSONException e) {
            throw new ActionException("Internal error", e);
        }

    }

    @Override
    public void handlePut(ActionParameters params) throws ActionException {
        // TODO: I'm ugly, please help me get rid of using JSONArrays and JSONObjects
        // and all the JSONExceptions by replacing the model with appropriate Java class, thanks!

        final User user = params.getUser();

        final JSONArray features;
        try {
            features = new JSONArray(params.getRequiredParam(PARAM_FEATURES));
        } catch (JSONException e) {
            throw new ActionParamsException("Invalid parameter for key " + PARAM_FEATURES);
        }

        final long[] ids = new long[features.length()];
        try {
            for (int i = 0; i < features.length() ; i++) {
                JSONObject feature = features.getJSONObject(i);
                long placeId = feature.getLong(JSKEY_ID);
                ids[i] = placeId;
            }
        } catch (JSONException e) {
            throw new ActionParamsException("Invalid parameter for key " + PARAM_FEATURES);
        }

        for (long id : ids) {
            if (!service.canModifyPlace(user, id)) {
                throw new ActionDeniedException("User: " + user.getId() +
                        " tried to modify place: " + id);
            }
        }

        OMElement updateRequest;
        try {
            updateRequest = requestBuilder.buildFeaturesUpdate(user.getUuid(), features);
        } catch (JSONException e) {
            throw new ActionParamsException("Invalid parameter for key " + PARAM_FEATURES);
        }

        JSONArray updatedFeatures;
        try {
            String updateResponse = GeoServerHelper.sendRequest(updateRequest);
            int updated = responseBuilder.getTotalUpdated(updateResponse);
            if (updated < 0) {
                throw new ActionException("Failed to update features");
            }

            OMElement getFeaturesRequest = requestBuilder.buildFeaturesGetByIds(ids);
            String getFeaturesResponse = GeoServerHelper.sendRequest(getFeaturesRequest);
            updatedFeatures = responseBuilder.buildFeaturesGet(getFeaturesResponse);
        } catch (IOException e) {
            throw new ActionException("IOException occured", e);
        } catch (JSONException e) {
            throw new ActionException("Internal error", e);
        }

        JSONObject response = new JSONObject();
        JSONHelper.putValue(response, JSKEY_FEATURES, updatedFeatures);
        ResponseHelper.writeResponse(params, response);
    }

    @Override
    public void handleDelete(ActionParameters params) throws ActionException {
        final User user = params.getUser();
        final long[] ids = Arrays.stream(params.getRequiredParam(PARAM_FEATURES).split(","))
                .mapToLong(Long::parseLong)
                .toArray();

        for (long id : ids) {
            if (!service.canModifyPlace(user, id)) {
                throw new ActionDeniedException("User: " + user.getId() +
                        " tried to delete place: " + id);
            }
        }

        try {
            OMElement deleteRequest = requestBuilder.buildFeaturesDelete(ids);
            String deleteResponse = GeoServerHelper.sendRequest(deleteRequest);
            int deleted = responseBuilder.getTotalDeleted(deleteResponse);
            if (deleted < 0) {
                throw new ActionException("Failed to delete features");
            }
            JSONObject response = new JSONObject();
            JSONHelper.putValue(response, JSKEY_DELETED, deleted);
            ResponseHelper.writeResponse(params, response);
        } catch (IOException e) {
            throw new ActionException("IOException occured", e);
        }
    }

}