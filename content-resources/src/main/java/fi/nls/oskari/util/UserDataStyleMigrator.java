package fi.nls.oskari.util;

import org.json.JSONObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class UserDataStyleMigrator {
    public static int migrateStyles (Connection conn, final String layerTable, final String styleTable) throws SQLException {
        Map<Long,String> options = getOptions(conn, styleTable);
        updateLayers(conn, layerTable, options);
        return options.size();
    }
    public static Map<Long,String> getOptions(Connection conn, final String tableName) throws SQLException {

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
    public static void updateLayers(Connection conn, final String tableName, Map<Long, String> styleMap) throws SQLException {
        final String sql = String.format("UPDATE %s SET options=?::json WHERE id=?", tableName);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for(long id : styleMap.keySet()) {
                ps.setString(1, styleMap.get(id));
                ps.setLong(2, id);
                ps.addBatch();
            }
            ps.executeBatch();
        }
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
        JSONHelper.putValue(strokeArea, "color", rs.getString("border_color"));
        JSONHelper.putValue(strokeArea, "width", rs.getInt("border_width"));
        JSONHelper.putValue(strokeArea, "lineDash", convertDash(rs.getString("border_dasharray")));
        JSONHelper.putValue(strokeArea, "lineJoin", convertLineJoin(rs.getString("border_linejoin")));

        // fill
        JSONObject fill = new JSONObject();
        JSONHelper.putValue(featureStyle, "fill", fill);
        JSONHelper.putValue(fill, "color", rs.getString("fill_color"));
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
}
