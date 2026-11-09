package flyway.myfeatures_userlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.json.JSONException;
import org.json.JSONObject;

import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.PropertyUtil;

public class V1_2__remove_default_locales extends BaseJavaMigration {

    private static final Logger LOG = LogFactory.getLogger(V1_2__remove_default_locales.class);
    private static final Set<String> SUPPORTED_KEYS = Set.of("name", "desc", "source");

    public void migrate(Context ctx) throws Exception {
        DatasourceHelper helper = DatasourceHelper.getInstance();
        List<Layer> layers = null;
        int removedDefaults = 0;
        int removedUnrecognized = 0;
        int addedDefaults = 0;

        try (Connection c = helper.getDataSource(helper.getOskariDataSourceName("myfeatures")).getConnection()) {
            layers = getLayers(c);
            if (layers.isEmpty()) {
                return;
            }
            for (Layer layer: layers) {
                JSONObject locale = parseJSONObject(layer.locale);
                boolean modifiedDefaults = removeLocaleDefaults(locale, getLocaleFieldForMigration());
                if (modifiedDefaults) {
                    removedDefaults++;
                }
                boolean modifiedFields = removeUnrecognizedFields(locale);
                if (modifiedFields) {
                    removedUnrecognized++;
                }
                boolean addedFields = addMissingDefaults(locale);
                if (addedFields) {
                    addedDefaults++;
                }
                if (modifiedFields || modifiedDefaults || addedFields) {
                    updateLocale(c, layer.id, locale.toString());
                }
            }
        }
        LOG.info("Updated myfeatures layer locales. From", layers.size(), "layers:"
        , "\n - removed default names from", removedDefaults
        , "\n - removed unrecognized keys from", removedUnrecognized
        , "\n - added missing default name to", addedDefaults);
    }

    record Layer(UUID id, String locale) { }

