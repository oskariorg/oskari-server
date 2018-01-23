package fi.nls.oskari.map.view;


import com.ibatis.sqlmap.client.SqlMapSession;
import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.domain.map.view.ViewTypes;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.db.BaseIbatisService;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.PropertyUtil;

import java.sql.SQLException;
import java.util.*;

public class ViewServiceIbatisImpl extends BaseIbatisService<Object> implements
        ViewService {

    private static final Logger LOG = LogFactory.getLogger(ViewServiceIbatisImpl.class);

    private static final String PROP_VIEW_DEFAULT = "view.default";
    private static final String PROP_VIEW_DEFAULT_ROLES = "view.default.roles";

    private final Map<String, Long> roleToDefaultViewId;
    private final String[] defaultViewRoles;
    private final long defaultViewId;

    public ViewServiceIbatisImpl() {
        // roles in preferred order which we use to resolve default view
        // view.default.roles=Admin, User, Guest
        defaultViewRoles = PropertyUtil.getCommaSeparatedList(PROP_VIEW_DEFAULT_ROLES);
        roleToDefaultViewId = initDefaultViewsByRole(defaultViewRoles);
        defaultViewId = initDefaultViewId();
    }

    private Map<String, Long> initDefaultViewsByRole(String[] roles) {
        if (roles.length == 0) {
            return Collections.emptyMap();
        }
        Map<String, Long> roleToDefaultViewId = new HashMap<>();
        for (String role : roles) {
            String roleViewIdStr = PropertyUtil.get(PROP_VIEW_DEFAULT + "." + role);
            long roleViewId = ConversionHelper.getLong(roleViewIdStr, -1);
            if (roleViewId != -1) {
                roleToDefaultViewId.put(role, roleViewId);
                LOG.debug("Added default view", roleViewId, "for role", role);
            } else {
                LOG.info("Failed to set default view id for role", role,
                        "property missing or value invalid");
            }
        }
        return roleToDefaultViewId;
    }

    private long initDefaultViewId() {
        long property = ConversionHelper.getLong(PropertyUtil.get(PROP_VIEW_DEFAULT), -1);
        if (property != -1) {
            LOG.debug("Global default view id from properties:" , property);
            return property;
        }
        // use one from db if property doesn't exist or is invalid
        Long database = ((Long) queryForObject("View.get-default-view-id", ViewTypes.DEFAULT));
        LOG.debug("Global default view id from database:" , database);
        return database;
    }

    @Override
    protected String getNameSpace() {
        return "View";
    }

    public boolean hasPermissionToAlterView(final View view, final User user) {

        // uuids are much longer than 10 actually but check for atleast 10
        if(user.getUuid() == null || user.getUuid().length() < 10) {
            LOG.debug("Users uuid is missing or invalid: ", user.getUuid());
            // user doesn't have an uuid, he shouldn't have any published maps
            return false;
        }
        if(view == null) {
            LOG.debug("View is null");
            // view with id not found
            return false;
        }
        if(user.isGuest()) {
            LOG.debug("User is default/guest user");
            return false;
        }
        if(view.getCreator() != user.getId()) {
            // check current user id against view creator (is it the same user)
            LOG.debug("Users id:", user.getId(), "didn't match view creator:", view.getCreator());
            return false;
        }
        return true;
    }


    public List<View> getViews(int page, int pagesize) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("limit", pagesize);
        params.put("offset", (page -1) * pagesize);
        List<View> views = queryForList("View.paged-views", params);
        return views;
    }

    public View getViewWithConf(long viewId) {
        if (viewId < 1)
            return null;
        View view = queryForObject("View.view-with-conf-by-view-id", viewId);
        return view;
    }

    public View getViewWithConfByUuId(String uuId) {
        if (uuId == null)
            return null;
        LOG.debug("uuid != null --> view-with-conf-by-uuid");
        View view = (View) queryForObject("View.view-with-conf-by-uuid", uuId);
        setBundlesForView(view);
        return view;
    }

    public View getViewWithConfByOldId(long oldId) {
        if (oldId < 1)
            return null;
        View view = queryForObject("View.view-with-conf-by-old-id", oldId);
        return view;
    }

    public View getViewWithConf(String viewName) {
        View view = (View) queryForObject("View.view-with-conf-by-view-name",
                viewName);
        return view;
    }

    public List<View> getViewsForUser(long userId) {
        List<View> views = queryForList("View.views-with-conf-by-user-id",
                userId);
        LOG.debug("Found", views.size(), "views for user", userId);
        return views;
    }

    private void setBundlesForView(View view) {
        if (view == null) {
            return;
        }
        long id = view.getId();
        List<Bundle> bundles = queryForList("View.bundle-by-view-id", id);
        view.setBundles(bundles);
    }

    public long addView(View view) throws ViewException {
        SqlMapSession session = openSession();

        try {
            view.setUuid(UUID.randomUUID().toString());

            session.startTransaction();
            Object ret =  queryForObject("View.add-view", view);
            long id = ((Long) ret).longValue();
            LOG.info("Inserted view with id", id);
            view.setId(id);
            for (Bundle bundle : view.getBundles()) {
                addBundleForView(view.getId(), bundle);
            }
            session.commitTransaction();
            return id;
        }  catch (Exception e) {
            throw new ViewException("Error adding a view ", e);
        } finally {
            endSession(session);
        }
    }

    public void updateAccessFlag(View view) {
        update("View.update-access", view);
    }

    public void deleteViewById(final long id) throws DeleteViewException {
        View view = queryForObject("View.view-with-conf-by-view-id", id);
        if(view == null) {
            throw new DeleteViewException("Couldn't find a view with id:" + id);
        }
        SqlMapSession session = openSession();
        try {
            session.startTransaction();
            session.delete("View.delete-bundle-by-view", id);
            session.delete("View.delete-view", id);
            session.commitTransaction();
        } catch (Exception e) {
            throw new DeleteViewException("Error deleting a view with id:" + id, e);
        } finally {
            endSession(session);
        }
    }

    public void deleteViewByUserId(long userId) throws DeleteViewException {
        SqlMapSession session = openSession();
        try {
            session.startTransaction();
            delete("View.delete-view-by-user", userId);
            session.commitTransaction();
        } catch (Exception e) {
            throw new DeleteViewException("Error deleting a view with user id:" + userId, e);
        } finally {
            endSession(session);
        }
    }


    public void resetUsersDefaultViews(long userId) {
        update("View.resetUsersDefaultViews", userId);
    }
	public void updateView(View view) {
        update("View.update", view);
    }

    public void updateViewUsage(View view) {
        update("View.updateUsage", view);
    }

    public void updatePublishedView(final View view) throws ViewException {
        SqlMapSession session = openSession();
        long id = view.getId();

        try {
            session.startTransaction();
            updateView(view);
            delete("View.delete-bundle-by-view", id);

            for (Bundle bundle : view.getBundles()) {
                addBundleForView(view.getId(), bundle);
            }
            session.commitTransaction();
        } catch (Exception e) {
            throw new ViewException("Error updating a view with id:" + id, e);
        } finally {
            endSession(session);
        }
    }

    public void addBundleForView(final long viewId, final Bundle bundle) throws SQLException {
        // TODO: maybe setup sequencenumber to last if not set?
        bundle.setViewId(viewId);
        queryForObject("View.add-bundle", bundle);
        LOG.debug("Added bundle to view", bundle.getName());
    }

    public void updateBundleSettingsForView(final long viewId, final Bundle bundle) throws ViewException {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("view_id", viewId);
        params.put("bundle_id", bundle.getBundleId());

        params.put("seqno", bundle.getSeqNo());
        params.put("startup", bundle.getStartup());
        params.put("config", bundle.getConfig());
        params.put("state", bundle.getState());
        params.put("bundleinstance", bundle.getBundleinstance());

        try {
            final int numUpdated = getSqlMapClient().update("View.update-bundle-settings-in-view", params);
            if(numUpdated == 0) {
                // not updated, bundle not found
                throw new ViewException("Failed to update - bundle not found in view?");
            }
        } catch (Exception e) {
            throw new ViewException("Failed to update", e);
        }
    }

    public long getDefaultViewId() {
        return defaultViewId;
    }

    /**
     * Returns default view id for the user, based on user roles. Configured by properties:
     *
     * view.default=[global default view id that is used if role-based default view is not found]
     * view.default.roles=[comma-separated list of role names in descending order f.ex. Admin, User, Guest]
     * view.default.[role name]=[default view id for the role]
     *
     * If properties are not found, defaults to #getDefaultViewId()
     * @param user to get default view for
     * @return view id based on users roles
     */
    public long getDefaultViewId(final User user) {
        if(user == null) {
            LOG.debug("Tried to get default view for <null> user");
            return getDefaultViewId();
        }
        else {
            final long personalizedId = getPersonalizedDefaultViewId(user);
            if(personalizedId != -1) {
                return personalizedId;
            }
            return getSystemDefaultViewId(user.getRoles());
        }
    }

    public long getSystemDefaultViewId(Collection<Role> roles) {
        if (roles == null) {
            LOG.debug("Tried to get default view for <null> roles");
        } else {
            // Check the roles in given order and return the first match
            for (String defaultViewRole : defaultViewRoles) {
                if (Role.hasRoleWithName(roles, defaultViewRole)) {
                    Long rolesDefaultViewId = roleToDefaultViewId.get(defaultViewRole);
                    if (rolesDefaultViewId != null) {
                        LOG.debug("Default view found for role", defaultViewRole, ":", rolesDefaultViewId);
                        return rolesDefaultViewId;
                    }
                }
            }
        }
        LOG.debug("No role based default views matched user roles:", roles, ". Defaulting to global default.");
        return getDefaultViewId();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Long> getSystemDefaultViewIds() throws ServiceException {
        try {
            return (List<Long>) getSqlMapClient().queryForList("View.get-default-view-ids");
        } catch (SQLException e) {
            throw new ServiceException(e.getMessage(), e);
        }
    }

    public boolean isSystemDefaultView(final long id) {
        return roleToDefaultViewId.containsValue(id) || getDefaultViewId() == id;
    }

    /**
     * Returns the saved default view id for the user, if one exists
     *
     * @param user to get default view for
     * @return view id of a saved default view
     */
    private long getPersonalizedDefaultViewId(final User user) {
        if (!user.isGuest() && user.getId() != -1) {
            Object queryResult = queryForObject("View.get-default-view-id-by-user-id", user.getId());
            if (queryResult != null) {
                return (Long) queryResult;
            }
        }
        return -1;
    }

    /**
     * Returns default view id for given role name
     * @param roleName
     * @return
     */
    public long getDefaultViewIdForRole(final String roleName) {
        Long rolesDefaultViewId = roleToDefaultViewId.get(roleName);
        return rolesDefaultViewId != null ? rolesDefaultViewId : defaultViewId;
    }

}
