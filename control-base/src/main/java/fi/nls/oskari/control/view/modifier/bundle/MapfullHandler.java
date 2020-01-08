package fi.nls.oskari.control.view.modifier.bundle;

import fi.mml.map.mapwindow.util.OskariLayerWorker;
import fi.nls.oskari.analysis.AnalysisHelper;
import fi.nls.oskari.annotation.OskariViewModifier;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.MyPlaceCategory;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.analysis.Analysis;
import fi.nls.oskari.domain.map.userlayer.UserLayer;
import fi.nls.oskari.domain.map.view.ViewTypes;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.analysis.domain.AnalysisLayer;
import fi.nls.oskari.map.analysis.service.AnalysisDbService;
import fi.nls.oskari.map.analysis.service.AnalysisDbServiceMybatisImpl;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.myplaces.MyPlacesService;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.view.modifier.ModifierException;
import fi.nls.oskari.view.modifier.ModifierParams;
import fi.nls.oskari.view.modifier.ViewModifier;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.oskari.map.userlayer.service.UserLayerDataService;
import org.oskari.map.userlayer.service.UserLayerDbService;
import org.oskari.permissions.PermissionService;
import org.oskari.permissions.model.PermissionType;
import org.oskari.service.util.ServiceFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@OskariViewModifier("mapfull")
public class MapfullHandler extends BundleHandler {

    private static final Logger LOGGER = LogFactory.getLogger(MapfullHandler.class);
    private static PermissionService permissionsService;

    // FIXME: default srs is hardcoded into frontend if srs is not defined in mapOptions!!
    public static final String DEFAULT_MAP_SRS = "EPSG:3067";

    private static final String KEY_LAYERS = "layers";
    private static final String KEY_SEL_LAYERS = "selectedLayers";
    private static final String KEY_ID = "id";
    private static final String KEY_FORCE_PROXY = "forceProxy";

    private static final String KEY_MAP_OPTIONS = "mapOptions";
    private static final String KEY_PROJ_DEFS = "projectionDefs";
    private static final String KEY_SRS = "srsName";


    private static final String KEY_PLUGINS = "plugins";
    public static final String KEY_CONFIG = "config";
    private static final String KEY_BASELAYERS = "baseLayers";
    private static final String KEY_CENTER_MAP_AUTOMATICALLY = "centerMapAutomatically";

    private static final String PREFIX_MYPLACES = "myplaces_";
    private static final String PREFIX_ANALYSIS = "analysis_";
    private static final String PREFIX_USERLAYERS = "userlayer_";
    private static final Set<String> BUNDLES_HANDLING_MYPLACES_LAYERS = ConversionHelper.asSet(ViewModifier.BUNDLE_MYPLACES2, ViewModifier.BUNDLE_MYPLACES3);

    private static final String PLUGIN_LAYERSELECTION = "Oskari.mapframework.bundle.mapmodule.plugin.LayerSelectionPlugin";
    private static final String PLUGIN_GEOLOCATION = "Oskari.mapframework.bundle.mapmodule.plugin.GeoLocationPlugin";
    public static final String PLUGIN_WFSVECTORLAYER = "Oskari.wfsvector.WfsVectorLayerPlugin";
    private static final String PLUGIN_MYLOCATION = "Oskari.mapframework.bundle.mapmodule.plugin.MyLocationPlugin";

    public static final String EPSG_PROJ4_FORMATS = "epsg_proj4_formats.json";

    private static MyPlacesService myPlaceService = null;
    private static final AnalysisDbService analysisService = new AnalysisDbServiceMybatisImpl();
    private static UserLayerDbService userLayerService;
    private static final UserLayerDataService userLayerDataService = new UserLayerDataService();
    private static OskariLayerService mapLayerService;

    private static JSONObject epsgMap = null;
    private HashMap<String, PluginHandler> pluginHandlers = null;


