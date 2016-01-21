package fi.nls.oskari.map.layer.formatters;

import fi.mml.map.mapwindow.service.wms.WebMapService;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: SMAKINEN
 * Date: 17.12.2013
 * Time: 15:37
 * To change this template use File | Settings | File Templates.
 */
public class LayerJSONFormatterWMS extends LayerJSONFormatter {

    private static Logger log = LogFactory.getLogger(LayerJSONFormatterWMS.class);

    public static final String KEY_STYLE = "style";
    public static final String KEY_LEGEND = "legend";

    // There working only plain text and html so ranked up
    private static String[] SUPPORTED_GET_FEATURE_INFO_FORMATS = new String[] {
            "text/html", "text/plain", "application/vnd.ogc.se_xml",
            "application/vnd.ogc.gml", "application/vnd.ogc.wms_xml",
            "text/xml" };


    public JSONObject getJSON(OskariLayer layer,
                              final String lang,
                              final boolean isSecure) {

        final JSONObject layerJson = getBaseJSON(layer, lang, isSecure);
        JSONHelper.putValue(layerJson, "style", layer.getStyle());
        JSONHelper.putValue(layerJson, "gfiContent", layer.getGfiContent());

        if (layer.getGfiType() != null && !layer.getGfiType().isEmpty()) {
            // setup default if saved
            JSONObject formats = layerJson.optJSONObject("formats");
            if(formats == null) {
                // create formats node if not found
                formats = JSONHelper.createJSONObject("value", layer.getGfiType());
                JSONHelper.putValue(layerJson, "formats", formats);
            }
            else {
                JSONHelper.putValue(formats, "value", layer.getGfiType());
            }
        }
        includeCapabilitiesInfo(layerJson, layer, layer.getCapabilities());
        return layerJson;
    }

    public JSONObject getJSON(OskariLayer layer,
                              final String lang,
                              final boolean isSecure,
                              final WebMapService capabilities) {

        final JSONObject layerJson = getJSON(layer, lang, isSecure);
        final JSONObject capsJSON = createCapabilitiesJSON(capabilities);
        includeCapabilitiesInfo(layerJson, layer, capsJSON);

        return layerJson;
    }

    /**
     * Populate JSON with capabilities values
     * @param layerJson
     * @param layer
     * @param capabilities
     */
    private void includeCapabilitiesInfo(final JSONObject layerJson,
                                         final OskariLayer layer,
                                         final JSONObject capabilities) {
        if(capabilities == null) {
            return;
        }

        final boolean useProxy = useProxy(layer);
        try {
            final JSONArray styles;
            if (useProxy) {
                // construct a modified styles list
                final JSONArray styleList = capabilities.optJSONArray(KEY_STYLES);
                styles = new JSONArray();
                // replace legendimage urls
                if(styleList != null) {
                    for(int i = 0; i < styleList.length(); ++i) {
                        JSONObject style = styleList.optJSONObject(i);
                        if (style != null && style.has(KEY_LEGEND)) {
                            // copy the values to a new object to not affect the original
                            style = new JSONObject(style, STYLE_KEYS);
                            // update url from actual to proxied version
                            JSONHelper.putValue(style, KEY_LEGEND, buildLegendUrl(layer, style.optString("name")));
                        }
                        styles.put(style);
                    }
                }
            }
            else {
                styles = capabilities.optJSONArray(KEY_STYLES);
            }
            JSONHelper.putValue(layerJson, KEY_STYLES, styles);

            final String globalLegend = layer.getLegendImage();
            // if we have a global legend url, setup the JSON
            if(globalLegend != null && !globalLegend.isEmpty()) {
                if (useProxy) {
                    JSONHelper.putValue(layerJson, "legendImage", buildLegendUrl(layer, null));
                    // copy the original value so we can show them for admins
                    addInfoForAdmin(layerJson, "legendImage", globalLegend);
                } else {
                    JSONHelper.putValue(layerJson, "legendImage", globalLegend);
                }
            }

        } catch (Exception e) {
            log.warn(e, "Populating layer styles failed!");
        }

        JSONHelper.putValue(layerJson, "formats", capabilities.optJSONObject("formats"));
        JSONHelper.putValue(layerJson, "isQueryable", capabilities.optBoolean("isQueryable"));
        JSONHelper.putValue(layerJson, "version", capabilities.optString("version"));
        // copy time from capabilities to attributes
        // timedata is merged into attributes  (times:{start:,end:,interval:}  or times: []
        // only reason for this is that admin can see the values offered by service
        if(capabilities.has("time")) {
            JSONHelper.putValue(layerJson, "attributes", JSONHelper.merge(
                    JSONHelper.getJSONObject(layerJson, "attributes"),
                    createTimesJSON(JSONHelper.getJSONArray(capabilities, "time"))));
        }

    }

