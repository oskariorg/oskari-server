package fi.nls.oskari.control.view.modifier.bundle;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.view.modifier.ModifierParams;
import org.json.JSONObject;

import java.util.Map;

/**
 * Helper for
 */
public class LogoPluginHandler implements PluginHandler {

    private static final Logger LOGGER = LogFactory.getLogger(LogoPluginHandler.class);
    public static final String PLUGIN_NAME = "Oskari.mapframework.bundle.mapmodule.plugin.LogoPlugin";
    public static final String KEY_ID = "id";
    public static final String KEY_CONFIG = "config";
    public static final String KEY_MAP_URL = "mapUrlPrefix";
    public static final String KEY_TERMS_URL = "termsUrl";
    public static final String KEY_GEOPORTAL_LINK = "geoportalLink";

    @Override
    public boolean modifyPlugin(final JSONObject plugin,
                                final ModifierParams params,
                                final String mapSrs) {
        return setupLogoPluginConfig(plugin);
    }

    private boolean setupLogoPluginConfig(final JSONObject originalPlugin) {
        if(originalPlugin == null) {
            LOGGER.debug("Tried to modify LogoPlugin URLS, but plugin didn't exist!");
            return false;
        }
        if(!PLUGIN_NAME.equals(originalPlugin.optString(KEY_ID))) {
            LOGGER.debug("Tried to modify LogoPlugin URLS, but given JSON isn't LogoPlugin!");
            return false;
        }

        JSONObject config = getConfig(originalPlugin);
        setupMapUrl(config);
        setupTerms(config);
        if (!PropertyUtil.getOptional("plugin.logo.geoportalLink", true)) {
            JSONHelper.putValue(config, KEY_GEOPORTAL_LINK, false);
        }

        return true;
    }

    private JSONObject getConfig(JSONObject original) {
        JSONObject config = original.optJSONObject(KEY_CONFIG);
        if(config == null) {
            config = new JSONObject();
            JSONHelper.putValue(original, KEY_CONFIG, config);
        }
        return config;
    }

    private void setupMapUrl(final JSONObject config) {
        if(config.has(KEY_MAP_URL)) {
            return;
        }
        final Object urlObj = PropertyUtil.getLocalizableProperty("oskari.map.url", null);
        // single value configured
        if(urlObj instanceof String) {
            JSONHelper.putValue(config, KEY_MAP_URL, urlObj);
        } else if(urlObj instanceof Map) {
            // localized values configured
            Map<String, String> values = (Map<String, String>) urlObj;
            JSONHelper.putValue(config, KEY_MAP_URL, new JSONObject(values));
        }
    }

    private void setupTerms(final JSONObject config) {
        if(config.has(KEY_TERMS_URL)) {
            return;
        }
        final Object termsObj = PropertyUtil.getLocalizableProperty("oskari.map.terms.url");
        if(termsObj instanceof String) {
            JSONHelper.putValue(config, KEY_TERMS_URL, termsObj);
        } else if(termsObj instanceof Map) {
            Map<String, String> values = (Map<String, String>) termsObj;
            JSONHelper.putValue(config, KEY_TERMS_URL, new JSONObject(values));
        }
    }
}
