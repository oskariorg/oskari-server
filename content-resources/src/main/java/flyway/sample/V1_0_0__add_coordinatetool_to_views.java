package flyway.sample;

import fi.nls.oskari.db.DBHandler;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.map.view.ViewServiceIbatisImpl;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by Marko Kuosmanen on 23.9.2015.
 */
public class V1_0_0__add_coordinatetool_to_views implements JdbcMigration {
    private static final ViewService VIEW_SERVICE = new ViewServiceIbatisImpl();
    private static final Logger LOG = LogFactory.getLogger(V1_0_0__add_coordinatetool_to_views.class);
    private static final int BATCH_SIZE = 50;
    private static final  String COORDINATE_TOOL = "coordinatetool";
    private static final  String COORDINATE_DISPLAY = "coordinatedisplay";

    private int counter = 0;

    public void migrate(Connection connection)
            throws Exception {

        DBHandler.setupAppContent(connection, "app-sample");
        // check existing value before inserting
        final boolean hasCoordinateTool = hasExistingBundle(connection, COORDINATE_TOOL);
        if (!hasCoordinateTool) {
            // check exiting coordinate display before inserting
            final boolean hasCoordinateDisplay = hasExistingBundle(connection, COORDINATE_DISPLAY);
            if(!hasCoordinateDisplay) {
                int page = 1;
                while (insertViews(page, connection)) {
                    page++;
                }
                LOG.info("Inserts: ", counter);
            } else {
                updateViews(connection);
            }
        }
    }

    private boolean hasExistingBundle(Connection connection, String bundle)
            throws Exception {
        final PreparedStatement statement =
                connection.prepareStatement("SELECT * FROM portti_view_bundle_seq " +
                        "WHERE bundle_id = (SELECT id FROM portti_bundle WHERE name=?)");
        statement.setString(1,bundle);
        try (ResultSet rs = statement.executeQuery()) {
            return rs.next();
        } finally {
            statement.close();
        }
    }

    private boolean insertViews(int page, Connection connection)
            throws Exception {

        List<View> list = VIEW_SERVICE.getViews(page, BATCH_SIZE);
        LOG.info("Got", list.size(), "views on page", page);
        for(View view : list) {
            if(view.isDefault()) {
                makeInsert(view, connection);
                counter++;
            }
        }
        return list.size() == BATCH_SIZE;
    }

    private void makeInsert(View view, Connection connection) throws SQLException {

        final PreparedStatement statement =
                connection.prepareStatement("INSERT INTO portti_view_bundle_seq" +
                        "(view_id, bundle_id, seqno, config, state, startup, bundleinstance) " +
                        "VALUES (" +
                        "?, " +
                        "(SELECT id FROM portti_bundle WHERE name=?), " +
                        "(SELECT max(seqno)+1 FROM portti_view_bundle_seq WHERE view_id=?), " +
                        "?, ?, " +
                        "(SELECT startup FROM portti_bundle WHERE name=?), " +
                        "?)");
        try {
            statement.setLong(1, view.getId());
            statement.setString(2, COORDINATE_TOOL);
            statement.setLong(3, view.getId());
            statement.setString(4, "{\"roundToDecimals\": 6 }");
            statement.setString(5, "{}");
            statement.setString(6, COORDINATE_TOOL);
            statement.setString(7, COORDINATE_TOOL);
            statement.execute();
        } finally {
            statement.close();
        }
    }


    private void updateViews(Connection connection)
            throws SQLException {

        final PreparedStatement statement =
                connection.prepareStatement("UPDATE portti_view_bundle_seq " +
                        "SET " +
                        "    bundle_id=(SELECT id FROM portti_bundle WHERE NAME='coordinatetool'), " +
                        "    config='{ " +
                        "            \"roundToDecimals\": 6" +
                        "        }', " +
                        "    bundleinstance='coordinatetool'," +
                        "    startup='{" +
                        "    \"title\" : \"Coordinate tool\"," +
                        "    \"bundlename\" : \"coordinatetool\"," +
                        "    \"bundleinstancename\" : \"coordinatetool\"," +
                        "    \"metadata\" : {" +
                        "        \"Import-Bundle\" : {" +
                        "            \"coordinatetool\" : {" +
                        "                \"bundlePath\" : \"/Oskari/packages/framework/bundle/\"" +
                        "            }" +
                        "        }" +
                        "    }" +
                        "}'" +
                        "WHERE bundle_id = (SELECT id FROM portti_bundle WHERE NAME='coordinatedisplay')");

        try {
            statement.execute();
        } finally {
            statement.close();
        }
    }
}
