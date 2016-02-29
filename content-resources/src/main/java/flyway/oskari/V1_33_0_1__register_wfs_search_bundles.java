package flyway.oskari;

import fi.nls.oskari.db.BundleHelper;
import fi.nls.oskari.domain.map.view.Bundle;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by SMAKINEN on 25.8.2015.
 */
public class V1_33_0_1__register_wfs_search_bundles implements JdbcMigration {

    private static final String NAMESPACE = "tampere";
    private static final String ADMIN = "admin-wfs-search-channel";
    private static final String SEARCH = "search-from-channels";

    public void migrate(Connection connection) throws SQLException {
        // BundleHelper checks if these bundles are already registered
        Bundle admin = new Bundle();
        admin.setName(ADMIN);
        admin.setStartup(BundleHelper.getDefaultBundleStartup(NAMESPACE, ADMIN, "WFS Search channel admin"));
        BundleHelper.registerBundle(admin, connection);

        Bundle search = new Bundle();
        search.setName(SEARCH);
        search.setStartup(BundleHelper.getDefaultBundleStartup(NAMESPACE, SEARCH, "SearchFromChannelsBundle"));
        BundleHelper.registerBundle(search, connection);
    }
}
