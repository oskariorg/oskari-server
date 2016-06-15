package fi.nls.oskari.util;

import fi.nls.oskari.control.ActionConstants;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.text.DecimalFormatSymbols;
/**
 * Created by SMAKINEN on 16.5.2016.
 */
public class EnvHelper {
    private static final Logger LOGGER = LogFactory.getLogger(EnvHelper.class);

    private static final String KEY_DECIMAL_SEPARATOR = "decimalSeparator";
    private static final String KEY_SUPPORTED_LOCALES = "locales";
    private static final String KEY_SVG_MARKERS = "svgMarkers";

    public static final String SVG_MARKERS_JSON = "svg-markers.json";

    public static JSONObject getEnvironmentJSON(ActionParameters params) {
        final JSONObject env = new JSONObject();
        JSONHelper.putValue(env, ActionConstants.PARAM_LANGUAGE, params.getLocale().getLanguage());

        JSONHelper.putValue(env, KEY_SUPPORTED_LOCALES, PropertyUtil.getSupportedLocales());
        final DecimalFormatSymbols dfs = new DecimalFormatSymbols(params.getLocale());
        JSONHelper.putValue(env, KEY_DECIMAL_SEPARATOR, Character.toString(dfs.getDecimalSeparator()));


        try {
            InputStream inp = EnvHelper.class.getResourceAsStream(SVG_MARKERS_JSON);
            if (inp != null) {
                JSONArray svgMarkers = JSONHelper.createJSONArray(IOUtils.toString(inp, "UTF-8") );
                if(svgMarkers != null || svgMarkers.length() > 0) {
                    JSONHelper.putValue(env, KEY_SVG_MARKERS, svgMarkers);
                }
            }
        } catch (Exception e) {
            LOGGER.info("No setup for svg markers found", e);
        }
        return env;
    }

}
