package fi.nls.oskari.control.layer;

import fi.mml.map.mapwindow.service.db.LayerClassService;
import fi.mml.map.mapwindow.service.db.LayerClassServiceIbatisImpl;
import fi.mml.map.mapwindow.service.db.MapLayerService;
import fi.mml.map.mapwindow.service.db.MapLayerServiceIbatisImpl;
import fi.mml.map.mapwindow.util.MapLayerWorker;
import fi.mml.portti.domain.permissions.Permissions;
import fi.mml.portti.service.db.permissions.PermissionsService;
import fi.mml.portti.service.db.permissions.PermissionsServiceIbatisImpl;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionHandler;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.CapabilitiesCache;
import fi.nls.oskari.domain.map.wms.MapLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.*;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Enumeration;

/**
 * Admin insert/update of WMS map layer
 * 
 */
@OskariActionRoute("SaveLayer")
public class SaveLayerHandler extends ActionHandler {

    private MapLayerService mapLayerService = new MapLayerServiceIbatisImpl();
    private PermissionsService permissionsService = new PermissionsServiceIbatisImpl();
    private LayerClassService layerClassService = new LayerClassServiceIbatisImpl();
    
    private static final Logger log = LogFactory.getLogger(SaveLayerHandler.class);
    private static final String PARM_LAYER_ID = "layer_id";

    private static final String LAYER_NAME_PREFIX = "name";
    private static final String LAYER_TITLE_PREFIX = "title";

    @Override
    public void handleAction(ActionParameters params) throws ActionException {

        HttpServletRequest request = params.getRequest();

        try {

            final String layer_id = params.getHttpParam(PARM_LAYER_ID, "");
            final int mapLayerId = ConversionHelper.getInt(layer_id, 0);
            


            // ************** UPDATE ************************
            if (!layer_id.isEmpty()) {

                if(!permissionsService.hasEditPermissionForLayerByLayerId(params.getUser(), mapLayerId)) {
                    throw new ActionDeniedException("Unauthorized user tried to update layer - id=" + layer_id);
                }

                if (mapLayerId > 0) {
                    MapLayer ml = new MapLayer();
                    ml.setId(mapLayerId);
                    handleRequestToMapLayer(request, ml);

                    ml.setUpdated(new Date(System.currentTimeMillis()));
                    mapLayerService.update(ml);
                    
                    org.json.JSONObject mapJson = ml.toJSON();
                    mapJson.put("orgName",layerClassService.find(ml.getLayerClassId()).getName(PropertyUtil.getDefaultLanguage()));
                    
                    // update cache
                    try {
                        updateCache(ml);
                    } catch (ActionException ae) {
                        // Cache update failed, no biggie
                        mapJson.put("warn", "metadataReadFailure");
                    }
                    ResponseHelper.writeResponse(params, mapJson.toString());
                }
            }

            // ************** INSERT ************************
            else {

                if(!permissionsService.hasAddLayerPermission(params.getUser())) {
                    throw new ActionDeniedException("Unauthorized user tried to add layer - id=" + layer_id);
                }

                MapLayer ml = new MapLayer();
                Date currentDate = new Date(System.currentTimeMillis());
                ml.setCreated(currentDate);
                ml.setUpdated(currentDate);
                handleRequestToMapLayer(request, ml);
                int id = mapLayerService.insert(ml);
                ml.setId(id);

                final String[] externalIds = params.getHttpParam("viewPermissions", "").split(",");

                addPermissionsForRoles(ml, params.getUser(), externalIds);

                org.json.JSONObject mapJson = ml.toJSON();
                mapJson.put("orgName",layerClassService.find(ml.getLayerClassId()).getName(PropertyUtil.getDefaultLanguage()));
                // add user roles, assume that user is admin...
                JSONObject perms = new JSONObject();
                mapJson.put("permissions", perms);
                perms.put("edit", "true");
                perms.put("publish", MapLayerWorker.PUBLICATION_PERMISSION_OK);

                // update keywords
                GetLayerKeywords glk = new GetLayerKeywords();
                glk.updateLayerKeywords(id, ml.getDataUrl());

                // update cache
                try {
                    insertCache(ml);
                } catch (ActionException ae) {
                    // Cache update failed, no biggie
                    mapJson.put("warn", "metadataReadFailure");
                }
                ResponseHelper.writeResponse(params, mapJson.toString());
            }

        } catch (Exception e) {
           throw new ActionException("Couldn't update/insert map layer ", e);
        }
    }

    private int insertCache(MapLayer ml) throws ActionException {
        // retrieve capabilities

        CapabilitiesCache cc = mapLayerService.getCapabilitiesCache(ml.getId());
        if (cc == null) {
            cc = new CapabilitiesCache();

            String wmsUrl = getWmsUrl(ml.getWmsUrl());
            final String capabilitiesXML = GetWMSCapabilities.getResponse(wmsUrl);
            cc.setLayerId(ml.getId());
            cc.setData(capabilitiesXML);
            cc.setVersion(ml.getVersion());
            // update cache by inserting to db
            return   mapLayerService.insertCapabilities(cc);
        } else {
            updateCache(ml);
        }
        return ml.getId();
    }

    private void updateCache(MapLayer ml) throws ActionException {
        // retrieve capabilities
        CapabilitiesCache cc = mapLayerService.getCapabilitiesCache(ml.getId());

        String wmsUrl = getWmsUrl(ml.getWmsUrl());

        final String capabilitiesXML = GetWMSCapabilities.getResponse(wmsUrl);
        cc.setData(capabilitiesXML);
        
        // update cache by updating db
        mapLayerService.updateCapabilities(cc);
    }

