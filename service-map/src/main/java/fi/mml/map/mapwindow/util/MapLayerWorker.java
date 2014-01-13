package fi.mml.map.mapwindow.util;

import fi.mml.map.mapwindow.service.db.*;
import fi.mml.map.mapwindow.service.wms.WebMapService;
import fi.mml.map.mapwindow.service.wms.WebMapServiceFactory;
import fi.mml.map.mapwindow.service.wms.WebMapServiceParseException;
import fi.mml.portti.domain.permissions.Permissions;
import fi.mml.portti.service.db.permissions.PermissionsService;
import fi.mml.portti.service.db.permissions.PermissionsServiceIbatisImpl;
import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.InspireTheme;
import fi.nls.oskari.domain.map.Layer;
import fi.nls.oskari.domain.map.stats.StatsLayer;
import fi.nls.oskari.domain.map.stats.StatsVisualization;
import fi.nls.oskari.domain.map.wfs.WFSSLDStyle;

import fi.nls.oskari.domain.map.wms.LayerClass;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.wfs.WFSLayerConfigurationService;
import fi.nls.oskari.wfs.WFSLayerConfigurationServiceIbatisImpl;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.util.*;

/**
 * Worker class for rendering json objects from domain objects
 * DEPRECATED: Use OskariLayerWorker instead!
 */
@Deprecated
public class MapLayerWorker {

    private static PermissionsService permissionsService = new PermissionsServiceIbatisImpl();
    private static MapLayerService mapLayerService = new MapLayerServiceIbatisImpl();
    private static LayerClassService layerClassService = new LayerClassServiceIbatisImpl();
    private static InspireThemeService inspireThemeService = new InspireThemeServiceIbatisImpl();
    private static WFSLayerConfigurationService wfsService = new WFSLayerConfigurationServiceIbatisImpl();

    /** Logger */
    private static Logger log = LogFactory.getLogger(MapLayerWorker.class);

    // There working only plain text and html so ranked up
    private static String[] SUPPORTED_GET_FEATURE_INFO_FORMATS = new String[] {
            "text/html", "text/plain", "application/vnd.ogc.se_xml",
            "application/vnd.ogc.gml", "application/vnd.ogc.wms_xml",
            "text/xml" };

    /**
     * Gets all the map layers the user is permitted to view
     * @param user User
     * @param lang Language
     * @param showEmpty Remote IP
     * @return JSONObject of map layers
     */
    public static JSONObject getListOfAllMapLayers(final User user,
                                                   final String lang, boolean showEmpty) {

        List<String> resources = permissionsService
                .getResourcesWithGrantedPermissions(
                        Permissions.RESOURCE_TYPE_MAP_LAYER, user,
                        Permissions.PERMISSION_TYPE_VIEW_LAYER);

        List<String> groupResources = permissionsService
                .getResourcesWithGrantedPermissions(
                        Permissions.RESOURCE_TYPE_LAYER_GROUP, user,
                        Permissions.PERMISSION_TYPE_VIEW_LAYER);

        // Get the whole layerclass structure
        List<LayerClass> allLayerClass = layerClassService
                .findOrganizationalStructure(showEmpty);

        return populateListOfMapLayersJSON(allLayerClass,
                resources, groupResources, lang, false, user.getRoles(), showEmpty);
    }

