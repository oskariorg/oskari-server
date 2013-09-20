package fi.nls.oskari.map.view;


import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.util.ConversionHelper;
import fi.nls.oskari.util.PropertyUtil;
import org.json.JSONException;
import org.json.JSONObject;

import com.ibatis.sqlmap.client.SqlMapSession;

import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.db.BaseIbatisService;

public class ViewServiceIbatisImpl extends BaseIbatisService<Object> implements
        ViewService {

    private static final Logger log = LogFactory.getLogger(ViewServiceIbatisImpl.class);
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
            log.debug("Added default views for roles:", defaultViewIds);
        }
        else {
            log.debug("No role based default views configured");
        }

        // check properties for global default view
        defaultViewProperty = ConversionHelper.getLong(PropertyUtil.get("view.default"), -1);
        if(defaultViewProperty != -1) {
            log.debug("Global default view is:", defaultViewProperty);
        }
    }

    @Override
    protected String getNameSpace() {
        return "View";
    }

    public boolean hasPermissionToAlterView(final View view, final User user) {
        
        // uuids are much longer than 10 actually but check for atleast 10
        if(user.getUuid() == null || user.getUuid().length() < 10) {
            log.debug("Users uuid is missing or invalid: ", user.getUuid());
            // user doesn't have an uuid, he shouldn't have any published maps
            return false;
        }
        if(view == null) {
            log.debug("View is null");
            // view with id not found
            return false;
        }
        if(view.getId() <=3) {
            log.debug("View id must be over 4:", view.getId());
            // 1 == default view
            // 2 == print view
            // 3 == published view template
            // these cannot be users views, though these shouldn't be hardcoded
            return false;
        }
/*
        if(!userUuid.equals(view.getUuid())) {
            log.debug("Users uuid:", userUuid, "didn't match the one on view:", view.getUuid());
            // uuid didn't match -> not users view
            return false;
        }
        */
        if(user.isGuest()) {
            log.debug("User is default/guest user");
            return false;
        }
        if(view.getCreator() != user.getId()) {
            // check current user id against view creator (is it the same user)
            log.debug("Users id:", user.getId(), "didn't match view creator:", view.getCreator());
            return false;
        }
        return true;
    }

    public View getViewWithConf(long viewId) {
        if (viewId < 1)
            return null;
        View view = queryForObject("View.view-with-conf-by-view-id", viewId);
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

    public long addView(View view, final JSONObject viewJson) throws ViewException {
        SqlMapSession session = openSession();
        
        try {
            session.startTransaction();
            
            Object ret = queryForObject("View.add-supplement", view);
            
            long suppId = ((Long) ret).longValue();
            
            view.setSupplementId(suppId);
            ret =  queryForObject("View.add-view", view);
            long id = ((Long) ret).longValue();
            
            view.setId(id);
            //insertStates(view, viewJson);
            insertBundle(view, viewJson);
            session.commitTransaction();
            return id;
        }  catch (Exception e) {
            e.printStackTrace();
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
        SqlMapSession session = openSession();
        try {
            session.startTransaction();
            //session.delete("View.delete-state-by-view", id);
            // delete("View.delete-config-by-view", id);
            session.delete("View.delete-bundle-by-view", id);
            session.delete("View.delete-view", id);
            session.delete("View.delete-view-supplement",
            view.getSupplementId());
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
            // delete("View.delete-config-by-user", userId);
            delete("View.delete-seq-by-user", userId);
            delete("View.delete-view-by-user", userId);
            session.commitTransaction();
        } catch (Exception e) {
            throw new DeleteViewException("Error deleting a view with user id:" + userId, e);
        } finally {
            endSession(session);
        }
    }
    

	public void updateView(View view) {
        update("View.update-view", view);
    }
	
	public void updatePublishedView(final View view, final JSONObject viewJson) throws ViewException {
//	    SqlMapSession session = openSession();
        long id = view.getId();
        
        try {
//            session.startTransaction();
            update("View.update-supplement",view);
            update("View.update",view);
            //delete("View.delete-state-by-view", id);
            //delete("View.delete-config-by-view", id);
            delete("View.delete-bundle-by-view", id);
            
            insertBundle(view, viewJson);
//            session.commitTransaction();
        } catch (Exception e) {
            throw new ViewException("Error updating a view with id:" + id, e);
        } finally {
//            endSession(session);
        }
	}
	


    private void insertBundle(final View view, final JSONObject viewJson) throws SQLException {
        int seqIndex = 1;

        for (Bundle bundle : view.getBundles()) {

            String bundleName = bundle.getName();
            long bundleId = bundle.getBundleId();
            // Do we have data for this bundle?
            JSONObject bundleJson = null;
            try {
                bundleJson = viewJson.getJSONObject(bundleName);
            } catch (JSONException je) {
                //je.printStackTrace();
                log.error("bundle "+ bundleName +"  not found from JSON");
            }

            if (bundleJson != null && !bundleJson.isNull("config")) {
                try {
                    bundle.setConfig(bundleJson.getJSONObject("config").toString());
                } catch (JSONException jsonex) {
                    throw new RuntimeException("Malformed config" + " for '"
                            + bundleName + "'" + " in request");
                }
            }

            if (bundleJson != null && !bundleJson.isNull("state")) {
                try {
                    bundle.setState(bundleJson.getJSONObject("state").toString());
                } catch (JSONException jsonex) {
                    throw new RuntimeException("Malformed state" + " for '"
                            + bundleName + "'" + " in request");
                }
            }

            addBundleForView(view.getId(), bundle);
        }
    }
    public void addBundleForView(final long viewId, final Bundle bundle) throws SQLException {
        // TODO: maybe setup sequencenumber to last if not set?
        bundle.setViewId(viewId);
        queryForObject("View.add-bundle", bundle);
    }


    public long getDefaultViewId() {
        return ((Long) queryForObject("View.get-default-view-id", "DEFAULT"))
                .longValue();
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
            log.debug("Tried to get default view for <null> user");
        }

        // Check the roles in given order and return the first match
        for(String role : viewRoles) {
            if(user.hasRole(role) &&
                    defaultViewIds.containsKey(role)) {
                log.debug("Default view found for role", role, ":", defaultViewIds.get(role));
                return defaultViewIds.get(role);
            }
        }
        // property overrides db default, no particular reason for this
        if(defaultViewProperty != -1) {
            return defaultViewProperty;
        }
        // global default view property not defined, check db
        log.debug("No properties based default views matched user", user, ". Defaulting to DB.");
        defaultViewProperty = getDefaultViewId();
        return defaultViewProperty;
    }

    public long getDefaultViewId(String type) {
        return ((Long) queryForObject("View.get-default-view-id", type))
                .longValue();
    }

    
}
