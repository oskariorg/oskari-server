package fi.nls.oskari.control.data;


import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.*;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.myplaces.MyPlacesService;
import fi.nls.oskari.myplaces.MyPlacesServiceMybatisImpl;
import fi.nls.oskari.util.*;
import org.json.JSONArray;
import org.json.JSONObject;

@OskariActionRoute("MyPlacesLayer")
public class MyPlacesLayersHandler extends RestActionHandler {

    private final static Logger log = LogFactory.getLogger(MyPlacesLayersHandler.class);

    private MyPlacesService service = new MyPlacesServiceMybatisImpl();

    private enum ModifyOperationType { INSERT, UPDATE, DELETE }

    public void handleGet(ActionParameters params) throws ActionException {
        //checkCredentials(params);
        GeoServerRequestBuilder requestBuilder = new GeoServerRequestBuilder();
        GeoServerResponseBuilder responseBuilder = new GeoServerResponseBuilder();
        try {
            ResponseHelper.writeResponse(params, responseBuilder.buildLayersGet(
                    GeoServerHelper.sendRequest(requestBuilder.buildLayersGet("fdsa-fdsa-fdsa-fdsa-fdsa"))));//;params.getUser().getUuid()))));
        }
        catch (Exception e) {
            throw new ActionException(e.getMessage());
        }
    }

    public void handlePut(ActionParameters params) throws ActionException {
        handleModifyRequest(params, MyPlacesFeaturesHandler.ModifyOperationType.INSERT);
    }

    public void handlePost(ActionParameters params) throws ActionException {
        handleModifyRequest(params, MyPlacesFeaturesHandler.ModifyOperationType.UPDATE);
    }

    public void handleDelete(ActionParameters params) throws ActionException {
        handleModifyRequest(params, MyPlacesFeaturesHandler.ModifyOperationType.DELETE);
    }

    private void handleModifyRequest(ActionParameters params, MyPlacesFeaturesHandler.ModifyOperationType operation) throws ActionException {
        //checkCredentials(params);

        String jsonString = readPayload(params);
        /*JSONArray json = null;
        Integer categoryId = null;
        try { //TODO: Loop through array and get category_name
            json = new JSONObject(jsonString).getJSONArray("categories");
            categoryId = json.getInt("category_name");
        }
        catch (Exception e) {
            throw new ActionParamsException("Invalid input, expected JSON");
        }
        if (!service.canModifyCategory(params.getUser(), categoryId)) {
            throw new ActionDeniedException("Not allowed");
        }*/
        GeoServerRequestBuilder requestBuilder = new GeoServerRequestBuilder();
        GeoServerResponseBuilder responseBuilder = new GeoServerResponseBuilder();
        try {
            switch (operation) {
                case INSERT:
                    ResponseHelper.writeResponse(params, responseBuilder.buildLayersInsert(
                            GeoServerHelper.sendRequest(requestBuilder.buildLayersInsert(jsonString))));
                    break;
                case UPDATE:
                    ResponseHelper.writeResponse(params, responseBuilder.buildLayersUpdate(
                            GeoServerHelper.sendRequest(requestBuilder.buildLayersUpdate(jsonString))));
                    break;
                case DELETE:
                    ResponseHelper.writeResponse(params, responseBuilder.buildLayersDelete(
                            GeoServerHelper.sendRequest(requestBuilder.buildLayersDelete(jsonString))));
                    break;
            }
        }
        catch (Exception e) {
            throw new ActionException(e.getMessage());
        }
    }

    private void checkCredentials(ActionParameters params) throws ActionException {
        if(params.getUser().isGuest()) {
            throw new ActionDeniedException("Session expired");
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