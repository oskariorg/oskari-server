package fi.nls.oskari.control.layer;

import fi.mml.map.mapwindow.service.db.CapabilitiesCacheService;
import fi.mml.map.mapwindow.service.db.InspireThemeService;
import fi.mml.map.mapwindow.util.OskariLayerWorker;
import fi.mml.portti.domain.permissions.Permissions;
import fi.mml.portti.service.db.permissions.PermissionsService;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.*;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.CapabilitiesCache;
import fi.nls.oskari.domain.map.InspireTheme;
import fi.nls.oskari.domain.map.LayerGroup;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.LayerGroupService;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.util.*;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Enumeration;

/**
 * Admin insert/update of WMS map layer
 */
@OskariActionRoute("SaveLayer")
public class SaveLayerHandler extends ActionHandler {

    private OskariLayerService mapLayerService = ServiceFactory.getMapLayerService();
    private PermissionsService permissionsService = ServiceFactory.getPermissionsService();
    private LayerGroupService layerGroupService = ServiceFactory.getLayerGroupService();
    private InspireThemeService inspireThemeService = ServiceFactory.getInspireThemeService();
    private CapabilitiesCacheService capabilitiesService = ServiceFactory.getCapabilitiesCacheService();

    private static final Logger log = LogFactory.getLogger(SaveLayerHandler.class);
    private static final String PARM_LAYER_ID = "layer_id";

    private static final String LAYER_NAME_PREFIX = "name_";
    private static final String LAYER_TITLE_PREFIX = "title_";

    @Override
    public void handleAction(ActionParameters params) throws ActionException {

        final int layerId = saveLayer(params);
        final OskariLayer ml = mapLayerService.find(layerId);
        if(ml == null) {
            throw new ActionException("Couldn't get the saved layer from DB - id:" + layerId);
        }

        // update cache - do this before creating json!
        final boolean cacheUpdated = updateCache(ml, params.getHttpParam("version"));

        // construct response as layer json
        final JSONObject layerJSON = OskariLayerWorker.getMapLayerJSON(ml, params.getUser(), params.getLocale().getLanguage());
        if (layerJSON == null) {
            // handle error getting JSON failed
            throw new ActionException("Error constructing JSON for layer");
        }
        if(!cacheUpdated) {
            // Cache update failed, no biggie
            JSONHelper.putValue(layerJSON, "warn", "metadataReadFailure");
        }
        ResponseHelper.writeResponse(params, layerJSON);
    }

    private int saveLayer(final ActionParameters params) throws ActionException {

        // layer_id can be string -> external id!
        final String layer_id = params.getHttpParam(PARM_LAYER_ID);

        try {
            // ************** UPDATE ************************
            if (layer_id != null) {

                final OskariLayer ml = mapLayerService.find(layer_id);
                if (!permissionsService.hasEditPermissionForLayerByLayerId(params.getUser(), ml.getId())) {
                    throw new ActionDeniedException("Unauthorized user tried to update layer - id=" + layer_id);
                }

                handleRequestToMapLayer(params, ml);

                ml.setUpdated(new Date(System.currentTimeMillis()));
                mapLayerService.update(ml);

                return ml.getId();
            }

            // ************** INSERT ************************
            else {

                if (!permissionsService.hasAddLayerPermission(params.getUser())) {
                    throw new ActionDeniedException("Unauthorized user tried to add layer - id=" + layer_id);
                }

                final OskariLayer ml = new OskariLayer();
                final Date currentDate = new Date(System.currentTimeMillis());
                ml.setCreated(currentDate);
                ml.setUpdated(currentDate);
                handleRequestToMapLayer(params, ml);
                int id = mapLayerService.insert(ml);
                ml.setId(id);

                if(ml.isCollection()) {
                    // update the name with the id for permission mapping
                    ml.setName(ml.getId() + "_group");
                    mapLayerService.update(ml);
                }

                final String[] externalIds = params.getHttpParam("viewPermissions", "").split(",");

                addPermissionsForRoles(ml, params.getUser(), externalIds);

                // update keywords
                GetLayerKeywords glk = new GetLayerKeywords();
                glk.updateLayerKeywords(id, ml.getMetadataId());

                return ml.getId();
            }

        } catch (Exception e) {
            throw new ActionException("Couldn't update/insert map layer ", e);
        }
    }

