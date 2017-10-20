package fi.nls.oskari.control.data;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.*;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.myplaces.MyPlacesService;
import fi.nls.oskari.myplaces.MyPlacesServiceMybatisImpl;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.*;
import org.json.JSONObject;


@OskariActionRoute("MyPlacesFeture")
public class MyPlacesFeaturesHandler extends RestActionHandler {

    private final static Logger log = LogFactory.getLogger(MyPlacesFeaturesHandler.class);

    private MyPlacesService service = new MyPlacesServiceMybatisImpl();

    enum ModifyOperationType { INSERT, UPDATE, DELETE }

    public void handleGet(ActionParameters params) throws ActionException {
        checkCredentials(params);
        GeoServerRequestBuilder requestBuilder = new GeoServerRequestBuilder();
        GeoServerRequestBuilder responseBuilder = new GeoServerRequestBuilder();
        try {
            ResponseHelper.writeResponse(params, responseBuilder.buildFeaturesGet(
                    GeoServerHelper.sendRequest(requestBuilder.buildFeaturesGet(params.getUser().getUuid()))));
        }
        catch (Exception e) {
            throw new ActionException(e.getMessage());
        }
    }

    public void handlePut(ActionParameters params) throws ActionException {
        handleModifyRequest(params, ModifyOperationType.INSERT);
    }

    public void handlePost(ActionParameters params) throws ActionException {
        handleModifyRequest(params, ModifyOperationType.UPDATE);
    }

    public void handleDelete(ActionParameters params) throws ActionException {
        handleModifyRequest(params, ModifyOperationType.DELETE);
    }

    private void handleModifyRequest(ActionParameters params, ModifyOperationType operation) throws ActionException {
        checkCredentials(params);

        String jsonString = readPayload(params);
        JSONObject json = null;
        Integer categoryId = null;
        try {
            json = new JSONObject(jsonString);
            categoryId = json.getInt("categoryId");
        }
        catch (Exception e) {
            throw new ActionParamsException("Invalid input, expected JSON");
        }
        if (!service.canModifyCategory(params.getUser(), categoryId)) {
            throw new ActionDeniedException("Not allowed");
        }
        GeoServerRequestBuilder requestBuilder = new GeoServerRequestBuilder();
        GeoServerRequestBuilder responseBuilder = new GeoServerRequestBuilder();
        try {
            switch (operation) {
                case INSERT:
                    ResponseHelper.writeResponse(params, responseBuilder.buildFeaturesInsert(
                            GeoServerHelper.sendRequest(requestBuilder.buildFeaturesInsert(jsonString))));
                    break;
                case UPDATE:
                    ResponseHelper.writeResponse(params, responseBuilder.buildFeaturesUpdate(
                            GeoServerHelper.sendRequest(requestBuilder.buildFeaturesUpdate(jsonString))));
                    break;
                case DELETE:
                    ResponseHelper.writeResponse(params, responseBuilder.buildFeaturesDelete(
                            GeoServerHelper.sendRequest(requestBuilder.buildFeaturesDelete(jsonString))));
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