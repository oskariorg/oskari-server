package fi.nls.oskari.control.data;


import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.*;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.myplaces.MyPlacesService;
import fi.nls.oskari.myplaces.MyPlacesServiceMybatisImpl;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.*;

import java.io.InputStream;

import org.apache.axiom.om.OMElement;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@OskariActionRoute("MyPlacesLayers")
public class MyPlacesLayersHandler extends RestActionHandler {

    private final static Logger log = LogFactory.getLogger(MyPlacesLayersHandler.class);
    private static final String PARAM_LAYERS = "layers";
    private static final String JSKEY_MYPLACESLAYERS = "myplaceslayers";
    private static final String JSKEY_IDLIST = "idList";
    private static final String JSKEY_SUCCESS = "success";
    private static final String JSKEY_UPDATED = "updated";
    private static final String JSKEY_DELETED = "deleted";
    private static final String DEFAULT_CATEGORY = "default_category.json";

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

    @Override
    public void handleGet(ActionParameters params) throws ActionException {
        //params.requireLoggedInUser();
        JSONObject responseJson = new JSONObject();

        try {
            OMElement request = requestBuilder.buildLayersGet(params.getUser());
            String response = GeoServerHelper.sendRequest(request);
            JSONArray layers = responseBuilder.buildLayersGet(response);
            // if user have no layers, create default
            if (layers.length() == 0){
                createDefaultCategory(params.getUser());
                response = GeoServerHelper.sendRequest(request);
                layers = responseBuilder.buildLayersGet(response);
            }
            JSONHelper.putValue(responseJson, JSKEY_MYPLACESLAYERS, layers);
            ResponseHelper.writeResponse(params, responseJson);
        }
        catch (Exception e) {
            throw new ActionException(e.getMessage());
        }
    }

    @Override
    public void handlePost(ActionParameters params) throws ActionException {
        //params.requireLoggedInUser();
        JSONObject responseJson = new JSONObject();
        try {
            String jsonString = params.getHttpParam(PARAM_LAYERS);
            OMElement request = requestBuilder.buildLayersInsert(params.getUser(), jsonString);
            String response = GeoServerHelper.sendRequest(request);
            JSONArray idList = responseBuilder.buildLayersInsert(response);
            JSONHelper.putValue(responseJson, JSKEY_IDLIST, idList);
            ResponseHelper.writeResponse(params, responseJson);
        }
        catch (Exception e) {
            log.error(e);
            throw new ActionException(e.getMessage());
        }
    }
    @Override
    public void handlePut(ActionParameters params) throws ActionException {
        //params.requireLoggedInUser();
        JSONObject responseJson = new JSONObject();
        try {
            String jsonString = params.getHttpParam(PARAM_LAYERS);

            OMElement request = requestBuilder.buildLayersUpdate(params.getUser(), jsonString);
            String response = GeoServerHelper.sendRequest(request);
            int updated = responseBuilder.buildLayersUpdate(response);
            JSONHelper.putValue(responseJson, JSKEY_SUCCESS, true); //TODO add checking
            JSONHelper.putValue(responseJson, JSKEY_UPDATED, updated);
            ResponseHelper.writeResponse(params, responseJson);
        }
        catch (Exception e) {
            if(e instanceof JSONException) {
                throw new ActionException("JSON processing error", e);
            } else if (e instanceof ActionDeniedException){
                throw (ActionException) e;
            }
            throw new ActionException(e.getMessage(), e);
        }
    }
    @Override
    public void handleDelete(ActionParameters params) throws ActionException {
        //params.requireLoggedInUser();
        JSONObject responseJson = new JSONObject();
        try {
            String jsonString = params.getHttpParam(PARAM_LAYERS);
            OMElement request = requestBuilder.buildLayersDelete(params.getUser(),jsonString);
            String response = GeoServerHelper.sendRequest(request);
            int deleted = responseBuilder.buildLayersDelete(response);
            JSONHelper.putValue(responseJson, JSKEY_SUCCESS, true); //TODO add checking
            JSONHelper.putValue(responseJson, JSKEY_DELETED, deleted);
            ResponseHelper.writeResponse(params, responseJson);
        }
        catch (Exception e) {
            throw new ActionException(e.getMessage());
        }
    }


    private void createDefaultCategory (User user) throws ActionException{
        try {
            InputStream inputStream = getClass().getResourceAsStream(DEFAULT_CATEGORY);
            String jsonString = IOHelper.readString(inputStream);
            OMElement request = requestBuilder.buildLayersInsert(user, jsonString);
            String response = GeoServerHelper.sendRequest(request);
            JSONArray idList = responseBuilder.buildLayersInsert(response);
            log.info("Created default category for user: ", user.getUuid(), " with layer id: ", idList.toString());
        }
        catch (Exception e) {
            throw new ActionException(e.getMessage());
        }
    }

    //TODO: move to util
    private String readPayload(ActionParameters params) throws ActionException {
        try {
            return IOHelper.readString(params.getRequest().getInputStream());
        } catch (Exception e) {
            throw new ActionException(e.getMessage());
        }
    }
}