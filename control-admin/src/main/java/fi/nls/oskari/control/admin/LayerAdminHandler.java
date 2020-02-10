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
import fi.nls.oskari.util.GetLayerKeywords;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import org.json.JSONException;
import org.oskari.maplayer.admin.LayerAdminJSONHelper;
import org.oskari.admin.LayerCapabilitiesHelper;
import org.oskari.admin.MapLayerGroupsHelper;
import org.oskari.admin.MapLayerPermissionsHelper;
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
        Result result;
        if (layer.getId() != null && layer.getId() > 0) {
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

            // TODO: this will be made unnecessary when transport is cleaned out
            if (OskariLayer.TYPE_WFS.equals(ml.getType())) {
                // needed only for cleaning layer from portti_wfs_layer when wfs layer is deleted
                ServiceFactory.getWfsLayerService().delete(id);
            }

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
        final int layerId = layer.getId();
        OskariLayer ml = getMapLayer(params.getUser(), layer.getId());
        // also checks that user has permission to update

        mergeInputToOskariLayer(ml, layer);

        mapLayerService.update(ml);

        AuditLog.user(params.getClientIp(), params.getUser())
                .withParam("id", ml.getId())
                .withParam("uiName", ml.getName(PropertyUtil.getDefaultLanguage()))
                .withParam("url", ml.getUrl())
                .withParam("name", ml.getName())
                .updated(AuditLog.ResourceType.MAPLAYER);

        MapLayerPermissionsHelper.removePermissions(ml.getId());

        Result result = new Result();
        result.id = layerId;

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
        final OskariLayer ml = LayerAdminJSONHelper.fromJSON(layer);
        updateCapabilities(ml);

        final int layerId = mapLayerService.insert(ml);
        ml.setId(layerId);


        if (ml.isCollection()) {
            // update the name with the id for permission mapping
            ml.setName(layerId + "_group");
            mapLayerService.update(ml);
        }

        AuditLog.user(params.getClientIp(), params.getUser())
                .withParam("id", ml.getId())
                .withParam("uiName", ml.getName(PropertyUtil.getDefaultLanguage()))
                .withParam("url", ml.getUrl())
                .withParam("name", ml.getName())
                .added(AuditLog.ResourceType.MAPLAYER);

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
        ml.setName(layer.getName());
        // TODO: should we rather modify OskariMaplayer setters so they won't accept null value over existing value?
        ml.setVersion(getOrDefaultStr(layer.getVersion(), ml.getVersion()));

        Map<String, Map<String, String>> locale = layer.getLocale();
        for (String lang : locale.keySet()) {
            Map<String, String> langLocale = locale.getOrDefault(lang, Collections.emptyMap());
            // TODO: check that name is given
            ml.setName(lang, langLocale.get(KEY_LOCALIZED_NAME));  // mandatory
            ml.setTitle(lang, langLocale.get(KEY_LOCALIZED_TITLE));
        }

        // Add dataprovider
        DataProvider provider = dataProviderService.find(layer.getDataprovider_id());
        // dataProviders is Set so is safety to use add also to update layer
        ml.addDataprovider(provider);

        if (ml.isCollection()) {
            // url is needed for permission mapping, name is updated after we get the layer id
            ml.setUrl(ml.getType());
            // the rest is not relevant for collection layers
            return;
        }

        ml.setUrl(layer.getUrl());

        ml.setSrs_name(getOrDefaultStr(layer.getSrs(), PropertyUtil.get("oskari.native.srs", "EPSG:4326")));

        ml.setUpdated(new Date(System.currentTimeMillis()));
        /*
        ml.setBaseMap(layer.optBoolean(KEY_IS_BASE, ml.isBaseMap()));
        ml.setOpacity(layer.optInt(KEY_OPACITY, ml.getOpacity()));
        ml.setStyle(layer.optString(KEY_STYLE, ml.getStyle()));
        ml.setMinScale(layer.optDouble(KEY_MIN_SCALE, ml.getMinScale()));
        ml.setMaxScale(layer.optDouble(KEY_MAX_SCALE, ml.getMaxScale()));
        ml.setLegendImage(layer.optString(KEY_LEGEND_IMAGE, ml.getLegendImage()));
        ml.setMetadataId(layer.optString(KEY_METADATA_ID, ml.getMetadataId()));
        ml.setCapabilitiesUpdateRateSec(layer.optInt(KEY_CAPABILITIES_UPDATE_RATE, ml.getCapabilitiesUpdateRateSec()));
        ml.setRealtime(layer.optBoolean(KEY_REALTIME, ml.getRealtime()));
        ml.setRefreshRate(layer.optInt(KEY_REFRESH_RATE, ml.getRefreshRate()));

        if (layer.has(KEY_ATTRIBUTES)) ml.setAttributes(layer.getJSONObject(KEY_ATTRIBUTES));
        if (layer.has(KEY_PARAMS)) ml.setParams(layer.getJSONObject(KEY_PARAMS));
        if (layer.has(KEY_OPTIONS)) ml.setOptions(layer.getJSONObject(KEY_OPTIONS));
        if (layer.has(KEY_PASSWORD)) ml.setPassword(layer.getString(KEY_PASSWORD));
        if (layer.has(KEY_USERNAME)) ml.setUsername(layer.getString(KEY_USERNAME));
        if (layer.has(KEY_CAPABILITIES)) ml.setCapabilities(layer.getJSONObject(KEY_CAPABILITIES));

        ml.setGfiContent(layer.optString(KEY_GFI_CONTENT));
        */
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
