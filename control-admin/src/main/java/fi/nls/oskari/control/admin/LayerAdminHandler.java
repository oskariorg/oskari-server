package fi.nls.oskari.control.admin;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.DataProvider;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.DataProviderService;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.service.ServiceRuntimeException;
import fi.nls.oskari.util.GetLayerKeywords;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONException;
import org.json.JSONObject;
import org.oskari.maplayer.admin.LayerAdminJSONHelper;
import org.oskari.admin.LayerCapabilitiesHelper;
import org.oskari.admin.MapLayerGroupsHelper;
import org.oskari.admin.MapLayerPermissionsHelper;
import org.oskari.maplayer.admin.LayerValidator;
import org.oskari.maplayer.model.MapLayer;
import org.oskari.maplayer.model.MapLayerAdminOutput;
import org.oskari.log.AuditLog;
import org.oskari.service.util.ServiceFactory;

import java.util.*;

@OskariActionRoute("LayerAdmin")
public class LayerAdminHandler extends AbstractLayerAdminHandler {
    private static final Logger LOG = LogFactory.getLogger(LayerAdminHandler.class);

    private static final String PARAM_LAYER_ID = "id";
    private static final String KEY_LOCALIZED_NAME = "name";
    private static final String KEY_LOCALIZED_TITLE = "subtitle";
    // Response from service
    private static final String KEY_UPDATE_CAPA_FAIL = "updateCapabilitiesFail";
    private static final String KEY_PERMISSIONS_FAIL = "insertPermissionsFail";
    private static final String ERROR_NO_LAYER_WITH_ID = "layer_not_found";
    private OskariLayerService mapLayerService = ServiceFactory.getMapLayerService();
    private DataProviderService dataProviderService = ServiceFactory.getDataProviderService();

    /**
     * Get layer for edit (admin)
     *
     * @param params
     * @throws ActionException
     */
    @Override
    public void handleGet(ActionParameters params) throws ActionException {
        final int layerId = params.getRequiredParamInt(PARAM_LAYER_ID);
        OskariLayer ml = getMapLayer(params.getUser(), layerId);
        MapLayerAdminOutput output = getLayerForEdit(params.getUser(), ml);
        if (!updateCapabilities(ml)) {
            output.setWarn(KEY_UPDATE_CAPA_FAIL);
        }
        writeResponse(params, output);
    }

    @Override
    public void handlePost(ActionParameters params) throws ActionException {
        MapLayer layer = LayerAdminJSONHelper.readJSON(params.getPayLoad());
        boolean isExisting = layer.getId() != null && layer.getId() > 0;
        Result result;
        if (isExisting) {
            result = updateLayer(params, layer);
        } else {
            result = insertLayer(params, layer);
        }
        MapLayerPermissionsHelper.setLayerPermissions(result.id, layer.getRole_permissions());
        MapLayerGroupsHelper.setGroupsForLayer(result.id, layer.getGroup_ids());

        OskariLayer ml = mapLayerService.find(result.id);
        if (ml == null) {
            throw new ActionParamsException("Couldn't get the saved layer from DB - id:" + result.id, ERROR_NO_LAYER_WITH_ID);
        }

        AuditLog audit = AuditLog.user(params.getClientIp(), params.getUser())
                .withParam("id", ml.getId())
                .withParam("uiName", ml.getName(PropertyUtil.getDefaultLanguage()))
                .withParam("url", ml.getUrl())
                .withParam("name", ml.getName());

        if (isExisting) {
            audit.updated(AuditLog.ResourceType.MAPLAYER);
        } else {
            audit.added(AuditLog.ResourceType.MAPLAYER);
        }

        MapLayerAdminOutput output = getLayerForEdit(params.getUser(), ml);
        if (!result.permissions) {
            // NOTE! only tell if permissions failed, this probably needs some refactoring to be useful
            output.setWarn(KEY_PERMISSIONS_FAIL);
        }

        writeResponse(params, output);
    }

