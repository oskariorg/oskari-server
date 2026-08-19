package flyway.oskari;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.oskari.helpers.BundleHelper;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Removes the flyoutClazz config that was used in sample-apps to get a single tab userguide.
 * It doesn't work anymore and potentially breaks the userguide if the previously declared clazz is not defined by the app itself.
 * Current implementation can use tab-config from localization files (provided by apps) and defaults to a single tab userguide with
 * default config/implementation.
 */
public class V3_4_2__update_userguide extends BaseJavaMigration {

    private static final Logger LOG = LogFactory.getLogger(V3_4_2__update_userguide.class);
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        List<UserGuide> guides = getUserGuides(connection);
        AtomicInteger errorCount = new AtomicInteger();
        guides.forEach(guide -> {
            boolean neededUpdate = removeFlyoutClazz(guide.config);
            if(neededUpdate) {
                try {
                    updateGuide(connection, guide);
                } catch (SQLException e) {
                    errorCount.getAndIncrement();
                }
            }
        });
        if (errorCount.get() > 0) {
            LOG.error("Failed to update", errorCount.get(), "userguide bundles (tried removing flyoutClazz config).");
        }
    }

    private List<UserGuide> getUserGuides(Connection conn) throws SQLException {
        List<UserGuide> results = new ArrayList<>();
        final String sql = "SELECT appsetup_id, bundle_id, config FROM oskari_appsetup_bundles where bundle_id = (select id from oskari_bundle where name = 'userguide')";
        try(PreparedStatement statement = conn.prepareStatement(sql)) {
            try (ResultSet rs = statement.executeQuery()) {
                while(rs.next()) {
                    UserGuide userGuide = new UserGuide();
                    userGuide.appsetup = rs.getInt("appsetup_id");
                    userGuide.id = rs.getInt("bundle_id");
                    userGuide.config = JSONHelper.createJSONObject(rs.getString("config"));
                    results.add(userGuide);
                }
            }
        }
        return results;
    }

    private boolean removeFlyoutClazz(JSONObject conf) {
        if (conf == null) {
            return false;
        }
        String flyoutClazz = conf.optString("flyoutClazz", null);
        if (flyoutClazz == null) {
            return false;
        }
        if (!flyoutClazz.equals("Oskari.mapframework.bundle.userguide.SimpleFlyout")) {
            // should we notify that one is configured?
            return false;
        }
        conf.remove("flyoutClazz");
        return true;
    }

    public static void updateGuide(Connection connection, UserGuide guide)
            throws SQLException {
        final String sql = "UPDATE oskari_appsetup_bundles SET " +
                "config=?" +
                " WHERE bundle_id=? " +
                " AND appsetup_id=?";

        try (final PreparedStatement statement =
                     connection.prepareStatement(sql)) {
            statement.setString(1, guide.config.toString());
            statement.setLong(2, guide.id);
            statement.setLong(3, guide.appsetup);
            statement.execute();
        }
    }

    class UserGuide {
        long appsetup = -1;
        long id = -1;
        JSONObject config;
    }
}
