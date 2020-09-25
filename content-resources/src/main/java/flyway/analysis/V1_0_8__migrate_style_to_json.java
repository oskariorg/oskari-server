package flyway.analysis;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.oskari.usercontent.UserDataStyleMigrator;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

public class V1_0_8__migrate_style_to_json extends BaseJavaMigration {
    private static final Logger LOG = LogFactory.getLogger(V1_0_8__migrate_style_to_json.class);
    private static final String STYLE_TABLE = "analysis_style";
    private static final String LAYER_TABLE = "analysis";
    private static final String LAYER_NAME = "oskari:analysis_data";
    private static final String STYLE_ID_COLUMN = "style_id";
    private static final String PROPERTY_PREXIX = "analysis";

    public void migrate(Context context) throws Exception {
        int count = UserDataStyleMigrator.migrateStyles(context.getConnection(), LAYER_TABLE, STYLE_TABLE, STYLE_ID_COLUMN);
        LOG.info("Migrated", count, "styles from table:", STYLE_TABLE, "to options column in table:", LAYER_TABLE);
        UserDataStyleMigrator.updateBaseLayerOptions(LAYER_NAME, PROPERTY_PREXIX, null);
        LOG.info("Updated options for:", LAYER_NAME);
    }
}
