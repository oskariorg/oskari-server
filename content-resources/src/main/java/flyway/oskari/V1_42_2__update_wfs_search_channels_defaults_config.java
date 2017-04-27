package flyway.oskari;

import fi.nls.oskari.util.JSONHelper;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Renames defaults.village key to defaults.region if the search channel defaults config exists.
 *
 * Defaults config for search results in database oskari_wfs_search_channels-table config-column:
 * {
 *     "defaults" : {
 *         "village" : "",
 *         "desc" : "",
 *         "locationType" : ""
 *     }
 * }
 */
public class V1_42_2__update_wfs_search_channels_defaults_config implements JdbcMigration {

    public void migrate(Connection connection) throws Exception {
        // oskari_wfs_search_channels config
        final ArrayList<Channel> configs = getChannelConfigs(connection);
        for(Channel channel: configs) {
            if(!modifyConfig(channel)) {
                continue;
            }
            // update config back to db
            updateChannelConfig(connection, channel);
        }
    }

    class Channel {
        long id;
        JSONObject config;
    }

    private ArrayList<Channel> getChannelConfigs(Connection connection) throws Exception {
        ArrayList<Channel> list = new ArrayList<>();
        final String sql = "SELECT id, config FROM oskari_wfs_search_channels";
        try (final PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while(rs.next()) {
                Channel b = new Channel();
                b.id = rs.getLong("id");
                b.config = JSONHelper.createJSONObject(rs.getString("config"));
                list.add(b);
            }
        }
        return list;
    }

    private boolean modifyConfig(Channel channel) throws Exception {
        if(channel.config == null) {
            return false;
        }
        JSONObject defaults = channel.config.optJSONObject("defaults");
        if(defaults == null) {
            return false;
        }
        String region = defaults.optString("village");
        if(region == null) {
            return false;
        }
        JSONHelper.putValue(defaults, "region", region);
        defaults.remove("village");
        return true;
    }

    public static void updateChannelConfig(Connection connection, Channel channel)
            throws SQLException {
        final String sql = "UPDATE oskari_wfs_search_channels SET " +
                "config=? " +
                " WHERE id=?";

        try (final PreparedStatement statement =
                     connection.prepareStatement(sql)) {
            statement.setString(1, channel.config.toString());
            statement.setLong(2, channel.id);
            statement.execute();
        }
    }
}
