package flyway.sample;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.map.view.ViewServiceIbatisImpl;
import fi.nls.oskari.util.Publisher2Migrator;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.util.List;

/**
 * Created by Marko Kuosmanen on 25.9.2015.
 */
public class V1_0_5__publisher2_migration implements JdbcMigration {
    private static final Logger LOG = LogFactory.getLogger(V1_0_5__publisher2_migration.class);

    public void migrate(Connection connection)
            throws Exception {
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
