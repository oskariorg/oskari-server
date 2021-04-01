package flyway.myplaces;

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
import java.util.Arrays;
import java.util.List;

/**
 * Migrate name column to a locale JSONObject on categories table
 */
public class V1_0_15__name_localization extends BaseJavaMigration {
    private static final Logger LOG = LogFactory.getLogger(V1_0_15__name_localization.class);
    private static final String DFFAULT_LANGUAGE = PropertyUtil.getDefaultLanguage();

    public void migrate(Context context) throws Exception {
        Connection conn = context.getConnection();
        createLocaleColumn(conn);
        setLocales(conn);
        LOG.info("Migrated categories table name column to locale with language:", DFFAULT_LANGUAGE );
    }
    private void createLocaleColumn (Connection connection) throws Exception {
        final String sql = "ALTER TABLE categories ADD COLUMN IF NOT EXISTS locale json DEFAULT '{}'";
        try (PreparedStatement statement = connection.prepareStatement(sql)){
            statement.execute();
        }
    }
    public void setLocales(Connection connection) throws Exception {
        String select = "SELECT id, category_name FROM categories";
        String update = "UPDATE categories SET locale=?::json WHERE id=?";

        try (PreparedStatement psSelect = connection.prepareStatement(select);
             ResultSet rs = psSelect.executeQuery();
             PreparedStatement psUpdate = connection.prepareStatement(update)) {
            while (rs.next()) {
                long id = rs.getLong("id");
                String name = rs.getString("category_name");
                JSONObject locale = createLocale(name);
                psUpdate.setString(1, locale.toString());
                psUpdate.setLong(2, id);
                psUpdate.addBatch();
            }
            psUpdate.executeBatch();
            connection.commit();
        }
    }
    private JSONObject createLocale (String name) {
        JSONObject locale = new JSONObject();
        JSONHelper.putValue(locale, DFFAULT_LANGUAGE, JSONHelper.createJSONObject("name", name));
        return locale;
    }
}