    public void init() {
        myPlaceService = OskariComponentManager.getComponentOfType(MyPlacesService.class);
        userLayerService = OskariComponentManager.getComponentOfType(UserLayerDbService.class);
        mapLayerService = OskariComponentManager.getComponentOfType(OskariLayerService.class);
        // to prevent mocking issues in JUnit tests....
        permissionsService = ServiceFactory.getPermissionsService(); // OskariComponentManager.getComponentOfType(PermissionService.class);
        epsgInit();
        svgInit();
        pluginHandlers = new HashMap<>();
        registerPluginHandler(LogoPluginHandler.PLUGIN_NAME, new LogoPluginHandler());
        registerPluginHandler(WfsLayerPluginHandler.PLUGIN_NAME, new WfsLayerPluginHandler());
        registerPluginHandler(MyPlacesLayerPluginHandler.PLUGIN_NAME, new MyPlacesLayerPluginHandler());
        registerPluginHandler(UserLayerPluginHandler.PLUGIN_NAME, new UserLayerPluginHandler());
    }

    public void registerPluginHandler (String pluginId, PluginHandler handler) {
        pluginHandlers.put(pluginId, handler);
    }

    public boolean modifyBundle(final ModifierParams params) throws ModifierException {
        final JSONObject mapfullConfig = getBundleConfig(params.getConfig());
        final JSONObject mapfullState = getBundleState(params.getConfig());

        if (mapfullConfig == null) {
            return false;
        }

        // Any layer referenced in state.selectedLayers array NEEDS to
        // be in conf.layers otherwise it cant be added to map on startup
        final JSONArray mfConfigLayers = JSONHelper.getEmptyIfNull(mapfullConfig.optJSONArray(KEY_LAYERS));
        final JSONArray mfStateLayers = JSONHelper.getEmptyIfNull(mapfullState.optJSONArray(KEY_SEL_LAYERS));
        copySelectedLayersToConfigLayers(mfConfigLayers, mfStateLayers);
        final Set<String> bundleIds = getBundleIds(params.getStartupSequence());
        final boolean useDirectURLForMyplaces = false;
        final String mapSRS = getSRSFromMapConfig(mapfullConfig);
        final boolean forceProxy = mapfullConfig.optBoolean(KEY_FORCE_PROXY, false);
        final JSONArray fullConfigLayers = getFullLayerConfig(mfConfigLayers,
                params.getUser(),
                params.getLocale().getLanguage(),
                params.getViewId(),
                params.getViewType(),
                bundleIds,
                useDirectURLForMyplaces,
                params.isModifyURLs(),
                mapSRS,
                forceProxy,
                JSONHelper.getJSONArray(mapfullConfig, KEY_PLUGINS));

        setProjDefsForMapConfig(mapfullConfig, mapSRS);
        // overwrite layers
        try {
            mapfullConfig.put(KEY_LAYERS, fullConfigLayers);
        } catch (Exception e) {
            LOGGER.error(e, "Unable to overwrite layers");
        }

        // dummyfix: because migration tool added layer selection to all migrated maps
        // remove it from old published maps if only one layer is selected
        if (params.isOldPublishedMap()) {
            this.killLayerSelectionPlugin(mapfullConfig);
        }

        if (params.isLocationModified()) {
            LOGGER.info("locationModifiedByParams -> disabling GeoLocationPlugin");
            removePlugin(PLUGIN_GEOLOCATION, mapfullConfig);
            removeMyLocationPluginAutoCenter(mapfullConfig);
        }

        pluginHandlers.entrySet().stream().forEach(entry -> {
            JSONObject pluginJSON = getPlugin(entry.getKey(), mapfullConfig);
            if (pluginJSON != null) {
                entry.getValue().modifyPlugin(pluginJSON, params, mapSRS);
            }
        });

        return false;
    }

    public static JSONArray getFullLayerConfig(final JSONArray layersArray,
                                               final User user, final String lang, final long viewID,
                                               final String viewType, final Set<String> bundleIds,
                                               final boolean useDirectURLForMyplaces, final String mapSRS) {
        return getFullLayerConfig(layersArray, user, lang, viewID, viewType, bundleIds, useDirectURLForMyplaces, false, mapSRS);
    }

    public static JSONArray getFullLayerConfig(final JSONArray layersArray,
                                               final User user, final String lang, final long viewID,
                                               final String viewType, final Set<String> bundleIds,
                                               final boolean useDirectURLForMyplaces,
                                               final boolean modifyURLs,
                                               final String mapSRS) {
        return getFullLayerConfig(
                layersArray, user, lang, viewID, viewType, bundleIds,
                useDirectURLForMyplaces, modifyURLs, mapSRS, false, null);
    }

