package flyway.oskari;

import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Delete content from selected_feature_params and feature_params_locales for userlayers as handling has changed
 * (property_json is no longer sent to frontend)
 */
public class V1_42_4__fix_mapfull_startup implements JdbcMigration {

    public void migrate(Connection connection) throws Exception {

        Bundle mapfull = getMapFullTemplate(connection);
        if(isJSON(mapfull.startup)) {
            // everything is fine, startup id valid json -> do nothing
            return;
        }
        // content was not valid JSON, refresh with best guess
        String fixedJSON = IOHelper.readString(getClass().getResourceAsStream("NonFlywayResource_1_42_mapfull.json"));
        JSONObject json = JSONHelper.createJSONObject(fixedJSON);
        mapfull.startup = json.toString(2);
        // update template back to db
        updateBundleTemplate(connection, mapfull);
    }

    private boolean isJSON(String startup) {
        JSONObject json = JSONHelper.createJSONObject(startup);
        return json != null;
    }

    private Bundle getMapFullTemplate(Connection connection) throws Exception {
        final String sql = "SELECT id, startup FROM portti_bundle WHERE name = 'mapfull'";
        try (final PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while(rs.next()) {
                Bundle b = new Bundle();
                b.bundleId = rs.getLong("id");
                b.startup = rs.getString("startup");
                return b;
            }
        }
        return null;
    }

    public static Bundle updateBundleTemplate(Connection connection, Bundle bundle)
            throws SQLException {
        final String sql = "UPDATE portti_bundle SET startup=? WHERE id=?";

        try (final PreparedStatement statement =
                     connection.prepareStatement(sql)) {
            statement.setString(1, bundle.startup);
            statement.setLong(2, bundle.bundleId);
            statement.execute();
        }
        return null;
    }

    class Bundle {
        long bundleId;
        String startup;
    }
}