package flyway.userlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.PropertyUtil;

public class V1_0_11__populate_userlayer_wkt implements JdbcMigration {

    private static final Logger LOG = LogFactory.getLogger(V1_0_11__populate_userlayer_wkt.class);
    private static final int WGS84_SRID = 4326;

    public void migrate(Connection connection) throws Exception {
        String srsName = getSrsName(connection);
        if (srsName == null){
            LOG.error("Cannot get srs name for userlayer data");
            throw new IllegalArgumentException("Cannot get srs name for userlayer data");
        }
        int srid = getSRID(srsName);
        String geometryColumn = PropertyUtil.get("userlayer.geometry.name", "geometry");

        String subselect = String.format("SELECT ST_AsText(ST_Transform(ST_SetSRID(ST_Extent(b.%s),%d),%d)) "
                + "FROM user_layer_data b "
                + "WHERE a.id = b.user_layer_id",
                geometryColumn, srid, WGS84_SRID);
        String sql = "UPDATE user_layer a SET wkt = (" + subselect + ")";
        LOG.debug(sql);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            int updated = ps.executeUpdate();
            LOG.info("Updated wkt for ", updated, "user_layer rows");
        }
    }

    private int getSRID(String srsName) {
        int i = srsName.lastIndexOf(':');
        int srid = Integer.parseInt(srsName.substring(i + 1));
        LOG.debug("Parsed SRID", srid, "from", srsName);
        return srid;
    }

    private String getSrsName(Connection conn) throws SQLException {
        String fromProperty = PropertyUtil.getOptionalNonLocalized("oskari.native.srs");
        if (fromProperty != null && !fromProperty.isEmpty()) {
            return fromProperty;
        }
        LOG.debug("Could not find 'oskari.native.srs' property");
        int baselayerId = PropertyUtil.getOptional("userlayer.baselayer.id", -1);
        String sql = "SELECT srs_name FROM oskari_maplayer WHERE id=?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, baselayerId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()){
                    return rs.getString("srs_name");
                }
                return null;
            }
        }
    }

}