    private boolean updateCache(OskariLayer ml, final String version) throws ActionException {
        if(ml == null) {
            return false;
        }
        if(ml.isCollection()) {
            // just be happy for collection layers, nothing to do
            return true;
        }
        if(version == null) {
            // check this here since it's not always required (for collection layers)
            throw new ActionParamsException("Version is required!");
        }
        // retrieve capabilities
        final String wmsUrl = getWmsUrl(ml.getUrl());
        CapabilitiesCache cc = null;
        try {
            cc = capabilitiesService.find(ml.getId());
            boolean isNew = false;
            if (cc == null) {
                cc = new CapabilitiesCache();
                cc.setLayerId(ml.getId());
                isNew = true;
            }
            cc.setVersion(version);

            final String capabilitiesXML = GetWMSCapabilities.getResponse(wmsUrl);
            cc.setData(capabilitiesXML);

            // update cache by updating db
            if (isNew) {
                capabilitiesService.insert(cc);
            } else {
                capabilitiesService.update(cc);
            }
        } catch (Exception ex) {
            log.info(ex, "Error updating capabilities: ", cc, "from URL:", wmsUrl);
            return false;
        }
        return true;
    }

    private String getWmsUrl(String wmsUrl) {
        if(wmsUrl == null) {
            return null;
        }
        //check if comma separated urls
        if (wmsUrl.indexOf(",http:") > 0) {
            wmsUrl = wmsUrl.substring(0, wmsUrl.indexOf(",http:"));
        }

        return wmsUrl;

    }

    private void handleRequestToMapLayer(final ActionParameters params, OskariLayer ml) throws ActionParamsException {

        HttpServletRequest request = params.getRequest();

        if(ml.getId() == -1) {
            // setup type and parent for new layers only
            ml.setType(params.getHttpParam("layerType"));
            ml.setParentId(params.getHttpParam("parentId", -1));
        }

        // organization id
        final LayerGroup group = layerGroupService.find(params.getHttpParam("groupId", -1));
        ml.addGroup(group);

        // get names and descriptions
        final Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String nextName = paramNames.nextElement();
            if (nextName.indexOf(LAYER_NAME_PREFIX) == 0) {
                ml.setName(nextName.substring(LAYER_NAME_PREFIX.length()).toLowerCase(), params.getHttpParam(nextName));
            } else if (nextName.indexOf(LAYER_TITLE_PREFIX) == 0) {
                ml.setTitle(nextName.substring(LAYER_TITLE_PREFIX.length()).toLowerCase(), params.getHttpParam(nextName));
            }
        }

        InspireTheme theme = inspireThemeService.find(params.getHttpParam("inspireTheme", -1));
        ml.addInspireTheme(theme);

        ml.setBaseMap(ConversionHelper.getBoolean(params.getHttpParam("isBase"), false));

        if(ml.isCollection()) {
            // ulr is needed for permission mapping, name is updated after we get the layer id
            ml.setUrl(ml.getType());
            // the rest is not relevant for collection layers
            return;
        }

        ml.setName(params.getRequiredParam("wmsName"));
        ml.setUrl(params.getRequiredParam("wmsUrl"));

        ml.setOpacity(params.getHttpParam("opacity", ml.getOpacity()));
        ml.setStyle(params.getHttpParam("style", ml.getStyle()));
        ml.setMinScale(ConversionHelper.getDouble(params.getHttpParam("minScale"), ml.getMinScale()));
        ml.setMaxScale(ConversionHelper.getDouble(params.getHttpParam("maxScale"), ml.getMaxScale()));

        ml.setLegendImage(params.getHttpParam("legendImage", ml.getLegendImage()));
        ml.setMetadataId(params.getHttpParam("metadataId", ml.getMetadataId()));
        ml.setTileMatrixSetId(params.getHttpParam("tileMatrixSetId"));
        ml.setTileMatrixSetData(params.getHttpParam("tileMatrixSetData"));

        final String xslt = request.getParameter("xslt");
        if(xslt != null) {
            // TODO: some validation of XSLT data
            ml.setGfiXslt(xslt);
        }
        ml.setGfiType(params.getHttpParam("gfiType", ml.getGfiType()));
    }

    private void addPermissionsForRoles(final OskariLayer ml, final User user, final String[] externalIds) {

        final Permissions permissions = new Permissions();
        permissions.getUniqueResourceName().setType(Permissions.RESOURCE_TYPE_MAP_LAYER);
        permissions.getUniqueResourceName().setNamespace(ml.getUrl());
        permissions.getUniqueResourceName().setName(ml.getName());
        // insert permissions
        for (String externalId : externalIds) {
            final long extId = ConversionHelper.getLong(externalId, -1);
            if (extId != -1 && user.hasRoleWithId(extId)) {
                permissionsService.insertPermissions(permissions.getUniqueResourceName(), externalId, Permissions.EXTERNAL_TYPE_ROLE, Permissions.PERMISSION_TYPE_VIEW_LAYER);
                permissionsService.insertPermissions(permissions.getUniqueResourceName(), externalId, Permissions.EXTERNAL_TYPE_ROLE, Permissions.PERMISSION_TYPE_EDIT_LAYER);
            }
        }

    }
}
