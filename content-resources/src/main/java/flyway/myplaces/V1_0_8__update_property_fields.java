package flyway.myplaces;

import fi.nls.oskari.db.DatasourceHelper;
import org.oskari.usercontent.UserDataLayerPopulator;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.json.JSONObject;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class V1_0_8__update_property_fields extends BaseJavaMigration {

    public void migrate(Context ignored) throws Exception {
        // myplaces _can_ use other db than the default one
        // -> Use connection to default db for this migration
        DataSource ds = DatasourceHelper.getInstance().getDataSource();
        if (ds == null) {
            ds = DatasourceHelper.getInstance().createDataSource();
        }
        Connection conn = ds.getConnection();
        JSONObject attributes = UserDataLayerPopulator.addMyplacesAttributes(UserDataLayerPopulator.createUserContentAttributes());
        final String sql = "update oskari_maplayer set attributes =? where name = 'oskari:my_places';";

        try (final PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, attributes.toString());
            statement.execute();
        }
    }

}
