package fi.nls.oskari.control.view.modifier.bundle;

import java.util.ArrayList;
import java.util.List;

import fi.mml.map.mapwindow.util.OskariLayerWorker;
import fi.nls.oskari.annotation.OskariViewModifier;
import fi.nls.oskari.domain.Role;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.view.modifier.ModifierException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fi.mml.map.mapwindow.service.db.MyPlacesService;
import fi.mml.map.mapwindow.service.db.MyPlacesServiceIbatisImpl;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.MyPlaceCategory;
import fi.nls.oskari.domain.map.view.ViewTypes;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.view.modifier.ModifierParams;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;

@OskariViewModifier("mapfull")
public class MapfullHandler extends BundleHandler {

    private static final Logger log = LogFactory.getLogger(MapfullHandler.class);
    private static final String KEY_LAYERS = "layers";
    private static final String KEY_SEL_LAYERS = "selectedLayers";
    private static final String KEY_ID = "id";

    private static final String KEY_USER = "user";
    private static final String KEY_FIRSTNAME = "firstName";
    private static final String KEY_LASTNAME = "lastName";
    private static final String KEY_LOGINNAME = "loginName";
    private static final String KEY_NICKNAME = "nickName";
    private static final String KEY_USERUUID = "userUUID";
    private static final String KEY_USERID = "userID";


    private final static String KEY_ROLE_ID = "id";
    private final static String KEY_ROLE_NAME = "name";
    private final static String KEY_ROLES = "roles";

    
    private static final String KEY_PLUGINS = "plugins";
    public static final String KEY_CONFIG = "config";
    private static final String KEY_BASELAYERS = "baseLayers";

    private static final String PREFIX_MYPLACES = "myplaces_";

    private static final String PLUGIN_LAYERSELECTION = "Oskari.mapframework.bundle.mapmodule.plugin.LayerSelectionPlugin";
    private static final String PLUGIN_GEOLOCATION = "Oskari.mapframework.bundle.mapmodule.plugin.GeoLocationPlugin";
    public static final String PLUGIN_SEARCH = "Oskari.mapframework.bundle.mapmodule.plugin.SearchPlugin";

    private static final MyPlacesService myPlaceService = new MyPlacesServiceIbatisImpl();

    public boolean modifyBundle(final ModifierParams params) throws ModifierException {

        final JSONObject mapfullConfig = getBundleConfig(params.getConfig());
        final JSONObject mapfullState = getBundleState(params.getConfig());
        
        if(mapfullConfig == null) {
            return false;
        }
        // setup correct ajax url
        final String ajaxUrl = mapfullConfig.optString("globalMapAjaxUrl");
        try {
            // fix ajaxurl to current community if possible 
            // (required to show correct help articles)
            mapfullConfig.put("globalMapAjaxUrl", params.getBaseAjaxUrl());
            log.debug("Replaced ajax url: ", ajaxUrl, "->", params.getBaseAjaxUrl());
        } catch (Exception e) {
            log.error(e, "Replacing ajax url failed: ", ajaxUrl, "- Parsed:",
                    params.getBaseAjaxUrl());
        }

        // setup user data
        populateUserData(mapfullConfig, params.getUser());

        // Any layer referenced in state.selectedLayers array NEEDS to 
        // be in conf.layers otherwise it cant be added to map on startup
        final JSONArray mfConfigLayers = JSONHelper.getEmptyIfNull(mapfullConfig.optJSONArray(KEY_LAYERS));
        final JSONArray mfStateLayers = JSONHelper.getEmptyIfNull(mapfullState.optJSONArray(KEY_SEL_LAYERS));
        copySelectedLayersToConfigLayers(mfConfigLayers, mfStateLayers);

        final boolean isMyplacesPresent = isBundlePresent(params.getStartupSequence(), BUNDLE_MYPLACES2);
        final boolean useDirectURLForMyplaces = false;
        final JSONArray fullConfigLayers = getFullLayerConfig(mfConfigLayers,
        		params.getUser(), 
        		params.getLocale().getLanguage(), 
        		params.getClientIP(),  
        		params.getViewId(), 
        		params.getViewType(),
                isMyplacesPresent,
                useDirectURLForMyplaces,
        		params.isModifyURLs());
        
        // overwrite layers
        try {
            mapfullConfig.put(KEY_LAYERS, fullConfigLayers);
        } catch (Exception e) {
            log.error(e, "Unable to overwrite layers");
        }
        
        // dummyfix: because migration tool added layer selection to all migrated maps
        // remove it from old published maps if only one layer is selected
        if(params.isOldPublishedMap()) {
            this.killLayerSelectionPlugin(mapfullConfig);
        }

        if (params.isLocationModified()) {
            log.info("locationModifiedByParams -> disabling GeoLocationPlugin");
            removePlugin(PLUGIN_GEOLOCATION, mapfullConfig);
        }
        return false;
    }

