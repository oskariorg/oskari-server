package fi.nls.oskari.map.layer.formatters;

import fi.mml.map.mapwindow.service.wms.WebMapService;
import fi.mml.map.mapwindow.service.wms.WebMapServiceFactory;
import fi.mml.map.mapwindow.service.wms.WebMapServiceParseException;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: SMAKINEN
 * Date: 17.12.2013
 * Time: 15:37
 * To change this template use File | Settings | File Templates.
 */
public class LayerJSONFormatterWMS extends LayerJSONFormatter {

    private static Logger log = LogFactory.getLogger(LayerJSONFormatterWMS.class);

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

        final Map<String, String> stylesMap = capabilities.getSupportedStyles();
        final JSONArray styles = new JSONArray();
        try {
            final Map<String, String> legends = capabilities.getSupportedLegends();
            final boolean hasLegendImage = layer.getLegendImage() != null && !layer.getLegendImage().isEmpty();
            for (String styleName : stylesMap.keySet()) {
                final String styleLegend = legends.get(styleName);
                JSONObject obj = createStylesJSON(styleName, stylesMap.get(styleName), styleLegend);
                styles.put(obj);
                if(hasLegendImage) {
                    continue;
                }
                // set legend image from capabilities if admin hasn't configured it
                if(styleName.equals(layer.getStyle()) && styleLegend != null && !styleLegend.isEmpty()) {
                    // if default style match and style has legend image - fix legendImage
                    JSONHelper.putValue(layerJson, "legendImage", styleLegend);
                }
            }
        } catch (Exception e) {
            log.warn(e, "Populating layer styles failed!");
        }
        JSONHelper.putValue(layerJson, "styles", styles);
        JSONObject formats = getFormatsJSON(capabilities);
        JSONHelper.putValue(layerJson, "formats", formats);
        JSONHelper.putValue(layerJson, "isQueryable", capabilities.isQueryable());
        JSONHelper.putValue(layerJson, "version", capabilities.getVersion());
    }

    /**
     * Builds a new WebMapService
     * @param layer layer
     * @return WebMapService or null if something goes wrong.
     */
    private WebMapService buildWebMapService(final OskariLayer layer) {
        try {
            return WebMapServiceFactory.buildWebMapService(layer.getId(), layer.getName());
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
    private static JSONObject getFormatsJSON(WebMapService wms) {
        final JSONObject formatJSON = new JSONObject();
        final JSONArray available = new JSONArray();
        JSONHelper.putValue(formatJSON, "available", available);
        if(wms == null) {
            return formatJSON;
        }
        // simple but inefficient...
        final Set<String> formats =  new HashSet<String>(Arrays.asList(wms.getFormats()));
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