    /**
     * Detect projection that will be used for view that is being loaded
     * <p/>
     * {
     * "mapOptions" : {
     * "srsName":"EPSG:3067"
     * }
     * }
     *
     * @param mapfullConfig
     * @return conf.mapOptions.srsName or DEFAULT_MAP_SRS if it doesn't exist
     */
    public String getSRSFromMapConfig(final JSONObject mapfullConfig) {
        if (mapfullConfig == null) {
            return DEFAULT_MAP_SRS;
        }
        final JSONObject options = mapfullConfig.optJSONObject(KEY_MAP_OPTIONS);
        if (options == null) {
            return DEFAULT_MAP_SRS;
        }
        final String mapSRS = options.optString(KEY_SRS);
        if (mapSRS != null) {
            return mapSRS;
        }
        return DEFAULT_MAP_SRS;
    }

    public void setProjDefsForMapConfig(final JSONObject mapfullConfig, final String... srs) {
        if (mapfullConfig == null || srs == null) {
            return;
        }
        JSONObject defs = JSONHelper.getJSONObject(mapfullConfig, KEY_PROJ_DEFS);
        if(defs == null) {
            defs = new JSONObject();
            JSONHelper.putValue(mapfullConfig, KEY_PROJ_DEFS, defs);
        }
        for(String mapSRS : srs) {
            final String mapSRSProjDef = getMapSRSProjDef(mapSRS);

            // couldn't get data or already defined -> go to next one
            if (mapSRSProjDef == null || defs.has(mapSRS)) {
                continue;
            }
            JSONHelper.putValue(defs, mapSRS, mapSRSProjDef);
        }
    }

    public String getMapSRSProjDef(final String mapSRS) {

        try {

            if(this.epsgMap.has(mapSRS.toUpperCase())){
                return JSONHelper.getStringFromJSON(this.epsgMap, mapSRS.toUpperCase(), null);
            }
            else {
                LOGGER.debug("ProjectionDefs not found in epsg_proj4_formats.json", mapSRS);
            }
        } catch (Exception e) {
            LOGGER.debug("ProjectionDefs read failed in epsg_proj4_formats.json", mapSRS, " - exception: ", e);
        }
        return null;
    }

    /**
     * Creates JSON array of layer configurations.
     *
     * @param layersArray
     * @param user
     * @param lang
     * @param viewID
     * @param viewType
     * @param bundleIds
     * @param useDirectURLForMyplaces
     * @param modifyURLs              false to keep urls as is, true to modify them for easier proxy forwards
     * @param forceProxy              false to keep urls as is, true to proxy all layers
     * @param plugins
     * @return
     */
    public static JSONArray getFullLayerConfig(final JSONArray layersArray,
                                               final User user, final String lang, final long viewID,
                                               final String viewType, final Set<String> bundleIds,
                                               final boolean useDirectURLForMyplaces,
                                               final boolean modifyURLs,
                                               final String mapSRS,
                                               final boolean forceProxy,
                                               final JSONArray plugins) {

        // Create a list of layer ids
        final List<Integer> layerIdList = new ArrayList<>();
        final List<Long> publishedMyPlaces = new ArrayList<Long>();
        final List<Long> publishedAnalysis = new ArrayList<Long>();
        final List<Long> publishedUserLayers = new ArrayList<Long>();

        for (int i = 0; i < layersArray.length(); i++) {
            String layerId = null;
            try {
                final JSONObject layer = layersArray.getJSONObject(i);
                layerId = layer.getString(KEY_ID);
                if (layerId == null || layerIdList.contains(layerId)) {
                    continue;
                }
                // special handling for myplaces and analysis layers
                if (layerId.startsWith(PREFIX_MYPLACES)) {
                    final long categoryId =
                            ConversionHelper.getLong(layerId.substring(PREFIX_MYPLACES.length()), -1);
                    if (categoryId != -1) {
                        publishedMyPlaces.add(categoryId);
                    } else {
                        LOGGER.warn("Found my places layer in selected. Error parsing id with category id: ", layerId);
                    }
                } else if (layerId.startsWith(PREFIX_ANALYSIS)) {
                    final long categoryId = AnalysisHelper.getAnalysisIdFromLayerId(layerId);
                    if (categoryId != -1) {
                        publishedAnalysis.add(categoryId);
                    } else {
                        LOGGER.warn("Found analysis layer in selected. Error parsing id with category id: ", layerId);
                    }
                } else if (layerId.startsWith(PREFIX_USERLAYERS)) {
                    final long userLayerId = ConversionHelper
                            .getLong(layerId.substring(PREFIX_USERLAYERS.length()), -1);
                    if (userLayerId != -1) {
                        publishedUserLayers.add(userLayerId);
                    } else {
                        LOGGER.warn("Found user layer in selected. Error parsing id with prefixed id: ", layerId);
                    }
                } else {
                    int id = ConversionHelper.getInt(layerId, -1);
                    if (id != -1) {
                        // these should all be pointing at a layer in oskari_maplayer
                        layerIdList.add(id);
                    }
                }
            } catch (JSONException je) {
                LOGGER.error(je, "Problem handling layer id:", layerId, "skipping it!.");
            }
        }

        final List<OskariLayer> layers = mapLayerService.findByIdList(layerIdList);
        if (forceProxy) {
            layers.forEach(lyr -> {
                if (lyr.getType().equals(OskariLayer.TYPE_3DTILES)) {
                    // Proxy is not supported for 3D layers
                    return;
                }
                JSONObject attributes = lyr.getAttributes();
                if (attributes == null) {
                    attributes = new JSONObject();
                }
                JSONHelper.putValue(attributes, "forceProxy", forceProxy);
                lyr.setAttributes(attributes);
            });
        }

        final JSONObject struct = OskariLayerWorker.getListOfMapLayers(
                layers, user, lang, mapSRS, ViewTypes.PUBLISHED.equals(viewType), modifyURLs);

        if (struct.isNull(KEY_LAYERS)) {
            LOGGER.warn("getSelectedLayersStructure did not return layers when expanding:",
                    layerIdList);
        }

        // construct layers JSON
        final JSONArray prefetch = getLayersArray(struct);
        appendMyPlacesLayers(prefetch, publishedMyPlaces, user, viewID, lang, bundleIds, useDirectURLForMyplaces, modifyURLs, plugins);
        appendAnalysisLayers(prefetch, publishedAnalysis, user, viewID, lang, bundleIds, useDirectURLForMyplaces, modifyURLs);
        appendUserLayers(prefetch, publishedUserLayers, user, viewID, bundleIds, mapSRS);
        return prefetch;
    }