    public static JSONObject createCapabilitiesJSON(final WebMapService wms) {

        JSONObject capabilities = new JSONObject();
        if(wms == null) {
            return capabilities;
        }
        JSONHelper.putValue(capabilities, "isQueryable", wms.isQueryable());
        List<JSONObject> styles = LayerJSONFormatterWMS.createStylesArray(wms);
        JSONHelper.putValue(capabilities, KEY_STYLES, new JSONArray(styles));

        JSONObject formats = LayerJSONFormatterWMS.getFormatsJSON(wms);
        JSONHelper.putValue(capabilities, "formats", formats);
        JSONHelper.putValue(capabilities, "version", wms.getVersion());
        capabilities = JSONHelper.merge(capabilities, LayerJSONFormatterWMS.formatTime(wms.getTime()));
        return capabilities;
    }

    public static List<JSONObject> createStylesArray(final WebMapService capabilities) {
        final List<JSONObject> styles = new ArrayList<>();
        final Map<String, String> stylesMap = capabilities.getSupportedStyles();
        final Map<String, String> legends = capabilities.getSupportedLegends();
        for (String styleName : stylesMap.keySet()) {
            styles.add(createStylesJSON(styleName, stylesMap.get(styleName), legends.get(styleName)));
        }
        return styles;
    }

    private String buildLegendUrl(final OskariLayer layer, final String styleName) {
        Map<String, String> urlParams = new HashMap<String, String>();
        urlParams.put("action_route", "GetLayerTile");
        urlParams.put("id", Integer.toString(layer.getId()));
        urlParams.put("legend", "true");
        if(styleName != null){
            urlParams.put(KEY_STYLE, styleName);
        }
        return IOHelper.constructUrl(PropertyUtil.get(PROPERTY_AJAXURL), urlParams);
    }

    public static JSONObject formatTime(List<String> timeList) {
        final JSONObject time = new JSONObject();
        final JSONArray values = new JSONArray();
        for (String string : timeList) {
            values.put(string);
        }
        if (values.length() > 0) {
            JSONHelper.putValue(time, "time", values);
        }
        return time;
    }

    public static JSONObject createTimesJSON(final JSONArray time) {

        JSONObject times = new JSONObject();
        JSONObject timerange = new JSONObject();
        try {

            if (time == null) {
                return times;
            }
            //Loop array
            for (int i = 0; i < time.length(); i++) {
                String tim = time.getString(i);
                String[] tims = tim.split("/");
                if(tims.length > 2){
                    JSONHelper.putValue(timerange, "start", tims[0]);
                    JSONHelper.putValue(timerange, "end", tims[1]);
                    JSONHelper.putValue(timerange, "interval", tims[2]);
                    JSONHelper.putValue(times, "times", timerange);
                }
                else {
                    final JSONArray values = new JSONArray();
                    String[] atims = tim.split(",");
                    for (String string : atims) {
                        values.put(string);
                    }
                    JSONHelper.putValue(times, "times", values);
                }
                break;
            }


        } catch (Exception e) {
            log.warn(e, "Populating layer time failed!");
        }
        return times;
    }

    /**
     * Constructs a formats json containing the most preferred supported format
     *
     * @param wms WebMapService
     * @return JSONObject containing the most preferred supported format
     */
    public static JSONObject getFormatsJSON(WebMapService wms) {
        final Set<String> formats = new HashSet<String>(Arrays.asList(wms.getFormats()));
        return getFormatsJSON(formats);
    }

    public static JSONObject getFormatsJSON(final Collection<String> formats) {
        final JSONObject formatJSON = new JSONObject();
        final JSONArray available = new JSONArray();
        JSONHelper.putValue(formatJSON, "available", available);
        if(formats == null) {
            return formatJSON;
        }
        // simple but inefficient...
        // We support the following formats. Formats are presented
        // in order of preference.
        // 'application/vnd.ogc.se_xml' == GML
        // 'application/vnd.ogc.gml' == GML
        // 'application/vnd.ogc.wms_xml' == text/xml
        // 'text/xml'
        // 'text/html'
        // 'text/plain'
        try {
            String value = null;
            for (String supported : SUPPORTED_GET_FEATURE_INFO_FORMATS) {
                if (formats.contains(supported)) {
                    if(value == null) {
                        // get the first one as default
                        value = supported;
                    }
                    // gather list of supported formats
                    available.put(supported);
                }
            }
            // default format
            JSONHelper.putValue(formatJSON, "value", value);
            return formatJSON;

        } catch (Exception e) {
            log.warn(e, "Couldn't parse formats for layer");
        }
        return formatJSON;
    }
}