    private static List<Layer> getLayers(Connection c) throws Exception {
        String sql = "SELECT id, locale FROM myfeatures_layer";
        List<Layer> result = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                UUID layerId = rs.getObject("id", UUID.class);
                result.add(new Layer(layerId, rs.getString("locale")));
            }
        }
        return result;
    }

    // If the name for default language has NOT been changed from the default name, we keep the translations for other languages.
    // If name for default language has been changed, BUT the layer has default names for other languages, we remove those translations.
    public boolean removeLocaleDefaults(JSONObject locale, String field) {
        // loop locale keys as languages, find name, compare to default and remove when needed.
        if (locale == null) {
            return false;
        }

        String defaultLang = PropertyUtil.getDefaultLanguage();
        Set<String> supportedLangs = Set.of(PropertyUtil.getSupportedLanguages());
        // locale for default language is mandatory so use getJSONObject directly
        JSONObject defaultLocale = locale.getJSONObject(defaultLang);
        String valueOnDefaultLang = defaultLocale.optString(field);
        Set<String> defaults = getDefaultValuesForLang(defaultLang);
        boolean defaultNotChanged = defaults.stream().anyMatch(value -> value.equals(valueOnDefaultLang));
        if (defaultNotChanged) {
            // Default language name is default, we don't remove any translations.
            return false;
        }
        boolean modified = false;
        // Remove translations for unsupported languages and those with default names.
        for (String lang : locale.keySet().toArray(new String[0])) {
            if (lang.equals(defaultLang)) {
                continue;
            }
            if (!supportedLangs.contains(lang)) {
                locale.remove(lang);
                modified = true;
                continue;
            }
            JSONObject langLocale = locale.optJSONObject(lang);
            if (langLocale == null) {
                continue;
            }
            String fieldValue = langLocale.optString(field);
            Set<String> defaultValueOptions = getDefaultValuesForLang(lang);
            if (defaultValueOptions.contains(fieldValue)) {
                langLocale.remove(field);
                modified = true;
            }
        }
        return modified;
    }

    public boolean removeUnrecognizedFields(JSONObject locale) {
        // loop locale keys as languages, find unrecognized fields and remove them if needed
        if (locale == null) {
            return false;
        }

        boolean modified = false;
        // Remove translations for unsupported languages and those with default names.
        for (String lang : locale.keySet().toArray(new String[0])) {
            JSONObject langLocale = locale.optJSONObject(lang);
            if (langLocale == null) {
                continue;
            }
            for (String key : langLocale.keySet().toArray(new String[0])) {
                if (!SUPPORTED_KEYS.contains(key)) {
                    langLocale.remove(key);
                    modified = true;
                }
            }
        }
        return modified;
    }

    public boolean addMissingDefaults(JSONObject locale) {
        // loop locale keys as languages, find ones with missing names and add defaults
        if (locale == null) {
            return false;
        }
        String defaultLang = PropertyUtil.getDefaultLanguage();

        HashSet<String> nameFoundFor = new HashSet<>();
        for (String lang : locale.keySet().toArray(new String[0])) {
            JSONObject langLocale = locale.optJSONObject(lang);
            if (langLocale == null) {
                continue;
            }
            String nameForLang = langLocale.optString("name");
            if (nameForLang != null && !nameForLang.trim().isBlank()) {
                nameFoundFor.add(lang);
            }
        }
        if (nameFoundFor.contains(defaultLang)) {
            return false;
        }
        JSONObject defaultLangLocale = locale.optJSONObject(defaultLang);
        if (defaultLangLocale == null) {
            defaultLangLocale = new JSONObject();
            locale.put(defaultLang, defaultLangLocale);
        }
        if (nameFoundFor.isEmpty()) {
            // no name in any language, set default for the default language
            defaultLangLocale.put("name", getDefaultValueForLang(defaultLang));
        } else {
            // we have a name in some language that is not the default language of this instance
            // pick the first language with a name and use it as name for the default language
            String lang = nameFoundFor.iterator().next();
            JSONObject langLocale = locale.optJSONObject(lang);
            String nameInSomeLanguage = langLocale.optString("name");
            defaultLangLocale.put("name", nameInSomeLanguage);
        }
        return true;
    }

    // for app specific locale field migration, override this method to change which locale field is checked for defaults
    //  (mainly desc or name, but some apps might have other skeletons in their closet)
    public String getLocaleFieldForMigration() {
        return "name";
    }
    // for app specific locale default names, override this method to change which names are considered default for each language
    public Set<String> getDefaultValuesForLang(String lang) {
        return switch (lang) {
            case "fi" -> Set.of("Oma karttataso", "Omat aineistot");
            case "sv" -> Set.of("Mitt kartlage", "Användarens kartlager");
            case "en" -> Set.of("My map layer", "User layers");
            case "is" -> Set.of("Kortalagið mitt");
            default -> Set.of();
        };
    }

    // for app specific locale default names, override this method to change which names are considered default for each language
    public String getDefaultValueForLang(String lang) {
        return switch (lang) {
            case "fi" -> "Omat aineistot";
            case "sv" -> "Användarens kartlager";
            case "is" -> "Kortalagið mitt";
            default -> "User layer";
        };
    }

    private static void updateLocale(Connection c, UUID id, String locale) throws Exception {
        String sql = "UPDATE myfeatures_layer SET locale=?::json WHERE id=?";
        try (final PreparedStatement statement = c.prepareStatement(sql)) {
            statement.setString(1, locale);
            statement.setObject(2, id);
            statement.execute();
        }
    }

    // Simplified version of JSONObjectMyBatisTypeHandler.valueOf(String) (avoid the dependency)
    private static JSONObject parseJSONObject(String s) {
        if (s == null || s.isBlank()) {
            return new JSONObject();
        }
        try {
            return new JSONObject(s);
        } catch (JSONException e) {
            LOG.warn("Couldn't parse DB string to JSONObject:", s, e);
            return new JSONObject();
        }
    }
}