    private static void appendAnalysisLayers(final JSONArray layerList,
                                             final List<Long> publishedAnalysis,
                                             final User user,
                                             final long viewID,
                                             final String lang,
                                             final Set<String> bundleIds,
                                             final boolean useDirectURL,
                                             final boolean modifyURLs) {

        final boolean analyseBundlePresent = bundleIds.contains(BUNDLE_ANALYSE);
        final Set<String> permissions = permissionsService.getResourcesWithGrantedPermissions(
                AnalysisLayer.TYPE, user, PermissionType.VIEW_PUBLISHED.name());
        LOGGER.debug("Analysis layer permissions for published view", permissions);

        for (Long id : publishedAnalysis) {
            final Analysis analysis = analysisService.getAnalysisById(id);
            if(analysis == null){
                continue;
            }
            if (analyseBundlePresent && analysis.isOwnedBy(user.getUuid())) {
                // skip it's an own bundle and analysis bundle is present -> will be loaded via analysisbundle
                continue;
            }
            final String permissionKey = "analysis+" + id;
            boolean containsKey = permissions.contains(permissionKey);
            if (!containsKey) {
                LOGGER.info("Found analysis layer in selected that is no longer published. ViewID:",
                        viewID, "Analysis id:", id);
                continue;
            }
            final JSONObject json = AnalysisHelper.getlayerJSON(analysis, lang,
                    useDirectURL, user.getUuid(), modifyURLs);
            if (json != null) {
                layerList.put(json);
            }
        }
    }

    private static boolean isMyplacesBundlePresent(Set<String> bundleIdList) {
        for(String bundleId : BUNDLES_HANDLING_MYPLACES_LAYERS) {
            if(bundleIdList.contains(bundleId)) {
                return true;
            }
        }
        return false;
    }

