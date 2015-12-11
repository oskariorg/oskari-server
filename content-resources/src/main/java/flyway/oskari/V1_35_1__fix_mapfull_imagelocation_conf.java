package flyway.oskari;

import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.map.view.ViewServiceIbatisImpl;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by MKUOSMANEN on 11.12.2015.
 */
public class V1_35_1__fix_mapfull_imagelocation_conf implements JdbcMigration {
    private static final Logger LOG = LogFactory.getLogger(V1_35_1__fix_mapfull_imagelocation_conf.class);
    private static final String BUNDLE_MAPFULL = "mapfull";
    private static final String IMAGE_LOCATION = "imageLocation";
    private static final String IMAGE_LOCATION_VALUE  = "/Oskari/resources";

    private ViewService service = null;
    private int updatedViewCount = 0;

    public void migrate(Connection connection) throws Exception{
        service =  new ViewServiceIbatisImpl();
        try {
            updateViews(connection);
        }
        finally {
            LOG.info("Updated views:", updatedViewCount);
            service = null;
        }
    }

    private void updateViews(Connection conn)
            throws Exception {
        List<View> list = getOutdatedViews(conn);
        for(View view : list) {
            View modifyView = service.getViewWithConf(view.getId());

            final Bundle mapfull = modifyView.getBundleByName(BUNDLE_MAPFULL);
            boolean modified = modifyImageLocation(mapfull);
            if(modified) {
                service.updateBundleSettingsForView(view.getId(), mapfull);
                updatedViewCount++;
            }
        }
    }

    private List<View> getOutdatedViews(Connection conn) throws SQLException {
        List<View> list = new ArrayList<>();
        final String sql = "SELECT distinct view_id " +
                "FROM portti_view_bundle_seq " +
                "WHERE bundle_id IN (SELECT id FROM portti_bundle WHERE name = 'mapfull') " +
                "AND config LIKE '%/Oskari/resources%';";

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    View view = new View();
                    view.setId(rs.getLong("view_id"));
                    list.add(view);
                }
            }
        }
        return list;
    }

    private boolean modifyImageLocation(final Bundle mapfull) throws JSONException {
        final JSONObject config = mapfull.getConfigJSON();
        if(config.has(IMAGE_LOCATION) && IMAGE_LOCATION_VALUE.equals(config.getString(IMAGE_LOCATION))) {
            config.remove(IMAGE_LOCATION);
            return true;
        }
        return false;
    }
}
