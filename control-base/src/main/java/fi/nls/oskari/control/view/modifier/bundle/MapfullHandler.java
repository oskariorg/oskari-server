package fi.nls.oskari.control.view.modifier.bundle;

import fi.mml.map.mapwindow.util.OskariLayerWorker;
import fi.nls.oskari.annotation.OskariViewModifier;
import org.oskari.user.User;
import org.oskari.util.ObjectMapperProvider;

import fi.nls.oskari.domain.map.MyPlaceCategory;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.domain.map.myfeatures.MyFeaturesLayer;
import fi.nls.oskari.domain.map.myfeatures.MyFeaturesLayerInfo;
import fi.nls.oskari.domain.map.userlayer.UserLayer;
import fi.nls.oskari.domain.map.view.ViewTypes;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.formatters.LayerJSONFormatter;
import fi.nls.oskari.myplaces.MyPlacesService;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.view.modifier.ModifierException;
import fi.nls.oskari.view.modifier.ModifierParams;
import fi.nls.oskari.view.modifier.ViewModifier;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.oskari.domain.map.LayerExtendedOutput;
import org.oskari.map.myfeatures.service.MyFeaturesService;
import org.oskari.map.userlayer.service.UserLayerDataService;
import org.oskari.map.userlayer.service.UserLayerDbService;
import org.oskari.service.maplayer.DescribeLayerQuery;
import org.oskari.service.maplayer.LayerProvider;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@OskariViewModifier("mapfull")
public class MapfullHandler extends BundleHandler {

    private static final Logger LOGGER = LogFactory.getLogger(MapfullHandler.class);

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

    private static final String KEY_TERRAIN = "terrain";
    private static final String KEY_TERRAIN_TOKEN = "ionAccessToken";
    private static final String KEY_TERRAIN_ASSET = "ionAssetId";
    private static final String KEY_TERRAIN_URL = "providerUrl";
    private static final int TERRAIN_ASSET = PropertyUtil.getOptional("oskari.map.terrain.asset", -1);
    private static final String TERRAIN_TOKEN = PropertyUtil.getOptional("oskari.map.terrain.token");
    private static final String TERRAIN_URL = PropertyUtil.getOptional("oskari.map.terrain.url");

    private static final String PLUGIN_LAYERSELECTION = "Oskari.mapframework.bundle.mapmodule.plugin.LayerSelectionPlugin";
    private static final String PLUGIN_GEOLOCATION = "Oskari.mapframework.bundle.mapmodule.plugin.GeoLocationPlugin";
    private static final String PLUGIN_MYLOCATION = "Oskari.mapframework.bundle.mapmodule.plugin.MyLocationPlugin";

    public static final String EPSG_PROJ4_FORMATS = "epsg_proj4_formats.json";

    private Collection<LayerProvider> layerProviders;

    private JSONObject epsgMap = null;
    private HashMap<String, PluginHandler> pluginHandlers = null;