    /**
     * Gets all the selected map layers
     * @param layerList List of selected layer IDs(?)
     * @param user User
     * @param lang Language
     * @param remoteIp Remote IP
     * @param isPublished Determines the permission type used for the layers
     * @return JSONObject containing the selected layers
     */
    public static JSONObject getSelectedLayersStructure(List<String> layerList,
                                                        User user, String lang, String remoteIp, boolean isPublished) {
        return getSelectedLayersStructure(layerList, user, lang, remoteIp, isPublished, false);
    }
    /**
     * Gets all the selected map layers
     * @param layerList List of selected layer IDs(?)
     * @param user User
     * @param lang Language
     * @param remoteIp Remote IP
     * @param isPublished Determines the permission type used for the layers
     * @param modifyURLs true to modify urls for easier proxy forwarding/false to keep as is
     * @return JSONObject containing the selected layers
     */
    public static JSONObject getSelectedLayersStructure(List<String> layerList,
                                                        User user, String lang, String remoteIp, boolean isPublished, boolean modifyURLs) {

        final String permissionType = getPermissionType(isPublished);
        final List<String> resources = permissionsService
                .getResourcesWithGrantedPermissions(
                        Permissions.RESOURCE_TYPE_MAP_LAYER, user,
                        permissionType);

        final List<String> groupResources = permissionsService
                .getResourcesWithGrantedPermissions(
                        Permissions.RESOURCE_TYPE_LAYER_GROUP, user,
                        permissionType);

        final List<LayerClass> allLayerClassRoot = new ArrayList<LayerClass>();

        /* Reverse layer order as it is that way in javascript */
        for (String baseLayerIdstr : layerList) {
            if (baseLayerIdstr == null || "null".equalsIgnoreCase(baseLayerIdstr)) {
                continue;
            }
            if (baseLayerIdstr.startsWith("base_")) {
                // Creating a fake parent? Why?
                // This might cause the issues? It's pretty much the only difference between all and selected...
                final LayerClass mapBaseLayersClass = new LayerClass();
                final LayerClass lc = layerClassService
                        .findOrganizationalStructureByClassId(Integer
                                .parseInt(baseLayerIdstr.substring(5)));

                mapBaseLayersClass.setLocale(lc.getLocale());

                mapBaseLayersClass.setParent(0);
                mapBaseLayersClass.addChild(lc);
                allLayerClassRoot.add(mapBaseLayersClass);
            } else {
                //TODO this doesn't get the whole structure (causes the bug)
                final Layer ml = mapLayerService.find(Integer
                        .parseInt(baseLayerIdstr));
                if(ml == null) {
                    // if maplayer was not found from db, ignore it
                    continue;
                }
                LayerClass mapLayersClass = layerClassService.find(ml.getLayerClassId());
                //FIXME addMapLayer takes in MapLayer, addMapLayers takes in List<Layer>...
                List<Layer> mapLayers = new ArrayList<Layer>();
                mapLayers.add(ml);
                mapLayersClass.addMapLayers(mapLayers);
                allLayerClassRoot.add(mapLayersClass);
            }
        }

        return populateListOfMapLayersJSON(
                allLayerClassRoot, resources, groupResources, lang,
                modifyURLs, user.getRoles(), false);
    }

    /**
     * Shoves given layers to JSON, used by getall and getselected
     * @param parentLayerClasses  List of parent layer classes
     * @param resources List of resources
     * @param groupResources List of group resources
     * @param lang Language
     * @param isSecure Whether we use the wms URL or a hardcoded service URL?
     * @param roles List of user roles
     * @return JSONObject containing map layers
     */
    private static JSONObject populateListOfMapLayersJSON(
            final List<LayerClass> parentLayerClasses,
            final List<String> resources, final List<String> groupResources,
            String lang, boolean isSecure,
            Collection<Role> roles, final boolean showEmpty) {

        final JSONObject listLayer = new JSONObject();
        final Set<String> permissionsList = permissionsService
                .getPublishPermissions();

        final Set<String> editAccessList = permissionsService
                .getEditPermissions();


        for (LayerClass layerClass : parentLayerClasses) {
            List<LayerClass> allLayerClass = layerClass.getChildren();

            if (allLayerClass.size() > 0) {
                if (layerClass.getMapLayers().size() > 0) {
                    // Parent has map layers
                    //System.out.println("mapLayers");
                    accumulateJSONs(listLayer, layerClass,
                            layerClass, resources, groupResources, lang,
                            isSecure, roles, permissionsList, editAccessList, showEmpty);
                }
                for (LayerClass lc : allLayerClass) {
                    // Parent's children
                    accumulateJSONs(listLayer, lc, layerClass, resources,
                            groupResources, lang, isSecure, roles,
                            permissionsList, editAccessList, showEmpty);

                }

            } else if (layerClass.getParent() == null) {
                // Parent doesn't have a parent... so is it a base class?
                //System.out.println("Parent doesn't have a parent... so is it a base class?");
                accumulateJSONs(listLayer, layerClass, layerClass,
                        resources, groupResources, lang, isSecure,
                        roles, permissionsList, editAccessList, showEmpty);
            } else { //empty, base or grouplayer

            }

        }
        return listLayer;
    }

