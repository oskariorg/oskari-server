package fi.nls.oskari.myplaces.handler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.apache.axiom.om.OMElement;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.myplaces.MyPlaceWithGeometry;
import fi.nls.oskari.myplaces.MyPlacesService;
import fi.nls.oskari.myplaces.util.GeoJSONReader;
import fi.nls.oskari.myplaces.util.GeoServerHelper;
import fi.nls.oskari.myplaces.util.GeoServerRequestBuilder;
import fi.nls.oskari.myplaces.util.GeoServerResponseBuilder;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;

@OskariActionRoute("MyPlacesFeatures")
public class MyPlacesFeaturesHandler extends RestActionHandler {

    private static final String PARAM_FEATURES = "features";
    private static final String PARAM_LAYER_ID = "layerId";
    private static final String JSKEY_DELETED = "deleted";

    private GeoServerRequestBuilder requestBuilder;
    private MyPlacesService service;

    @Override
    public void init() {
        super.init();
        requestBuilder = new GeoServerRequestBuilder();
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

        final String response;
        if (layerId != null && !layerId.isEmpty()) {
            response = getFeaturesByLayerId(user, layerId);
        } else {
            response = getFeaturesByUserId(user.getUuid());
        }

        try {
            JSONObject featureCollection = new JSONObject(response);
            GeoServerResponseBuilder.removePrefixesFromIds(featureCollection);
            ResponseHelper.writeResponse(params, featureCollection);
        } catch (JSONException e) {
            throw new ActionException("Received unexpected response");
        }
    }

    @Override
    public void handlePost(ActionParameters params) throws ActionException {
        final User user = params.getUser();
        final byte[] payload = params.getRequestPayload();
        final MyPlaceWithGeometry[] places = parseMyPlaces(payload, false);

        long[] uniqueCategoryIds = Arrays.stream(places)
                .mapToLong(MyPlaceWithGeometry::getCategoryId)
                .distinct()
                .toArray();
        for (long categoryId : uniqueCategoryIds) {
            if (!service.canModifyCategory(user, categoryId)) {
                throw new ActionDeniedException("User: " + user.getId() +
                        " tried to insert feature into category: " + categoryId);
            }
        }

        for (MyPlaceWithGeometry place : places) {
            place.setUuid(user.getUuid());
        }

        long[] ids = insertFeatures(places);
        JSONObject featureCollection = getFeaturesByIds(ids);
        ResponseHelper.writeResponse(params, featureCollection);
    }

    @Override
    public void handlePut(ActionParameters params) throws ActionException {
        final User user = params.getUser();
        final byte[] payload = params.getRequestPayload();
        final MyPlaceWithGeometry[] places = parseMyPlaces(payload, true);

        long[] ids = Arrays.stream(places)
                .mapToLong(MyPlaceWithGeometry::getId)
                .toArray();
        for (long id : ids) {
            if (!service.canModifyPlace(user, id)) {
                throw new ActionDeniedException("User: " + user.getId() +
                        " tried to modify place: " + id);
            }
        }

        for (MyPlaceWithGeometry place : places) {
            place.setUuid(user.getUuid());
        }

        int updated = updateFeatures(places);
        if (updated < ids.length) {
            throw new ActionException("Failed to update features");
        }

        JSONObject featureCollection = getFeaturesByIds(ids);
        ResponseHelper.writeResponse(params, featureCollection);
    }

    @Override
    public void handleDelete(ActionParameters params) throws ActionException {
        final User user = params.getUser();
        final String paramFeatures = params.getRequiredParam(PARAM_FEATURES);
        final long[] ids = Arrays.stream(paramFeatures.split(","))
                .mapToLong(Long::parseLong)
                .toArray();

        for (long id : ids) {
            if (!service.canModifyPlace(user, id)) {
                throw new ActionDeniedException("User: " + user.getId() +
                        " tried to delete place: " + id);
            }
        }

        int deleted = deleteFeatures(ids);
        if (deleted < ids.length) {
            throw new ActionException("Failed to delete features");
        }

        JSONObject response = new JSONObject();
        JSONHelper.putValue(response, JSKEY_DELETED, deleted);
        ResponseHelper.writeResponse(params, response);
    }

