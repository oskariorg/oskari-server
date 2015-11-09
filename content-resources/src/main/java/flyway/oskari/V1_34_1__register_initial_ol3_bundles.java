package flyway.oskari;

import fi.nls.oskari.db.BundleHelper;
import fi.nls.oskari.domain.map.view.Bundle;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import org.json.JSONException;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by SMAKINEN on 25.8.2015.
 */
public class V1_34_1__register_initial_ol3_bundles implements JdbcMigration {

    private static final String PATH = "/Oskari/packages/mapping/";
    private static final String BUNDLE_ID_INFOBOX = "infobox_ol3";
    private static final String BUNDLE_ID_TOOLBAR = "toolbar_ol3";

    public void migrate(Connection connection) throws SQLException, JSONException {
        // BundleHelper checks if these bundles are already registered
        Bundle bundle_infobox = new Bundle();
        bundle_infobox.setName(BUNDLE_ID_INFOBOX);
        bundle_infobox.setStartup(BundleHelper.getBundleStartup(PATH, BUNDLE_ID_INFOBOX, "Infobox OL3"));
        bundle_infobox.getConfigJSON().putOpt("adaptable", true);
        BundleHelper.registerBundle(bundle_infobox, connection);

        Bundle bundle_toolbar = new Bundle();
        bundle_toolbar.setName(BUNDLE_ID_TOOLBAR);
        bundle_toolbar.setStartup(BundleHelper.getBundleStartup(PATH, BUNDLE_ID_TOOLBAR, "Toolbar OL3"));
        BundleHelper.registerBundle(bundle_toolbar, connection);
    }
}
