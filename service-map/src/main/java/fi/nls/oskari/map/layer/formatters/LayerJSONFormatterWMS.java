package fi.nls.oskari.map.layer.formatters;

import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import static fi.nls.oskari.service.capabilities.CapabilitiesConstants.*;

/**
 * Created with IntelliJ IDEA.
 * User: SMAKINEN
 * Date: 17.12.2013
 * Time: 15:37
 * To change this template use File | Settings | File Templates.
 */
public class LayerJSONFormatterWMS extends LayerJSONFormatter {

    public static final String KEY_GFICONTENT = "gfiContent";
    public static final String KEY_ATTRIBUTES = "attributes";
    private static Logger log = LogFactory.getLogger(LayerJSONFormatterWMS.class);

    public JSONObject getJSON(OskariLayer layer,
                              final String lang,
                              final boolean isSecure,
                              final String crs) {

        final JSONObject layerJson = getBaseJSON(layer, lang, isSecure, crs);
        JSONHelper.putValue(layerJson, KEY_GFICONTENT, layer.getGfiContent());

        if (layer.getGfiType() != null && !layer.getGfiType().isEmpty()) {
            // setup default if saved
            JSONObject formats = layerJson.optJSONObject(KEY_FORMATS);
            if (formats == null) {
                // create formats node if not found
                formats = JSONHelper.createJSONObject(KEY_VALUE, layer.getGfiType());
                JSONHelper.putValue(layerJson, KEY_FORMATS, formats);
            } else {
                JSONHelper.putValue(formats, KEY_VALUE, layer.getGfiType());
            }
        }
        try {
            JSONHelper.putValue(layerJson, KEY_STYLES, createStylesJSON(layer, isSecure));
        } catch (Exception e) {
            log.warn(e, "Populating layer styles failed for id: " + layer.getId());
        }
        includeCapabilitiesInfo(layerJson, layer, layer.getCapabilities());
        return layerJson;
    }

    /**
     * Populate JSON with capabilities values
     *
     * @param layerJson
     * @param layer
     * @param capabilities
     */
    private void includeCapabilitiesInfo(final JSONObject layerJson,
                                         final OskariLayer layer,
                                         final JSONObject capabilities) {
        if (capabilities == null) {
            return;
        }
        // TODO: only admin/server needs the gfi formats info -> remove?
        JSONHelper.putValue(layerJson, KEY_FORMATS, capabilities.optJSONObject(KEY_FORMATS));

        final JSONObject attrs = layer.getAttributes();
        if (attrs != null && attrs.has(KEY_ISQUERYABLE)) {
            // attributes can be used to force GFI for layer even if capabilities allow it or enable it not
            JSONHelper.putValue(layerJson, KEY_ISQUERYABLE, attrs.optBoolean(KEY_ISQUERYABLE));
        } else if (capabilities.has(KEY_ISQUERYABLE)) {
            JSONHelper.putValue(layerJson, KEY_ISQUERYABLE, capabilities.optBoolean(KEY_ISQUERYABLE));
        } else if (capabilities.has("queryable")) {
            JSONHelper.putValue(layerJson, KEY_ISQUERYABLE, capabilities.optBoolean("queryable"));
        }

        // Do not override version, if already available
        if (!layerJson.has(KEY_VERSION)) {
            JSONHelper.putValue(layerJson, KEY_VERSION,
                    JSONHelper.getStringFromJSON(capabilities, KEY_VERSION, null));
        }

        // copy time from capabilities to attributes
        // timedata is merged into attributes  (times:{start:,end:,interval:}  or times: []
        // frontend uses this to detect if layer is a timeseries and construct the UI based on this
        JSONArray times = getTimesFromCapabilities(capabilities);
        if (times != null && isTimeseriesLayer(layer)) {
            JSONHelper.putValue(layerJson, KEY_ATTRIBUTES, JSONHelper.merge(
                    JSONHelper.getJSONObject(layerJson, KEY_ATTRIBUTES),
                    JSONHelper.createJSONObject(KEY_TIMES, times)));
        }
    }

    protected static JSONArray getTimesFromCapabilities(JSONObject capabilities) {
        if (!capabilities.has(KEY_TIMES)) {
            return null;
        }
        JSONArray times = JSONHelper.getJSONArray(capabilities, KEY_TIMES);
        if (times == null || times.length() == 0) {
            return null;
        }
        if (times.length() > 1) {
            return times;
        }
        // https://cite.opengeospatial.org/teamengine/about/wms11/1.1.1/site/OGCTestData/wms/1.1.1/spec/wms1.1.1.html#table.dim_values
        // handle as not having timeseries if there is only single value present to prevent frontend from breaking
        String singleValue = times.optString(0);
        String[] parts = singleValue.split("/");
        if (parts.length < 3) {
            // should be start/end/resolution
            return null;
        }
/*
B.3. Period Format

Example 1. 1 year: P1Y
Example 2. 1 month plus 10 days: P1M10D
Example 3. 2 hours:  PT2H
Example 4. 1.5 seconds: PT1.5S
 */
// TODO: calculate array of times, now returns as is
        return times;
    }

    private Boolean isTimeseriesLayer(final OskariLayer layer) {
        JSONObject options = layer.getOptions();
        if (options != null && options.has("timeseries")) {
            JSONObject timeseriesOptions = options.optJSONObject("timeseries");
            if (timeseriesOptions != null && timeseriesOptions.has("ui")) {
                String ui = timeseriesOptions.optString("ui");
                if (ui != null && ui.equals("none")) {
                    return false;
                }
            }
        }
        return true;
    }

}