    private String getFeaturesByUserId(String uuid) throws ActionException {
        try {
            OMElement request = requestBuilder.getFeaturesByUserId(uuid);
            return GeoServerHelper.sendRequest(request);
        } catch (IOException e) {
            throw new ActionException("IOException occured", e);
        }
    }

    private String getFeaturesByLayerId(User user, String layerIdStr)
            throws ActionException {
        long layerId = Long.parseLong(layerIdStr);
        if (!service.canModifyCategory(user, layerId)) {
            throw new ActionDeniedException("User: " + user.getId() +
                    " tried to GET features from layer: " + layerId);
        }
        try {
            OMElement request = requestBuilder.getFeaturesByLayerId(layerIdStr);
            return GeoServerHelper.sendRequest(request);
        } catch (IOException e) {
            throw new ActionException("IOException occured", e);
        }
    }

    protected MyPlaceWithGeometry[] parseMyPlaces(byte[] input, boolean shouldSetId)
            throws ActionParamsException {
        try {
            String inputStr = new String(input, StandardCharsets.UTF_8);
            JSONObject featureCollection = new JSONObject(inputStr);
            JSONArray features = featureCollection.getJSONArray("features");
            final int n = features.length();
            MyPlaceWithGeometry[] myPlaces = new MyPlaceWithGeometry[n];
            for (int i = 0; i < n; i++) {
                JSONObject feature = features.getJSONObject(i);
                myPlaces[i] = parseMyPlace(feature, shouldSetId);
            }
            return myPlaces;
        } catch (JSONException e) {
            throw new ActionParamsException("Invalid input");
        }
    }

    protected MyPlaceWithGeometry parseMyPlace(JSONObject feature, boolean shouldSetId) throws JSONException {
        MyPlaceWithGeometry myPlace = new MyPlaceWithGeometry();

        if (shouldSetId) {
            myPlace.setId(feature.getLong("id"));
        }
        myPlace.setCategoryId(feature.getLong("category_id"));

        JSONObject geomJSON = feature.getJSONObject("geometry");
        myPlace.setGeometry(GeoJSONReader.toGeometry(geomJSON));

        JSONObject properties = feature.getJSONObject("properties");
        myPlace.setName(properties.getString("name"));

        // Optional fields
        myPlace.setAttentionText(properties.optString("attention_text"));
        myPlace.setDesc(properties.optString("place_desc"));
        myPlace.setLink(properties.optString("link"));
        myPlace.setImageUrl(properties.optString("image_url"));

        return myPlace;
    }

    private JSONObject getFeaturesByIds(long[] ids) throws ActionException {
        try {
            OMElement getFeaturesRequest = requestBuilder.getFeaturesByIds(ids);
            String getFeaturesResponse = GeoServerHelper.sendRequest(getFeaturesRequest);
            JSONObject featureCollection = new JSONObject(getFeaturesResponse);
            GeoServerResponseBuilder.removePrefixesFromIds(featureCollection);
            return featureCollection;
        } catch (IOException e) {
            throw new ActionException("IOException occured", e);
        } catch (JSONException e) {
            throw new ActionException("Received invalid JSON", e);
        }
    }

    private long[] insertFeatures(MyPlaceWithGeometry[] places) throws ActionException {
        try {
            OMElement insertFeaturesRequest = requestBuilder.insertFeatures(places);
            String insertFeaturesResponse = GeoServerHelper.sendRequest(insertFeaturesRequest);
            return GeoServerResponseBuilder.getInsertedIds(insertFeaturesResponse);
        } catch (IOException e) {
            throw new ActionException("IOException occured", e);
        }
    }

    private int updateFeatures(MyPlaceWithGeometry[] places) throws ActionException {
        try {
            OMElement updateRequest = requestBuilder.updateFeatures(places);
            String updateResponse = GeoServerHelper.sendRequest(updateRequest);
            return GeoServerResponseBuilder.getTotalUpdated(updateResponse);
        } catch (IOException e) {
            throw new ActionException("IOException occured!", e);
        }
    }

    private int deleteFeatures(long[] ids) throws ActionException {
        try {
            OMElement deleteRequest = requestBuilder.deleteFeaturesByIds(ids);
            String deleteResponse = GeoServerHelper.sendRequest(deleteRequest);
            return GeoServerResponseBuilder.getTotalDeleted(deleteResponse);
        } catch (IOException e) {
            throw new ActionException("IOException occured!", e);
        }
    }

}