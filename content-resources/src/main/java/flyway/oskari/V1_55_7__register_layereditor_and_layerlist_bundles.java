package flyway.oskari;

import fi.nls.oskari.db.BundleHelper;
import fi.nls.oskari.domain.map.view.Bundle;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;

public class V1_55_7__register_layereditor_and_layerlist_bundles implements JdbcMigration {

    @Override
    public void migrate(Connection connection) throws Exception {
        Bundle list = new Bundle();
        list.setName("layerlist");
        BundleHelper.registerBundle(list, connection);

        Bundle admin = new Bundle();
        admin.setName("admin-layereditor");
        BundleHelper.registerBundle(admin, connection);
    }
}
