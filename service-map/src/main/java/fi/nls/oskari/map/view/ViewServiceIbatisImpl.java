package fi.nls.oskari.map.view;


import com.ibatis.sqlmap.client.SqlMapSession;
import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.domain.map.view.ViewTypes;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.db.BaseIbatisService;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.PropertyUtil;

import java.sql.SQLException;
import java.util.*;

public class ViewServiceIbatisImpl extends BaseIbatisService<Object> implements
        ViewService {

    private static final Logger LOG = LogFactory.getLogger(ViewServiceIbatisImpl.class);
    private final Map<String, Long> defaultViewIds = new HashMap<String, Long>();
    private String[] viewRoles = new String[0];
    private long defaultViewProperty = -1;

    public ViewServiceIbatisImpl() {
        super();

        // roles in preferred order which we use to resolve default view
        // view.default.roles=Admin, User, Guest
        viewRoles = PropertyUtil.getCommaSeparatedList("view.default.roles");
        for(int i= 0; i < viewRoles.length; ++i) {
            final String role = viewRoles[i];

            // populate role based properties if available
            final long roleViewId = ConversionHelper.getLong(PropertyUtil.get("view.default." + role), -1);
            if(roleViewId != -1) {
                defaultViewIds.put(role, roleViewId);
            }
        }
        if(viewRoles.length > 0) {
            LOG.debug("Added default views for roles:", defaultViewIds);
        }
        else {
            LOG.debug("No role based default views configured");
        }

        // check properties for global default view
        defaultViewProperty = ConversionHelper.getLong(PropertyUtil.get("view.default"), -1);
        if(defaultViewProperty != -1) {
            LOG.debug("Global default view is:", defaultViewProperty);
        }
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
        System.err.println("[ViewService] Got " + views.size()
                + " views for user " + userId);
        return views;
    }

    public long addView(View view) throws ViewException {
        SqlMapSession session = openSession();

        try {
        	view.setUuid(generateUuid());

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
            delete("View.delete-state-by-user", userId);
            delete("View.delete-seq-by-user", userId);
            delete("View.delete-view-by-user", userId);
            session.commitTransaction();
        } catch (Exception e) {
            throw new DeleteViewException("Error deleting a view with user id:" + userId, e);
        } finally {
            endSession(session);
        }
    }


    public void resetUsersDefaultViews(long user_id) {
        update("View.resetUsersDefaultViews", user_id);
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
        LOG.info("Added bundle to view", bundle.getName());
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
        // property overrides db default, no particular reason for this
        if(defaultViewProperty == -1) {
            defaultViewProperty = getDefaultViewId(ViewTypes.DEFAULT);
        }
        return defaultViewProperty;
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

    private long getSystemDefaultViewId(Set<Role> roles) {

        if(roles == null) {
            LOG.debug("Tried to get default view for <null> roles");
        }
        else {
            // Check the roles in given order and return the first match
            for(String role : viewRoles) {
                if(Role.hasRoleWithName(roles, role) &&
                        defaultViewIds.containsKey(role)) {
                    LOG.debug("Default view found for role", role, ":", defaultViewIds.get(role));
                    return defaultViewIds.get(role);
                }
            }
        }
        LOG.debug("No properties based default views matched user roles:", roles, ". Defaulting to DB.");
        return getDefaultViewId();
    }

    public boolean isSystemDefaultView(final long id) {
        return defaultViewIds.containsValue(id) || getDefaultViewId() == id;
    }

    /**
     * Returns the saved default view id for the user, if one exists
     *
     * @param user to get default view for
     * @return view id of a saved default view
     */
    private long getPersonalizedDefaultViewId(final User user) {
        if (!user.isGuest() && user.getId() != -1) {
            Object queryResult = queryForObject("View.get-default-view-id-by-user-id",user.getId());
            if (queryResult != null) {
                Long userDefaultViewId = (Long)queryResult;
                return userDefaultViewId.longValue();
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
        if(defaultViewIds.containsKey(roleName)) {
            return defaultViewIds.get(roleName);
        }
        return getDefaultViewId();
    }

    public long getDefaultViewId(String type) {
        return ((Long) queryForObject("View.get-default-view-id", type))
                .longValue();
    }

    /**
     * Generates random UUID
     * @return uuid
     */
    public String generateUuid() {
        return UUID.randomUUID().toString();
    }

}
