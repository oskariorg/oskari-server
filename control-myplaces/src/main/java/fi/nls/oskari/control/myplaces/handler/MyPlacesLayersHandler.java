package fi.nls.oskari.control.myplaces.handler;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import fi.nls.oskari.domain.map.wfs.WFSLayerOptions;
import org.oskari.log.AuditLog;
import org.oskari.permissions.model.PermissionType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fi.mml.map.mapwindow.util.OskariLayerWorker;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.MyPlaceCategory;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.myplaces.MyPlaceCategoryHelper;
import fi.nls.oskari.myplaces.MyPlacesService;
import fi.nls.oskari.myplaces.service.MyPlacesLayersService;
import fi.nls.oskari.myplaces.service.wfst.MyPlacesLayersServiceWFST;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.ResponseHelper;

@OskariActionRoute("MyPlacesLayers")
public class MyPlacesLayersHandler extends RestActionHandler {

    private final static Logger LOG = LogFactory.getLogger(MyPlacesLayersHandler.class);

    private static final String PARAM_LAYERS = "layers";

    private static final String KEY_MYPLACES = "layers";
    private static final String KEY_PERMISSIONS = "permissions";
    private static final String KEY_DELETED = "deleted";

    private MyPlacesService service;
    private MyPlacesLayersService layerService;

    @Override
    public void init() {
        service = OskariComponentManager.getComponentOfType(MyPlacesService.class);
        layerService = new MyPlacesLayersServiceWFST();
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
            if (categories.isEmpty()) {
                // If user has no categories insert a new default category
                MyPlaceCategory category = insertDefaultCategory(uuid);
                categories = Collections.singletonList(category);
            }
        } catch (ServiceException e) {
            LOG.warn(e);
            throw new ActionException("Failed to get myplaces layers");
        }

        JSONObject response = toLayerJSON(categories);
        ResponseHelper.writeResponse(params, 200, response);
    }

    @Override
    public void handlePost(ActionParameters params) throws ActionException {
        String uuid = params.getUser().getUuid();
        List<MyPlaceCategory> categories = readCategories(params, false);
        for (MyPlaceCategory category : categories) {
            category.setUuid(uuid);
        }

        try {
            int inserted = layerService.insert(categories);
            LOG.info("Inserted", inserted, "/", categories.size());
        } catch (ServiceException e) {
            LOG.warn(e);
            throw new ActionException("Failed to insert layers");
        }

        for (MyPlaceCategory layer : categories) {
            AuditLog.user(params.getClientIp(), params.getUser())
                    .withParam("id", layer.getId())
                    .withParam("name", layer.getCategory_name())
                    .added(AuditLog.ResourceType.MYPLACES_LAYER);
        }

        JSONObject response = toLayerJSON(categories);
        ResponseHelper.writeResponse(params, 200, response);
    }

    @Override
    public void handlePut(ActionParameters params) throws ActionException {
        final User user = params.getUser();
        final String uuid = user.getUuid();
        final List<MyPlaceCategory> categories = readCategories(params, true);
        for (MyPlaceCategory category : categories) {
            category.setUuid(uuid);
        }
        checkUserCanModifyCategories(user, categories);
        long[] ids = categories.stream().mapToLong(MyPlaceCategory::getId).toArray();
        try {
            LOG.debug("Updating Categories:", ids);
            int updated = layerService.update(categories);
            LOG.info("Updated", updated, "/", categories.size());
        } catch (ServiceException e) {
            LOG.warn(e);
            throw new ActionException("Failed to update layers");
        }
        for (MyPlaceCategory layer : categories) {
            AuditLog.user(params.getClientIp(), params.getUser())
                    .withParam("id", layer.getId())
                    .withParam("name", layer.getCategory_name())
                    .updated(AuditLog.ResourceType.MYPLACES_LAYER);
        }

        JSONObject response = toLayerJSON(categories);
        ResponseHelper.writeResponse(params, 200, response);
    }

    @Override
    public void handleDelete(ActionParameters params) throws ActionException {
        final User user = params.getUser();
        final String layerIds = params.getRequiredParam(PARAM_LAYERS);
        final long[] ids = Arrays.stream(layerIds.split(","))
                .mapToLong(Long::parseLong)
                .toArray();
        for (long id : ids) {
            if (!service.canModifyCategory(user, id)) {
                throw new ActionDeniedException("Tried to delete category: " + id);
            }
        }

        final int deleted;
        try {
            deleted = layerService.delete(ids);
        } catch (ServiceException e) {
            LOG.warn(e);
            throw new ActionException("Failed to delete layers");
        }

        AuditLog.user(params.getClientIp(), params.getUser())
                .withParam("id", layerIds)
                .deleted(AuditLog.ResourceType.MYPLACES_LAYER);

        JSONObject response = new JSONObject();
        JSONHelper.putValue(response, KEY_DELETED, deleted);
        ResponseHelper.writeResponse(params, response);
    }

    private MyPlaceCategory insertDefaultCategory(String uuid)
            throws ActionException {
        try {
            MyPlaceCategory category = createDefaultCategory();
            category.setUuid(uuid);
            layerService.insert(Arrays.asList(category));
            return category;
        } catch (ServiceException e) {
            LOG.warn(e);
            throw new ActionException("Failed to insert default category");
        }
    }

    private MyPlaceCategory createDefaultCategory() {
        MyPlaceCategory category = new MyPlaceCategory();
        category.setCategory_name("");
        category.setDefault(true);
        category.getWFSLayerOptions().setDefaultFeatureStyle(WFSLayerOptions.getDefaultOskariStyle());
        return category;
    }

    private List<MyPlaceCategory> readCategories(ActionParameters params,
            boolean checkId) throws ActionException {
        try {
            final String payload;
            try (InputStream in = params.getRequest().getInputStream()) {
                payload = IOHelper.readString(in);
            } catch (IOException e) {
                throw new ActionException("IOException occured");
            }
            return MyPlaceCategoryHelper.parseFromGeoJSON(payload, checkId);
        } catch (JSONException e) {
            throw new ActionParamsException("Invalid input", e);
        } catch (IOException e) {
            throw new ActionParamsException("IOException occured", e);
        }
    }

    private void checkUserCanModifyCategories(User user,
            List<MyPlaceCategory> categories) throws ActionDeniedException {
        for (MyPlaceCategory category : categories) {
            if (!service.canModifyCategory(user, category.getId())) {
                throw new ActionDeniedException(
                        "Tried to modify category: " + category.getId());
            }
        }
    }

    private JSONObject toLayerJSON(List<MyPlaceCategory> categories) {
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
