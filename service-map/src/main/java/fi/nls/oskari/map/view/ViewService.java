package fi.nls.oskari.map.view;

import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.service.db.BaseService;

import java.sql.SQLException;
import java.util.List;

import org.json.JSONObject;

public interface ViewService extends BaseService<Object> {

    public boolean hasPermissionToAlterView(final View view, final User user);
    
    public View getViewWithConf(long viewId);

    public View getViewWithConfByOldId(long oldId);

    public View getViewWithConf(String viewName);

    public List<View> getViewsForUser(long userId);

    public long addView(View view) throws ViewException;
    @Deprecated
    public long addView(View view, final JSONObject viewJson)  throws ViewException;

    public void updateAccessFlag(View view);
    
    public void updateView(View view);
    
    public void deleteViewById(long id) throws DeleteViewException;

    public void deleteViewByUserId(long id) throws DeleteViewException;

    public long getDefaultViewId();

    /**
     * Returns default view id for given role name
     * @param roleName
     * @return
     */
    public long getDefaultViewIdForRole(final String roleName);

    /**
     * Returns default view id for the user.
     * @param user
     * @return view id
     */
    public long getDefaultViewId(final User user);
    
    public void updatePublishedView(View view) throws ViewException;
    @Deprecated
    public void updatePublishedView(View view, JSONObject json) throws ViewException;

    public void addBundleForView(final long viewId, final Bundle bundle) throws SQLException;

    public void updateBundleSettingsForView(final long viewId, final Bundle bundle) throws ViewException;

}
