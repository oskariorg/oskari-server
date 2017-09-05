package flyway.oskari;

import fi.nls.oskari.util.PropertyUtil;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * Delete content from selected_feature_params and feature_params_locales for userlayers as handling has changed
 * (property_json is no longer sent to frontend)
 */
public class V1_42_3__remove_selected_params_and_locales implements JdbcMigration {

    public void migrate(Connection connection) throws Exception {
        final String sql = "UPDATE portti_wfs_layer SET " +
                "selected_feature_params = '{}', feature_params_locales = '{}' " +
                " WHERE maplayer_id=?";

        int maplayerId = PropertyUtil.getOptional("userlayer.baselayer.id", -1);

        try (final PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, maplayerId);
            statement.execute();
        }

    }
}