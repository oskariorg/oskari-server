package fi.nls.oskari.map.layer.formatters;

import fi.mml.map.mapwindow.service.wms.WebMapService;
import fi.mml.map.mapwindow.service.wms.WebMapServiceFactory;
import fi.mml.map.mapwindow.service.wms.WebMapServiceParseException;
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

        final WebMapService wms = buildWebMapService(layer);
        return getJSON(layer, lang, isSecure, wms);
    }

    public JSONObject getJSON(OskariLayer layer,
                              final String lang,
                              final boolean isSecure,
                              final WebMapService capabilities) {

        final JSONObject layerJson = getBaseJSON(layer, lang, isSecure);
        JSONHelper.putValue(layerJson, "style", layer.getStyle());
        JSONHelper.putValue(layerJson, "gfiContent", layer.getGfiContent());

        includeCapabilitiesInfo(layerJson, layer, capabilities);

        if(layer.getGfiType() != null && !layer.getGfiType().isEmpty()) {
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
        return layerJson;
    }

    /**
     * Populate JSON with capabilities values
     * @param layerJson
     * @param layer
     * @param capabilities
     */
    public void includeCapabilitiesInfo(final JSONObject layerJson,
                                        final OskariLayer layer,
                                        final WebMapService capabilities) {
        if(capabilities == null) {
            return;
        }

        final boolean useProxy = useProxy(layer);
        try {
            List<JSONObject> styleList = createStylesArray(capabilities);
            final JSONArray styles;
            if (useProxy) {
                final JSONArray org_styles = new JSONArray();
                styles = new JSONArray();
                // replace legendimage urls
                for(JSONObject style : styleList) {
                    // copy the original style definitions so admin can show the real values
                    org_styles.put(new JSONObject(style, STYLE_KEYS));
                    if(style.has(KEY_LEGEND)) {
                        // update url from actual to proxy
                        JSONHelper.putValue(style, KEY_LEGEND, buildLegendUrl(layer, style.optString("name")));
                    }
                    styles.put(style);
                }
                // this is a workaround since we don't know the user here, check OskariLayerWorker for further handling
                JSONHelper.putValue(layerJson, "org_styles", org_styles);
            }
            else {
                styles = new JSONArray(styleList);
            }
            JSONHelper.putValue(layerJson, "styles", styles);

            // populate legend image from styles if not available
            String globalLegend = layer.getLegendImage();
            if(globalLegend == null || globalLegend.isEmpty()) {
                globalLegend = getAnyLegendFromStyles(styleList);
            }
            // if we now have legend url, setup the JSON
            if(globalLegend == null || globalLegend.isEmpty()) {
                if (useProxy) {
                    JSONHelper.putValue(layerJson, "legendImage", buildLegendUrl(layer, null));
                    JSONHelper.putValue(layerJson, "org_legendImage", globalLegend);
                } else {
                    JSONHelper.putValue(layerJson, "legendImage", globalLegend);
                }
            }

        } catch (Exception e) {
            log.warn(e, "Populating layer styles failed!");
        }

        JSONObject formats = getFormatsJSON(capabilities);
        JSONHelper.putValue(layerJson, "formats", formats);
        JSONHelper.putValue(layerJson, "isQueryable", capabilities.isQueryable());
        JSONHelper.putValue(layerJson, "version", capabilities.getVersion());
        JSONHelper.putValue(layerJson, "attributes", JSONHelper.merge(JSONHelper.getJSONObject(layerJson, "attributes"), formatTime(capabilities.getTime())));
    }

    /**
     * Return a legend image from styles, any will do so just use the first one
     * @param styleList
     * @return
     */
    private String getAnyLegendFromStyles(List<JSONObject> styleList) {
        for(JSONObject style : styleList) {
            final String legend = style.optString(KEY_LEGEND);
            if(legend != null && !legend.isEmpty()) {
                return legend;
            }
        }
        return null;
    }

    public static JSONObject createCapabilitiesJSON(final WebMapService wms) {

        JSONObject capabilities = new JSONObject();
        if(wms == null) {
            return capabilities;
        }
        JSONHelper.putValue(capabilities, "isQueryable", wms.isQueryable());
        List<JSONObject> styles = LayerJSONFormatterWMS.createStylesArray(wms);
        JSONHelper.putValue(capabilities, "styles", new JSONArray(styles));

        JSONObject formats = LayerJSONFormatterWMS.getFormatsJSON(wms);
        JSONHelper.putValue(capabilities, "formats", formats);
        JSONHelper.putValue(capabilities, "version", wms.getVersion());
        JSONHelper.merge(capabilities, LayerJSONFormatterWMS.formatTime(wms.getTime()));
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
            urlParams.put(KEY_STYLE, styleName );
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

    /**
     * Builds a new WebMapService
     * @param layer layer
     * @return WebMapService or null if something goes wrong.
     */
    private WebMapService buildWebMapService(final OskariLayer layer) {
        try {
            return WebMapServiceFactory.buildWebMapService(layer);
        } catch (WebMapServiceParseException e) {
            log.error("Failed to create WebMapService for layer id '" + layer.getId() + "'. No Styles available");
        }
        return null;
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

    public static JSONObject getFormatsJSON(final Set<String> formats) {
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
