package org.oskari.control.mvt;

import fi.nls.oskari.control.view.modifier.bundle.PluginHandler;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.view.modifier.ModifierParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.oskari.service.mvt.WFSTileGrid;
import org.oskari.service.mvt.WFSTileGridProperties;

/**
 *  modifier for WfsVectorLayerPlugin config
 */
public class WFSVectorLayerPluginViewModifier implements PluginHandler {

    private static final Logger LOGGER = LogFactory.getLogger(WFSVectorLayerPluginViewModifier.class);
    public static final String PLUGIN_NAME = "Oskari.wfsvector.WfsVectorLayerPlugin";
    public static final String KEY_ID = "id";
    public static final String KEY_CONFIG = "config";

    private WFSTileGridProperties tileGridProperties;

    public WFSVectorLayerPluginViewModifier() {
        tileGridProperties = new WFSTileGridProperties();
    }

    @Override
    public boolean modifyPlugin(final JSONObject plugin,
                                final ModifierParams params,
                                final String mapSrs) {
        return setupPluginConfig(plugin, mapSrs);
    }

    public boolean setupPluginConfig(JSONObject plugin, String mapSrs) {
        if(plugin == null) {
            LOGGER.debug("Tried to modify WfsVectorLayerPlugin, but plugin didn't exist!");
            return false;
        }
        if(!PLUGIN_NAME.equals(plugin.optString(KEY_ID))) {
            LOGGER.debug("Tried to modify WfsVectorLayerPlugin, but given JSON isn't WfsVectorLayerPlugin!");
            return false;
        }

        WFSTileGrid tileGrid = tileGridProperties.getTileGrid(mapSrs);
        if (tileGrid == null) {
            return false;
        }
        JSONObject config = getConfig(plugin);
        JSONArray resolutionArray = new JSONArray();
        try {
            for (double resolution : tileGrid.getResolutions()) {
                resolutionArray.put(resolution);
            }
        }
        catch (JSONException ex) {
            LOGGER.debug("Tried to modify WfsVectorLayerPlugin, but could not create resolution array", ex);
            return false;
        }
        JSONHelper.put(config, "resolutions", resolutionArray);
        JSONHelper.putValue(config, "tileSize", tileGrid.getTileSize());
        JSONHelper.putValue(config, "origin", tileGrid.getOrigin());

        return false;
    }

    private JSONObject getConfig(JSONObject original) {
        JSONObject config = original.optJSONObject(KEY_CONFIG);
        if(config == null) {
            config = new JSONObject();
            JSONHelper.putValue(original, KEY_CONFIG, config);
        }
        return config;
    }


}
