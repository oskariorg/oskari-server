package flyway.oskari;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.json.JSONArray;
import org.json.JSONObject;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.domain.map.style.VectorStyle;
import fi.nls.oskari.domain.map.OskariLayer;

public class V2_11_2__migrate_maplayer_styles extends BaseJavaMigration  {
    private static final String KEY_OSKARI = "styles";
    private static final String KEY_EXTERNAL = "externalStyles";
    private static final String DEFAULT_STYLE = "default";
    private static final List<String> FEATURE_STYLE_KEYS = Arrays.asList("fill", "stroke", "image", "text");
    private static final List<String> MAPBOX_STYLE_KEYS = Arrays.asList("version", "layers", "sources");
    private static final List<String> CESIUM_STYLE_KEYS = Arrays.asList("show", "color");

    @Override
    public void migrate(Context context) throws Exception {
        Logger log = LogFactory.getLogger(V2_11_2__migrate_maplayer_styles.class);
        Connection conn = context.getConnection();

        List<OskariLayer> layers = getLayers(conn);
        log.info("Found: " + layers.size() + " layers to migrate");

        List<StyleConfig> styles = getStyleConfigs(layers);
        log.info("Parsed: " + styles.size() + " styles from layers");

        // insert styles
        for (StyleConfig style: styles) {
            insertStyle(conn, style);
        }
        // update default styles
        for (OskariLayer layer : layers) {
            updateLayerDefaultStyle(conn, layer, styles);
        }

        int count = 0;
        // update appsetup styles
        long mapfullId = getMapfullId(conn);
        // Use String as key
        Map<String, JSONObject> states = getAppsetupStates(conn, mapfullId);
        for (String appId : states.keySet()) {
            boolean update = false;
            JSONObject state = states.get(appId);
            JSONArray stateLayers = JSONHelper.getEmptyIfNull(JSONHelper.getJSONArray(state, "selectedLayers"));
            for (int i = 0; i < stateLayers.length(); i++) {
                JSONObject layer = JSONHelper.getJSONObject(stateLayers, i);
                int layerId = layer.optInt("id", -1);
                String styleName = layer.optString("style");
                Optional<StyleConfig> style = styles.stream().filter(s -> s.layerId == layerId && styleName.equals(s.name)).findFirst();
                if (style.isPresent()) {
                    JSONHelper.putValue(layer, "style", style.get().id);
                    update = true;
                }
            }
            if (update) {
                try {
                    count++;
                    updateAppsetupState(conn, mapfullId, Long.parseLong(appId), state);
                } catch (SQLException e) {
                    log.error(e, "Error updating mapfull bundle state for appsetup: " + appId);
                    throw e;
                }
            }
        }
        log.info("Updated style id/name for:", count, "appsetups");
    }
    protected List<StyleConfig> getStyleConfigs (List<OskariLayer> layers) {
        List<StyleConfig> styles = new ArrayList();
        for (OskariLayer layer: layers) {
            String type = layer.getType();
            styles.addAll(getOskariStyleDefs(layer));
            if (OskariLayer.TYPE_3DTILES.equals(type)) {
                styles.addAll(getCesiumStyleDefs(layer));
            }
            if (OskariLayer.TYPE_VECTOR_TILE.equals(type)){
                styles.addAll(getMapboxStyleDefs(layer));
            }
        }
        return styles;
    }

    protected List<StyleConfig> getOskariStyleDefs(OskariLayer layer) {
        JSONObject stylesJson = JSONHelper.getJSONObject(layer.getOptions(), KEY_OSKARI);
        if (stylesJson == null) {
            return Collections.emptyList();
        }
        int layerId = layer.getId();
        if (isFeatureStyleDef(stylesJson)) {
            // style def without name
            return Collections.singletonList(new StyleConfig(layerId, VectorStyle.TYPE_OSKARI, DEFAULT_STYLE, stylesJson));
        }
        List<StyleConfig> styles = new ArrayList<>();

        Iterator it = stylesJson.keys();
        while(it.hasNext()) {
            String name = (String) it.next();
            JSONObject styleJson = JSONHelper.getJSONObject(stylesJson, name);
            StyleConfig style = parseOskari(layerId, name, styleJson);
            if (style != null) {
                styles.add(style);
            }
        }
        return styles;
    }

