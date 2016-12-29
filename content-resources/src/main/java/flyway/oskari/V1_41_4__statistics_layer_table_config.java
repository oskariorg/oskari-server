package flyway.oskari;

import fi.nls.oskari.util.JSONHelper;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Adds a new column for oskari_statistical_layer table for layer config.
 * Migrates source_property column to config column {"regionType" : [value] } and drops the column.
 * Drops the layer_property column as it's not used anywhere.
 * Uses an empty config for all layers that are NOT used with SotkaNET or KAPA datasources
 * since only they used the columns.
 */
public class V1_41_4__statistics_layer_table_config implements JdbcMigration {

    public void migrate(Connection connection) throws SQLException {
        addColumn(connection);
        for (Layer layer : getLayers(connection)) {

            JSONObject config = JSONHelper.createJSONObject("regionType", layer.source);
            updateLayerConfig(layer, config, connection);
        }
        dropColumn(connection, "source_property");
        dropColumn(connection, "layer_property");
        resetLayerConfigForNonSotkaNetOrKapaLayers(connection);
    }

    private void addColumn(Connection connection) throws SQLException {
        final String sql = "ALTER TABLE oskari_statistical_layer ADD COLUMN config TEXT DEFAULT '{}'::TEXT";

        try (final PreparedStatement statement =
                     connection.prepareStatement(sql)) {
            statement.execute();
        }
    }
    private void dropColumn(Connection connection, String col) throws SQLException {
        final String sql = "ALTER TABLE oskari_statistical_layer DROP COLUMN " + col;

        try (final PreparedStatement statement =
                     connection.prepareStatement(sql)) {
            statement.execute();
        }
    }

    private List<Layer> getLayers(Connection conn) throws SQLException {
        List<Layer> list = new ArrayList<>();
        String sql = "SELECT layer_id, datasource_id, source_property" +
                " FROM oskari_statistical_layer;";
        try(PreparedStatement statement = conn.prepareStatement(sql)) {
            try (ResultSet rs = statement.executeQuery()) {
                while(rs.next()) {
                    Layer layer = new Layer();
                    layer.id = rs.getLong("layer_id");
                    layer.datasource = rs.getLong("datasource_id");
                    layer.source = rs.getString("source_property");
                    list.add(layer);
                }
            }
        }
        return list;

    }

    private void updateLayerConfig(Layer layer, JSONObject config, Connection conn) throws SQLException {
        final String sql = "UPDATE oskari_statistical_layer SET config=? where layer_id=? AND datasource_id=?";
        try(PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, config.toString());
            statement.setLong(2, layer.id);
            statement.setLong(3, layer.datasource);
            statement.execute();
        }
    }

    /**
     * Only SotkaNet and Kapa plugins used source_property. Reset config from others.
     * @param conn
     * @throws SQLException
     */
    private void resetLayerConfigForNonSotkaNetOrKapaLayers(Connection conn) throws SQLException {
        final String sql = "UPDATE oskari_statistical_layer SET config='{}' where datasource_id IN (" +
                "SELECT distinct id FROM oskari_statistical_datasource" +
                " where plugin NOT IN ('SotkaNET','KAPA')" +
                ")";
        try(PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.execute();
        }
    }

    class Layer {
        long id;
        long datasource;
        String source;
    }
}
