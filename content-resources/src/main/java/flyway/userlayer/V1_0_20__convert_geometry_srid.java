package flyway.userlayer;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.PropertyUtil;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class V1_0_20__convert_geometry_srid extends BaseJavaMigration {
    private static final Logger LOG = LogFactory.getLogger(V1_0_20__convert_geometry_srid.class);

    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        String epsg = PropertyUtil.get("oskari.native.srs", "EPSG:4326");
        String srs = epsg.substring(epsg.indexOf(':') + 1);
        int srid = Integer.parseInt(srs);
        convertGeometries(connection, srid);
    }

    protected void convertGeometries(Connection conn, int srid) throws SQLException {
        final String sql = "UPDATE user_layer_data SET geometry = ST_SetSRID(geometry,?)";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, srid);
            int updated = statement.executeUpdate();
            LOG.info("Updated:" , updated, "user_layer_data geometry srid to:", srid);
        }
    }
}