    /**
     *
     * @param listLayer JSONObject to fill
     * @param layerClass Layer class
     * @param parentLayerClass Parent layer class
     * @param resources List of resources
     * @param groupResources List of group resources
     * @param lang Language
     * @param isSecure Whether we use the wms URL or a hardcoded service URL?
     * @param roles List of user roles
     * @param permissionsList List of user permissions
     */
    private static void accumulateJSONs(JSONObject listLayer, LayerClass layerClass,
                                        LayerClass parentLayerClass, final List<String> resources,
                                        final List<String> groupResources, final String lang,
                                        boolean isSecure, Collection<Role> roles,
                                        Set<String> permissionsList, final Set<String> editAccessList, final boolean showEmpty) {
        try {

            // Handle only those layers that can be found in resources.

            if (!layerClass.isMapLayersSelectable() && layerClass.getParent() != null) {
                // Apparently this is a base layer?
                // Check if this is in group resources
                if (groupResources.contains("+" + layerClass.getId())) {
                    JSONObject layerJson = populateMapLayersJson(
                            parentLayerClass, layerClass, lang, isSecure,
                            roles, permissionsList, editAccessList, showEmpty);
                    listLayer.accumulate("layers", layerJson);
                }
                // aaand nothing.
            } else {
                // Not a base layer?
                for (Layer layer : layerClass.getMapLayers()) {
                    // check if layer's wms thingie is in resources...
                    // why do we only pass on layers that have an iTheme?
                    if (resources.contains(layer.getWmsUrl() + "+" + layer.getWmsName())) {
                        JSONObject layerJson = populateLayerJson(layer, layerClass, lang,
                                isSecure, roles, permissionsList, editAccessList, inspireThemeService.find(layer.getInspireThemeId()));
                        listLayer.accumulate("layers", layerJson);
                    }

                }

            }
        } catch (JSONException e) {
            throw new RuntimeException(
                    "Something is wrong with map layers ajax request", e);
        }
    }

    /**
     * Creates a JSON object for a single map layer
     * @param parentLayerClass Parent LayerClass
     * @param layerClass LayerClass
     * @param lang Language
     * @param isSecure Whether we use the wms URL or a hardcoded service URL?
     * @param roles List of user roles
     * @param permissionsList List of user permissions
     * @return JSONObject containing the given maplayer and its sublayers
     * @throws JSONException
     */
    private static JSONObject populateMapLayersJson(final LayerClass parentLayerClass,
                                                    final LayerClass layerClass,
                                                    final String lang,
                                                    boolean isSecure,
                                                    Collection<Role> roles,
                                                    final Set<String> permissionsList,
                                                    final Set<String> editAccessList,
                                                    final boolean showEmpty)
            throws JSONException {
        JSONObject layerJson = new JSONObject();

        List<Layer> mapLayers = layerClass.getMapLayers();
        if (layerClass.isGroupMap()) {
            if (mapLayers != null && mapLayers.size() > 0) {
                // Group has map layers?
                // Apparently all the layers share these...
                Layer firstLayer = mapLayers.get(0);
                mapLayers.get(0).getInspireThemeId();
                InspireTheme iTheme = inspireThemeService.find(firstLayer
                        .getInspireThemeId());
                layerJson.put("orgName", parentLayerClass.getName(lang)).put("name", layerClass.getName(lang)).put("inspire", iTheme.getName(lang));

            } else if (showEmpty) {
                // Empty group?
                populateLocalizedNames(layerJson, parentLayerClass, layerClass, lang);
            }
            layerJson.put("type", "groupMap");

        } else {
            layerJson.put("type", "base");
            populateLocalizedNames(layerJson, parentLayerClass, layerClass, lang);
        }
        layerJson.put("styles", new JSONObject()).put("formats", new JSONObject()).put("isQueryable", false).put("dataUrl", layerClass.getDataUrl());

        JSONObject localeNames = new JSONObject();
        Set<String> langs = new TreeSet<String>(layerClass.getLanguages());
        langs.addAll(Arrays.asList(PropertyUtil.getSupportedLanguages()));
        Map<String, String> names = layerClass.getNames();
        for (String language : langs) {
            localeNames.put(language, names.get(language));
        }
        layerJson.put("names", localeNames);
        
        double minScale = 0;
        double maxScale = -1;

        if (null != mapLayers) {
            for (Layer mapLayer : mapLayers) {
                // get largest minscale and smallest maxscale, accept any maxscale if maxscale is not set
                minScale = Math.max(minScale, mapLayer.getMinScale());

                if (maxScale == -1 || maxScale > mapLayer.getMaxScale()) {
                    maxScale = mapLayer.getMaxScale();
                }

                JSONObject subLayer = populateLayerJson(mapLayer, layerClass, lang,
                        isSecure, roles, permissionsList, editAccessList, inspireThemeService.find(mapLayer.getInspireThemeId()));
                accumulateOrAppendJSON(mapLayers.size() > 1, layerJson, "subLayer", subLayer);
            }
        }
        layerJson.put("minScale", minScale).put("maxScale", maxScale);

        if (null != mapLayers && mapLayers.size() > 0) {
            layerJson.put("id", "base_" + layerClass.getId()).put("baseLayerId", layerClass.getId());
            populatePermissionInformation(layerJson, roles, "BASE+"+ layerClass.getId(), permissionsList, editAccessList);
        } else if ("groupMap".equals(layerJson.get("type")) || "base".equals(layerJson.get("type"))) {
            layerJson.put("id", "base_" + layerClass.getId()).put("baseLayerId", layerClass.getId());
            populatePermissionInformation(layerJson, roles, "BASE+"+layerClass.getId(), permissionsList, editAccessList);
        } else {
            layerJson.put("baseLayerId", "").put("id", layerClass.getId());
        }

        return layerJson;
    }

