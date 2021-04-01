package fi.nls.oskari.control.myplaces.handler;

import java.util.Collections;
import java.util.List;

import fi.nls.oskari.control.*;
import fi.nls.oskari.domain.map.wfs.WFSLayerOptions;
import fi.nls.oskari.util.PropertyUtil;
import org.oskari.log.AuditLog;
import org.oskari.myplaces.service.mybatis.MyPlacesLayersServiceMybatisImpl;
import org.oskari.permissions.model.PermissionType;
import org.json.JSONArray;
import org.json.JSONObject;

import fi.mml.map.mapwindow.util.OskariLayerWorker;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.MyPlaceCategory;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.myplaces.MyPlacesService;
import fi.nls.oskari.myplaces.service.MyPlacesLayersService;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;

@OskariActionRoute("MyPlacesLayers")
public class MyPlacesLayersHandler extends RestActionHandler {

    private final static Logger LOG = LogFactory.getLogger(MyPlacesLayersHandler.class);

    private static final String PARAM_ID = "id";
    private static final String PARAM_NAME = "name";
    private static final String PARAM_STYLE = "style";

    private static final String KEY_MYPLACES = "layers";
    private static final String KEY_LAYER = "layer";
    private static final String KEY_PERMISSIONS = "permissions";
    private static final String KEY_SUCCESS = "success";

    private MyPlacesService service;
    private MyPlacesLayersService layerService;

    @Override
    public void init() {
        service = OskariComponentManager.getComponentOfType(MyPlacesService.class);
        layerService = new MyPlacesLayersServiceMybatisImpl();
    }

    @Override
    public void preProcess(ActionParameters params) throws ActionException {
        params.requireLoggedInUser();
    }

    @Override
    public void handleGet(ActionParameters params) throws ActionException {
        List<MyPlaceCategory> categories;
        try {
            String uuid = params.getUser().getUuid();
            categories = layerService.getByUserId(uuid);
            boolean hasDefault = categories.stream().filter(c -> c.isDefault()).findAny().isPresent();
            if (!hasDefault) {
                // If user has no default category insert a new category as default
                MyPlaceCategory category = insertDefaultCategory(uuid);
                categories.add(category);
            }
        } catch (ServiceException e) {
            LOG.warn(e);
            throw new ActionException("Failed to get myplaces layers");
        }

        JSONObject response = toLayersJSON(categories);
        ResponseHelper.writeResponse(params, 200, response);
    }

    @Override
    public void handlePost(ActionParameters params) throws ActionException {
        MyPlaceCategory category = new MyPlaceCategory();
        category.setUuid(params.getUser().getUuid());
        String name = params.getRequiredParam(PARAM_NAME);
        category.setName(name);
        category.setName(PropertyUtil.getDefaultLanguage(), name); // FIXME: category.setLocale(params.getRequiredParam(PARAM_LOCALE));
        category.getWFSLayerOptions()
                .setDefaultFeatureStyle(JSONHelper.createJSONObject(params.getHttpParam(PARAM_STYLE)));
        try {
            layerService.insert(Collections.singletonList(category));
            LOG.info("Inserted category:", category.getId());
        } catch (ServiceException e) {
            LOG.warn(e);
            throw new ActionException("Failed to insert layers");
        }

        AuditLog.user(params.getClientIp(), params.getUser())
            .withParam("id", category.getId())
            .withParam("name", category.getName())
            .added(AuditLog.ResourceType.MYPLACES_LAYER);

        JSONObject response = toLayerJSON(category);
        ResponseHelper.writeResponse(params, 200, response);
    }