    public void init() {
        if (layerProviders == null) {
            Map<String, LayerProvider> components = OskariComponentManager.getComponentsOfType(LayerProvider.class);
            layerProviders = components.values();
        }
        epsgInit();
        pluginHandlers = new HashMap<>();
        // Note! atleast WFSVectorLayerPluginViewModifier is being registered outside this handler
        registerPluginHandler(LogoPluginHandler.PLUGIN_NAME, new LogoPluginHandler());
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
        final String mapSRS = getSRSFromMapConfig(mapfullConfig);
        final boolean forceProxy = mapfullConfig.optBoolean(KEY_FORCE_PROXY, false);
        final JSONArray fullConfigLayers = getFullLayerConfig(mfConfigLayers,
                params.getUser(),
                params.getLocale().getLanguage(),
                params.getViewId(),
                params.getViewType(),
                bundleIds,
                params.isModifyURLs(),
                mapSRS,
                forceProxy);

        setProjDefsForMapConfig(mapfullConfig, mapSRS);
        // overwrite layers
        try {
            mapfullConfig.put(KEY_LAYERS, fullConfigLayers);
        } catch (Exception e) {
            LOGGER.error(e, "Unable to overwrite layers");
        }
        // init terrain profile from properties if given
        setTerrainFromProperties(mapfullConfig);

        // dummyfix: because migration tool added layer selection to all migrated maps
        // remove it from old published maps if only one layer is selected
        if (params.isOldPublishedMap()) {
            this.killLayerSelectionPlugin(mapfullConfig);
        }

        if (params.isLocationModified()) {
            LOGGER.debug("locationModifiedByParams -> disabling GeoLocationPlugin");
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
        if (mapSRS == null) {
            return null;
        }
        String srsUpperCase = mapSRS.toUpperCase();
        if (!this.epsgMap.has(srsUpperCase)) {
            LOGGER.debug("ProjectionDefs not found in epsg_proj4_formats.json", mapSRS);
            return null;
        }
        return JSONHelper.getStringFromJSON(this.epsgMap, srsUpperCase, null);
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
     * @param modifyURLs              false to keep urls as is, true to modify them for easier proxy forwards
     * @param forceProxy              false to keep urls as is, true to proxy all layers
     * @return
     */
    public List<LayerExtendedOutput> getFullLayerConfig(final JSONArray layersArray,
                                               final User user, final String lang, final long viewID,
                                               final String viewType, final Set<String> bundleIds,
                                               final boolean modifyURLs,
                                               final String mapSRS,
                                               final boolean forceProxy) {

        Set<String> layerIds = getLayerIds(layersArray);

        List<LayerExtendedOutput> described = layerProviders.stream()
            .flatMap(provider -> layerIds.stream()
                .filter(provider::maybeProvides)
                .map(layerId -> new DescribeLayerQuery(viewID, layerId, user, lang, mapSRS))
                .map(provider::describeLayer))
            .collect(Collectors.toList());

        final JSONObject struct = OskariLayerWorker.getListOfMapLayers(
                layers, user, lang, mapSRS, ViewTypes.PUBLISHED.equals(viewType), modifyURLs);

        if (struct.isNull(KEY_LAYERS)) {
            LOGGER.warn("getSelectedLayersStructure did not return layers when expanding:",
                    layerIdList);
        }

        final JSONArray prefetch = getLayersArray(struct);
        appendMyPlacesLayers(prefetch, publishedMyPlaces, user, viewID, lang, bundleIds, mapSRS);
        appendUserLayers(prefetch, publishedUserLayers, user, viewID, lang, bundleIds, mapSRS);
        appendMyFeaturesLayers(prefetch, publishedMyFeatures, user, viewID, lang, bundleIds, mapSRS);
        return prefetch;
    }

    private List<

    private static Set<String> getLayerIds(final JSONArray layersArray) {
        Set<String> layerIds = new HashSet<>();
        for (int i = 0; i < layersArray.length(); i++) {
            JSONObject layer = layersArray.optJSONObject(i);
            if (layer != null) {
                String layerId = layer.optString(KEY_ID);
                if (layerId != null) {
                    layerIds.add(layerId);
                }
            }
        }
        return layerIds;
    } 

    private void copySelectedLayersToConfigLayers(final JSONArray mfConfigLayers,
                                                  final JSONArray mfStateLayers) {
        if (mfStateLayers.isEmpty()) {
            return;
        }
        Set<String> layerIds = getLayerIds(mfConfigLayers);
        for (int i = 0; i < mfStateLayers.length(); i++) {
            JSONObject stateLayer = mfStateLayers.optJSONObject(i);
            if (stateLayer == null) {
                continue;
            }
            String stateLayerId = stateLayer.optString(KEY_ID);
            if (stateLayerId == null) {
                continue;
            }
            if (layerIds.add(stateLayerId)) {
                mfConfigLayers.put(stateLayer);
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
                String id = plugin.optString(KEY_ID);
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
    private void setTerrainFromProperties (final JSONObject mapfullConfig) {
        if (TERRAIN_URL == null && TERRAIN_TOKEN == null) {
            return;
        }
        JSONObject options = mapfullConfig.optJSONObject(KEY_MAP_OPTIONS);
        if (options == null) {
            options = new JSONObject();
            JSONHelper.putValue(mapfullConfig, KEY_MAP_OPTIONS, options);
        }
        JSONObject terrain = new JSONObject();
        if (TERRAIN_URL != null) {
            JSONHelper.putValue(terrain, KEY_TERRAIN_URL, TERRAIN_URL);
        }
        if (TERRAIN_TOKEN != null) {
            JSONHelper.putValue(terrain, KEY_TERRAIN_TOKEN, TERRAIN_TOKEN);
            if (TERRAIN_ASSET != -1) {
                JSONHelper.putValue(terrain, KEY_TERRAIN_ASSET, TERRAIN_ASSET);
            }
        }
        JSONHelper.putValue(options, KEY_TERRAIN, terrain);
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
}
