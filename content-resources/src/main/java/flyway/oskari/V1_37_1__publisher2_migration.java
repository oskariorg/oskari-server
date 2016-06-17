package flyway.oskari;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.map.view.ViewServiceIbatisImpl;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.Publisher2Migrator;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.util.List;

/**
 * This will update any view with the original publisher bundle to use the current version (publisher2)
 */
public class V1_37_1__publisher2_migration implements JdbcMigration {
    private static final Logger LOG = LogFactory.getLogger(V1_37_1__publisher2_migration.class);

    public void migrate(Connection connection)
            throws Exception {
        if(PropertyUtil.getOptional("flyway.1_37_1.skip", false)) {
            LOG.warn("You are skipping publisher -> publisher2 forced migration.",
                    "The original publisher bundle is no longer maintained and might not work properly anymore.",
                    "You will have to make an app specific migration since you skipped this one.");
            return;
        }

        ViewService service = new ViewServiceIbatisImpl();
        // generate the metadata
        Publisher2Migrator migrator = new Publisher2Migrator(service);
        migrator.migratePublishedAppsetups(connection);
        List<Long> idList = migrator.getViewsWithOldPublisher(connection);
        LOG.info("Switching publisher -> publisher2 in views with ids:", idList);
        for (long id : idList) {
            migrator.switchPublisherBundles(id, connection);
        }
    }
}
