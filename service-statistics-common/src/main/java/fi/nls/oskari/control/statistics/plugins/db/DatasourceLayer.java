package fi.nls.oskari.control.statistics.plugins.db;

import org.oskari.user.Role;
import org.oskari.user.User;
import fi.nls.oskari.util.PropertyUtil;
import org.json.JSONObject;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * This is the value object for the statistical datasource layer
 * for example SotkaNET plugin layer mapping rows in the database.
 * MyBatis Type for the SQL table oskari_statistical_datasource_regionsets
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
    // set by mybatis mapper
    private JSONObject locale;

    private Integer orderNumber;

    private Set<Long> allowedRoles = new HashSet<>();

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

    public Integer getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(Integer orderNumber) {
        this.orderNumber = orderNumber;
    }

    public void addRoles(Collection<Long> roleIds) {
        allowedRoles.addAll(roleIds);
    }

    public boolean hasPermission(User user) {
        return user.getRoles().stream()
                .map(Role::getId)
                .anyMatch(id -> allowedRoles.contains(id));
    }

    public String getTitle(String lang) {
        return getLocalized(lang, "name");
    }

    private String getLocalized(String lang, String key) {
        if(locale == null || locale.length() == 0) {
            return null;
        }
        if (lang == null) {
            lang = PropertyUtil.getDefaultLanguage();
        }
        String value = getLocalizedValue(locale.optJSONObject(lang), key);
        if (value != null) {
            return value;
        }
        // Try default language
        if (!lang.equalsIgnoreCase(PropertyUtil.getDefaultLanguage())) {
            value = getLocalizedValue(locale.optJSONObject(PropertyUtil.getDefaultLanguage()), key);
            if (value != null) {
                return value;
            }
        }
        // Find any language
        while (locale.keys().hasNext() && value == null) {
            String randomLang = (String) locale.keys().next();
            value = getLocalizedValue(locale.optJSONObject(randomLang), key);
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
}
