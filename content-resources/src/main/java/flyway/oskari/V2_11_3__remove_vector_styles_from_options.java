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

/**
 * This removes styles from oskari_maplayer.options column for vector layers
 * Styles were migrated to new table by 2.11.2
 */
public class V2_11_3__remove_vector_styles_from_options extends BaseJavaMigration {
    private static final String KEY_OSKARI = "styles";
    private static final String KEY_EXTERNAL = "externalStyles";

    @Override
    public void migrate(Context context) throws Exception {
        Logger log = LogFactory.getLogger(V2_11_3__remove_vector_styles_from_options.class);
        Connection connection = context.getConnection();
        String select = "SELECT id, options FROM oskari_maplayer WHERE type IN ('wfslayer','tiles3dlayer','vectortilelayer')";
        String update = "UPDATE oskari_maplayer SET options=? WHERE id=?";
        int count = 0;
        try (PreparedStatement psSelect = connection.prepareStatement(select);
             ResultSet rs = psSelect.executeQuery();
             PreparedStatement psUpdate = connection.prepareStatement(update)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                JSONObject options = JSONHelper.createJSONObject(rs.getString("options"));
                if (!options.has(KEY_OSKARI) && !options.has(KEY_EXTERNAL)) {
                    continue;
                }
                count++;
                options.remove(KEY_OSKARI);
                options.remove(KEY_EXTERNAL);
                psUpdate.setString(1, options.toString());
                psUpdate.setInt(2, id);
                psUpdate.addBatch();
            }
            psUpdate.executeBatch();
            connection.commit();
        }
        log.info("Removed styles and external styles from:", count, "vector layers");
    }

}
