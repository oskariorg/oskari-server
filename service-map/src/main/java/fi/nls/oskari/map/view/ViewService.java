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

    public List<View> getViews(int page, int pagesize);

    public View getViewWithConf(long viewId);

    public View getViewWithConfByUuId(String uuId);

    public View getViewWithConfByOldId(long oldId);

    public View getViewWithConf(String viewName);

    public List<View> getViewsForUser(long userId);

    public long addView(View view) throws ViewException;

    public void updateAccessFlag(View view);

    public void updateView(View view);

    public void deleteViewById(long id) throws DeleteViewException;

    public void deleteViewByUserId(long id) throws DeleteViewException;

    public long getDefaultViewId();

    public void updateViewUsage(View view);

    public void resetUsersDefaultViews(long user_id);
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

    public void addBundleForView(final long viewId, final Bundle bundle) throws SQLException;

    /**
     * Updates bundle settings for single bundle in given view.
     * @param viewId
     * @param bundle
     * @throws ViewException if bundle is not part of the view or update failed.
     */
    public void updateBundleSettingsForView(final long viewId, final Bundle bundle) throws ViewException;

}
