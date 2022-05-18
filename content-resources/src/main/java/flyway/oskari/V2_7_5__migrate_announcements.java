package flyway.oskari;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import fi.nls.oskari.util.PropertyUtil;

public class V2_7_5__migrate_announcements extends BaseJavaMigration {
    private static final Logger LOG = LogFactory.getLogger(V2_7_5__migrate_announcements.class);

    @Override
    public void migrate(Context context) throws Exception {
        Connection conn = context.getConnection();
        JSONArray announcements = getAnnouncements(conn);
        for(int i = 0; i < announcements.length(); i++) {
            JSONObject announcement = announcements.getJSONObject(i);
            updateAnnouncements(conn, announcement);
        }
    }

    private void updateAnnouncements (Connection conn, JSONObject announcement) throws SQLException {
        JSONObject locale = new JSONObject();
        JSONObject lang = new JSONObject();

        try {
            lang.put("title", announcement.getString("title"));
            lang.put("content", announcement.getString("content"));
            locale.put(PropertyUtil.getDefaultLanguage(), lang);
        } catch (JSONException e) {
            LOG.warn("Failed to create announcement locale ", e);
        }

        final String sql = "UPDATE oskari_announcements SET locale=? where id=?";
        try(PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, locale.toString());
            statement.setInt(2, announcement.getInt("id"));
            statement.execute();
        } catch (JSONException e) {
            LOG.warn("Failed to update announcements ", e);
        }
    }

    private JSONArray getAnnouncements(Connection conn) throws SQLException {
        JSONArray announcements = new JSONArray();
        // only WFS layers have WPS params
        final String sql = "SELECT id, title, content FROM oskari_announcements";
        try(PreparedStatement statement = conn.prepareStatement(sql)) {
            try (ResultSet rs = statement.executeQuery()) {
                while(rs.next()) {
                    JSONObject announcement = new JSONObject();
                    announcement.put("id", rs.getInt("id"));
                    announcement.put("title", rs.getString("title"));
                    announcement.put("content", rs.getString("content"));
                    announcements.put(announcement);
                }
            } catch (JSONException e) {
                LOG.warn("Failed to fetch announcements for migration ", e);
            }
        }
        LOG.info("Found", announcements.length(), "announcements for migration");
        return announcements;
    }
}