    @Override
    public void handlePut(ActionParameters params) throws ActionException {
        final User user = params.getUser();
        final long id = params.getRequiredParamLong(PARAM_ID);
        if (!service.canModifyCategory(user, id)) {
            throw new ActionDeniedException(
                    "Tried to modify category: " + id);
        }
        try {
            final MyPlaceCategory category = layerService.getById(id)
                    .orElseThrow(() -> new ActionParamsException("Couldn't find myplaces layer, " + id));
            String name = params.getRequiredParam(PARAM_NAME);
            category.setName(name);
            category.setName(PropertyUtil.getDefaultLanguage(), name); // FIXME: category.setLocale(params.getRequiredParam(PARAM_LOCALE));
            category.getWFSLayerOptions()
                .setDefaultFeatureStyle(JSONHelper.createJSONObject(params.getHttpParam(PARAM_STYLE)));
            layerService.update(Collections.singletonList(category));
            LOG.info("Updated category:", id);
            AuditLog.user(params.getClientIp(), params.getUser())
                    .withParam("id", category.getId())
                    .withParam("name", category.getName())
                    .updated(AuditLog.ResourceType.MYPLACES_LAYER);
            JSONObject response = toLayerJSON(category);
            ResponseHelper.writeResponse(params, 200, response);
        } catch (ServiceException e) {
            LOG.warn(e);
            throw new ActionException("Failed to update layers");
        }
    }

    @Override
    public void handleDelete(ActionParameters params) throws ActionException {
        final User user = params.getUser();
        final long id =  params.getRequiredParamLong(PARAM_ID);
        if (!service.canModifyCategory(user, id)) {
            throw new ActionDeniedException("Tried to delete category: " + id);
        }
        try {
            layerService.delete(new long [] {id});
        } catch (ServiceException e) {
            LOG.warn(e);
            throw new ActionException("Failed to delete layers");
        }

        AuditLog.user(params.getClientIp(), params.getUser())
                .withParam("id", id)
                .deleted(AuditLog.ResourceType.MYPLACES_LAYER);

        JSONObject response = new JSONObject();
        JSONHelper.putValue(response, KEY_SUCCESS, true);
        ResponseHelper.writeResponse(params, response);
    }

    private MyPlaceCategory insertDefaultCategory(String uuid)
            throws ActionException {
        try {
            MyPlaceCategory category = createDefaultCategory();
            category.setUuid(uuid);
            layerService.insert(Collections.singletonList(category));
            return category;
        } catch (ServiceException e) {
            LOG.warn(e);
            throw new ActionException("Failed to insert default category");
        }
    }

    private MyPlaceCategory createDefaultCategory() {
        MyPlaceCategory category = new MyPlaceCategory();
        category.setName("");
        category.setDefault(true);
        category.getWFSLayerOptions().setDefaultFeatureStyle(WFSLayerOptions.getDefaultOskariStyle());
        return category;
    }

    private JSONObject toLayerJSON (MyPlaceCategory category) {
        JSONObject layerJSON = MyPlacesService.parseLayerToJSON(category, null);
        JSONHelper.putValue(layerJSON, KEY_PERMISSIONS, getPermissions());
        return JSONHelper.createJSONObject(KEY_LAYER, layerJSON);
    }

    private JSONObject toLayersJSON(List<MyPlaceCategory> categories) {
        JSONArray layers = new JSONArray();
        for (MyPlaceCategory category : categories) {
            JSONObject layerJSON = MyPlacesService.parseLayerToJSON(category, null);
            JSONHelper.putValue(layerJSON, KEY_PERMISSIONS, getPermissions());
            layers.put(layerJSON);
        }

        return JSONHelper.createJSONObject(KEY_MYPLACES, layers);
    }

    private JSONObject getPermissions() {
        JSONObject permissions = new JSONObject();
        JSONHelper.putValue(permissions, PermissionType.PUBLISH.getJsonKey(), OskariLayerWorker.PUBLICATION_PERMISSION_OK);
        JSONHelper.putValue(permissions, PermissionType.DOWNLOAD.getJsonKey(), OskariLayerWorker.DOWNLOAD_PERMISSION_OK);
        return permissions;
    }

}
