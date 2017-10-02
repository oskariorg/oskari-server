package flyway.userlayer;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Add name and type to fields
 * [{"the_geom","MultiPolygon"},..] -> [{"name": "the_geom", "type":"MultiPolygon"},..]
 * 
 * Split java class name to simpleName format 
 * java.lang.Long -> Long
 */
public class __add_name_and_type_to_fields implements JdbcMigration {
    private static final Logger LOG = LogFactory.getLogger(__add_name_and_type_to_fields.class);

    public void migrate(Connection connection) throws Exception {
        Map<Long, String> resultMap = getFields (connection);
        for (Map.Entry<Long, String> entry: resultMap.entrySet()){
            Long id = entry.getKey();
            JSONArray json = JSONHelper.createJSONArray(entry.getValue()); 
            JSONArray updatedJson = addNameAndType(json);
            if (updatedJson != null){
                updateFields (connection, id, updatedJson);
                LOG.debug("Userlayer id:",id,"fields:",json.toString(),"migrated to:", updatedJson.toString());
            }else{
                LOG.error("Error on updating userlayer fields. Skipping id:", id);
            }           
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
    private JSONArray addNameAndType (JSONArray json){
        JSONArray jsonArr = new JSONArray();        
        try{
            for (int i=0; i<json.length(); i++){
                JSONObject obj = new JSONObject();
                String key = jsonArr.getJSONObject(i).keys().next().toString();
                String type =jsonArr.getJSONObject(i).get(key).toString();
                // simpleName
                String simpleName = type.substring(type.lastIndexOf('.')+1);
                obj.put("name", key);
                obj.put("type", simpleName);
                jsonArr.put(obj);
            }
        }catch (JSONException e){
            //LOG.error("Error on converting json", e);
            return null;
        }
        return jsonArr;
    }

}
