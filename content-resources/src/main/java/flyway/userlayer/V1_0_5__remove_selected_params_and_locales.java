package flyway.userlayer;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import fi.nls.oskari.util.PropertyUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * Delete content from selected_feature_params and feature_params_locales
 */
public class V1_0_5__remove_selected_params_and_locales implements JdbcMigration {

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