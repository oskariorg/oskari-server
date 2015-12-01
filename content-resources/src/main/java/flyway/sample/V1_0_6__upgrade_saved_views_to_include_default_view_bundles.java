package flyway.sample;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.map.view.ViewServiceIbatisImpl;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.UserViewMigrator1_33;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;

/**
 * Updates all views of type USER having the same application (portti_view.application) as the default view to
 * include the bundles present in the default view, but using the bundle states from the saved view. This synchronizes
 * views saved by users with the setup from default view and fixes an issue where old saved views are missing bundles
 * that have been added after the view has been saved. This makes personalized default views up to date with the default view.
 *
 * Additional steps will be taken in the future to add functionalities on both default and USER views.
 */
public class V1_0_6__upgrade_saved_views_to_include_default_view_bundles implements JdbcMigration {
    private static final Logger LOG = LogFactory.getLogger(V1_0_6__upgrade_saved_views_to_include_default_view_bundles.class);

    public void migrate(Connection connection)
            throws Exception {
        long templateId = PropertyUtil.getOptional("view.default", -1);
        ViewService service = new ViewServiceIbatisImpl();
        LOG.info("Starting view update with template view id:", templateId);
        // Update USER views using the authenticated user default view as template
        UserViewMigrator1_33 migrator = new UserViewMigrator1_33();
        migrator.migrateUserViewsAppsetups(service, templateId);
    }
}
