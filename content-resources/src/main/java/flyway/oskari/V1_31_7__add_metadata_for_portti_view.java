package flyway.oskari;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.*;

/**
 * Checks if portti_view already has a column metadata, adds it if not.
 */
public class V1_31_7__add_metadata_for_portti_view implements JdbcMigration {

    private static final String ADMIN = "admin";
    private static final String METRICS = "metrics";

    public void migrate(Connection connection) throws SQLException {
        // BundleHelper checks if these bundles are already registered
        final DatabaseMetaData metadata = connection.getMetaData();
        String   catalog           = null;
        String   schemaPattern     = null;
        String   tableNamePattern  = "portti_view";
        String   columnNamePattern = "metadata";

        ResultSet result = metadata.getColumns(
                catalog, schemaPattern, tableNamePattern, columnNamePattern);
        if(result.next()) {
            // already present, do nothing
            return;
        }
        addColumn(connection);
    }
    private void addColumn(Connection connection)
            throws SQLException {

        final PreparedStatement statement =
                connection.prepareStatement("ALTER TABLE portti_view ADD COLUMN metadata TEXT DEFAULT '{}'::TEXT");
        try {
            statement.execute();
        } finally {
            statement.close();
        }
    }
}
