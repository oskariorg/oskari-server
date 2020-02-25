package flyway.oskari;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class V1_55_5__migrate_wfslayers implements JdbcMigration {

    @Override
    public void migrate(Connection conn) throws Exception {
        // read previous config from portti_wfs_layer
        List<WFSConfig> configs = getCurrentConfigs(conn);
        /*
        Migrates portti_wfs_layer:
        - selected_feature_params
        - feature_params_locales
        - feature_namespace_url
        - max_features

        To oskari_maplayer.attributes merging the current with the new attributes.

        The feature_namespace and feature_element are already available as oskari_maplayer.name so we don't need to migrate those.
        We might need to migrate wps_params. It might be used in analysis.
         */
        List<LayerAttributes> list = migrateAttributes(conn, configs);
        // update in db
        updateAttrs(conn, list);

        /*
        After this migration we change WFSLayerConfigurationService to return stuff from oskari_maplayer and can drop some tables:

        - portti_wfs_template_model
        - portti_wfs_layers_styles
        - portti_wfs_layer_style
        - oskari_wfs_parser_config
        - portti_wfs_layer

        After that we can migrate code to use OskariLayerService instead of WFSLayerConfigurationService
        */
    }

    private List<LayerAttributes> migrateAttributes(Connection conn, List<WFSConfig> configs) throws SQLException {
        List<LayerAttributes> list = new ArrayList<>();
        for(WFSConfig data : configs) {
            try {
                list.add(migrateAttributes(data, getCurrentAttributes(conn, data.layerId)));
            } catch (SQLException ignored) {
                // ignored as db might be out of sync and layer in portti_wfs_layer might not exist in oskari_maplayer
            }
        }
        return list;
    }

    private String getStrFromArray(JSONArray arr, int index) {
        if (arr == null) {
            return null;
        }
        return (String) arr.opt(index);
    }

    protected LayerAttributes migrateAttributes(WFSConfig conf, JSONObject current) {
        LayerAttributes attr = new LayerAttributes();
        attr.id = conf.layerId;
        try {
            current.put("namespaceURL", conf.namespaceURL);
            current.put("maxFeatures", conf.maxFeatures);
            current.put("wpsParams", conf.wps_params);
            JSONObject selected = createJSON(conf.selectedAttrs);
            JSONObject locales = createJSON(conf.localeAttrs);

            JSONObject data = new JSONObject();
            AttrBuilder localesBuilder = new AttrBuilder();

            Iterator keys = selected.keys();
            while (keys.hasNext()) {
                String lang = (String) keys.next();
                JSONArray attrsForLang = selected.optJSONArray(lang);
                JSONArray localesForLang = locales.optJSONArray(lang);
                if(attrsForLang == null) {
                    continue;
                }

                for (int i = 0; i < attrsForLang.length(); ++i) {
                    String field = attrsForLang.optString(i);
                    localesBuilder.addAttr(lang, field);
                    localesBuilder.addAttrLocale(lang, field, getStrFromArray(localesForLang, i));
                }
            }

            if (localesBuilder.hasMultipleLanguages()) {
                data.put("filter", localesBuilder.getMultiLanguageFilter());
            } else {
                List<String> filterList = localesBuilder.getSingleFilter();
                if (!filterList.isEmpty()) {
                    data.put("filter", new JSONArray(filterList));
                }
            }
            if (!localesBuilder.getLocales().isEmpty()) {
                data.put("locale", new JSONObject(localesBuilder.getLocales()));
            }
            if (data.keys().hasNext()) {
                // attach data if its not empty
                current.put("data", data);
            }

            attr.attrs = current.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Error migrating WFS configs", ex);
        }
        return attr;
    }

    private void updateAttrs(Connection conn, List<LayerAttributes> attrs) throws SQLException {
        String sql = "UPDATE oskari_maplayer SET attributes = ? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for(LayerAttributes data : attrs) {
                ps.setString(1, data.attrs);
                ps.setInt(2, data.id);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
    private JSONObject getCurrentAttributes(Connection conn, int layerId) throws SQLException {
        String sql = "select attributes from oskari_maplayer where id=" + layerId;

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return createJSON(rs.getString("attributes"));
            }
        }
        throw new SQLException("No such layer:" + layerId);
    }

    private List<WFSConfig> getCurrentConfigs(Connection conn) throws SQLException {
        String sql = "select selected_feature_params, feature_params_locales, maplayer_id, feature_namespace_uri, max_features, wps_params from portti_wfs_layer";
        List<WFSConfig> list = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while(rs.next()) {
                WFSConfig config = new WFSConfig();
                config.selectedAttrs = rs.getString("selected_feature_params");
                config.localeAttrs = rs.getString("feature_params_locales");
                config.namespaceURL = rs.getString("feature_namespace_uri");
                config.layerId = rs.getInt("maplayer_id");
                config.maxFeatures = rs.getInt("max_features");
                config.wps_params = rs.getString("wps_params");
                list.add(config);
            }
        }
        return list;
    }

    private JSONObject createJSON(String json) {
        if (json == null) {
            return new JSONObject();
        }
        try {
            return new JSONObject(json);
        } catch (JSONException ignored) {}

        // couldn't parse it so it's not working anyway -> rewrite it
        return new JSONObject();
    }

    class AttrBuilder {

        private Map<String, List<String>> langAttrs = new HashMap<>();
        private Map<String, Map<String, String>> newLocales = new HashMap<>();

        void addAttr(String lang, String attrName) {
            List<String> list = langAttrs.getOrDefault(lang, new ArrayList<>());
            list.add(attrName);
            langAttrs.put(lang, list);
        }

        void addAttrLocale(String lang, String attrName, String uiName) {
            if (uiName == null || uiName.isEmpty()) {
                return;
            }
            Map<String, String> locale = newLocales.getOrDefault(lang, new HashMap<>());
            locale.put(attrName, uiName);
            newLocales.put(lang, locale);
        }

        Map<String, Map<String, String>> getLocales() {
            return newLocales;
        }

        boolean hasMultipleLanguages() {
            return langAttrs.keySet().size() > 1;
        }

        List<String> getSingleFilter() {
            List<String> list = new ArrayList<>();
            if (langAttrs.isEmpty()) {
                return list;
            }
            // just return the first
            return langAttrs.values().iterator().next();
        }

        JSONObject getMultiLanguageFilter() {
            return new JSONObject(langAttrs);
        }
    }

    public WFSConfig createConfig(String selected, String locale, String url, int layer, int count) {
        WFSConfig conf = new WFSConfig();
        conf.selectedAttrs = selected;
        conf.localeAttrs = locale;
        conf.namespaceURL = url;
        conf.layerId = layer;
        conf.maxFeatures = count;
        return conf;
    }


    class LayerAttributes {
        int id = -1;
        String attrs;
    }

    class WFSConfig {
        String selectedAttrs;
        String localeAttrs;
        String namespaceURL;
        String wps_params;
        int layerId = -1;
        int maxFeatures = -1;
    }
}
