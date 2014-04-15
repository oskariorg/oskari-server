package fi.nls.oskari.control.view.modifier.param;

import fi.nls.oskari.annotation.OskariViewModifier;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.view.modifier.ModifierException;
import fi.nls.oskari.view.modifier.ModifierParams;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.StringWriter;

/**
 * Handles the markers parameter. Writes MarkersPlugin state if the plugin is part of the view.
 *  markers=shape|size|hexcolor|x_y|User input text___shape|size|hexcolor|x_y|input 2
 * For example and url with "markers=1|2|FF0000|6819589_327545|test" will produce the following state object for markers plugin:
 *
 *  {
 *    "id":"Oskari.mapframework.mapmodule.MarkersPlugin",
 *    "state":{
 *      "markers":[{"color":"FF0000","shape":"1","msg":"test","y":327545,"x":6819589,"size":"2"}]
 *    }
 *  }
 */
@OskariViewModifier("markers")
public class MarkersParamHandler extends ParamHandler {

    private static final Logger log = LogFactory.getLogger(MarkersParamHandler.class);

    // defaults
    private final static String DEFAULT_SHAPE = "2";
    private final static String DEFAULT_SIZE = "3";
    private final static String DEFAULT_COLOR = "ffde00";

    // link parameter value separators
    private static final String MARKER_SEPARATOR = "___";
    private static final String FIELD_SEPARATOR = "|";
    private static final String COORD_SEPARATOR = "_";

    private static final String KEY_MARKERS = "markers";
    // config marker keys
    private static final String KEY_SHAPE = "shape";
    private static final String KEY_SIZE = "size";
    private static final String KEY_COLOR = "color";
    private static final String KEY_MESSAGE = "msg";
    private static final String KEY_X = "x";
    private static final String KEY_Y = "y";

    // for parsing mapfull config
    private static final String MARKERSPLUGIN_ID = "Oskari.mapframework.mapmodule.MarkersPlugin";
    private static final String KEY_PLUGINS = "plugins";
    private static final String KEY_ID = "id";

    public boolean handleParam(final ModifierParams params) throws ModifierException {
        if(params.getParamValue() == null) {
            return false;
        }
        final String[] markers = params.getParamValue().split(MARKER_SEPARATOR);
        final JSONArray list = new JSONArray();
        for(String str : markers) {
            final JSONObject marker = getMarker(str);
            if(marker != null) {
                list.put(marker);
            }
            else {
                log.info("Failed to parse marker from:", str);
            }
        }
        // setup plugin config if there were any markers
        if(list.length() > 0) {
            final JSONObject mapfullConf = getBundleConfig(params.getConfig(), BUNDLE_MAPFULL);
            final JSONObject markersPlugin = getMarkersPlugin(mapfullConf);
            final JSONArray existing = getMarkersFromPluginState(markersPlugin);
            if(existing == null) {
                log.info("Couldn't create markers array");
                return false;
            }
            for(int i = 0; i < list.length(); ++i) {
                existing.put(list.optJSONObject(i));
            }
        }
        return false;
    }

    /**
     * Construct a marker JSON from param value part
     * @param linkText
     * @return
     */
    private JSONObject getMarker(final String linkText) {
        final String[] fields = linkText.split("\\" + FIELD_SEPARATOR);
        if(fields.length < 5 ) {
            log.debug(fields);
            return null;
        }

        final String[] coords = fields[3].split(COORD_SEPARATOR);
        if(coords.length != 2) {
            return null;
        }
        // parse as doubles to be sure
        final double x = ConversionHelper.getDouble(coords[0], -1);
        final double y = ConversionHelper.getDouble(coords[1], -1);

        // just to be sure loop out every last part if user happened to use separator in text
        final StringWriter txt = new StringWriter();
        for(int i = 4; i < fields.length; ++i) {
            txt.write(fields[i]);
            if(i != fields.length -1) {
                txt.write(FIELD_SEPARATOR);
            }
        }
        final JSONObject marker = getMarker(fields[0], fields[1], fields[2], x, y,  txt.toString());

        return marker;
    }

    /**
     * Create a default marker for coordinates
     * @param x
     * @param y
     * @return
     */
    protected JSONObject getMarker(final double x, final double y) {
        return getMarker(DEFAULT_SHAPE, DEFAULT_SIZE, DEFAULT_COLOR, x, y, null);
    }

    protected JSONObject getMarker(final String shape, final String size, final String color,
                                final double x, final double y, final String text) {

        if(x == -1 || y == -1) {
            // couldn't parse coordinates
            return null;
        }
        final JSONObject marker = new JSONObject();
        JSONHelper.putValue(marker, KEY_SHAPE, shape);
        JSONHelper.putValue(marker, KEY_SIZE, size);
        JSONHelper.putValue(marker, KEY_COLOR, color);
        JSONHelper.putValue(marker, KEY_X, x);
        JSONHelper.putValue(marker, KEY_Y, y);
        if(text != null) {
            JSONHelper.putValue(marker, KEY_MESSAGE, text);
        }
        return marker;
    }

    /**
     * Find MarkersPlugin from mapfull config
     * @param mapfullConf
     * @return JSONObject for MarkersPlugin or null if not found
     */
    protected JSONObject getMarkersPlugin(final JSONObject mapfullConf) {
        final JSONArray plugins = mapfullConf.optJSONArray(KEY_PLUGINS);
        for(int i = 0; i < plugins.length(); ++i) {
            final JSONObject plugin = plugins.optJSONObject(i);
            final String id = plugin.optString(KEY_ID);
            if(MARKERSPLUGIN_ID.equals(id)) {
                return plugin;
            }
        }
        log.info("Tried to modify markers config but couldn't find plugin in view");
        return null;
    }

    /**
     * Find markers array from MarkersPlugin. Creating it if doesn't exist.
     * @param plugin
     * @return JSONArray for adding markers or null if plugin was null
     */
    protected JSONArray getMarkersFromPluginState(final JSONObject plugin) {
        if(plugin == null) {
            return null;
        }
        JSONObject state = plugin.optJSONObject(KEY_STATE);
        if(state == null) {
            // no existing config, create one
            state = new JSONObject();
            JSONHelper.putValue(plugin, KEY_STATE, state);
        }
        JSONArray existing = state.optJSONArray(KEY_MARKERS);
        if(existing == null) {
            existing = new JSONArray();
            JSONHelper.putValue(state, KEY_MARKERS, existing);
        }
        return existing;
    }

}
