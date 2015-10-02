package fi.nls.oskari.map.view;

import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.view.Bundle;
import fi.nls.oskari.domain.map.view.View;
import fi.nls.oskari.service.db.BaseService;

import java.sql.SQLException;
import java.util.List;

public interface ViewService extends BaseService<Object> {

    boolean hasPermissionToAlterView(final View view, final User user);

    List<View> getViews(int page, int pagesize);

    View getViewWithConf(long viewId);

    View getViewWithConfByUuId(String uuId);

    View getViewWithConfByOldId(long oldId);

    View getViewWithConf(String viewName);

    List<View> getViewsForUser(long userId);

    long addView(View view)
            throws ViewException;

    void updateAccessFlag(View view);

    void updateView(View view);

    void deleteViewById(long id)
            throws DeleteViewException;

    void deleteViewByUserId(long id)
            throws DeleteViewException;

    long getDefaultViewId();

    void updateViewUsage(View view);

    void resetUsersDefaultViews(long user_id);

    /**
     * Returns default view id for given role name
     *
     * @param roleName
     * @return
     */
    long getDefaultViewIdForRole(final String roleName);

    /**
     * Returns default view id for the user (based on role)
     *
     * @param user
     * @return view id
     */
    long getDefaultViewId(final User user);

    boolean isSystemDefaultView(final long id);

    void updatePublishedView(View view)
            throws ViewException;

    void addBundleForView(final long viewId, final Bundle bundle)
            throws SQLException;

    /**
     * Updates bundle settings for single bundle in given view.
     *
     * @param viewId
     * @param bundle
     * @throws ViewException if bundle is not part of the view or update failed.
     */
    void updateBundleSettingsForView(final long viewId, final Bundle bundle)
            throws ViewException;

}