    public static JSONArray getFullLayerConfig(final JSONArray layersArray,
                                               final User user, final String lang,
                                               final String clientIP, final long viewID,
                                               final String viewType, final boolean isMyplacesBundleLoaded,
                                               final boolean useDirectURLForMyplaces) {
        return getFullLayerConfig(layersArray, user, lang, clientIP, viewID, viewType, isMyplacesBundleLoaded, useDirectURLForMyplaces, false);
    }

    /**
     * Creates JSON array of layer configurations.
     * @param layersArray
     * @param user
     * @param lang
     * @param clientIP
     * @param viewID
     * @param viewType
     * @param isMyplacesBundleLoaded
     * @param useDirectURLForMyplaces
     * @param modifyURLs false to keep urls as is, true to modify them for easier proxy forwards
     * @return
     */
    public static JSONArray getFullLayerConfig(final JSONArray layersArray,
    		final User user, final String lang, 
    		final String clientIP, final long viewID, 
    		final String viewType, final boolean isMyplacesBundleLoaded,
            final boolean useDirectURLForMyplaces,
            final boolean modifyURLs) {

        // Create a list of layer ids
        final List<String> layerIdList = new ArrayList<String>();
        final List<Long> publishedMyPlaces = new ArrayList<Long>();

        for (int i = 0; i < layersArray.length(); i++) {
            String layerId = null;
            try {
                final JSONObject layer = layersArray.getJSONObject(i);
                layerId = layer.getString(KEY_ID);
                if (layerId == null || layerIdList.contains(layerId)) {
                    continue;
                }
                if (layerId.toLowerCase().startsWith("base_")) {
                    layerIdList.add(layerId);
                } else if (layerId.startsWith(PREFIX_MYPLACES)) {
                    final long categoryId = 
                            ConversionHelper.getLong(layerId.substring(PREFIX_MYPLACES.length()), -1);
                    if (categoryId != -1) {
                        publishedMyPlaces.add(categoryId);
                    } else {
                        log.warn("Found my places layer in selected. Error parsing id with category id: ",
                                layerId);
                    }
                } else if (ConversionHelper.getLong(layerId, -1) != -1) {
                    // these should all be numbers since base_ and myplaces_ are already handled
                    layerIdList.add(layerId);
                }
            } catch (JSONException je) {
                log.error(je, "Problem handling layer id:", layerId, "skipping it!.");
            }
        }

        final JSONObject struct = OskariLayerWorker.getListOfMapLayersById(
                layerIdList,user, lang, ViewTypes.PUBLISHED.equals(viewType), modifyURLs);

        if (struct.isNull(KEY_LAYERS)) {
            log.warn("getSelectedLayersStructure did not return layers when expanding:",
                    layerIdList);
        }

        // construct layers JSON
        final JSONArray prefetch = getLayersArray(struct);
        appendMyPlacesLayers(prefetch, publishedMyPlaces, user, viewID, isMyplacesBundleLoaded, useDirectURLForMyplaces, modifyURLs);
        return prefetch;
    }
    
    private static void appendMyPlacesLayers(final JSONArray layerList,
            final List<Long> publishedMyPlaces,
            final User user,
            final long viewID,
            final boolean skipOwnMyPlacesLayers,
            final boolean useDirectURL,
            final boolean modifyURLs) {
        if (publishedMyPlaces.isEmpty()) {
            return;
        }
        // get myplaces categories from service and generate layer jsons
        final String uuid = user.getUuid();
        final List<MyPlaceCategory> myPlacesLayers = myPlaceService
                .getMyPlaceLayersById(publishedMyPlaces);

        for (MyPlaceCategory mpLayer : myPlacesLayers) {
            if (mpLayer.getPublisher_name() == null
                    && !mpLayer.getUuid().equals(uuid)) {
                log.info("Found my places layer in selected that is no longer published. ViewID:",
                		viewID, "Myplaces layerId:", mpLayer.getId());
                // no longer published -> skip if isn't current users layer
                continue;
            }
            if (skipOwnMyPlacesLayers && mpLayer.getUuid().equals(uuid)) {
                // if the layer is users own -> myplaces2 bundle handles it
                // so if myplaces2 is present we must skip the users layers
                continue;
            }

            final JSONObject myPlaceLayer = getMyPlacesJSON(mpLayer, useDirectURL, user.getUuid(), modifyURLs);
            if(myPlaceLayer != null) {
                layerList.put(myPlaceLayer);
            }
        }
    }

