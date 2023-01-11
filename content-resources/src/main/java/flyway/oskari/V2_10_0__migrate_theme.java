package flyway.oskari;

import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.ViewTypes;
import fi.nls.oskari.util.JSONHelper;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.json.JSONObject;
import org.oskari.helpers.AppSetupHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class V2_10_0__migrate_theme extends BaseJavaMigration {
    private static final String BUNDLE = "mapfull";
    //appsetup.metadata
    /*
    {
    // transform style to theme
        "style":{
            "font":"arial",
            "toolStyle":"rounded-light"
        },
        "preview":"desktop",
        "name":"valkoinen.menu",
        "domain":"valkoinen.menu",
        "toolLayout":"userlayout",
        "language":"fi"
    }
     */

    // mapfull.config
    /*
    {
        "plugins": [...],
        "mapOptions": {
        // remove style here
            "style": {
                "toolStyle": "rounded-light",
                "font": "arial"
            },
            "maxExtent": {...}
        },
        "layers": [...]
    }
     */
    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        List<Long> ids = AppSetupHelper.getSetupsForType(connection, ViewTypes.DEFAULT, ViewTypes.PUBLISHED, ViewTypes.USER);
        for (Long id: ids) {
            JSONObject style = removeMapStyle(connection, id);
            JSONObject metadata = getAppSetupMetadata(connection, id);

            if (style != null) {
                // transform toolStyle/font to theme and add it to metadata
                JSONObject theme = generateTheme(style.optString("toolStyle"), style.optString("font"));
                JSONHelper.putValue(metadata, "theme", theme);
            }
            // remove unused style (replaced by theme)
            metadata.remove("style");
            updateAppMetadata(connection, id, metadata);
        }
    }
    private JSONObject removeMapStyle (Connection connection, long id) throws Exception {
        Bundle bundle = AppSetupHelper.getAppBundle(connection, id, BUNDLE);
        if (bundle == null) {
            return null;
        }
        JSONObject config = bundle.getConfigJSON();
        if (config == null) {
            return null;
        }
        JSONObject mapOptions = config.optJSONObject("mapOptions");
        if (mapOptions == null) {
            return null;
        }
        JSONObject style = mapOptions.optJSONObject("style");
        if (style == null) {
            return null;
        }
        mapOptions.remove("style");
        AppSetupHelper.updateAppBundle(connection, id, bundle);
        return style;
    }

    private JSONObject getAppSetupMetadata(Connection conn, long id) throws SQLException {
        try (PreparedStatement statement = conn
                .prepareStatement("SELECT metadata FROM oskari_appsetup WHERE id=?")) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return JSONHelper.createJSONObject(rs.getString("metadata"));
            }
        }
    }

    private void updateAppMetadata(Connection connection, long viewId, JSONObject metadata)
            throws SQLException {
        final String sql = "UPDATE oskari_appsetup SET metadata=? WHERE id=?";

        try (final PreparedStatement statement =
                     connection.prepareStatement(sql)) {
            statement.setString(1, metadata.toString());
            statement.setLong(2, viewId);
            statement.execute();
        }
    }

    protected JSONObject generateTheme(String toolStyle, String fontStyle) {
        String style = toolStyle;
        if (style == null) {
            style = "rounded-dark";
        }
        String font = fontStyle;
        if (font == null) {
            font = "arial";
        }
        JSONObject theme = new JSONObject();
        JSONObject mapTheme = new JSONObject();
        JSONHelper.putValue(theme, "map", mapTheme);
        JSONHelper.putValue(mapTheme, "font", font);

        JSONObject nav = new JSONObject();
        JSONHelper.putValue(mapTheme, "navigation", nav);
        JSONHelper.putValue(nav, "roundness", getRoundness(style));
        JSONHelper.putValue(nav, "opacity", 0.8);
        if (style.startsWith("3d-")) {
            JSONHelper.putValue(nav, "effect", "3D");
        }

        JSONObject navColor = new JSONObject();
        JSONHelper.putValue(nav, "color", navColor);
        JSONHelper.putValue(navColor, "primary", getButtonPrimaryColor(style));
        JSONHelper.putValue(navColor, "accent", "#ffd400");
        JSONHelper.putValue(navColor, "text", getTextColor(style));

        JSONObject mainColor = new JSONObject();
        JSONHelper.putValue(mapTheme, "color", mainColor);
        JSONObject headerColor = new JSONObject();
        JSONHelper.putValue(mainColor, "header", headerColor);
        JSONHelper.putValue(headerColor, "bg", getPopupHeaderColor(style));

        return theme;
    }

    private int getRoundness(String style) {
        if (style.startsWith("rounded-")) {
            return 100;
        } else if (style.startsWith("3d-")) {
            return 20;
        }
        return 0;
    }

    private String getPopupHeaderColor(String style) {
        if (style.endsWith("-light")) {
            return "#ffffff";
        }
        return "#3c3c3c";
    }
    private String getButtonPrimaryColor(String style) {
        if (style.endsWith("-light")) {
            return "#ffffff";
        }
        return "#141414";
    }
    private String getTextColor(String style) {
        if (style.endsWith("-light")) {
            return "#000000";
        }
        return "#ffffff";
    }
}
