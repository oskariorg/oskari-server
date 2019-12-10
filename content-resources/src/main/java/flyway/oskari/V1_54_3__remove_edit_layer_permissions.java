package flyway.oskari;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.PropertyUtil;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class V1_54_3__remove_edit_layer_permissions implements JdbcMigration {
    private static final Logger LOG = LogFactory.getLogger(V1_54_3__remove_edit_layer_permissions.class);

    public void migrate(Connection conn) throws Exception {
        if(PropertyUtil.getOptional("flyway.1_54_3.skip", false)) {
            LOG.warn("You are skipping remove edit layer permissions migration.",
                    "Note that Admin has always edit layer permission.",
                    "Only reason to skip this is if you are using separate role for layer editing");
            return;
        }
        final String sql = "DELETE FROM oskari_permission WHERE permission = 'EDIT_LAYER'";
        try(PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.execute();
        }
    }
}
