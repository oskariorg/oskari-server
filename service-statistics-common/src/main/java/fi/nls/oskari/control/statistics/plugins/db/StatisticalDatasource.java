package fi.nls.oskari.control.statistics.plugins.db;

import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This is the value object for the statistical indicator data source definition rows in the database.
 * MyBatis Type for the SQL table oskari_statistical_datasource
 */
public class StatisticalDatasource {

    private long id;
    private String locale;
    private String config;
    private String plugin;
    private List<DatasourceLayer> layers = new ArrayList<>();
    private long maxUpdateDuration = TimeUnit.HOURS.toMillis(6);
    private long updateInterval = TimeUnit.HOURS.toMillis(24);
    private long cachePeriod = updateInterval * 7;
    private JSONObject hints;

    public List<DatasourceLayer> getLayers() {
        return layers;
    }

    public void setLayers(List<DatasourceLayer> layers) {
        this.layers = layers;
    }

    /**
     * How long the update process is allowed to last before
     * we can safely determine something horrible has happened
     * 
     * TODO: Remove hard-coded value
     */
    public long getMaxUpdateDuration() {
        return maxUpdateDuration;
    }

    public long getUpdateInterval() {
        return updateInterval;
    }

    public long getCachePeriod() {
        return cachePeriod;
    }

    /**
     * The <plugin> in the plugin implementing class @Oskari("<name>") annotation.
     *
     * The plugin class must extend OskariComponent and implement StatisticalDatasourcePlugin interface
     * and be situated in the classpath.
     */
    public String getPlugin() {
        return plugin;
    }

    public void setPlugin(String plugin) {
        this.plugin = plugin;
    }
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLocale() {
        return locale;
    }

    private JSONObject localeJSON = null;

    public String getName(String lang) {
        final String key = "name";
        if(localeJSON == null) {
            localeJSON =  JSONHelper.createJSONObject(locale);
        }
        if(localeJSON == null) {
            // nothing to use as name
            return null;
        }
        if(lang == null) {
            lang = PropertyUtil.getDefaultLanguage();
        }

        String value = getLocalizedValue(localeJSON.optJSONObject(lang), key);
        if (value != null) {
            return value;
        }
        // Try default language
        if (!lang.equalsIgnoreCase(PropertyUtil.getDefaultLanguage())) {
            value = getLocalizedValue(localeJSON.optJSONObject(PropertyUtil.getDefaultLanguage()), key);
            if (value != null) {
                return value;
            }
        }
        // Find any language
        while (localeJSON.keys().hasNext() && value == null) {
            String randomLang = (String) localeJSON.keys().next();
            value = getLocalizedValue(localeJSON.optJSONObject(randomLang), key);
        }
        return value;
    }

    private String getLocalizedValue(JSONObject langJSON, String key) {
        if (langJSON == null) {
            return null;
        }
        String value = langJSON.optString(key).trim();
        if (!value.isEmpty()) {
            return value;
        }
        return null;
    }

    public void setLocale(String locale) {
        this.locale = locale;
        localeJSON = null;
    }
    private JSONObject resolveHints() {
        JSONObject config = getConfigJSON();
        if(config == null) {
            return new JSONObject();
        }
        JSONObject hints = config.optJSONObject("hints");
        if(hints == null) {
            return new JSONObject();
        }
        return hints;
    }

    public JSONObject getHints() {
        if(hints == null) {
            hints = resolveHints();
        }
        return hints;
    }
    public JSONObject getConfigJSON() {
        JSONObject obj = JSONHelper.createJSONObject(config);
        if(obj != null) {
            return obj;
        }
        return new JSONObject();
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }
}
