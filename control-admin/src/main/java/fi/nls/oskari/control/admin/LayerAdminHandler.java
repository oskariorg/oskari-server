package fi.nls.oskari.control.admin;

import fi.mml.map.mapwindow.service.db.OskariMapLayerGroupService;
import fi.mml.map.mapwindow.util.OskariLayerWorker;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.cache.JedisManager;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.DataProvider;
import fi.nls.oskari.domain.map.MaplayerGroup;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.wfs.WFSLayerConfiguration;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.DataProviderService;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.group.link.OskariLayerGroupLink;
import fi.nls.oskari.map.layer.group.link.OskariLayerGroupLinkService;
import fi.nls.oskari.util.*;
import fi.nls.oskari.wfs.WFSLayerConfigurationService;
import fi.nls.oskari.wfs.WFSLayerConfigurationServiceIbatisImpl;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.oskari.admin.LayerAdminJSONHelper;
import org.oskari.admin.LayerCapabilitiesHelper;
import org.oskari.admin.model.MapLayerAdminOutput;
import org.oskari.log.AuditLog;
import org.oskari.permissions.model.Permission;
import org.oskari.permissions.model.PermissionType;
import org.oskari.permissions.model.Resource;
import org.oskari.permissions.model.ResourceType;
import org.oskari.service.util.ServiceFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@OskariActionRoute("LayerAdmin")
public class LayerAdminHandler extends AbstractLayerAdminHandler {
    private static final String PARAM_LAYER_ID = "id";
    private static final String PARAM_CURRENT_SRS = "srs";
    private static final String KEY_LAYER_FOR_ADMIN = "layer";
    private static final String KEY_LAYER_FOR_LIST = "layerForList";
    // Common
    private static final String KEY_LAYER_ID = "id";
    private static final String KEY_LAYER_NAME = "name";
    private static final String KEY_LAYER_URL = "url";
    private static final String KEY_LAYER_SRS = "srs_name";
    private static final String KEY_GROUPS = "groups";
    private static final String KEY_INSPIRE_THEME = "inspiretheme";
    private static final String KEY_PROVIDER_NAME = "organization";
    private static final String KEY_PROVIDER_ID = "organization_id";
    private static final String KEY_PERMISSIONS = "permissions";
    private static final String KEY_LAYER_TYPE = "type";
    private static final String KEY_PARENT_ID = "parentid";
    private static final String KEY_LOCALIZED_NAME = "name";
    private static final String KEY_LOCALIZED_TITLE = "subtitle";
    private static final String KEY_LOCALE = "locale";
    private static final String KEY_VERSION = "version";
    private static final String KEY_IS_BASE = "is_base";
    private static final String KEY_OPACITY = "opacity";
    private static final String KEY_STYLE = "style";
    private static final String KEY_MIN_SCALE = "minscale";
    private static final String KEY_MAX_SCALE = "maxscale";
    private static final String KEY_METADATA_ID = "metadataid";
    private static final String KEY_CAPABILITIES_UPDATE_RATE = "capabilities_update_rate";
    private static final String KEY_ATTRIBUTES = "attributes";
    private static final String KEY_PARAMS = "params";
    private static final String KEY_OPTIONS = "options";
    private static final String KEY_REALTIME = "realtime";
    private static final String KEY_REFRESH_RATE = "refresh_rate";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_CAPABILITIES = "capabilities";
    // WMS / WMTS
    private static final String KEY_LEGEND_IMAGE = "legend_image";
    private static final String KEY_GFI_CONTENT = "gfi_content";
    private static final String KEY_GFI_XSLT = "gfi_xslt";
    private static final String KEY_GFI_TYPE = "gfi_type";
    // Response from service
    private static final String KEY_RESPONSE_WARN = "warn";
    private static final String KEY_UPDATE_CAPA_FAIL = "updateCapabilitiesFail";
    private static final String KEY_PERMISSIONS_FAIL = "insertPermissionsFail";
    private static final String KEY_KEYWORDS_FAIL = "insertKeywordsFail";
    private static final String ERROR_NO_LAYER_WITH_ID = "layer_not_found";
    private static final String ERROR_MANDATORY_FIELD_MISSING = "mandatory_field_missing";
    private static final String ERROR_INVALID_FIELD_VALUE = "invalid_field_value";
    private static final Logger LOG = LogFactory.getLogger(LayerAdminHandler.class);
    private OskariLayerService mapLayerService = ServiceFactory.getMapLayerService();
    private DataProviderService dataProviderService = ServiceFactory.getDataProviderService();
    private OskariMapLayerGroupService groupService = ServiceFactory.getOskariMapLayerGroupService();
    private OskariLayerGroupLinkService layerGroupLinkService = ServiceFactory.getOskariLayerGroupLinkService();

