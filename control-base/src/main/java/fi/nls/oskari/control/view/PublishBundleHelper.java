package fi.nls.oskari.control.view;

import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.JSONHelper;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Set;
import java.util.TreeSet;

import static fi.nls.oskari.control.ActionConstants.KEY_ID;

/**
 * Created by SMAKINEN on 17.8.2015.
 */
public class PublishBundleHelper {

    private static final Set<String> CLASS_WHITELIST =  ConversionHelper.asSet("center", "top", "right", "bottom", "left");
    private static final String KEY_LOCATION = "location";
    private static final String KEY_CLASSES = "classes";

    private PublishBundleHelper() {

    }

    /**
     * Removes the plugin and returns the removed value or null if not found.
     * NOTE! Modifies input list
     * @param plugins
     * @param pluginId
     * @return
     */
    public static JSONObject removePlugin(final JSONArray plugins, final String pluginId) {
        if(pluginId == null || plugins == null) {
            return null;
        }
        for(int i = 0; i < plugins.length(); ++i) {
            JSONObject pluginObj = plugins.optJSONObject(i);
            if(pluginObj != null && pluginId.equals(pluginObj.optString(KEY_ID))) {
                plugins.remove(i);
                return pluginObj;
            }
        }
        return null;
    }

    /**
     * Merges user selections to bundles default config/state.
     * @param bundle bundle to configure
     * @param userConfig overrides for default config
     * @param userState overrides for default state
     * @return root configuration object containing both config and state
     */
    public static void mergeBundleConfiguration(final Bundle bundle, final JSONObject userConfig, final JSONObject userState) {
        final JSONObject defaultConfig = bundle.getConfigJSON();
        final JSONObject defaultState = bundle.getStateJSON();
        final JSONObject mergedConfig = JSONHelper.merge(defaultConfig, userConfig);
        final JSONObject mergedState = JSONHelper.merge(defaultState, userState);
        bundle.setConfig(mergedConfig.toString());
        bundle.setState(mergedState.toString());
    }


    public static JSONObject sanitizeConfigLocation(final JSONObject config) {
        if(config == null) {
            return null;
        }

        // sanitize plugin.config.location.classes
        JSONObject location = config.optJSONObject(KEY_LOCATION);
        if (location != null) {
            String classes = location.optString(KEY_CLASSES);
            if (classes != null && classes.length() > 0) {
                String[] filteredClasses = filterClasses(classes.split(" "));
                JSONHelper.putValue(location, KEY_CLASSES, StringUtils.join(filteredClasses, " "));
            }
            // Make sure we don't have inline css set
            for(String str : CLASS_WHITELIST) {
                location.remove(str);
            }
        }

        return config;
    }

    private static String[] filterClasses(String[] classes) {
        Set<String> filteredClasses = new TreeSet<String>();
        for (int i = 0; i < classes.length; i++) {
            if (CLASS_WHITELIST.contains(classes[i])) {
                filteredClasses.add(classes[i]);
            }
        }
        return filteredClasses.toArray(new String[filteredClasses.size()]);
    }

}
