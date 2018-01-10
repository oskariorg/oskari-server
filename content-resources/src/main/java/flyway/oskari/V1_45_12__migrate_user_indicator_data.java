package flyway.oskari;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.apache.commons.lang.NotImplementedException;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import org.json.JSONArray;
import org.json.JSONException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Migrates oskari_user_indicator data from sotkanet regions ids to features in regionsets.
 * Also moves data to new table in oskari_user_indicator -> oskari_user_indicator_data:
 * - data -> data
 * - layer_id -> regionset_id
 * - year -> year
 */
public class V1_45_12__migrate_user_indicator_data implements JdbcMigration {

    private static final Logger LOG = LogFactory.getLogger(V1_45_12__migrate_user_indicator_data.class);

    class UserIndicator {
        long id;
        long userId;
        long layerId;
        int year;
        String data;
        String category = "";
    }

    public void migrate(Connection conn) throws SQLException {
        if(true) throw new NotImplementedException("Not ready yet");
        List<UserIndicator> indicators = getUserIndicators(conn);
        for(UserIndicator ind : indicators) {
            if(migrateData(conn, ind)) {
                insertData(conn, ind);
            } else {
                deleteData(conn, ind.id);
            }
        }
    }

    private List<UserIndicator> getUserIndicators(Connection conn) throws SQLException {
        String sql = "SELECT id, user_id, layer_id, year, data, category FROM oskari_user_indicator";
        List<UserIndicator> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while(rs.next()) {
                    UserIndicator ind = new UserIndicator();
                    ind.id = rs.getLong("id");
                    ind.userId = rs.getLong("user_id");
                    ind.layerId = rs.getLong("layer_id");
                    ind.year = rs.getInt("year");
                    ind.category = rs.getString("category");
                    ind.data = rs.getString("data");
                    list.add(ind);
                }
            }
        }
        return list;
    }

    /**
     * Returns true if indicator had content/migrated successfully and false if the indicator can be removed
     * @param conn
     * @param indicator
     * @return
     * @throws SQLException
     */
    private static boolean migrateData(Connection conn, UserIndicator indicator) throws SQLException {
        JSONArray data = null;
        try {
            data = new JSONArray(indicator.data);
        } catch (JSONException ex) {
            LOG.warn("User indicator data not JSON, removing it:\n", indicator);
            return false;
        }
        // TODO: migrate data from sotkanet region ids to regionset feature ids and
        // from [{"region":"[SOTKANET region id]","primary value":"23"}...] to {"[region id for feature in regionset layer]":"23"}...]
        return true;
    }

    /**
     * Deletes an user indicator (if data is empty or not JSON)
     * @param conn
     * @param id
     * @throws SQLException
     */
    private static void deleteData(Connection conn, long id) throws SQLException {
        final String sql ="DELETE FROM oskari_user_indicator where id = ?";
        try(final PreparedStatement statement =
                    conn.prepareStatement(sql)) {
            statement.setLong(1, id);
            statement.execute();
        }
    }

    /**
     * Inserts user indicator data to the new table
     */
    private static void insertData(Connection conn, UserIndicator data) throws SQLException {

        final String sql ="INSERT INTO oskari_user_indicator_data" +
                "(indicator_id, regionset_id, year, data) " +
                "VALUES (?, ?, ?, ?)";
        try(final PreparedStatement statement =
                    conn.prepareStatement(sql)) {
            statement.setLong(1, data.id);
            statement.setLong(2, data.layerId);
            statement.setInt(3, data.year);
            statement.setString(4, data.data);
            statement.execute();
        }
    }
}