    private static void populateLocalizedNames(JSONObject layerJson,
                                               LayerClass parentLayerClass, LayerClass layerClass, String lang)
            throws JSONException {
        String parentLayerClassName = parentLayerClass.getName(lang);
        layerJson.put("orgName", parentLayerClassName).put("name", layerClass.getName(lang)).put("inspire", parentLayerClassName);
    }

    /**
     * Creates a JSON object for a single layer
     * @param layer The layer itself
     * @param layerClass LayerClass
     * @param lang Language
     * @param isSecure Whether we use the wms URL or a hardcoded service URL?
     * @param roles List of user roles
     * @param permissionsList List of user permissions
     * @param iTheme Inspire theme to use
     */
    private static JSONObject populateLayerJson(final Layer layer,
                                                final LayerClass layerClass,
                                                final String lang,
                                                final boolean isSecure,
                                                final Collection<Role> roles,
                                                final Set<String> permissionsList,
                                                final Set<String> editAccessList,
                                                final InspireTheme iTheme) {
        JSONObject layerJson = new JSONObject();
        try {
            layerJson.put("orgName", layerClass.getName(lang));
            layerJson.put("name", layer.getName(lang));
            layerJson.put("subtitle", layer.getTitle(lang));
            layerJson.put("inspire", iTheme.getName(lang));

            if (Layer.TYPE_WMS.equals(layer.getType())) {
                populateWmsJSON(layerJson, layer);
            } else if (Layer.TYPE_WMTS.equals(layer.getType())) {
                populateWmtsJSON(layerJson, layer);
            } else if (Layer.TYPE_WFS.equals(layer.getType())) {
                populateWfsJSON(layerJson, layer);
            } else if (Layer.TYPE_STATS.equals(layer.getType())) {
                populateStatsJSON(layerJson, layer, lang);
            }

            layerJson.put("id", layer.getId());
            layerJson.put("type", layer.getType());
            layerJson.put("geom", layer.getGeom());
            layerJson.put("minScale", layer.getMinScale());
            layerJson.put("maxScale", layer.getMaxScale());

            layerJson.put("descriptionLink", layer.getDescriptionLink());
            layerJson.put("legendImage", layer.getLegendImage());
            layerJson.put("baseLayerId", layerClass.getId());

            layerJson.put("orderNumber", layer.getOrdernumber());
            layerJson.put("opacity", layer.getOpacity());

            if (null != layer.getCreated()) {
                layerJson.put("created", layer.getCreated());
            }

            if (null != layer.getUpdated()) {
                layerJson.put("updated", layer.getUpdated());
            }
            if (!isSecure) {
                layerJson.put("wmsUrl", layer.getWmsUrl());
            } else {
                // for easier proxy routing on ssl hosts, maps all urls with prefix and a simplified url
                // so tiles can be fetched from same host from browsers p.o.v. and the actual url
                // is proxied with a proxy for example: /proxythis/<actual wmsurl>
                layerJson.put("wmsUrl", PropertyUtil.get("maplayer.wmsurl.secure") + layer.getSimplifiedWmsUrl());
            }

            if (null != layer.getWms_parameter_layers() && !"".equals(layer.getWms_parameter_layers())) {
                layerJson.put("wmsName", layer.getWms_parameter_layers());
            } else {
                layerJson.put("wmsName", layer.getWmsName());
            }

            populatePermissionInformation(layerJson, roles,layer.getWmsUrl() + "+" +  layer.getWmsName(), permissionsList,
                    editAccessList);

            layerJson.put("resource_url_client_pattern", layer.getResource_url_client_pattern());

            setDataUrl(layerJson, layer);

        } catch (JSONException e) {
            log.error("problem when trying populate layer json", e);
        }
        return layerJson;
    }

