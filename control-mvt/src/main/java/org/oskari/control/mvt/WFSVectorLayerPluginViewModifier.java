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

import java.util.HashMap;

/**
 *  modifier for WfsVectorLayerPlugin config
 */
public class WFSVectorLayerPluginViewModifier implements PluginHandler {

    private static final Logger LOGGER = LogFactory.getLogger(WFSVectorLayerPluginViewModifier.class);
    public static final String PLUGIN_NAME = "Oskari.wfsvector.WfsVectorLayerPlugin";
    public static final String KEY_ID = "id";
    public static final String KEY_CONFIG = "config";

    private HashMap<String, WFSTileGrid> tileGrids = new HashMap<>();
    private HashMap<String, Integer> minZoomLevels = new HashMap<>();

    public void setMinZoomLevelForSRS(String srsName, int minZoomLevel) {
        minZoomLevels.put(srsName.toUpperCase(), minZoomLevel);
    }
    public void setTileGridForSRS(String srsName, WFSTileGrid tileGrid) {
        tileGrids.put(srsName.toUpperCase(), tileGrid);
    }

    @Override
    public boolean modifyPlugin(final JSONObject plugin,
                                final ModifierParams params,
                                final String mapSrs) {
        return setupPluginConfig(plugin, mapSrs);
    }

    private boolean setupPluginConfig(JSONObject plugin, String mapSrs) {
        if(plugin == null) {
            LOGGER.debug("Tried to modify WfsVectorLayerPlugin, but plugin didn't exist!");
            return false;
        }
        if(!PLUGIN_NAME.equals(plugin.optString(KEY_ID))) {
            LOGGER.debug("Tried to modify WfsVectorLayerPlugin, but given JSON isn't WfsVectorLayerPlugin!");
            return false;
        }
        if (mapSrs == null) {
            LOGGER.debug("Tried to modify WfsVectorLayerPlugin, but map has no srsName!");
            return false;
        }

        JSONObject config = getConfig(plugin);

        Integer minZoomLevel = this.minZoomLevels.get(mapSrs.toUpperCase());
        if (minZoomLevel != null) {
            JSONHelper.putValue(config, "minZoomLevel", minZoomLevel);
        }

        WFSTileGrid tileGrid = tileGrids.get(mapSrs.toUpperCase());
        if (tileGrid != null) {
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
        }

        return true;
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