    protected StyleConfig parseOskari (int layerId, String name, JSONObject style) {
        if (style == null) {
            return null;
        }
        // 3D-layers have not required featureStyle
        // for consistency wrap inside featureStyle
        if (isFeatureStyleDef(style)) {
            style = JSONHelper.createJSONObject("featureStyle", style);
        }
        // Bypass possible layer definitions
        if (!style.has("featureStyle") && !style.has("optionalStyles")) {
            Iterator itr = style.keys();
            String key = itr.hasNext() ? itr.next().toString() : "";
            return parseOskari(layerId, name, JSONHelper.getJSONObject(style, key));
        }
        String title = JSONHelper.optString(style, "title");
        style.remove("title");

        StyleConfig conf = new StyleConfig(layerId, VectorStyle.TYPE_OSKARI, name, style);
        conf.title = title;

        return conf;
    }
    private boolean isFeatureStyleDef(JSONObject styleLike) {
        return FEATURE_STYLE_KEYS.stream().filter(key -> styleLike.has(key)).findAny().isPresent();
    }
    protected List<StyleConfig> getCesiumStyleDefs (OskariLayer layer) {
        JSONObject external = JSONHelper.getJSONObject(layer.getOptions(), KEY_EXTERNAL);
        if (external == null || external.length() == 0) {
            return Collections.emptyList();
        }
        int layerId = layer.getId();
        List<StyleConfig> styles = new ArrayList<>();
        for (String name: JSONObject.getNames(external)) {
            if (CESIUM_STYLE_KEYS.contains(name)) {
                // style def without name
                return Collections.singletonList(new StyleConfig(layerId, VectorStyle.TYPE_3D, DEFAULT_STYLE, external));
            }
            try {
                JSONObject style = JSONHelper.getJSONObject(external, name);
                styles.add(new StyleConfig(layerId, VectorStyle.TYPE_3D, name, style));
            } catch (Exception ignored) {
                // if we have def without style name, style.name might be something else than style def (JSONObject)
                // in that case styles list is ignored and external json is used as style def
            }
        }
        return styles;
    }
    protected List<StyleConfig> getMapboxStyleDefs (OskariLayer layer) {
        JSONObject external = JSONHelper.getJSONObject(layer.getOptions(), KEY_EXTERNAL);
        if (external == null || external.length() == 0) {
            return Collections.emptyList();
        }
        int layerId = layer.getId();
        List<StyleConfig> styles = new ArrayList<>();
        for (String name: JSONObject.getNames(external)) {
            if (MAPBOX_STYLE_KEYS.contains(name)) {
                // style def without name
                return Collections.singletonList(new StyleConfig(layerId, VectorStyle.TYPE_MAPBOX, DEFAULT_STYLE, external));
            }
            try {
                JSONObject style = JSONHelper.getJSONObject(external, name);
                styles.add(new StyleConfig(layerId, VectorStyle.TYPE_MAPBOX, name, style));
            } catch (Exception ignored) {
                // if we have def without style name, style.name might be something else than style def (JSONObject)
                // in that case styles list is ignored and external json is used as style def
            }
        }
        return styles;
    }

    protected void insertStyle (Connection conn, StyleConfig style) throws SQLException {
        String name = style.title.isEmpty() ? style.name : style.title;
        final String sql = "INSERT INTO oskari_maplayer_style"
                + " (layer_id, type, name, style) VALUES"
                + " (?, ?, ?, ?)";
        try (PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, style.layerId);
            statement.setString(2, style.type);
            statement.setString(3, name);
            statement.setString(4, style.def.toString());
            statement.execute();
            try (ResultSet rs = statement.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new SQLException ("Couldn't get generated id for inserted style");
                }
                style.id = rs.getLong(1);
            }
        }
    }
    private void updateAppsetupState (Connection conn, long bundleId,  long appId, JSONObject state) throws SQLException {
        final String sql = "UPDATE oskari_appsetup_bundles SET state=? where bundle_id=? AND appsetup_id=?";
        try(PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, state.toString());
            statement.setLong(2, bundleId);
            statement.setLong(3, appId);
            statement.execute();
        }
    }
    protected void updateLayerDefaultStyle(Connection conn, OskariLayer layer, List<StyleConfig> styles ) throws SQLException {
        int layerId = layer.getId();
        List<StyleConfig> layerStyles = styles.stream().filter(s -> s.layerId == layerId).collect(Collectors.toList());
        if (!layerStyles.isEmpty()) {
            String style = layer.getStyle() != null ? layer.getStyle() : "";
            // select first style as default if selected not found
            StyleConfig selected = layerStyles.stream().filter(s -> style.equals(s.name)).findFirst().orElse(layerStyles.get(0));
            updateDefaultStyle(conn, layerId, String.valueOf(selected.id));
        }

    }
    protected void updateDefaultStyle (Connection conn, int layerId, String styleName) throws SQLException {
        final String sql = "UPDATE oskari_maplayer SET style=? WHERE id=?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, styleName);
            statement.setInt(2, layerId);
            statement.execute();
        }
    }
    protected long getMapfullId (Connection conn) throws SQLException {
        final String sql = "SELECT id from oskari_bundle WHERE name = 'mapfull'";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException ("Couldn't find id for mapfull bundle");
                }
                return rs.getLong("id");
            }
        }
    }
    protected List<OskariLayer> getLayers(Connection conn) throws SQLException {
        List<OskariLayer> layers = new ArrayList();
        final String sql = "SELECT id, style, type, options FROM oskari_maplayer where type in ('wfslayer','tiles3dlayer','vectortilelayer')";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    OskariLayer layer = new OskariLayer();
                    layer.setId(rs.getInt("id"));
                    layer.setStyle(rs.getString("style"));
                    layer.setType(rs.getString("type"));
                    layer.setOptions(JSONHelper.createJSONObject(rs.getString("options")));
                    layers.add(layer);
                }
            }
        }
        return layers;
    }
    protected Map<String, JSONObject> getAppsetupStates(Connection conn, long mapfullId) throws SQLException {
        Map<String, JSONObject> states = new HashMap();
        final String sql = "SELECT appsetup_id, state from oskari_appsetup_bundles where bundle_id = ?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setLong(1, mapfullId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    long id = rs.getLong("appsetup_id");
                    JSONObject state = JSONHelper.createJSONObject(rs.getString("state"));
                    states.put(Long.toString(id), state);
                }
            }
        }
        return states;
    }

    class StyleConfig {
        long id;
        int layerId;
        String type;
        String name;
        JSONObject def;
        String title = ""; // optional for oskari style
        StyleConfig(int layerId, String type, String name, JSONObject def) {
            this.layerId = layerId;
            this.type = type;
            this.name = name;
            this.def = def;
        }
    }
}
