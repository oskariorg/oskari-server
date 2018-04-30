package fi.nls.oskari.util;

import fi.nls.oskari.control.ActionConstants;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;

import static fi.nls.oskari.control.ActionConstants.*;

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

    // localization
    private static final String KEY_SUPPORTED_LOCALES = "locales";
    private static final String KEY_DECIMAL_SEPARATOR = "decimalSeparator";

    // markers
    private static final String KEY_SVG_MARKERS = "svgMarkers";
    private static final String SVG_MARKERS_JSON = "svg-markers.json";

    // urls
    private static final String KEY_URLS = "urls";
    private static final String KEY_API = "api";
    private static final String KEY_REGISTER = "register";
    private static final String KEY_LOGIN = "login";
    private static final String KEY_LOGOUT = "logout";

    private static final String registerUrl = PropertyUtil.get("auth.register.url", "/user");
    private static final String loginUrl = PropertyUtil.get("auth.login.url", "/j_security_check");
    private static final String logoutUrl = PropertyUtil.get("auth.logout.url", "/logout");
    private static final String PROPERTY_AJAXURL = "oskari.ajax.url.prefix";

    // user
    private static final String KEY_USER = "user";
    private static final String KEY_APIKEY = "apikey";

    // appsetups
    private static final String KEY_APPSETUP = "app";
    private static final String KEY_DEFAULT_VIEWS = "defaultApps";
    private static final String KEY_ISPUBLIC = "public";
    private static final List<JSONObject> DEFAULT_VIEWS = new ArrayList<>();

    public static void setupViews(List<View> views) {
        DEFAULT_VIEWS.clear();
        for(View view: views) {
            try {
                JSONObject json = new JSONObject();
                json.put("uuid", view.getUuid());
                json.put("name", view.getName());
                json.put("srsName", view.getSrsName());
                DEFAULT_VIEWS.add(json);
            } catch (JSONException e) {
                LOGGER.warn("Couldn't format default views for appsetup.env:", e.getMessage());
            }
        }
    }

    public static JSONObject getEnvironmentJSON(ActionParameters params, View view) {
        final JSONObject env = new JSONObject();

        // setup locale info
        JSONHelper.putValue(env, ActionConstants.PARAM_LANGUAGE, params.getLocale().getLanguage());
        JSONHelper.putValue(env, KEY_SUPPORTED_LOCALES, PropertyUtil.getSupportedLocales());
        final DecimalFormatSymbols dfs = new DecimalFormatSymbols(params.getLocale());
        JSONHelper.putValue(env, KEY_DECIMAL_SEPARATOR, Character.toString(dfs.getDecimalSeparator()));

        // setup user info
        final JSONObject user = params.getUser().toJSON();
        JSONHelper.putValue(user, KEY_APIKEY, params.getAPIkey());
        JSONHelper.putValue(env, KEY_USER, user);

        // setup env urls info (api, terms of use, "geoportal url?")
        JSONObject urlConfig = new JSONObject();
        JSONHelper.putValue(urlConfig, KEY_API, getAPIurl(params));
        JSONHelper.putValue(urlConfig, KEY_LOGIN, getLoginUrl());
        JSONHelper.putValue(urlConfig, KEY_REGISTER, getRegisterUrl());
        JSONHelper.putValue(urlConfig, KEY_LOGOUT, getLogoutUrl());
        JSONHelper.putValue(env, KEY_URLS, urlConfig);

        // setup appsetup info
        JSONObject viewConfig = new JSONObject();
        JSONHelper.putValue(viewConfig, KEY_UUID, view.getUuid());
        // should type be only system OR user?
        // for links the main interest is to know if the link would point to a non-public user view
        // for other functionality it might be interesting to check if we are in a published map or a geoportal view
        JSONHelper.putValue(viewConfig, KEY_TYPE, view.getType().toLowerCase());
        JSONHelper.putValue(viewConfig, KEY_ISPUBLIC, view.isPublic());
        JSONHelper.putValue(env, KEY_APPSETUP, viewConfig);

        // setup additional default views info
        JSONHelper.putValue(env, KEY_DEFAULT_VIEWS, new JSONArray(DEFAULT_VIEWS));

        // setup markers SVG info
        try {
            InputStream inp = EnvHelper.class.getResourceAsStream(SVG_MARKERS_JSON);
            if (inp != null) {
                JSONArray svgMarkers = JSONHelper.createJSONArray(IOHelper.readString(inp));
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
            // this isn't really necessary any more
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

    public static String getLoginUrl() {
        return loginUrl;
    }
    public static String getRegisterUrl() {
        return registerUrl;
    }
    public static String getLogoutUrl() {
        return logoutUrl;
    }
}
