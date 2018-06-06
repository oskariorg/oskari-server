package flyway.sample;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;

/**
 * Removed in 1.47.x which migrates all views to OL3+
 */
public class V1_0_11__add_initial_ol3_geoportal_view implements JdbcMigration {

    public void migrate(Connection connection) throws Exception {
        return;
    }
}
