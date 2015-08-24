package flyway.oskari;

import fi.nls.oskari.db.BundleHelper;
import fi.nls.oskari.domain.map.view.Bundle;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;

/**
 * Checks if feature id config is already present in the db and inserts it if not
 */
public class V1_31_6__register_admin_and_metrics_bundles implements JdbcMigration {

    public void migrate(Connection connection)
            throws Exception {
        // BundleHelper checks if these bundles are already registered

        Bundle admin = new Bundle();
        admin.setName("admin");
        admin.setStartup("{\n" +
                "    \"title\": \"Generic Admin\",\n" +
                "    \"bundleinstancename\": \"admin\",\n" +
                "    \"bundlename\": \"admin\",\n" +
                "    \"metadata\": {\n" +
                "        \"Import-Bundle\": {\n" +
                "            \"admin\": {\n" +
                "                \"bundlePath\": \"/Oskari/packages/admin/bundle/\"\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}");
        BundleHelper.registerBundle(admin);

        Bundle metrics = new Bundle();
        metrics.setName("metrics");
        metrics.setStartup("{\n" +
                "    \"title\": \"Admin metrics panel\",\n" +
                "    \"bundleinstancename\": \"metrics\",\n" +
                "    \"bundlename\": \"metrics\",\n" +
                "    \"metadata\": {\n" +
                "        \"Import-Bundle\": {\n" +
                "            \"metrics\": {\n" +
                "                \"bundlePath\": \"/Oskari/packages/admin/bundle/\"\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}");
        BundleHelper.registerBundle(metrics);

    }
}