    private static void appendMyPlacesLayers(final JSONArray layerList,
                                             final List<Long> publishedMyPlaces,
                                             final User user,
                                             final long viewID,
                                             final String lang,
                                             final Set<String> bundleIds,
                                             final boolean useDirectURL,
                                             final boolean modifyURLs,
                                             final JSONArray plugins) {
        if (publishedMyPlaces.isEmpty()) {
            return;
        }
        final boolean myPlacesBundlePresent = isMyplacesBundlePresent(bundleIds);
        // get myplaces categories from service and generate layer jsons
        final String uuid = user.getUuid();
        final List<MyPlaceCategory> myPlacesLayers = myPlaceService
                .getMyPlaceLayersById(publishedMyPlaces);

        for (MyPlaceCategory mpLayer : myPlacesLayers) {
            if (!mpLayer.isPublished() && !mpLayer.isOwnedBy(uuid)) {
                LOGGER.info("Found my places layer in selected that is no longer published. ViewID:",
                        viewID, "Myplaces layerId:", mpLayer.getId());
                // no longer published -> skip if isn't current users layer
                continue;
            }
            if (myPlacesBundlePresent && mpLayer.isOwnedBy(uuid)) {
                // if the layer is users own -> myplaces2 bundle handles it
                // so if myplaces2 is present we must skip the users layers
                continue;
            }

            JSONObject myPlaceLayer = null;
            if (plugins != null && plugins.toString().indexOf(PLUGIN_WFSVECTORLAYER) != -1) {
                myPlaceLayer = myPlaceService.getCategoryAsWfsLayerJSON(mpLayer, lang);
            } else {
                myPlaceLayer = myPlaceService.getCategoryAsWmsLayerJSON(
                        mpLayer, lang, useDirectURL, user.getUuid(), modifyURLs);
            }
            if (myPlaceLayer != null) {
                layerList.put(myPlaceLayer);
            }
        }
    }

    private static void appendUserLayers(final JSONArray layerList,
                                         final List<Long> publishedUserLayers,
                                         final User user,
                                         final long viewID,
                                         final Set<String> bundleIds,
                                         final String mapSrs) {
        final boolean userLayersBundlePresent = bundleIds.contains(BUNDLE_MYPLACESIMPORT);
        final OskariLayer baseLayer = userLayerDataService.getBaseLayer();
        for (Long id : publishedUserLayers) {
            final UserLayer userLayer = userLayerService.getUserLayerById(id);

            if (userLayer == null) {
                LOGGER.warn("Unable to find published user layer with id", id);
                continue;
            }

            if (userLayersBundlePresent && userLayer.isOwnedBy(user.getUuid())) {
                // skip if it's an own layer and myplacesimport bundle is present ->
                // will be loaded via aforementioned bundle
                continue;
            }

            if (!userLayer.isPublished() && !userLayer.isOwnedBy(user.getUuid())) {
                LOGGER.info("Found user layer in selected that is no longer published. ViewID:",
                        viewID, "User layer id:", userLayer.getId());
                // no longer published -> skip if isn't current users layer
                continue;
            }

            final JSONObject json = userLayerDataService.parseUserLayer2JSON(userLayer, baseLayer, mapSrs);
            if (json != null) {
                layerList.put(json);
            }
        }
    }

    private static JSONArray getLayersArray(final JSONObject struct) {
        try {
            final Object layers = struct.get(KEY_LAYERS);
            if (layers instanceof JSONArray) {
                return (JSONArray) layers;
            } else if (layers instanceof JSONObject) {
                final JSONArray list = new JSONArray();
                list.put(layers);
                return list;
            } else {
                LOGGER.error("getSelectedLayersStructure returned garbage layers.");
            }
        } catch (JSONException jsonex) {
            LOGGER.error("Could not set prefetch layers.");
        }
        return new JSONArray();
    }

    private void copySelectedLayersToConfigLayers(final JSONArray mfConfigLayers,
                                                  final JSONArray mfStateLayers) {
        for (int i = 0; i < mfStateLayers.length(); i++) {
            String stateLayerId = null;
            String confLayerId = null;
            JSONObject stateLayer = null;
            JSONObject confLayer = null;
            try {
                boolean inConfigLayers = false;
                stateLayer = mfStateLayers.getJSONObject(i);
                stateLayerId = stateLayer.getString(KEY_ID);

                for (int j = 0; j < mfConfigLayers.length(); j++) {
                    confLayer = mfConfigLayers.getJSONObject(j);
                    confLayerId = confLayer.getString(KEY_ID);
                    if (stateLayerId.equals(confLayerId)) {
                        inConfigLayers = true;
                    }
                }
                if (!inConfigLayers) {
                    mfConfigLayers.put(stateLayer);
                }
            } catch (JSONException je) {
                LOGGER.error(je, "Problem comparing layers - StateLayerId:",
                        stateLayerId, "vs confLayerId:", confLayerId);
            }
        }
    }

