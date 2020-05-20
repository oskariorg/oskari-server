package fi.nls.oskari.util;

import fi.nls.oskari.db.DatasourceHelper;
import org.json.JSONObject;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class UserDataStyleMigrator {
    private static final String CLUSTERING_PROPERTY = ".clustering.distance";
    private static final String LAYER_ID_PROPERTY = ".baselayer.id";
    private static final String DEFAULT_RENDER_MODE = "vector";
    private static final String KEY_RENDER_MODE = "renderMode";
    private static final String KEY_CLUSTER = "clusteringDistance";
    private static final String KEY_LABEL = "labelProperty";

    public static void updateBaseLayerOptions (final String layerName, final String propertyPrefix, final String labelProperty) throws SQLException {
        // UserDataLayers _can_ use other db than the default one
        // -> Use connection to default db for this migration
        DataSource ds = DatasourceHelper.getInstance().getDataSource();
        if (ds == null) {
            ds = DatasourceHelper.getInstance().createDataSource();
        }
        Connection conn = ds.getConnection();
        // Get existing options
        int layerId = PropertyUtil.getOptional(propertyPrefix + LAYER_ID_PROPERTY, -1);
        final String selectSQL = "select options from oskari_maplayer where id=? or name=?";
        JSONObject options = null;
        try (final PreparedStatement ps = conn.prepareStatement(selectSQL)) {
            ps.setInt(1, layerId);
            ps.setString(2, layerName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                options = JSONHelper.createJSONObject(rs.getString("options"));
                if (rs.next()) throw new SQLException("More than one result");
            }
        }
        // Update values
        JSONHelper.putValue(options, KEY_RENDER_MODE, DEFAULT_RENDER_MODE);
        int cluster = PropertyUtil.getOptional(propertyPrefix + CLUSTERING_PROPERTY, -1);
        if (cluster > 0) {
            JSONHelper.putValue(options, KEY_CLUSTER, cluster);
        }
        if (labelProperty != null) {
            JSONHelper.putValue(options, KEY_LABEL, labelProperty);
        }
        // Update options
        final String updateSQL = "update oskari_maplayer set options=? where id=? or name=?";

        try (final PreparedStatement statement = conn.prepareStatement(updateSQL)) {
            statement.setString(1, options.toString());
            statement.setInt(2, layerId);
            statement.setString(3, layerName);
            statement.execute();
        }
    }

    public static int migrateStyles (Connection conn, final String layerTable, final String styleTable, final String styleIdColumn) throws SQLException {
        Map<Long,String> options = getOptions(conn, styleTable);
        updateLayers(conn, layerTable, styleIdColumn, options);
        return options.size();
    }

    private static Map<Long,String> getOptions(Connection conn, final String tableName) throws SQLException {

        final String sql =  String.format("SELECT id, dot_shape, dot_color, dot_size, " +
                "stroke_width, stroke_color, stroke_linejoin, stroke_linecap, stroke_dasharray, " +
                "border_color, border_width, border_linejoin, border_dasharray, fill_color, fill_pattern " +
                "FROM %s", tableName);
        Map<Long, String> options = new HashMap<>();
        try(PreparedStatement statement = conn.prepareStatement(sql)) {
            try (ResultSet rs = statement.executeQuery()) {
                while(rs.next()) {
                    options.put(rs.getLong("id"), parseOptions(rs));
                }
            }
        }
        return options;
    }
    private static String parseOptions(ResultSet rs) throws SQLException  {
        JSONObject options = new JSONObject();
        JSONObject styles = new JSONObject();
        JSONHelper.putValue(options, "styles", styles);
        JSONObject defaultStyle = new JSONObject();
        JSONHelper.putValue(styles, "default", defaultStyle);
        JSONObject featureStyle = new JSONObject();
        JSONHelper.putValue(defaultStyle, "featureStyle", featureStyle);
        // parse Oskari style for options
        // image
        JSONObject image = new JSONObject();
        JSONHelper.putValue(featureStyle, "image", image);
        JSONObject imageFill = new JSONObject();
        JSONHelper.putValue(imageFill, "color", rs.getString("dot_color"));
        JSONHelper.putValue(image, "fill", imageFill);
        JSONHelper.putValue(image, "shape", rs.getString("dot_shape"));
        JSONHelper.putValue(image, "size", rs.getInt("dot_size"));

        // stroke
        JSONObject stroke = new JSONObject();
        JSONHelper.putValue(featureStyle, "stroke", stroke);
        JSONHelper.putValue(stroke, "color", rs.getString("stroke_color"));
        JSONHelper.putValue(stroke, "width", rs.getInt("stroke_width"));
        JSONHelper.putValue(stroke, "lineDash", convertDash(rs.getString("stroke_dasharray")));
        JSONHelper.putValue(stroke, "lineCap", convertLineCap(rs.getString("stroke_linecap")));
        JSONHelper.putValue(stroke, "lineJoin", convertLineJoin(rs.getString("stroke_linejoin")));

        // stroke.area
        JSONObject strokeArea = new JSONObject();
        JSONHelper.putValue(stroke, "area", strokeArea);
        String borderColor = rs.getString("border_color");
        JSONHelper.putValue(strokeArea, "color", borderColor == null ? JSONObject.NULL : borderColor );
        JSONHelper.putValue(strokeArea, "width", rs.getInt("border_width"));
        JSONHelper.putValue(strokeArea, "lineDash", convertDash(rs.getString("border_dasharray")));
        JSONHelper.putValue(strokeArea, "lineJoin", convertLineJoin(rs.getString("border_linejoin")));

        // fill
        JSONObject fill = new JSONObject();
        JSONHelper.putValue(featureStyle, "fill", fill);
        String fillColor = rs.getString("fill_color");
        JSONHelper.putValue(fill, "color", fillColor == null ? JSONObject.NULL : fillColor );
        JSONObject fillArea = new JSONObject();
        JSONHelper.putValue(fillArea, "pattern", rs.getInt("fill_pattern"));
        JSONHelper.putValue(fill, "area", fillArea);
        return options.toString();
    }
    
    private static String convertDash (String dashArray) {
        if ("5 2".equals(dashArray)){
            return "dash";
        }
        return "solid";
    }
    private static String convertLineJoin (String lineJoin) {
        if (lineJoin == null || "0".equals(lineJoin)){
            return "dash";
        }
        return lineJoin;
    }
    private static String convertLineCap (String lineCap) {
        if (lineCap == null || "0".equals(lineCap)){
            return "butt";
        }
        return lineCap;
    }

    private static void updateLayers(Connection conn, final String tableName, final String styleIdColumn, Map<Long, String> styleMap) throws SQLException {
        final String sql = String.format("UPDATE %s SET options=?::json WHERE %s=?", tableName, styleIdColumn);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for(long id : styleMap.keySet()) {
                ps.setString(1, styleMap.get(id));
                ps.setLong(2, id);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}
