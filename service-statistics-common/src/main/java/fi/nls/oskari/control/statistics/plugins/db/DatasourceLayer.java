package fi.nls.oskari.control.statistics.plugins.db;

import fi.nls.oskari.util.PropertyUtil;
import org.json.JSONObject;

/**
 * This is the value object for the statistical datasource layer
 * for example SotkaNET plugin layer mapping rows in the database.
 * MyBatis Type for the SQL table oskari_statistical_layers
 */
public class DatasourceLayer {

    /**
     * Id from datasource
     */
    private long datasourceId;
    /**
     *  The layer id in Oskari, for example: 7. This maps to the name in the oskari_maplayers table.
     */
    private long maplayerId;

    /**
     *  For example one of:
     *  {"regionType" : "Kunta"}
     *  or "Maakunta","Erva","Aluehallintovirasto","Sairaanhoitopiiri","Seutukunta",
     *  "Nuts1","ELY-KESKUS" for SotkaNet data
     *  Note: These are case independent and the above list is possibly not exhaustive.
     *  These are the layer names used by the plugin.
     */
    private JSONObject config;

    // {"fi":{"name":"Pohjois-Karjalan maakuntakaava: Aluevaraukset"}}} from oskari_maplayer.locale
    private JSONObject locale;

    public long getDatasourceId() {
        return datasourceId;
    }

    public void setDatasourceId(long datasourceId) {
        this.datasourceId = datasourceId;
    }

    public long getMaplayerId() {
        return maplayerId;
    }

    public void setMaplayerId(long maplayerId) {
        this.maplayerId = maplayerId;
    }

    public String getConfig(String key) {
        if(config == null) {
            return null;
        }
        return config.optString(key, null);
    }
    public JSONObject getConfig() {
        return config;
    }

    public void setConfig(JSONObject config) {
        this.config = config;
    }


    public String getTitle(String lang) {
        JSONObject langJSON = getLocalized(lang);
        if(langJSON == null) {
            return null;
        }
        return langJSON.optString("name");
    }
    private JSONObject getLocalized(String lang) {
        if(locale == null || locale.length() == 0) {
            return null;
        }
        if (lang == null) {
            lang = PropertyUtil.getDefaultLanguage();
        }
        final JSONObject value = locale.optJSONObject(lang);
        if(value != null) {
            return value;
        }

        if (!lang.equalsIgnoreCase(PropertyUtil.getDefaultLanguage())) {
            final JSONObject defaultValue = locale.optJSONObject(PropertyUtil.getDefaultLanguage());
            if(defaultValue != null) {
                return defaultValue;
            }
        }
        final String randomLang = (String) locale.keys().next();
        return locale.optJSONObject(randomLang);
    }
}
