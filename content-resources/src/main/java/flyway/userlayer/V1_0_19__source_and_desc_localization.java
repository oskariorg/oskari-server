package flyway.userlayer;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Migrate name column to a locale JSONObject on user_layer table
 */
public class V1_0_19__source_and_desc_localization extends BaseJavaMigration {
    private static final Logger LOG = LogFactory.getLogger(V1_0_19__source_and_desc_localization.class);
    private static final String DFFAULT_LANGUAGE = PropertyUtil.getDefaultLanguage();

    public void migrate(Context context) throws Exception {
        Connection conn = context.getConnection();
        updateLocale(conn);
        LOG.info("Migrated user_layer table layer_source and layer_desc columns to locale with language:", DFFAULT_LANGUAGE );
    }
    public void updateLocale(Connection connection) throws Exception {
        String select = "SELECT id, layer_desc, layer_source, locale::text FROM user_layer";
        String update = "UPDATE user_layer SET locale=?::json WHERE id=?";

        try (PreparedStatement psSelect = connection.prepareStatement(select);
             ResultSet rs = psSelect.executeQuery();
             PreparedStatement psUpdate = connection.prepareStatement(update)) {
            while (rs.next()) {
                long id = rs.getLong("id");
                String desc = rs.getString("layer_desc");
                String source = rs.getString("layer_source");
                JSONObject locale = JSONHelper.createJSONObject(rs.getString("locale"));
                updateDefaultLangValues(locale, desc, source);
                psUpdate.setString(1, locale.toString());
                psUpdate.setLong(2, id);
                psUpdate.addBatch();
            }
            psUpdate.executeBatch();
            connection.commit();
        }
    }
    private void updateDefaultLangValues (JSONObject locale, String desc, String source) {
        JSONObject values = JSONHelper.getJSONObject(locale, DFFAULT_LANGUAGE);
        if (values == null) {
            values = new JSONObject();
        }
        if (desc!= null && !desc.isEmpty()) {
            JSONHelper.putValue(values, "desc", desc);
        }
        if (source !=null && !source.isEmpty()) {
            JSONHelper.putValue(values, "source", source);
        }
    }
}
