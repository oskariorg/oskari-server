package flyway.myfeatures_userlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
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

    public void migrate(Context ctx) throws Exception {
        DatasourceHelper helper = DatasourceHelper.getInstance();
        List<Layer> layers = null;
        try (Connection c = helper.getDataSource(helper.getOskariDataSourceName("myfeatures")).getConnection()) {
            layers = getLayers(c);
            if (layers.isEmpty()) {
                return;
            }
            for (Layer layer: layers) {
                JSONObject locale = parseJSONObject(layer.locale);
                boolean modified = removeLocaleDefaults(locale, layer.locale, "name");
                if (modified) {
                    updateLocale(c, layer.id, locale.toString());
                }
            }
        }
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
    protected boolean removeLocaleDefaults(JSONObject locale, String localeKey, String field) {
        // loop locale keys as languages, find name, compare to default and remove when needed.
        if (locale == null) {
            return false;
        }
        String defaultLang = PropertyUtil.getDefaultLanguage();
        Set<String> supportedLangs = Set.of(PropertyUtil.getSupportedLanguages());
        // the locales we had in the base layer by default
        
        JSONObject defaultLocale = locale.getJSONObject(defaultLang);
        String nameWithDefaultLang = defaultLocale.getString(field);
        Set<String> defaults = getDefaultNamesForLang(defaultLang);
        boolean defaultNotChanged = defaults.stream().anyMatch(name -> name.equals(nameWithDefaultLang));
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
            JSONObject langLocale = locale.getJSONObject(lang);
            String name = langLocale.getString(field);
            Set<String> defaultNames = getDefaultNamesForLang(lang);
            if (defaultNames.contains(name)) {
                langLocale.remove(field);
                modified = true;
            }
        }
        return modified;
    }

    protected Set<String> getDefaultNamesForLang(String lang) {
        return switch (lang) {
            case "fi" -> Set.of("Oma karttataso", "Omat aineistot");
            case "sv" -> Set.of("Mitt kartlage", "Användarens kartlager");
            case "en" -> Set.of("My map layer", "User layers");
            case "is" -> Set.of("Kortalagið mitt");
            default -> Set.of();
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
