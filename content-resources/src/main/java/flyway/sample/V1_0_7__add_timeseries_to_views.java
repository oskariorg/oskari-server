package flyway.sample;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by MKUOSMANEN on 26.1.2016.
 */
public class V1_0_7__add_timeseries_to_views implements JdbcMigration {
    private static final  String BUNDLE_TIMESERIES = "timeseries";

    public void migrate(Connection connection)
            throws Exception {
        // Get default views
        final ArrayList<Long> views = getDefaultViews(connection);

        for(int i=0;i<views.size();i++){
            Long viewId = views.get(i);
            final boolean hasTimeseries = hasExistingBundle(connection, BUNDLE_TIMESERIES, viewId);
            if (!hasTimeseries) {
                makeInsert(viewId, connection);
            }
        }
    }

    private void makeInsert(Long viewId, Connection connection) throws SQLException {
        final PreparedStatement statement =
                connection.prepareStatement("INSERT INTO portti_view_bundle_seq" +
                        "(view_id, bundle_id, seqno, config, state, startup, bundleinstance) " +
                        "VALUES (" +
                        "?, " +
                        "(SELECT id FROM portti_bundle WHERE name=?), " +
                        "(SELECT max(seqno)+1 FROM portti_view_bundle_seq WHERE view_id=?), " +
                        "?, ?, " +
                        "(SELECT startup FROM portti_bundle WHERE name=?), " +
                        "?)");
        try {
            statement.setLong(1, viewId);
            statement.setString(2, BUNDLE_TIMESERIES);
            statement.setLong(3, viewId);
            statement.setString(4, "{}");
            statement.setString(5, "{}");
            statement.setString(6, BUNDLE_TIMESERIES);
            statement.setString(7, BUNDLE_TIMESERIES);
            statement.execute();
        } finally {
            statement.close();
        }
    }

    private ArrayList<Long> getDefaultViews(Connection connection) throws Exception {
        ArrayList<Long> ids = new ArrayList<>();

        final PreparedStatement statement =
                connection.prepareStatement("SELECT id FROM portti_view " +
                        "WHERE type='DEFAULT' OR type='USER'");
        try (ResultSet rs = statement.executeQuery()) {
            while(rs.next()) {
                ids.add(rs.getLong("id"));
            }
        } finally {
            statement.close();
        }
        return ids;
    }

    private boolean hasExistingBundle(Connection connection, String bundle, Long viewId)
            throws Exception {
        final PreparedStatement statement =
                connection.prepareStatement("SELECT * FROM portti_view_bundle_seq " +
                        "WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name=?) " +
                        "AND view_id=?");
        statement.setString(1,bundle);
        statement.setLong(2, viewId);
        try (ResultSet rs = statement.executeQuery()) {
            return rs.next();
        } finally {
            statement.close();
        }
    }
}
