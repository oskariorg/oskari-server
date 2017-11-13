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

/**
 * Add name and type to fields
 * [{"the_geom","MultiPolygon"},..] -> [{"name": "the_geom", "type":"MultiPolygon"},..]
 *
 * Split java class name to simpleName format
 * java.lang.Long -> Long
 */
public class V1_0_8__add_name_and_type_to_fields implements JdbcMigration {
    private static final Logger LOG = LogFactory.getLogger(V1_0_8__add_name_and_type_to_fields.class);

   public void migrate(Connection connection) throws Exception {
        String select = "SELECT id, fields::text FROM user_layer";
        String update = "UPDATE user_layer SET fields=?::json WHERE id=?";

        try (PreparedStatement psSelect = connection.prepareStatement(select);
             ResultSet rs = psSelect.executeQuery();
             PreparedStatement psUpdate = connection.prepareStatement(update)) {
            while (rs.next()) {
                long id = rs.getLong("id");
                String fields = rs.getString("fields");

                JSONArray json = JSONHelper.createJSONArray(fields, true);
                JSONArray updatedJson = addNameAndType(json);

                psUpdate.setString(1, updatedJson.toString());
                psUpdate.setLong(2, id);
                psUpdate.addBatch();
            }
            psUpdate.executeBatch();
            connection.commit();
        }
    }

    private JSONArray addNameAndType (JSONArray json){
        JSONArray jsonArr = new JSONArray();
        try{
            for (int i=0; i<json.length(); i++){
                String key = json.getJSONObject(i).keys().next().toString();
                String type = json.getJSONObject(i).get(key).toString();
                // simpleName
                type = type.substring(type.lastIndexOf('.')+1);
                JSONObject obj = new JSONObject();
                obj.put("name", key);
                obj.put("type", type);
                jsonArr.put(obj);
            }
        }catch (JSONException e){
            //LOG.error("Error on converting json", e);
            return new JSONArray();
        }
        return jsonArr;
    }
}
