package flyway.oskari;

import fi.nls.oskari.db.BundleHelper;
import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.view.ViewService;
import fi.nls.oskari.map.view.ViewServiceIbatisImpl;
import fi.nls.oskari.service.UserService;
import fi.nls.oskari.util.PropertyUtil;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This is a rare exception to the rule of not updating views automatically. The previous upgrades didn't update views
 * saved by users so old installs might have the old WFS implementation bundles referenced in some of these views.
 *
 * This upgrade replaces outdated bundles in views of type USER:
 * - mapwfs -> mapwfs2 (in mapfull startup)
 * - featuredata -> featuredata2
 *
 * This should fix issues with old views to make them ready for personalized default views. Outdated views don't start since
 * they has references to deprecated/removed code.
 * Affected views that are updated can be identified by running this before the update:
 *
 SELECT distinct view_id FROM portti_view_bundle_seq where startup LIKE '%"featuredata"%' OR startup LIKE '%"mapwfs"%';

 Notes:
 - mapwfs2: views with type PUBLISHED should use mapfull startup from publish template
 - mapwfs2: views with type USER should use mapfull startup from system default view.
 - featuredata: any view with featuredata should use featuredata2 startup from system default view.

 */
public class V1_33_5__replace_deprecated_bundles_with_new_versions implements JdbcMigration {
    private static final Logger LOG = LogFactory.getLogger(V1_33_5__replace_deprecated_bundles_with_new_versions.class);
    private static final String ERROR_TEMPLATE_NOT_DEFINED =
            "Publish template not defined. Please add property: " + ViewService.PROPERTY_PUBLISH_TEMPLATE;

    private static final String BUNDLE_MAPFULL = "mapfull";
    private static final String BUNDLE_FEATUREDATA = "featuredata";
    private static final String BUNDLE_FEATUREDATA2 = "featuredata2";

    private static final String PROP_FORCE_USER_ROLE = PropertyUtil.getOptional("V1_33_5.force.user.role");
    private static final boolean PROP_FORCE_USER_SERVICE = PropertyUtil.getOptional("V1_33_5.force.user.service", false);

    private int updatedViewCount = 0;
    private ViewService service = null;

    public void migrate(Connection connection) throws Exception {
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
        LOG.info("Got", list.size(), "outdated views");
        for(View view : list) {
            // replace with defaultView mapfull startup
            final String mapfullStartup = getBundleStartupFromView(conn, getDefaultViewId(conn, view.getCreator()), BUNDLE_MAPFULL);
            updateBundleStartup(conn, view.getId(), mapfullStartup, BUNDLE_MAPFULL);
            switchFeaturedataBundles(conn, view.getId());
            updatedViewCount++;
        }
    }
    private List<View> getOutdatedViews(Connection conn) throws SQLException {

        List<View> list = new ArrayList<>();
        final String sql = "SELECT id, creator from portti_view where type = 'USER' AND  id in " +
                "(select distinct view_id FROM portti_view_bundle_seq where startup LIKE '%\"featuredata\"%' OR startup LIKE '%\"mapwfs\"%');";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    View view = new View();
                    view.setId(rs.getLong("id"));
                    view.setCreator(rs.getLong("creator"));
                    list.add(view);
                }
            }
        }
        return list;
    }

    private String getBundleStartupFromView(Connection conn, long viewId, String bundlename) throws SQLException {

        int publishTemplateId = PropertyUtil.getOptional(ViewService.PROPERTY_PUBLISH_TEMPLATE, -1);
        if(publishTemplateId == -1) {
            throw new RuntimeException(ERROR_TEMPLATE_NOT_DEFINED);
        }
        final String sql = "SELECT startup FROM portti_view_bundle_seq " +
                "WHERE bundle_id=(SELECT id from portti_bundle where name =?) AND view_id=?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, bundlename);
            statement.setLong(2, viewId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("startup");
                }
            }
        }
        return null;
    }

    private void updateBundleStartup(Connection conn, long viewId, String startup, String bundlename) throws SQLException {
        final String sql = "UPDATE portti_view_bundle_seq SET startup=? where bundle_id=(SELECT id from portti_bundle where name =?) AND view_id=?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, startup);
            statement.setString(2, bundlename);
            statement.setLong(3, viewId);
            statement.execute();
        }
    }

    private long getDefaultViewId(Connection conn, long userId) throws SQLException  {
        return service.getSystemDefaultViewId(getRolesForUser(userId, conn));
    }

    private Collection<Role> getRolesForUser(long userId, Connection conn) throws SQLException {
        if(PROP_FORCE_USER_ROLE != null) {
            List<Role> roles = new ArrayList<>();
            Role role = new Role();
            // default view mapping is done with role name so this will work
            role.setName(PROP_FORCE_USER_ROLE);
            roles.add(role);
            return roles;
        } else if(PROP_FORCE_USER_SERVICE) {
            try {
                User user = UserService.getInstance().getUser(userId);
                return user.getRoles();
            } catch (Exception ex) {
                throw new SQLException("Couldn't load user", ex);
            }
        } else {
            final String sql = "SELECT r.id, r.name FROM oskari_roles r " +
                    "WHERE r.id = (SELECT id FROM oskari_role_oskari_user where user_id = ?)";
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setLong(1, userId);
                try (ResultSet rs = statement.executeQuery()) {
                    List<Role> res = new ArrayList<>();
                    while (rs.next()) {
                        Role role = new Role();
                        role.setId(rs.getLong("id"));
                        role.setName(rs.getString("name"));
                        res.add(role);
                    }
                    return res;
                }
            }
        }
    }

    public void switchFeaturedataBundles(Connection conn, final long viewId) throws SQLException {
        Bundle oldBundle = BundleHelper.getRegisteredBundle(BUNDLE_FEATUREDATA, conn);
        if(oldBundle == null) {
            // not even registered so migration not needed
            return;
        }
        Bundle newBundle = BundleHelper.getRegisteredBundle(BUNDLE_FEATUREDATA2, conn);
        if(newBundle == null) {
            throw new RuntimeException("Bundle not registered: " + BUNDLE_FEATUREDATA2);
        }
        final String sql = "UPDATE portti_view_bundle_seq " +
                "SET " +
                "    bundle_id=?, " +
                "    startup=?, " +
                "    bundleinstance=?" +
                "WHERE bundle_id = ? and view_id=?";

        try (PreparedStatement statement =
                     conn.prepareStatement(sql)){
            statement.setLong(1, newBundle.getBundleId());
            statement.setString(2, newBundle.getStartup());
            statement.setString(3, newBundle.getName());
            statement.setLong(4, oldBundle.getBundleId());
            statement.setLong(5, viewId);
            statement.execute();
        }
    }
}
