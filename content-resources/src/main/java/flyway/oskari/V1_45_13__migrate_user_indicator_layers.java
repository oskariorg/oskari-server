package flyway.oskari;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * Migrate oskari_user_indicator table layer_id column to match the new layers.
 *
 * Required configuration (same as 1.45.4):
 * ----------------------------------
 # layer name mappings for old regionsets
 # [select name from oskari_maplayer where type = 'statslayer']
 flyway.1_45_4.layer.name.kunta=tilastointialueet:kunta4500k_2017
 flyway.1_45_4.layer.name.aluehallintovirasto=tilastointialueet:avi4500k
 flyway.1_45_4.layer.name.maakunta=tilastointialueet:maakunta4500k
 flyway.1_45_4.layer.name.seutukunta=tilastointialueet:seutukunta4500k
 flyway.1_45_4.layer.name.elykeskus=tilastointialueet:ely4500k
 flyway.1_45_4.layer.name.nuts1=dummy:nuts1
 flyway.1_45_4.layer.name.erva=dummy:erva
 flyway.1_45_4.layer.name.sairaanhoitopiiri=dummy:sairaanhoitopiiri
 * ----------------------------------
 *
 * @see https://github.com/oskariorg/oskari-frontend/blob/master/bundles/statistics/statsgrid/plugin/ManageClassificationPlugin.js
 */
@SuppressWarnings("JavadocReference")
public class V1_45_13__migrate_user_indicator_layers implements JdbcMigration {

    private static final Logger LOG = LogFactory.getLogger(V1_45_13__migrate_user_indicator_layers.class);

    public void migrate(Connection conn) throws SQLException {
        if(!migrationRequired(conn)) {
            // not required if there's no user indicators
            return;
        }
        ThematicMapsRegionsetHelper regionsetHelper = new ThematicMapsRegionsetHelper(conn);
        Map<String, Integer> mapping = regionsetHelper.getOldNameToLayerIdMapping();

        String sql = "UPDATE oskari_user_indicator SET layer_id = ? WHERE category = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for(Map.Entry<String, Integer> entry : mapping.entrySet()) {
                ps.setInt(1, entry.getValue());
                ps.setString(2, entry.getKey());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /**
     * Returns true if the user indicator table has content
     * @param conn
     * @return
     * @throws SQLException
     */
    public boolean migrationRequired(Connection conn) throws SQLException {
        String sql = "SELECT count(id) FROM oskari_user_indicator";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                if(rs.next()) {
                    return rs.getInt(1) > 0;
                }
                return false;
            }
        }
    }
}