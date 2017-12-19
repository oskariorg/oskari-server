package flyway.oskari;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

/**
 * Adds
 *   'capabilities_last_updated'
 *   'capabilities_update_rate_sec'
 * columns to
 *   'oskari_maplayer'
 */
public class V1_45_2__add_automatic_capabilities_columns implements JdbcMigration {

    private static final String ADD_LAST_UPDATED = ""
            + "ALTER TABLE oskari_maplayer "
            +" ADD COLUMN capabilities_last_updated timestamp with time zone";
    private static final String ADD_UPDATE_RATE_SEC = ""
            + "ALTER TABLE oskari_maplayer "
            +" ADD COLUMN capabilities_update_rate_sec int DEFAULT 0";

    public void migrate(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(ADD_LAST_UPDATED);
            stmt.execute(ADD_UPDATE_RATE_SEC);
        }
    }

}
