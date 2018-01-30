package flyway.analysis;

import fi.nls.oskari.geoserver.GeoserverPopulator;
import fi.nls.oskari.util.PropertyUtil;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class V1_0_4__add_baselayer implements JdbcMigration {

    private static final String NAME = "oskari:analysis_data";

    public void migrate(Connection connection) throws Exception {
        if (hasLayer(connection)) {
            // already has base layer
            return;
        }
        GeoserverPopulator.setupAnalysisLayer(PropertyUtil.get("oskari.native.srs", "EPSG:4326"));
    }

    private boolean hasLayer(Connection connection) throws SQLException {
        String sql = "SELECT id FROM oskari_maplayer where name = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, NAME);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