    private static JSONObject getMyPlacesJSON(final MyPlaceCategory mpLayer,
            final boolean useDirectURL, final String uuid,
            final boolean modifyURLs) {
        try {
            final JSONObject myPlaceLayer = new JSONObject();
            myPlaceLayer.put("wmsName", "ows:my_places_categories");
            //myPlaceLayer.put("descriptionLink", "");
            myPlaceLayer.put("type", "wmslayer");
            myPlaceLayer.put("formats",
                    new JSONObject().put("value", "text/html"));
            myPlaceLayer.put("isQueryable", true);
            myPlaceLayer.put("opacity", "50");

            JSONObject options = new JSONObject();
            JSONHelper.putValue(options, "singleTile", true);
            //options.put("transitionEffect", JSONObject.NULL);
            JSONHelper.putValue(myPlaceLayer, "options", options);

            myPlaceLayer.put("metaType", "published");
            // if useDirectURL -> geoserver URL
            // TODO: check "modifyURLs" and prefix wmsurl if true
            if(useDirectURL) {
                myPlaceLayer.put("wmsUrl", PropertyUtil.get("myplaces.wms.url") +
                		"(uuid='" + uuid + "'+OR+publisher_name+IS+NOT+NULL)+AND+category_id=" + mpLayer.getId());
            }
            else {
                myPlaceLayer.put("wmsUrl", "/karttatiili/myplaces?myCat="
                        + mpLayer.getId() + "&");
            }
            myPlaceLayer.put("name", mpLayer.getCategory_name());
            myPlaceLayer.put("subtitle", mpLayer.getPublisher_name());
            myPlaceLayer.put("id", "myplaces_" + mpLayer.getId());
            return myPlaceLayer;
        } catch (JSONException e) {
            log.warn(e, "Error populating myplaces layer");
        }
        return null;
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
                log.error("getSelectedLayersStructure returned garbage layers.");
            }
        } catch (JSONException jsonex) {
            log.error("Could not set prefetch layers.");
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
                log.error(je, "Problem comparing layers - StateLayerId:",
                        stateLayerId, "vs confLayerId:", confLayerId);
            }
        }
    }

    private void populateUserData(final JSONObject mapfullConfig, final User user) {

        try {
            JSONObject userData = new JSONObject();
            userData.put(KEY_FIRSTNAME, user.getFirstname());
            userData.put(KEY_LASTNAME, user.getLastname());
            userData.put(KEY_LOGINNAME, user.getEmail());
            userData.put(KEY_NICKNAME, user.getScreenname());
            userData.put(KEY_USERUUID, user.getUuid());
            userData.put(KEY_USERID, user.getId());

            populateUserRoles(userData, user);

            mapfullConfig.put(KEY_USER, userData);
        } catch (JSONException jsonex) {
            log.warn("Unable to populate user data:", user);
        }
    }

    private void populateUserRoles(final JSONObject userData, final User user) {
        try {
            JSONArray userRoles = new JSONArray();

            for (Role role: user.getRoles()) {
                JSONObject roleData = new JSONObject();
                roleData.put(KEY_ROLE_ID, role.getId());
                roleData.put(KEY_ROLE_NAME, role.getName());
                userRoles.put(roleData);
            }
            userData.put(KEY_ROLES, userRoles);

        } catch (JSONException jsonex) {
            log.warn("Unable to populate user data:", user);
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
                log.debug(pluginClassName, "plugin found at index:", i);
                return plugin;
            }
        }
        return null;
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
                log.debug(pluginClassName, "plugin found at index:", i, "- removing it");
                plugins.remove(i);
                break;
            }
        }
    }

    private void killLayerSelectionPlugin(final JSONObject mapfullConfig) {
        log.debug("[killLayerSelectionPlugin] removing layer selection plugin");
        try {
            final JSONArray plugins = mapfullConfig.getJSONArray(KEY_PLUGINS);
            for (int i = 0; i < plugins.length(); i++) {
                JSONObject plugin = plugins.getJSONObject(i);
                if (!plugin.has(KEY_ID) || !plugin.has(KEY_CONFIG)) {
                    continue;
                }
                String id = plugin.getString(KEY_ID);
                log.debug("[killLayerSelectionPlugin] got plugin " + id);
                if (!id.equals(PLUGIN_LAYERSELECTION)) {
                    continue;
                }
                JSONObject config = plugin.getJSONObject(KEY_CONFIG);
                log.debug("[killLayerSelectionPlugin] got config");
                if (!config.has(KEY_BASELAYERS)) {
                    continue;
                }
                JSONArray bl = config.getJSONArray(KEY_BASELAYERS);
                if (bl.length() < 2) {
                    log.debug("[killLayerSelectionPlugin] "
                            + "layercount < 2, removing plugin");
                    plugins.remove(i--);
                    log.info("[killLayerSelectionPlugin] " + "Removed "
                            + PLUGIN_LAYERSELECTION
                            + "as layercount < 2 and oldId > 0");

                }
            }
        } catch (JSONException jsonex) {
            log.error("Problem trying to figure out whether "
                    + PLUGIN_LAYERSELECTION + " should be removed.", jsonex);
        }
    }    
}
