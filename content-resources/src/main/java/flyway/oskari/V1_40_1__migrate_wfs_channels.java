package flyway.oskari;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Migrates all views of type "PUBLISHED" and having a bundle with "openlayers" referenced in the startup JSON.
 * This targets all the Openlayers 2 based published maps. The views are then programmatically
 * "republished" with the current publish template which should be Openlayers 3 based, but the
 * doesn't really care about it. You could use it to migrate published maps to any new publish template with
 * some modification to the selection of views to migrate.
 */
public class V1_40_1__migrate_wfs_channels implements JdbcMigration {

    private static final Logger LOG = LogFactory.getLogger(V1_40_1__migrate_wfs_channels.class);

    public void migrate(Connection conn)
            throws Exception {
        List<Channel> list = getChannels(conn);
        for(Channel c: list) {
            JSONObject locale = getUILabels(JSONHelper.createJSONObject(c.topic), JSONHelper.createJSONObject(c.desc));
            JSONObject config = new JSONObject();
            if(c.address) {
                JSONHelper.putValue(config, "handler", "SimpleAddress");
            }
            updateChannel(conn, c.id, locale.toString(), config.toString());
        }
    }

    public JSONObject getUILabels(JSONObject topic, JSONObject desc) {
        JSONObject response = new JSONObject();
        Iterator<String> keys = topic.keys();
        while(keys.hasNext()) {
            String language = keys.next();
            JSONObject locale = JSONHelper.createJSONObject("name", topic.optString(language));
            JSONHelper.putValue(locale, "desc", desc.optString(language));
            JSONHelper.putValue(response, language, locale);
        }
        return response;
    }

    private List<Channel> getChannels(Connection conn)
            throws SQLException {
        List<Channel> list = new ArrayList<>();
        String sql = "select id, topic, description, is_address from oskari_wfs_search_channels";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Channel c = new Channel();
                    c.id = rs.getLong("id");
                    c.topic = rs.getString("topic");
                    c.desc = rs.getString("description");
                    c.address = rs.getBoolean("is_address");
                    list.add(c);
                }
            }
        }
        return list;
    }

    private void updateChannel(Connection conn, long id, String locale, String config) throws SQLException {
        final String sql = "UPDATE oskari_wfs_search_channels SET locale=?, config=? where id=?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, locale);
            statement.setString(2, config);
            statement.setLong(3, id);
            statement.execute();
        }
    }
    class Channel {
        long id;
        String topic;
        String desc;
        boolean address;
    }

}