    /**
     * Builds a new WebMapService
     * @param layer layer
     * @return WebMapService or null if something goes wrong.
     */
    private static WebMapService buildWebMapService(final Layer layer) {
        try {
            return WebMapServiceFactory.buildWebMapService(layer.getId(), layer.getWmsName());
        } catch (WebMapServiceParseException e) {
            log.error("Failed to create WebMapService for layer id '" + layer.getId() + "'. No Styles available");
        }
        return null;
    }

    private static JSONObject createStylesJSON(String name, String title, String legend) throws JSONException {
        return new JSONObject().put("name", name).put("title", title).put("legend", legend);
    }

    //TODO why do we accumulate or append?
    private static void accumulateOrAppendJSON(boolean accumulate, JSONObject parent, String childName, JSONObject child) throws JSONException {
        if (accumulate) {
            parent.accumulate(childName, child);
        } else {
            parent.append(childName, child);
        }
    }

    /**
     * Constructs a style json
     * 
     * @param wms WebMapService
     * @param styleJSON JSONObject to populate
     */
    private static void populateLayerStylesOnJSONArray(WebMapService wms, JSONObject styleJSON) {
        Map<String, String> styles = wms.getSupportedStyles();
        try {
            if (styles.size() > 0) {
                Map<String, String> legends = wms.getSupportedLegends();
                for (String styleName : styles.keySet()) {
                    JSONObject obj = createStylesJSON(styleName, styles.get(styleName), legends.get(styleName));
                    //TODO why does this only accumulate?
                    styleJSON.accumulate("styles", obj);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
      * Constructs a style json
      *
      * @param styleJSON JSONObject to populate
      * @param layer layer of which styles will be retrieved
     */
       private static void populateLayerStylesOnJSONArray(JSONObject styleJSON, Layer layer) {
          List<WFSSLDStyle> styleList = wfsService.findWFSLayerStyles(layer.getId());
          try{
            if ( styleList.size() > 0) {
               JSONArray arr = new JSONArray();
               for (WFSSLDStyle style : styleList) {
                 JSONObject obj =  createStylesJSON(style.getName(), style.getName(), style.getName());
                 if(obj.length() > 0) {
                     arr.put(obj);
                 }
               }
               styleJSON.put("styles", arr);
            }
            }catch (Exception e) {
              throw new RuntimeException(e);
            }
       }



    /**
     * Constructs a formats json containing the most preferred supported format
     * 
     * @param wms WebMapService
     * @return JSONObject containing the most preferred supported format
     */
    private static JSONObject getFormatsJSON(WebMapService wms) {
        JSONObject formatJSON = new JSONObject();
        // simple but inefficient...
        Set<String> formats =  new HashSet<String>(Arrays.asList(wms.getFormats()));
        // We support the following formats. Formats are presented
        // in order of preference.
        // 'application/vnd.ogc.se_xml' == GML
        // 'application/vnd.ogc.gml' == GML
        // 'application/vnd.ogc.wms_xml' == text/xml
        // 'text/xml'
        // 'text/html'
        // 'text/plain'

        boolean foundSupported = false;
        try {
            for (String supported : SUPPORTED_GET_FEATURE_INFO_FORMATS) {
                if (formats.contains(supported)) {
                    formatJSON.put("value", supported);
                    foundSupported = true;
                    break;
                }
            }

            if (!foundSupported) {
                formatJSON.put("value", "");
            }

            return formatJSON;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static final String NO_PUBLICATION_PERMISSION = "no_publication_permission";
    public static final String PUBLICATION_PERMISSION_OK = "publication_permission_ok";

    /**
     * Add permission information to JSON
     * @param layerJson JSONObject to populate
     * @param roles List of user roles
     * @param layerPermissionKey Layer permission key
     * @param permissionsList List of user permissions
     * @throws JSONException
     */
    private static void populatePermissionInformation(final JSONObject layerJson,
            final Collection<Role> roles, final String layerPermissionKey,
            final Set<String> permissionsList, final Set<String> editAccessList) throws JSONException {

        JSONObject permission = new JSONObject();
        layerJson.put("permissions", permission);

        for (Role role : roles) {

            if (role.isAdminRole()) {
                permission.put("edit", "true");
                permission.put("publish", PUBLICATION_PERMISSION_OK);
                break;
            }

            if ("Guest".equals(role.getName())) {   // TODO: need refactoring, check user.isGuest()
                permission.put("publish", NO_PUBLICATION_PERMISSION); // Guest role
                return;
            } else {
                if (editAccessList.contains(layerPermissionKey + ":" + role.getId())) {
                    permission.put("edit", "true");
                }
                if (permissionsList.contains(layerPermissionKey + ":" + role.getId())) {
                    permission.put("publish", PUBLICATION_PERMISSION_OK);
                }

            }
        }
    }

    /**
     * Populates given JSONObject with a given stats layer
     * @param layerJson JSONObject to populate
     * @param layer Stats layer
     * @param lang Language
     * @throws JSONException
     */
    private static void populateStatsJSON(JSONObject layerJson, Layer layer, String lang) throws JSONException{
        StatsLayer statsLayer = (StatsLayer) layer;
        final JSONArray visualizationList = new JSONArray();
        for(StatsVisualization vis : statsLayer.getVisualizations()) {
            final JSONObject visualization = new JSONObject();
            JSONHelper.putValue(visualization, "id", vis.getId());
            JSONHelper.putValue(visualization, "name", vis.getName(lang));
            JSONHelper.putValue(visualization, "filterproperty", vis.getFilterproperty());
            visualizationList.put(visualization);
        }
        layerJson.put("visualizations", visualizationList);
    }

    /**
     * Populates given  JSONObject with a given WFS layer
     * @param layerJson JSONObject to populate
     * @param layer Layer
     * @throws JSONException
     */
    private static void populateWfsJSON(JSONObject layerJson, Layer layer) throws JSONException{
        layerJson.put("style", "default");
        MapLayerWorker.populateLayerStylesOnJSONArray(layerJson, layer);
        layerJson.put("formats", new JSONObject());
        layerJson.put("isQueryable", true);
    }

    /**
     * Populates given JSONObject with a given WMS layer
     * @param layerJson JSONObject to populate
     * @param layer Layer
     * @throws JSONException
     */
    private static void populateWmsJSON(JSONObject layerJson, Layer layer) throws JSONException {
        layerJson.put("style", layer.getStyle());

        WebMapService wms = MapLayerWorker.buildWebMapService(layer);
        if (wms != null) {
            MapLayerWorker.populateLayerStylesOnJSONArray(wms, layerJson);
            layerJson.put(
                    "formats",
                    MapLayerWorker.getFormatsJSON(wms));
            layerJson.put("isQueryable", wms.isQueryable());
            // Set Legend url to layer, if defined in GetCapabilities
            setLegendUrl2Layer(layerJson, layer);
        } else {
            // Something is not right, build dummy objects.
            layerJson.put("styles", new JSONObject());
            layerJson.put("formats", new JSONObject());
            layerJson.put("isQueryable", false);
        }
    }

    /**
     * Populates given JSONObject with a given WMTS layer
     * @param layerJson JSONObject to populate
     * @param layer Layer
     * @throws JSONException
     */
    private static void populateWmtsJSON(JSONObject layerJson, Layer layer) throws JSONException{
        layerJson.put("style", "");

        layerJson.put("tileMatrixSetId", layer.getTileMatrixSetId());
        layerJson.put("tileMatrixSetData",
                new JSONObject(layer.getTileMatrixSetData()));

        layerJson.put("styles", new JSONArray(layer.getStyle()));

        layerJson.put("formats", new JSONObject());
        layerJson.put("isQueryable", false);
    }

    private static String getHashCode(String clientIp) {

        Calendar cal = Calendar.getInstance();
        final String timezoneProp = PropertyUtil.getOptional("maplayer.timezone");
        if(timezoneProp != null) {
            cal.setTimeZone(TimeZone.getTimeZone(timezoneProp));
        }

        StringBuilder hashParams = new StringBuilder();
        final String secret = PropertyUtil.getOptional("maplayer.secretkey");
        if(secret != null) {
            hashParams.append(secret);
        }
        hashParams.append(cal.get(Calendar.DAY_OF_MONTH));
        hashParams.append(clientIp);
        hashParams.append(cal.get(Calendar.YEAR));

        byte[] bytesOfMessage;
        try {
            bytesOfMessage = hashParams.toString().getBytes("UTF-8");
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] theDigest = md.digest(bytesOfMessage);
            return new String(new org.apache.commons.codec.binary.Base64().encode(theDigest),
                    "ASCII").trim();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // This is solution of transition for dataUrl and for dataUrl_uuid
    private static void setDataUrl(final JSONObject layerJson, Layer layer)
            throws JSONException {

        if (layer.getDataUrl().indexOf("uuid=") > 0) {
            // we have an uuid
            layerJson.put("dataUrl", layer.getDataUrl());
            layerJson.put(
                    "dataUrl_uuid",
                    layer.getDataUrl().substring(
                            layer.getDataUrl().indexOf("uuid=") + 5));
        } else {
            if (!"".equals(layer.getDataUrl())) {
                layerJson.put("dataUrl", "/catalogue/ui/metadata.html?uuid="
                        + layer.getDataUrl());
            } else {
                layerJson.put("dataUrl", layer.getDataUrl());
            }

            layerJson.put("dataUrl_uuid", layer.getDataUrl());

        }
    }

    /**
     * Set legendurl to layer, if defined in GetCapabilities
     * @param layerJson layer JSONObject
     * @param layer layer
     */
    private static void setLegendUrl2Layer(JSONObject layerJson, Layer layer) {
        try {
            String mylegend = "";
            if (layerJson.has("styles")) {
                if (layerJson.optJSONArray("styles") != null) {
                    JSONArray styles = layerJson.getJSONArray("styles");

                    if (styles.length() > 0) {

                        for (int i = 0; i < styles.length(); i++) {
                            JSONObject style = styles.getJSONObject(i);
                            if (style.getString("name").equalsIgnoreCase(
                                    layer.getStyle())) {
                                mylegend = style.getString("legend");
                                // TODO should we break here?
                            }
                        }
                        // TODO so a null legend is fine?
                        if (!"".equals(mylegend)) {
                            layer.setLegendImage(mylegend);
                        }
                    }

                } else {
                    JSONObject styles = layerJson.getJSONObject("styles");

                    if (styles.length() > 0) {

                        if (styles.getString("name").equalsIgnoreCase(
                                layer.getStyle())) {
                            mylegend = styles.getString("legend");
                        }

                        if (!"".equals(mylegend)) {
                            layer.setLegendImage(mylegend);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String getPermissionType(final boolean isPublished) {
        if (isPublished) {
            return Permissions.PERMISSION_TYPE_VIEW_PUBLISHED;
        }
        return Permissions.PERMISSION_TYPE_VIEW_LAYER;
    }
}
