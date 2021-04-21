package flyway.oskari;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class V2_3_1__migrate_wps_params extends BaseJavaMigration {
    private static final Logger LOG = LogFactory.getLogger(V2_3_1__migrate_wps_params.class);
    public static final String WPS_PARAMS = "wpsParams";

    @Override
    public void migrate(Context context) throws Exception {
        Connection conn = context.getConnection();
        getLayerAttributes(conn).forEach((id, attr) -> migrateWpsParams(conn, id, attr));
    }
    private void migrateWpsParams (Connection conn, int id, JSONObject attr) {
        String wpsString = attr.optString(WPS_PARAMS);
        if (wpsString.isEmpty()) {
            //skip
            return;
        }
        try {
            JSONObject wpsParams = new JSONObject(wpsString);
            JSONObject data = attr.optJSONObject("data");
            if (data == null) {
                data = new JSONObject();
            }
            Iterator <String> keys = wpsParams.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                if ("no_data".equals(key)) {
                    data.put("noDataValue", wpsParams.optInt(key, -1));
                } else if ("join_key".equals(key)) {
                    data.putOpt("commonId", wpsParams.optString(key, null));
                } else if ("input_type".equals(key)) {
                    data.putOpt("wpsType", wpsParams.optString(key, null));
                } else {
                    // if wps params has additional keys, store them
                    data.put(key, wpsParams.get(key));
                }
            }

            updateAttributes(conn, id, attr);
        } catch (Exception ignored) {
            LOG.warn("Failed to migrate WPS params String:", wpsString, "to JSONObject for layer:", id);
        }
    }
    private void updateAttributes (Connection conn, int layerId, JSONObject attributes) throws SQLException {
        final String sql = "UPDATE oskari_maplayer SET attributes=? where id=?";
        try(PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, attributes.toString());
            statement.setInt(2, layerId);
            statement.execute();
        }
    }
    private Map<Integer, JSONObject> getLayerAttributes(Connection conn) throws SQLException {
        Map<Integer, JSONObject> attributes = new HashMap<>();
        // only WFS layers have WPS params
        final String sql = "SELECT id, attributes FROM oskari_maplayer where type = 'wfslayer'";
        try(PreparedStatement statement = conn.prepareStatement(sql)) {
            try (ResultSet rs = statement.executeQuery()) {
                while(rs.next()) {
                    int id = rs.getInt("id");
                    JSONObject attr = JSONHelper.createJSONObject(rs.getString("attributes"));
                    attributes.put(id, attr);
                }
            }
        }
        LOG.info("Found", attributes.size(), "WFS layers for WPS migration");
        return attributes;
    }
}
