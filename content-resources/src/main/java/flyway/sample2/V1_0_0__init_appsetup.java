package flyway.sample2;

import fi.nls.oskari.db.DBHandler;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;

public class V1_0_0__init_appsetup implements JdbcMigration {
    public void migrate(Connection connection)
            throws Exception {

        DBHandler.setupAppContent(connection, "app-sample-2");
    }

}