    // needed only for cleaning layer from portti_wfs_layer when wfs layer is deleted
    private WFSLayerConfigurationService wfsLayerService = new WFSLayerConfigurationServiceIbatisImpl();

    /**
     * Get layer for editing or list available layers for adding new layer
     *
     * @param params
     * @throws ActionException
     */
    @Override
    public void handleGet(ActionParameters params) throws ActionException {
        // If params has layerId then requested layer for editing
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
        JSONObject layer = params.getPayLoadJSON();
        Result result;
        try {
            if (layer.optInt(KEY_LAYER_ID) > 0) {
                result = updateLayer(params, layer);
            } else {
                result = insertLayer(params, layer);
            }
        } catch (JSONException e) {
            throw new ActionParamsException("Mandatory field missing", ERROR_MANDATORY_FIELD_MISSING, e);
        }

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

    private void writeResponse(ActionParameters params, MapLayerAdminOutput output) {
        params.getResponse().setCharacterEncoding("UTF-8");
        params.getResponse().setContentType("application/json;charset=UTF-8");
        ResponseHelper.writeResponse(params, LayerAdminJSONHelper.writeJSON(output));
    }

    @Override
    public void handleDelete(ActionParameters params) throws ActionException {
        final int id = params.getRequiredParamInt(PARAM_LAYER_ID);
        final OskariLayer ml = getMapLayer(params.getUser(), id);
        final String type = ml.getType();
        try {
            mapLayerService.delete(id);
            if (OskariLayer.TYPE_WFS.equals(type)) {
                wfsLayerService.delete(id);
                JedisManager.delAll(WFSLayerConfiguration.KEY + id);
                JedisManager.delAll(WFSLayerConfiguration.IMAGE_KEY + id);
            }
            writeAuditLogDelete(params, ml);
        } catch (Exception e) {
            throw new ActionException("Couldn't delete map layer - id:" + id, e);
        }
    }

    private void requireAdd(User user) throws ActionDeniedException {
        if (!userHasAddPermission(user)) {
            throw new ActionDeniedException("User doesn't have add layer permission");
        }
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
        // Add maplayer groups
        Set<Integer> groupLinks = layerGroupLinkService.findByLayerId(ml.getId())
                .stream()
                .map(gl -> gl.getGroupId())
                .collect(Collectors.toSet());
        layer.setGroup_ids(groupLinks);
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

    private void writeAuditLogUpdate(ActionParameters params, OskariLayer ml) {
        if (ml == null) return;
        AuditLog.user(params.getClientIp(), params.getUser())
                .withParam("id", ml.getId())
                .withParam("uiName", ml.getName(PropertyUtil.getDefaultLanguage()))
                .withParam("url", ml.getUrl())
                .withParam("name", ml.getName())
                .updated(AuditLog.ResourceType.MAPLAYER);
    }

    private void writeAuditLogInsert(ActionParameters params, OskariLayer ml) {
        if (ml == null) return;
        AuditLog.user(params.getClientIp(), params.getUser())
                .withParam("id", ml.getId())
                .withParam("uiName", ml.getName(PropertyUtil.getDefaultLanguage()))
                .withParam("url", ml.getUrl())
                .withParam("name", ml.getName())
                .added(AuditLog.ResourceType.MAPLAYER);
    }

    private void writeAuditLogDelete(ActionParameters params, OskariLayer ml) {
        if (ml == null) return;
        AuditLog.user(params.getClientIp(), params.getUser())
                .withParam("id", ml.getId())
                .withParam("uiName", ml.getName(PropertyUtil.getDefaultLanguage()))
                .withParam("url", ml.getUrl())
                .withParam("name", ml.getName())
                .deleted(AuditLog.ResourceType.MAPLAYER);
    }

    /**
     * @param layer
     * @return
     * @throws ActionException
     * @throws JSONException   if mandatory field is missing or invalid type
     */
    private Result updateLayer(final ActionParameters params, final JSONObject layer) throws ActionException, JSONException {
        final int layerId = layer.getInt(KEY_LAYER_ID);
        Result result = new Result();
        result.id = layerId;
        User user = params.getUser();
        OskariLayer ml = getMapLayer(user, layerId);

        handleRequestToMapLayer(ml, layer);
        ml.setUpdated(new Date(System.currentTimeMillis()));

        // delete old layer groups before adding new ones
        layerGroupLinkService.deleteLinksByLayerId(layerId);
        addMapLayerGroupds(layerId, layer);

        mapLayerService.update(ml);
        writeAuditLogUpdate(params, ml);

        removePermissions(user, layerId);
        result.permissions = addPermissionsForRoles(user, layerId, layer);

        // TODO is this needed
        // Remove old redis data of WFSLayer_xx
        JedisManager.delAll(WFSLayerConfiguration.KEY + layerId);
        JedisManager.delAll(WFSLayerConfiguration.IMAGE_KEY + layerId);

        return result;
    }

    /**
     * @param layer
     * @return
     * @throws ActionException
     * @throws JSONException   if mandatory field is missing or invalid type
     */
    private Result insertLayer(final ActionParameters params, final JSONObject layer) throws ActionException, JSONException {
        Result result = new Result();
        final User user = params.getUser();
        requireAdd(user);
        final OskariLayer ml = new OskariLayer();
        ml.setCreated(new Date(System.currentTimeMillis()));
        ml.setType(layer.getString(KEY_LAYER_TYPE));
        ml.setParentId(layer.optInt(KEY_PARENT_ID, ml.getParentId()));
        handleRequestToMapLayer(ml, layer);


        final int layerId = mapLayerService.insert(ml);
        result.id = layerId;
        ml.setId(layerId);
        writeAuditLogInsert(params, ml);
        addMapLayerGroupds(layerId, layer);

        if (ml.isCollection()) {
            // update the name with the id for permission mapping
            ml.setName(layerId + "_group");
            mapLayerService.update(ml);
        }
        result.permissions = addPermissionsForRoles(user, layerId, layer);
        // insert keywords
        // TODO: only this requires oskari-control-base dependency
        GetLayerKeywords glk = new GetLayerKeywords();
        try {
            glk.updateLayerKeywords(layerId, ml.getMetadataId());
        } catch (Exception e) {
            LOG.warn("Failed to update keywords for layer:", layerId);
            result.keywords = false;
        }

        return result;
    }

    /**
     * Handles common request for insert and update
     *
     * @param ml
     * @param layer
     * @throws ActionException
     * @throws JSONException   if mandatory field is missing or invalid type
     */
    private void handleRequestToMapLayer(OskariLayer ml, final JSONObject layer) throws ActionException, JSONException {
        ml.setName(layer.getString(KEY_LAYER_NAME));
        ml.setVersion(layer.optString(KEY_VERSION, ml.getVersion()));

        JSONObject locale = layer.getJSONObject(KEY_LOCALE);
        Iterator langKeys = locale.keys();
        while (langKeys.hasNext()) {
            String key = (String) langKeys.next();
            JSONObject lang = locale.getJSONObject(key);
            ml.setName(key, lang.getString(KEY_LOCALIZED_NAME)); // mandatory
            String title = lang.optString(KEY_LOCALIZED_TITLE);
            if (!title.isEmpty()) {
                ml.setTitle(key, title);
            }
        }
        // TODO: or without validation we could set:
        //ml.setLocale(layer.getJSONObject(KEY_LOCALE));

        // Add dataprovider
        DataProvider provider = dataProviderService.find(layer.optInt(KEY_PROVIDER_ID));
        if (provider == null) {
            provider = dataProviderService.findByName(layer.optString(KEY_PROVIDER_NAME));
        }
        // dataProviders is Set so is safety to use add also to update layer
        ml.addDataprovider(provider);

        if (ml.isCollection()) {
            // ulr is needed for permission mapping, name is updated after we get the layer id
            ml.setUrl(ml.getType());
            // the rest is not relevant for collection layers
            return;
        }

        ml.setUrl(layer.getString(KEY_LAYER_URL));
        validateUrl(ml.getSimplifiedUrl(true));

        ml.setSrs_name(layer.optString(KEY_LAYER_SRS, PropertyUtil.get("oskari.native.srs", "EPSG:4326")));
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

        final String gfiContent = layer.optString(KEY_GFI_CONTENT);
        if (gfiContent != null) {
            // Clean GFI content
            final String[] tags = PropertyUtil.getCommaSeparatedList("gficontent.whitelist");
            HashMap<String, String[]> attributes = new HashMap<String, String[]>();
            HashMap<String[], String[]> protocols = new HashMap<String[], String[]>();
            String[] allAttributes = PropertyUtil.getCommaSeparatedList("gficontent.whitelist.attr");
            if (allAttributes.length > 0) {
                attributes.put(":all", allAttributes);
            }
            List<String> attrProps = PropertyUtil.getPropertyNamesStartingWith("gficontent.whitelist.attr.");
            for (String attrProp : attrProps) {
                String[] parts = attrProp.split("\\.");
                if (parts[parts.length - 2].equals("protocol")) {
                    protocols.put(new String[]{parts[parts.length - 3], parts[parts.length - 1]}, PropertyUtil.getCommaSeparatedList(attrProp));
                } else {
                    attributes.put(parts[parts.length - 1], PropertyUtil.getCommaSeparatedList(attrProp));
                }
            }
            ml.setGfiContent(RequestHelper.cleanHTMLString(gfiContent, tags, attributes, protocols));
        }
        if (OskariLayer.TYPE_WMS.equals(ml.getType())) {
            handleWMSSpecific(ml, layer);
        }
    }

    private void handleWMSSpecific(OskariLayer ml, JSONObject layer) {
        // Do NOT modify the 'xslt' parameter
        final String xslt = layer.optString(KEY_GFI_XSLT);
        if (xslt != null) {
            // TODO: some validation of XSLT data
            ml.setGfiXslt(xslt);
        }
        ml.setGfiType(layer.optString(KEY_GFI_TYPE, ml.getGfiType()));
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

    private String validateUrl(final String url) throws ActionParamsException {
        // TODO remove query part with ? check or with URL object
        String baseUrl = url.contains("?") ? url.substring(0, url.indexOf("?")) : url;
        baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        try {
            // check that it's a valid url by creating an URL object...
            URL u = new URL(url);
            String host = u.getProtocol() + "://" + u.getHost();
            String path = u.getPath();
            if (path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }
            //return u.getPort() != -1 ? host + ":" + u.getPort() + path : host +  path;
        } catch (MalformedURLException e) {
            throw new ActionParamsException("Invalid url: " + url, ERROR_INVALID_FIELD_VALUE);
        }
        return baseUrl;
    }

    private void addMapLayerGroupds(final int layerId, final JSONObject layer) throws ActionException {
        if (!layer.has(KEY_GROUPS) && !layer.has(KEY_INSPIRE_THEME))
            throw new ActionParamsException("Groups missing", ERROR_MANDATORY_FIELD_MISSING);
        Set<Integer> groupIds = new HashSet<>();
        groupIds.addAll(JSONHelper.getArrayAsList(layer.optJSONArray(KEY_GROUPS)));
        String inspiretheme = layer.optString(KEY_INSPIRE_THEME);
        if (!inspiretheme.isEmpty()) {
            MaplayerGroup group = groupService.findByName(inspiretheme);
            if (group == null) {
                LOG.warn("Didn't find match for theme:", inspiretheme);
            } else {
                groupIds.add(group.getId());
            }
        }
        List<OskariLayerGroupLink> links = groupIds
                .stream()
                .filter(id -> id > 0)
                .map(id -> new OskariLayerGroupLink(layerId, id))
                .collect(Collectors.toList());
        if (links.isEmpty()) throw new ActionParamsException("Couldn't find any valid maplayer group");
        layerGroupLinkService.insertAll(links);
    }

    private boolean addPermissionsForRoles(final User user, final int layerId, final JSONObject layer) {
        if (!user.isAdmin()) return false;
        Map<String, JSONArray> permissions = JSONHelper.getObjectAsMap(JSONHelper.getJSONObject(layer, KEY_PERMISSIONS));
        if (permissions.isEmpty()) return false;
        Resource res = new Resource();
        res.setType(ResourceType.maplayer);
        res.setMapping(Integer.toString(layerId));
        // insert permissions
        permissions
                .entrySet()
                .forEach(e -> setPermission(res, e.getKey(), e.getValue()));
        getPermissionsService().saveResource(res);
        return true;
    }

    private void setPermission(Resource res, String roleName, JSONArray permissions) {
        try {
            int id = getRoleId(roleName);
            for (int i = 0; i < permissions.length(); i++) {
                PermissionType type = PermissionType.valueOf(permissions.getString(i));
                LOG.debug("Adding permissions:", type, "for role:", roleName);
                Permission permission = new Permission();
                permission.setRoleId(id);
                permission.setType(type);
                res.addPermission(permission);
            }
        } catch (Exception e) {
            LOG.warn("Failed to add permissions for role:", roleName, e.getMessage());
        }
    }

    private void removePermissions(final User user, final int layerId) {
        if (!user.isAdmin()) return;
        Optional<Resource> res = getPermissionsService().findResource(ResourceType.maplayer, Integer.toString(layerId));
        if (res.isPresent()) {
            getPermissionsService().deleteResource(res.get());
        }
    }

    private class Result {
        int id;
        boolean permissions = true;
        boolean keywords = true;
    }


}
