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

import static fi.nls.oskari.control.ActionConstants.PARAM_SECURE;

/**
 * Describes the environment the appsetup is used in:
 * - Who's the user
 * - What's the API url
 * - Which locales are supported
 * - Which markers are used
 *
 * Attached as part of the GetAppSetup response.
 */
public class EnvHelper {
    private static final Logger LOGGER = LogFactory.getLogger(EnvHelper.class);

    private static final String KEY_DECIMAL_SEPARATOR = "decimalSeparator";
    private static final String KEY_SUPPORTED_LOCALES = "locales";
    private static final String KEY_SVG_MARKERS = "svgMarkers";
    private static final String KEY_USER = "user";
    private static final String KEY_API = "api";

    public static final String SVG_MARKERS_JSON = "svg-markers.json";
    public static final String PROPERTY_AJAXURL = "oskari.ajax.url.prefix";

    public static JSONObject getEnvironmentJSON(ActionParameters params) {
        final JSONObject env = new JSONObject();
        JSONHelper.putValue(env, ActionConstants.PARAM_LANGUAGE, params.getLocale().getLanguage());

        JSONHelper.putValue(env, KEY_SUPPORTED_LOCALES, PropertyUtil.getSupportedLocales());
        final DecimalFormatSymbols dfs = new DecimalFormatSymbols(params.getLocale());
        JSONHelper.putValue(env, KEY_DECIMAL_SEPARATOR, Character.toString(dfs.getDecimalSeparator()));

        // setup user data
        final JSONObject user = params.getUser().toJSON();
        // TODO: Remove "apikey" once frontend has been modifed to use it from under env.api.key
        JSONHelper.putValue(user, "apikey", params.getAPIkey());
        JSONHelper.putValue(env, KEY_USER, user);

        // setup API info
        JSONObject apiConfig = new JSONObject();
        JSONHelper.putValue(apiConfig, "key", params.getAPIkey());
        JSONHelper.putValue(apiConfig, "url", getAPIurl(params));
        JSONHelper.putValue(env, KEY_API, apiConfig);


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

    public static String getAPIurl(final ActionParameters params) {
        final String baseAjaxUrl = PropertyUtil.get(params.getLocale(), PROPERTY_AJAXURL);
        if (isSecure(params)) {
            return PropertyUtil.get("actionhandler.GetAppSetup.secureAjaxUrlPrefix", "") + baseAjaxUrl;
        }
        return baseAjaxUrl;
    }

    /**
     * Check if we are dealing with a forwarded "secure" url. This means that we will
     * modify urls on the fly to match proxy forwards. Checks an http parameter "ssl" for boolean value.
     * @param params
     * @return
     */
    public static boolean isSecure(final ActionParameters params) {
        return params.getHttpParam(PARAM_SECURE, params.getRequest().isSecure());
    }

}
