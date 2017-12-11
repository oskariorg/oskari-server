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

import java.io.InputStream;
import java.util.Arrays;

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
    private static final String JSKEY_UPDATED = "updated";
    private static final String JSKEY_DELETED = "deleted";
    private static final String JSKEY_ID = "id";
    private static final String DEFAULT_CATEGORY = "default_category.json";


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
        try {
            JSONObject responseJson = new JSONObject();
            OMElement request = requestBuilder.buildLayersGet(params.getUser().getUuid());
            String response = GeoServerHelper.sendRequest(request);
            JSONArray layers = responseBuilder.buildLayersGet(response);
            // if user have no layers, create default
            if (layers.length() == 0){
                createDefaultCategory(params.getUser().getUuid());
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
        try {
            JSONObject responseJson = new JSONObject();
            String jsonString = params.getHttpParam(PARAM_LAYERS);
            OMElement request = requestBuilder.buildLayersInsert(params.getUser().getUuid(), jsonString);
            String response = GeoServerHelper.sendRequest(request);
            long[] idList = responseBuilder.getInsertedIds(response);
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
        try {
            User user = params.getUser();
            JSONObject responseJson = new JSONObject();
            JSONArray jsonArray = new JSONArray(params.getHttpParam(PARAM_LAYERS));
            for (int i = 0 ; i < jsonArray.length() ; i++){
                long id = jsonArray.getJSONObject(i).getLong(JSKEY_ID);
                if (!service.canModifyCategory(user, id)){
                    throw new ActionDeniedException("Tried to modify category: " + id);
                }
            }
            OMElement request = requestBuilder.buildLayersUpdate(user.getUuid(), jsonArray);
            String response = GeoServerHelper.sendRequest(request);
            int updated = responseBuilder.getTotalUpdated(response);
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
        try {
            JSONObject responseJson = new JSONObject();
            User user = params.getUser();
            String ids = params.getHttpParam(PARAM_LAYERS);
            String [] idList = ids.split(",");
            for (String id : idList){
                if (!service.canModifyCategory(user, Long.parseLong(id))){
                    throw new ActionDeniedException("Tried to delete category: " + id);
                }
            }
            OMElement request = requestBuilder.buildLayersDelete(idList);
            String response = GeoServerHelper.sendRequest(request);
            int deleted = responseBuilder.getTotalDeleted(response);
            JSONHelper.putValue(responseJson, JSKEY_DELETED, deleted);
            ResponseHelper.writeResponse(params, responseJson);
        }
        catch (Exception e) {
            throw new ActionException(e.getMessage());
        }
    }

    private void createDefaultCategory(String uuid) throws ActionException{
        try {
            InputStream inputStream = getClass().getResourceAsStream(DEFAULT_CATEGORY);
            String jsonString = IOHelper.readString(inputStream);
            OMElement request = requestBuilder.buildLayersInsert(uuid, jsonString);
            String response = GeoServerHelper.sendRequest(request);
            long[] insertedIds = responseBuilder.getInsertedIds(response);
            log.info("Created default category for user:", uuid,
                    "with layer id:", Arrays.toString(insertedIds));
        }
        catch (Exception e) {
            throw new ActionException(e.getMessage());
        }
    }

}