    @Override
    public void handleDelete(ActionParameters params) throws ActionException {
        final int id = params.getRequiredParamInt(PARAM_LAYER_ID);
        final OskariLayer ml = getMapLayer(params.getUser(), id);
        MapLayerAdminOutput output = getLayerForEdit(params.getUser(), ml);
        try {
            mapLayerService.delete(id);

            MapLayerPermissionsHelper.removePermissions(id);

            AuditLog.user(params.getClientIp(), params.getUser())
                    .withParam("id", ml.getId())
                    .withParam("uiName", ml.getName(PropertyUtil.getDefaultLanguage()))
                    .withParam("url", ml.getUrl())
                    .withParam("name", ml.getName())
                    .deleted(AuditLog.ResourceType.MAPLAYER);

            writeResponse(params, output);
        } catch (Exception e) {
            throw new ActionException("Couldn't delete map layer - id:" + id, e);
        }
    }

    private void writeResponse(ActionParameters params, MapLayerAdminOutput output) {
        params.getResponse().setCharacterEncoding("UTF-8");
        params.getResponse().setContentType("application/json;charset=UTF-8");
        ResponseHelper.writeResponse(params, LayerAdminJSONHelper.writeJSON(output));
    }

    private OskariLayer getMapLayer(User user, final int layerId) throws ActionParamsException, ActionDeniedException {
        final OskariLayer ml = mapLayerService.find(layerId);
        if (ml == null) {
            // layer wasn't found
            throw new ActionParamsException("Couldn't find maplayer - id: " + layerId, ERROR_NO_LAYER_WITH_ID);
        }

        if (!userHasEditPermission(user, ml)) {
            throw new ActionDeniedException("User doesn't have edit permission for layer: " + layerId);
        }
        return ml;
    }

    private MapLayerAdminOutput getLayerForEdit(User user, final OskariLayer ml) {
        MapLayerAdminOutput layer = LayerAdminJSONHelper.toJSON(ml);
        layer.setGroup_ids(MapLayerGroupsHelper.findGroupIdsForLayer(ml.getId()));
        try {
            Map<String, Set<String>> rolePermissions = new HashMap<>();
            getPermissionsGroupByRole(user, ml).entrySet()
                    .stream()
                    .forEach(e -> rolePermissions.put(e.getKey().getName(), e.getValue()));

            layer.setRole_permissions(rolePermissions);
        } catch (Exception e) {
            LOG.warn("Failed to get permission roles for layer:", ml.getId());
        }
        return layer;
    }

    /**
     * @param layer
     * @return
     * @throws ActionException
     * @throws JSONException   if mandatory field is missing or invalid type
     */
    private Result updateLayer(final ActionParameters params, final MapLayer layer) throws ActionException {
        // also checks that user has permission to update
        OskariLayer ml = getMapLayer(params.getUser(), layer.getId());

        Result result = new Result();
        result.id = ml.getId();

        mergeInputToOskariLayer(ml, layer);
        updateCapabilities(ml);

        mapLayerService.update(ml);

        // TODO: removes all permissions but non-admins don't send all permissions back
        // Should we remove just the permissions that we get from frontend?
        MapLayerPermissionsHelper.removePermissions(ml.getId());

        return result;
    }

    /**
     * @param layer
     * @return
     * @throws ActionException
     * @throws JSONException   if mandatory field is missing or invalid type
     */
    private Result insertLayer(final ActionParameters params, MapLayer layer) throws ActionException {
        if (!userHasAddPermission(params.getUser())) {
            throw new ActionDeniedException("User doesn't have add layer permission");
        }
        OskariLayer ml;
        try {
            ml = LayerAdminJSONHelper.fromJSON(layer);
        } catch (ServiceRuntimeException e) {
            // validation failed -> params/payload was faulty. Thrown exception tells reason but wrapping it in
            // ActionParamsException so its handled better with logging/user messaging etc
            throw new ActionParamsException(e.getMessage(), e);
        }
        updateCapabilities(ml);

        final int layerId = mapLayerService.insert(ml);
        ml.setId(layerId);


        if (ml.isCollection()) {
            // update the name with the id for permission mapping
            ml.setName(layerId + "_group");
            mapLayerService.update(ml);
        }

        Result result = new Result();
        result.id = layerId;

        // insert keywords
        try {
            // FIXME: Looks like GetLayerKeywords is tied to geonetwork.nls.fi...
            // TODO: make it generic or create abstraction/remove it
            GetLayerKeywords layerKeywords = new GetLayerKeywords();
            layerKeywords.updateLayerKeywords(layerId, ml.getMetadataId());
        } catch (Exception e) {
            LOG.warn("Failed to update keywords for layer:", layerId);
            result.keywords = false;
        }

        return result;
    }

