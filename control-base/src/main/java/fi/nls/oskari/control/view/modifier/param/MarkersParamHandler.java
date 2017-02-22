package fi.nls.oskari.control.view.modifier.param;

import fi.nls.oskari.annotation.OskariViewModifier;
import fi.nls.oskari.view.modifier.ParamHandler;
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
    static final String KEY_TRANSIENT = "transient";

    // for parsing mapfull config
    private static final String MARKERSPLUGIN_ID = "MainMapModuleMarkersPlugin";
    private static final String KEY_PLUGINS = "plugins";
    private static final String KEY_ID = "id";

    public boolean handleParam(final ModifierParams params) throws ModifierException {
        log.info("handleParam");
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
        }
        // setup plugin config if there were any markers
        if(list.length() > 0) {
            final JSONObject mapfullState = getBundleState(params.getConfig(), BUNDLE_MAPFULL);
            final JSONObject markersPluginState = getMarkersPluginState(mapfullState);
            // showMarker shouldn't be true if we have user markers
            // so we can safely write over cookie state here...
            final JSONArray markersArray = new JSONArray();
            if (markersPluginState.has(KEY_MARKERS)) {
                markersPluginState.remove(KEY_MARKERS);
            }
            JSONHelper.putValue(markersPluginState, KEY_MARKERS, markersArray);
            for(int i = 0; i < list.length(); ++i) {
                markersArray.put(list.optJSONObject(i));
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
        final String[] fields = linkText.split("\\" + FIELD_SEPARATOR, -1);
        if(fields.length < 5 ) {
            log.warn("Failed to parse marker from string:", linkText, " (Field count was " + fields.length + ", expected >= 5)");
            log.debug(fields);
            return null;
        }

        final String[] coords = fields[3].split(COORD_SEPARATOR);
        if(coords.length != 2) {
            log.warn("Failed to parse marker from string:", linkText, "(Coords count was " + coords.length + ", expected 2)");
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
     * @param mapfullState mapfull bundle state
     * @return JSONObject for MarkersPlugin or null if not found
     */
    protected JSONObject getMarkersPluginState(final JSONObject mapfullState) {
        JSONObject plugins = mapfullState.optJSONObject(KEY_PLUGINS);
        JSONObject pluginState = null;
        if (plugins != null) {
            pluginState = plugins.optJSONObject(MARKERSPLUGIN_ID);
        } else {
            plugins = new JSONObject();
            JSONHelper.putValue(mapfullState, KEY_PLUGINS, plugins);
        }
        if (pluginState == null) {
            pluginState = new JSONObject();
            JSONHelper.putValue(pluginState, KEY_MARKERS, new JSONArray());
            JSONHelper.putValue(plugins, MARKERSPLUGIN_ID, pluginState);
        }
        return pluginState;
    }

    /**
     * Find markers array from MarkersPlugin. Creating it if doesn't exist.
     * @param state Markers plugin state
     * @return JSONArray for adding markers or null if plugin was null
     */
    protected JSONArray getMarkersFromPluginState(final JSONObject state) {
        if(state == null) {
            return null;
        }
        JSONArray existing = state.optJSONArray(KEY_MARKERS);
        if(existing == null) {
            existing = new JSONArray();
            JSONHelper.putValue(state, KEY_MARKERS, existing);
        }
        return existing;
    }

}
