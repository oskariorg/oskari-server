package flyway.myplaces;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.UserDataStyleMigrator;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;

public class V1_0_10__migrate_style_to_json implements JdbcMigration {
    private static final Logger LOG = LogFactory.getLogger(V1_0_10__migrate_style_to_json.class);
    private static final String TABLE = "categories";
    private static final String LAYER_NAME = "oskari:my_places";
    private static final String STYLE_ID_COLUMN = "id";
    private static final String LABEL_PROPERTY = "attention_text";
    private static final String PROPERTY_PREXIX = "myplaces";

    public void migrate(Connection connection) throws Exception {
        int count = UserDataStyleMigrator.migrateStyles(connection, TABLE, TABLE, STYLE_ID_COLUMN);
        LOG.info("Migrated", count, "styles to options in table:", TABLE);
        UserDataStyleMigrator.updateBaseLayerOptions(LAYER_NAME, PROPERTY_PREXIX, LABEL_PROPERTY);
        LOG.info("Updated options for:", LAYER_NAME);
    }
}