    private String getWmsUrl(String savedWmsUrl) {

        String wmsUrl = savedWmsUrl;

        //check if comma separated urls
        if (wmsUrl.indexOf(",http:") > 0) {
            wmsUrl = savedWmsUrl.substring(0,savedWmsUrl.indexOf(",http:"));
        }

        return wmsUrl;

    }
    
    private void handleRequestToMapLayer(HttpServletRequest request, MapLayer ml) {

        // FIXME: parameters are not filtered through getHttpParam, any reason for this?
        ml.setLayerClassId(new Integer(request.getParameter("lcId")));

        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String nextName = paramNames.nextElement();
            if (nextName.indexOf(LAYER_NAME_PREFIX) == 0) {
                ml.setName(nextName.substring(LAYER_NAME_PREFIX.length()).toLowerCase(), request.getParameter(nextName));
            } else if (nextName.indexOf(LAYER_TITLE_PREFIX) == 0) {
                ml.setTitle(nextName.substring(LAYER_TITLE_PREFIX.length()).toLowerCase(), request.getParameter(nextName));
            }
        }

        ml.setWmsName(request.getParameter("wmsName"));
        ml.setWmsUrl(request.getParameter("wmsUrl"));

        String opacity = "0";
        if (request.getParameter("opacity") != null
                && !"".equals(request.getParameter("opacity"))) {
            opacity = request.getParameter("opacity");
        }

        ml.setOpacity(new Integer(opacity));
        String style = "";
        if (request.getParameter("style") != null
                && !"".equals(request.getParameter("style"))) {
            style = request.getParameter("style");
            //style = IOHelper.decode64(style);
        }
        ml.setStyle(style);

        String minScale = request.getParameter("minScale");
        String maxScale = request.getParameter("maxScale");
        if(minScale != null && !"".equals(minScale)) {
            ml.setMinScale(new Double(minScale));
        } else {
            ml.setMinScale(new Double(16000000));
        }
        if(maxScale != null && !"".equals(maxScale)) {
            ml.setMaxScale(new Double(maxScale));
        } else {
            ml.setMaxScale(new Double(1));
        }
//        ml.setMinScale(new Double(request.getParameter("minScale")));
//        ml.setMaxScale(new Double(request.getParameter("maxScale")));

        ml.setDescriptionLink(request.getParameter("descriptionLink"));
        ml.setLegendImage(request.getParameter("legendImage"));

        String inspireThemeId = request.getParameter("inspireTheme");
        Integer inspireThemeInteger = Integer.valueOf(inspireThemeId);
        ml.setInspireThemeId(inspireThemeInteger);

        ml.setDataUrl(request.getParameter("dataUrl"));
        ml.setMetadataUrl(request.getParameter("metadataUrl"));
        ml.setOrdernumber(new Integer(request.getParameter("orderNumber")));

        ml.setType(request.getParameter("layerType"));
        ml.setTileMatrixSetId(request.getParameter("tileMatrixSetId"));

        ml.setTileMatrixSetData(request.getParameter("tileMatrixSetData"));

        ml.setWms_dcp_http(request.getParameter("wms_dcp_http"));
        ml.setWms_parameter_layers(request
                        .getParameter("wms_parameter_layers"));
        ml.setResource_url_scheme(request.getParameter("resource_url_scheme"));
        ml.setResource_url_scheme_pattern(request
                .getParameter("resource_url_scheme_pattern"));
        ml.setResource_url_scheme_pattern(request
                .getParameter("resource_url_client_pattern"));

        if (request.getParameter("resource_daily_max_per_ip") != null) {
            ml.setResource_daily_max_per_ip(ConversionHelper.getInt(request
                    .getParameter("resource_daily_max_per_ip"), 0));
        }
        String xslt = "";
        if (request.getParameter("xslt") != null
                && !"".equals(request.getParameter("xslt"))) {
            xslt = request.getParameter("xslt");
            //xslt = IOHelper.decode64(xslt);
        }
        ml.setXslt(xslt);
        ml.setGfiType(request.getParameter("gfiType"));
        String sel_style = "";
        if (request.getParameter("selection_style") != null
                && !"".equals(request.getParameter("selection_style"))) {
            sel_style = request.getParameter("selection_style");
            //sel_style = IOHelper.decode64(sel_style);
        }
        ml.setSelection_style(sel_style);
        ml.setVersion(request.getParameter("version"));
        if (request.getParameter("epsg") != null) {
            ml.setEpsg(ConversionHelper.getInt(request.getParameter("epsg"),3067));
        }
    }
    
    private void addPermissionsForRoles(MapLayer ml, User user, final String[] externalIds) {


        Permissions permissions = new Permissions();

        permissions.getUniqueResourceName().setType(Permissions.RESOURCE_TYPE_WMS_LAYER);
        permissions.getUniqueResourceName().setNamespace(ml.getWmsUrl());
        permissions.getUniqueResourceName().setName(ml.getWmsName());

        // insert permissions
        for (String externalId : externalIds) {
           if(user.hasRoleWithId(ConversionHelper.getLong(externalId, -1))) {
                permissionsService.insertPermissions(permissions.getUniqueResourceName(), externalId, Permissions.EXTERNAL_TYPE_ROLE, Permissions.PERMISSION_TYPE_VIEW_LAYER);
                permissionsService.insertPermissions(permissions.getUniqueResourceName(), externalId, Permissions.EXTERNAL_TYPE_ROLE, Permissions.PERMISSION_TYPE_EDIT_LAYER);
            }
        }

    }
}
