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

        final JSONObject layerJson = getBaseJSON(layer, lang, isSecure);

        JSONHelper.putValue(layerJson, "style", layer.getStyle());
        final JSONArray styles = new JSONArray();

        final WebMapService wms = buildWebMapService(layer);
        if(wms != null) {
            final Map<String, String> stylesMap = wms.getSupportedStyles();
            try {
                final Map<String, String> legends = wms.getSupportedLegends();
                for (String styleName : stylesMap.keySet()) {
                    final String styleLegend = legends.get(styleName);
                    JSONObject obj = createStylesJSON(styleName, stylesMap.get(styleName), styleLegend);
                    styles.put(obj);

                    if(styleName.equals(layer.getStyle()) && styleLegend != null && !"".equals(styleLegend)) {
                        // if default style match and style has legend image - fix legendImage
                        JSONHelper.putValue(layerJson, "legendImage", styleLegend);
                    }
                }
            } catch (Exception e) {
                log.warn(e, "Populating layer styles failed!");
            }
            JSONHelper.putValue(layerJson, "isQueryable", wms.isQueryable());
            JSONHelper.putValue(layerJson, "version", wms.getVersion());
        }

        JSONHelper.putValue(layerJson, "styles", styles);
        JSONHelper.putValue(layerJson, "formats", getFormatsJSON(wms));

        return layerJson;
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

        boolean foundSupported = false;
        try {
            for (String supported : SUPPORTED_GET_FEATURE_INFO_FORMATS) {
                if (formats.contains(supported)) {
                    formatJSON.put("value", supported);
//                    foundSupported = true;
                    break;
                }
            }
/*
            if (!foundSupported) {
                formatJSON.put("value", "");
            }
*/
            return formatJSON;

        } catch (Exception e) {
            log.warn(e, "Couldn't parse formats for layer");
        }
        return formatJSON;
    }
}
