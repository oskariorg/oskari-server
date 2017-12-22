package flyway.userlayer;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Migrate fields JSONObject to JSONArray from user_layer table
 */
public class V1_0_6__migrate_fields_to_jsonarray implements JdbcMigration {
    private static final Logger LOG = LogFactory.getLogger(V1_0_6__migrate_fields_to_jsonarray.class);

    public void migrate(Connection connection) throws Exception {
        Map<Long, String> resultMap = getFields (connection);
        for (Map.Entry<Long, String> entry: resultMap.entrySet()){
            Long id = entry.getKey();
            JSONObject json = JSONHelper.createJSONObject(entry.getValue()); 
            JSONArray jsarray = convertJSONObjectToJSONArray(json);
            updateFields (connection, id, jsarray);
            LOG.debug("Userlayer id:",id,"fields:",json.toString(),"migrated to:", jsarray.toString());
        }
    }

    private Map<Long, String> getFields (Connection conn) throws SQLException {
        final String sql = "SELECT id, fields::text FROM user_layer";
        Map<Long, String> result = new  HashMap<Long, String>();
        try (final PreparedStatement statement = conn.prepareStatement(sql)) {
            try (ResultSet rs = statement.executeQuery()) {
                while(rs.next()) {
                    result.put(rs.getLong("id"), rs.getString("fields"));
                }
            }
        }
        return result;
    }

    private void updateFields (Connection conn, Long id, JSONArray json) throws SQLException {
        final String sql = "UPDATE user_layer SET " +
                "fields=?::json " +
                "WHERE id=?";

        try (final PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, json.toString());
            statement.setLong(2, id);
            statement.execute();
        }

    }
    private JSONArray convertJSONObjectToJSONArray (JSONObject jsonObj){
        JSONArray jsonArr = new JSONArray();
        Iterator<String> keys = jsonObj.keys();
        while(keys.hasNext()){
            String key = keys.next();
            jsonArr.put(JSONHelper.createJSONObject(key, jsonObj.opt(key)));
        }
        return jsonArr;
    }

}