    private String getOrDefaultStr(String input, String defaultValue) {
        if (input != null) {
            return input;
        }
        return defaultValue;
    }

    /**
     * Handles common request for insert and update
     *
     * @param ml
     * @param layer
     * @throws ActionException
     * @throws JSONException   if mandatory field is missing or invalid type
     */
    private void mergeInputToOskariLayer(OskariLayer ml, MapLayer layer) {
        ml.setUpdated(new Date(System.currentTimeMillis()));
        ml.setName(layer.getName());
        // TODO: should we rather modify OskariMaplayer setters so they won't accept null value over existing value?
        ml.setVersion(getOrDefaultStr(layer.getVersion(), ml.getVersion()));

        Map<String, Map<String, String>> locale = layer.getLocale();
        if (locale != null) {
            LayerValidator.validateLocale(locale);
            for (String lang : locale.keySet()) {
                Map<String, String> langLocale = locale.getOrDefault(lang, Collections.emptyMap());
                // at least name in default lang is required - validation done in LayerAdmin is given
                ml.setName(lang, langLocale.get(KEY_LOCALIZED_NAME));
                ml.setTitle(lang, langLocale.get(KEY_LOCALIZED_TITLE));
            }
        }

        if (layer.getDataprovider_id() != -1) {
            // Add dataprovider
            DataProvider provider = dataProviderService.find(layer.getDataprovider_id());
            // dataProviders is Set so is safety to use add also to update layer
            ml.setDataprovider(provider);
        }

        if (ml.isCollection()) {
            // just for consistency - no real meaning for these
            ml.setName(ml.getId() + "_group");
            ml.setUrl(ml.getType());
            // the rest is not relevant for collection layers
            return;
        }

        ml.setUrl(getOrDefaultStr(layer.getUrl(), ml.getUrl()));
        ml.setUsername(getOrDefaultStr(layer.getUsername(), ml.getUsername()));
        ml.setPassword(getOrDefaultStr(layer.getPassword(), ml.getPassword()));
        ml.setSrs_name(getOrDefaultStr(layer.getSrs(),
                getOrDefaultStr(ml.getSrs_name(),
                        PropertyUtil.get("oskari.native.srs", "EPSG:4326"))));

        ml.setStyle(getOrDefaultStr(layer.getStyle(), ml.getStyle()));
        ml.setLegendImage(getOrDefaultStr(layer.getLegend_image(), ml.getLegendImage()));
        ml.setMetadataId(getOrDefaultStr(layer.getMetadataid(), ml.getMetadataId()));
        if (layer.getAttributes() != null) {
            ml.setAttributes(new JSONObject(layer.getAttributes()));
        }
        if (layer.getParams() != null) {
            ml.setParams(new JSONObject(layer.getParams()));
        }
        if (layer.getOptions() != null) {
            ml.setOptions(new JSONObject(layer.getOptions()));
        }

        ml.setGfiContent(getOrDefaultStr(LayerValidator.cleanGFIContent(layer.getGfi_content()), ml.getGfiContent()));
        ml.setGfiXslt(getOrDefaultStr(layer.getGfi_xslt(), ml.getGfiXslt()));
        ml.setGfiType(getOrDefaultStr(layer.getGfi_type(), ml.getGfiType()));

        // TODO: make nullable so these are NOT reset to default if not given?
        ml.setOpacity(layer.getOpacity());
        ml.setMinScale(layer.getMinscale());
        ml.setMaxScale(layer.getMaxscale());
        ml.setCapabilitiesUpdateRateSec(layer.getCapabilities_update_rate_sec());
        ml.setRealtime(layer.isRealtime());
        ml.setRefreshRate(layer.getRefresh_rate());
    }

    private boolean updateCapabilities(OskariLayer ml) {
        try {
            LayerCapabilitiesHelper.updateCapabilities(ml);
            return true;
        } catch (Exception e) {
            LOG.error("Failed to set capabilities for layer:", ml, e.getMessage());
            return false;
        }
    }

    private class Result {
        int id;
        boolean permissions = true;
        boolean keywords = true;
    }


}
