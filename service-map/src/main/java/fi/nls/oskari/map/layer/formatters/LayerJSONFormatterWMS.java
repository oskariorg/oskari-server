package fi.nls.oskari.map.layer.formatters;

import fi.mml.map.mapwindow.service.wms.WebMapService;
import fi.mml.map.mapwindow.service.wms.WebMapServiceV1_3_0_Impl;
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
    public static final String KEY_TIMES = "times";
    public static final String KEY_VALUE = "value";
    public static final String KEY_FORMATS = "formats";
    public static final String KEY_GFICONTENT = "gfiContent";
    public static final String KEY_LEGENDIMAGE = "legendImage";
    public static final String KEY_VERSION = "version";
    public static final String KEY_ISQUERYABLE = "isQueryable";
    public static final String KEY_ATTRIBUTES = "attributes";

    // There working only plain text and html so ranked up
    private static String[] SUPPORTED_GET_FEATURE_INFO_FORMATS = new String[] {
            "text/html", "text/plain", "application/vnd.ogc.se_xml",
            "application/vnd.ogc.gml", "application/vnd.ogc.wms_xml",
            "text/xml" };


    public JSONObject getJSON(OskariLayer layer,
                              final String lang,
                              final boolean isSecure) {

        final JSONObject layerJson = getBaseJSON(layer, lang, isSecure);
        JSONHelper.putValue(layerJson, KEY_STYLE, layer.getStyle());
        JSONHelper.putValue(layerJson, KEY_GFICONTENT, layer.getGfiContent());

        if (layer.getGfiType() != null && !layer.getGfiType().isEmpty()) {
            // setup default if saved
            JSONObject formats = layerJson.optJSONObject(KEY_FORMATS);
            if(formats == null) {
                // create formats node if not found
                formats = JSONHelper.createJSONObject(KEY_VALUE, layer.getGfiType());
                JSONHelper.putValue(layerJson, KEY_FORMATS, formats);
            }
            else {
                JSONHelper.putValue(formats, KEY_VALUE, layer.getGfiType());
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
                    JSONHelper.putValue(layerJson, KEY_LEGENDIMAGE, buildLegendUrl(layer, null));
                    // copy the original value so we can show them for admins
                    addInfoForAdmin(layerJson, KEY_LEGENDIMAGE, globalLegend);
                } else {
                    JSONHelper.putValue(layerJson, KEY_LEGENDIMAGE, globalLegend);
                }
            }

        } catch (Exception e) {
            log.warn(e, "Populating layer styles failed!");
        }

        JSONHelper.putValue(layerJson, KEY_FORMATS, capabilities.optJSONObject(KEY_FORMATS));
        JSONHelper.putValue(layerJson, KEY_ISQUERYABLE, capabilities.optBoolean(KEY_ISQUERYABLE));
        // Do not override version, if already available
        if(!layerJson.has(KEY_VERSION)) {
            JSONHelper.putValue(layerJson, KEY_VERSION, JSONHelper.getStringFromJSON(capabilities, KEY_VERSION, null));
        }
        // copy time from capabilities to attributes
        // timedata is merged into attributes  (times:{start:,end:,interval:}  or times: []
        // only reason for this is that admin can see the values offered by service
        if(capabilities.has(KEY_TIMES)) {
            JSONHelper.putValue(layerJson, KEY_ATTRIBUTES, JSONHelper.merge(
                    JSONHelper.getJSONObject(layerJson, KEY_ATTRIBUTES),
                    JSONHelper.createJSONObject(KEY_TIMES, JSONHelper.get(capabilities, KEY_TIMES))));
        }

    }

    public static JSONObject createCapabilitiesJSON(final WebMapService wms) {

        JSONObject capabilities = new JSONObject();
        if(wms == null) {
            return capabilities;
        }
        JSONHelper.putValue(capabilities, KEY_ISQUERYABLE, wms.isQueryable());
        List<JSONObject> styles = LayerJSONFormatterWMS.createStylesArray(wms);
        JSONHelper.putValue(capabilities, KEY_STYLES, new JSONArray(styles));

        JSONObject formats = LayerJSONFormatterWMS.getFormatsJSON(wms);
        JSONHelper.putValue(capabilities, KEY_FORMATS, formats);
        JSONHelper.putValue(capabilities, KEY_VERSION, wms.getVersion());
        capabilities = JSONHelper.merge(capabilities, LayerJSONFormatterWMS.formatTime(wms.getTime()));
        return capabilities;
    }

    public static List<JSONObject> createStylesArray(final WebMapService capabilities) {
        final List<JSONObject> styles = new ArrayList<>();
        final Map<String, String> stylesMap = capabilities.getSupportedStyles();
        for (String styleName : stylesMap.keySet()) {
            String legend = capabilities.getLegendForStyle(styleName);
            styles.add(createStylesJSON(styleName, stylesMap.get(styleName), legend));
        }
        return styles;
    }

    private String buildLegendUrl(final OskariLayer layer, final String styleName) {
        Map<String, String> urlParams = new HashMap<String, String>();
        urlParams.put("action_route", "GetLayerTile");
        urlParams.put("id", Integer.toString(layer.getId()));
        urlParams.put(KEY_LEGEND, "true");
        if(styleName != null){
            urlParams.put(KEY_STYLE, styleName);
        }
        return IOHelper.constructUrl(PropertyUtil.get(PROPERTY_AJAXURL), urlParams);
    }

    public static JSONObject formatTime(List<String> timeList) {
        final JSONArray values = new JSONArray();
        for (String string : timeList) {
            values.put(string);
        }
        return createTimesJSON(values);
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
                    JSONHelper.putValue(times, KEY_TIMES, timerange);
                }
                else {
                    final JSONArray values = new JSONArray();
                    String[] atims = tim.split(",");
                    for (String string : atims) {
                        values.put(string);
                    }
                    JSONHelper.putValue(times, KEY_TIMES, values);
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
            JSONHelper.putValue(formatJSON, KEY_VALUE, value);
            return formatJSON;

        } catch (Exception e) {
            log.warn(e, "Couldn't parse formats for layer");
        }
        return formatJSON;
    }
    /**
     * Constructs a  csr set containing the supported coordinate ref systems of WMS service
     *
     * @param wms WebMapService
     * @return Set<String> containing the supported coordinate ref systems of WMS service
     */
    public static Set<String> getCRSs(WebMapService wms) {
        if(wms.getCRSs().length > 0){
            final Set<String> crss = new HashSet<String>(Arrays.asList(wms.getCRSs()));
            return crss;
        }


        return null;
    }
}
