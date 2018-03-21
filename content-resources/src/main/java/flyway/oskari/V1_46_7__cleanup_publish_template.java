package flyway.oskari;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Publishing functionality now uses the mapOptions from the appsetup where the publish was made.
 * This enables publishing using different projections and also makes sure the zoom levels etc map
 * configuration matches the ones on the preview.
 *
 * Because of the above publish template no longer needs to have mapOptions at all. This migration removes it from all
 * appsetups/views of type PUBLISH (== publish templates)
 */
public class V1_46_7__cleanup_publish_template implements JdbcMigration {

    public void migrate(Connection connection) throws SQLException {
        for(Bundle bundle: getMapFullConfigsForPublishTemplates(connection)) {
            try {
                JSONObject config = new JSONObject(bundle.config);
                config.remove("mapOptions");
                bundle.config = config.toString();
                updateBundleConfig(connection, bundle);
            } catch (JSONException ignored) {
                // don't really care since this is cleanup that is not strictly necessary
            }
        }
    }

    private List<Bundle> getMapFullConfigsForPublishTemplates(Connection connection) throws SQLException {
        final String sql = "SELECT bundle_id, view_id, config FROM portti_view_bundle_seq" +
                " WHERE bundle_id = (select id from portti_bundle where name = 'mapfull') AND" +
                " view_id IN (select id from portti_view where type = 'PUBLISH')";
        List<Bundle> bundles = new ArrayList<>();
        try (final PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while(rs.next()) {
                Bundle b = new Bundle();
                b.view = rs.getLong("view_id");
                b.bundle = rs.getLong("bundle_id");
                b.config = rs.getString("config");
                bundles.add(b);
            }
        }
        return bundles;
    }

    private void updateBundleConfig(Connection connection, Bundle bundle)
            throws SQLException {
        final String sql = "UPDATE portti_view_bundle_seq SET config=? WHERE bundle_id=? AND view_id=?";

        try (final PreparedStatement statement =
                     connection.prepareStatement(sql)) {
            statement.setString(1, bundle.config);
            statement.setLong(2, bundle.bundle);
            statement.setLong(3, bundle.view);
            statement.execute();
        }
    }

    class Bundle {
        long view;
        long bundle;
        String config;
    }
}