    public static JSONObject getPlugin(final String pluginClassName,
                                       final JSONObject mapfullConfig) {

        if (mapfullConfig == null || !mapfullConfig.has(KEY_PLUGINS)) {
            return null;
        }
        final JSONArray plugins = mapfullConfig.optJSONArray(KEY_PLUGINS);
        for (int i = 0; i < plugins.length(); i++) {
            final JSONObject plugin = plugins.optJSONObject(i);
            if (plugin == null || !plugin.has(KEY_ID)) {
                continue;
            }
            if (pluginClassName.equals(plugin.optString(KEY_ID))) {
                LOGGER.debug(pluginClassName, "plugin found at index:", i);
                return plugin;
            }
        }
        return null;
    }

    private void removeMyLocationPluginAutoCenter(final JSONObject mapfullConfig) {
        JSONObject plugin = getPlugin(PLUGIN_MYLOCATION, mapfullConfig);
        if (plugin == null) {
            return;
        }
        JSONObject config = plugin.optJSONObject(KEY_CONFIG);
        if(config != null && config.has(KEY_CENTER_MAP_AUTOMATICALLY)) {
            config.remove(KEY_CENTER_MAP_AUTOMATICALLY);
        }
    }

    private void removePlugin(final String pluginClassName,
                              final JSONObject mapfullConfig) {

        if (mapfullConfig == null || !mapfullConfig.has(KEY_PLUGINS)) {
            return;
        }
        final JSONArray plugins = mapfullConfig.optJSONArray(KEY_PLUGINS);
        for (int i = 0; i < plugins.length(); i++) {
            final JSONObject plugin = plugins.optJSONObject(i);
            if (plugin == null || !plugin.has(KEY_ID)) {
                continue;
            }
            if (pluginClassName.equals(plugin.optString(KEY_ID))) {
                LOGGER.debug(pluginClassName, "plugin found at index:", i, "- removing it");
                plugins.remove(i);
                break;
            }
        }
    }

    private void killLayerSelectionPlugin(final JSONObject mapfullConfig) {
        LOGGER.debug("[killLayerSelectionPlugin] removing layer selection plugin");
        try {
            final JSONArray plugins = mapfullConfig.getJSONArray(KEY_PLUGINS);
            for (int i = 0; i < plugins.length(); i++) {
                JSONObject plugin = plugins.getJSONObject(i);
                if (!plugin.has(KEY_ID) || !plugin.has(KEY_CONFIG)) {
                    continue;
                }
                String id = plugin.getString(KEY_ID);
                LOGGER.debug("[killLayerSelectionPlugin] got plugin " + id);
                if (!id.equals(PLUGIN_LAYERSELECTION)) {
                    continue;
                }
                JSONObject config = plugin.getJSONObject(KEY_CONFIG);
                LOGGER.debug("[killLayerSelectionPlugin] got config");
                if (!config.has(KEY_BASELAYERS)) {
                    continue;
                }
                JSONArray bl = config.getJSONArray(KEY_BASELAYERS);
                if (bl.length() < 2) {
                    LOGGER.debug("[killLayerSelectionPlugin] "
                            + "layercount < 2, removing plugin");
                    plugins.remove(i--);
                    LOGGER.info("[killLayerSelectionPlugin] " + "Removed "
                            + PLUGIN_LAYERSELECTION
                            + "as layercount < 2 and oldId > 0");

                }
            }
        } catch (JSONException jsonex) {
            LOGGER.error("Problem trying to figure out whether "
                    + PLUGIN_LAYERSELECTION + " should be removed.", jsonex);
        }
    }

    void epsgInit() {

        try {
            InputStream inp = this.getClass().getResourceAsStream(EPSG_PROJ4_FORMATS);
            if (inp != null) {
                InputStreamReader reader = new InputStreamReader(inp, "UTF-8");
                JSONTokener tokenizer = new JSONTokener(reader);
                this.epsgMap = JSONHelper.createJSONObject4Tokener(tokenizer);
            }
        } catch (Exception e) {
            LOGGER.info("No setup for epsg proj4 formats found", e);
        }
    }

    void svgInit(){

    }
}
