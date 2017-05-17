package flyway.sample;

import fi.nls.oskari.db.ViewHelper;
import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.map.view.ViewServiceIbatisImpl;
import fi.nls.oskari.util.PropertyUtil;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import org.json.JSONObject;

import java.sql.Connection;

/**
 * Creates a new view that can be used to start developing a new Openlayers 3 based geoportal view.
 * Not for production use
 */
public class V1_0_11__add_initial_ol3_geoportal_view implements JdbcMigration {

    private static final Logger LOG = LogFactory.getLogger(V1_0_11__add_initial_ol3_geoportal_view.class);
    private ViewService service = null;

    public void migrate(Connection connection) throws Exception {
        if(PropertyUtil.getOptional("flyway.sample.1_0_11.skip", true)) {
            // skip by default
            return;
        }
        service = new ViewServiceIbatisImpl();

        final String file = PropertyUtil.get("flyway.sample.1_0_11.file", "ol3-geoportal-view.json");
        // configure the view that should be used as default options
        final int defaultViewId = PropertyUtil.getOptional("flyway.sample.1_0_11.view", (int) service.getDefaultViewId());
        try {
            // load view from json and update startups for bundles
            JSONObject json = ViewHelper.readViewFile(file);
            View view = ViewHelper.createView(json);

            View defaultView = service.getViewWithConf(defaultViewId);
            for(Bundle bundle: defaultView.getBundles()) {
                Bundle newBundle = view.getBundleByName(bundle.getName());
                if(newBundle == null) {
                    continue;
                }
                // copy the settings (state and config) from current default view
                newBundle.setState(bundle.getState());
                newBundle.setConfig(bundle.getConfig());
            }
            // save to db
            service.addView(view);
            LOG.info("Geoportal view with Openlayers 3 added with uuid", view.getUuid());
        } catch (Exception e) {
            LOG.warn(e, "Something went wrong while inserting the view!",
                    "The update failed so to have an ol3 view you need to remove this update from the database table oskari_status_sample, " +
                            "tune the template file:", file, " and restart the server to try again");
            throw e;
        }
    }
}
