package flyway.oskari;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

public class V1_43_4__update_backendstatus_schema implements JdbcMigration {

    private static final String[] CMDS = {
        "DROP VIEW IF EXISTS portti_backendstatus_allknown",
        "DROP VIEW IF EXISTS portti_backendalert",
        "ALTER TABLE portti_backendstatus RENAME TO oskari_backendstatus",
        "ALTER TABLE oskari_backendstatus DROP COLUMN id",
        "ALTER TABLE oskari_backendstatus DROP COLUMN source",
        "ALTER TABLE oskari_backendstatus DROP COLUMN statusjson",
        "ALTER TABLE oskari_backendstatus ALTER maplayer_id TYPE int USING maplayer_id::integer",
        "ALTER TABLE oskari_backendstatus ALTER maplayer_id SET NOT NULL"
    };

    public void migrate(Connection connection) throws SQLException {
        connection.setAutoCommit(false);
        try (Statement stmt = connection.createStatement()) {
            for (String cmd : CMDS) {
                stmt.execute(cmd);
            }
            connection.commit();
        }
    }

}
